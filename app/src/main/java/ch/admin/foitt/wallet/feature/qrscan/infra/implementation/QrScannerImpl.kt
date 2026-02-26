package ch.admin.foitt.wallet.feature.qrscan.infra.implementation

import android.util.Size
import androidx.annotation.CheckResult
import androidx.camera.core.Camera
import androidx.camera.core.CameraSelector
import androidx.camera.core.ImageAnalysis
import androidx.camera.core.ImageAnalysis.Analyzer
import androidx.camera.core.ImageProxy
import androidx.camera.core.Preview
import androidx.camera.core.TorchState
import androidx.camera.core.resolutionselector.ResolutionSelector
import androidx.camera.core.resolutionselector.ResolutionStrategy
import androidx.camera.lifecycle.ProcessCameraProvider
import androidx.camera.view.PreviewView
import androidx.core.content.ContextCompat
import androidx.lifecycle.findViewTreeLifecycleOwner
import ch.admin.foitt.wallet.feature.qrscan.domain.model.FlashLightState
import ch.admin.foitt.wallet.feature.qrscan.infra.QrScanner
import com.github.michaelbull.result.Err
import com.github.michaelbull.result.Ok
import com.github.michaelbull.result.Result
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.asExecutor
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import zxingcpp.BarcodeReader
import java.util.concurrent.Executors
import javax.inject.Inject
import kotlin.coroutines.resume
import kotlin.coroutines.suspendCoroutine

class QrScannerImpl @Inject constructor() : QrScanner {

    private val cameraSelector = CameraSelector.DEFAULT_BACK_CAMERA
    private val previewStream = Preview.Builder().build()

    private val barcodeReader = BarcodeReader(options = SCANNER_QR_OPTIONS)
    private val imageAnalysis = ImageAnalysis.Builder()
        .setBackpressureStrategy(ImageAnalysis.STRATEGY_KEEP_ONLY_LATEST)
        .setResolutionSelector(RESOLUTION_STRATEGY)
        .build()

    private var analysisExecutorService = Executors.newSingleThreadExecutor()

    private var analyser: Analyzer? = null
    private var camera: Camera? = null

    private val _isRunning = MutableStateFlow(false)
    override val isRunning = _isRunning.asStateFlow()

    private val _flashLightState = MutableStateFlow(FlashLightState.UNKNOWN)
    override val flashLightState = _flashLightState.asStateFlow()

    override fun initAnalyser(
        onBarcodesScanned: (List<String>) -> Unit,
    ) {
        analyser = Analyzer { imageProxy ->
            processImageProxy(
                imageProxy,
                barcodeReader,
                onBarcodesScanned,
            )
        }
    }

    @CheckResult
    override fun registerScanner(
        previewView: PreviewView,
    ): Result<Unit, Throwable> {
        val lifeCycleOwner = previewView.findViewTreeLifecycleOwner()

        if (analysisExecutorService.isShutdown) {
            analysisExecutorService = Executors.newSingleThreadExecutor()
        }

        analyser?.let { currentAnalyser ->
            imageAnalysis.setAnalyzer(
                analysisExecutorService,
                currentAnalyser,
            )
        }

        if (lifeCycleOwner == null || analyser == null) {
            return Err(IllegalStateException("No LifecycleOwner or analyser"))
        }

        previewStream.setSurfaceProvider(previewView.surfaceProvider)
        val cameraProviderFuture = ProcessCameraProvider.getInstance(previewView.context)
        cameraProviderFuture.addListener(
            {
                val cameraProvider = cameraProviderFuture.get()
                // Try to bind to lifecycle. Can fail.
                cameraProvider.unbindAll()
                camera = cameraProvider.bindToLifecycle(
                    lifeCycleOwner,
                    cameraSelector,
                    previewStream,
                    imageAnalysis,
                ).apply {
                    when (_flashLightState.value) {
                        FlashLightState.UNKNOWN -> {
                            _flashLightState.value = if (cameraInfo.hasFlashUnit()) {
                                mapToFlashLightState(cameraInfo.torchState.value)
                            } else {
                                FlashLightState.UNSUPPORTED
                            }
                        }

                        FlashLightState.ON -> cameraControl.enableTorch(true)

                        FlashLightState.OFF,
                        FlashLightState.UNSUPPORTED -> Unit
                    }
                    _isRunning.value = true
                }
            },
            ContextCompat.getMainExecutor(previewView.context)
        )

        return Ok(Unit)
    }

    override fun resumeScanner() {
        _isRunning.value = true
    }

    override fun pauseScanner() {
        _isRunning.value = false
    }

    override fun unRegisterScanner() {
        // Stop analysis and decouple the preview from the device camera
        _isRunning.value = false
        imageAnalysis.clearAnalyzer()
        previewStream.setSurfaceProvider(null)
        analysisExecutorService.shutdown()
    }

    override suspend fun toggleFlashLight() {
        camera?.let { camera ->
            if (camera.cameraInfo.hasFlashUnit()) {
                val state = camera.toggleTorch()
                _flashLightState.value = state
            }
        }
    }

    private suspend fun Camera.toggleTorch(): FlashLightState = suspendCoroutine { continuation ->
        val executor = Dispatchers.Main.asExecutor()
        cameraControl.enableTorch(cameraInfo.torchState.value == TorchState.OFF).addListener(
            {
                val state = mapToFlashLightState(cameraInfo.torchState.value)
                continuation.resume(state)
            },
            executor
        )
    }

    private fun mapToFlashLightState(torchState: Int?) = when (torchState) {
        TorchState.ON -> FlashLightState.ON
        TorchState.OFF -> FlashLightState.OFF
        else -> FlashLightState.UNSUPPORTED
    }

    private fun processImageProxy(
        imageProxy: ImageProxy,
        barcodeReader: BarcodeReader,
        onQrDetected: (List<String>) -> Unit,
    ) {
        if (!isRunning.value) {
            imageProxy.close()
            return
        }

        barcodeReader.read(imageProxy).forEach { result ->
            result.text?.let { text ->
                onQrDetected(listOf(text))
                imageProxy.close()
                return
            }
        }
        imageProxy.close()
    }

    companion object {
        val RESOLUTION_STRATEGY by lazy {
            ResolutionSelector.Builder().setResolutionStrategy(
                ResolutionStrategy(
                    Size(1500, 1500),
                    ResolutionStrategy.FALLBACK_RULE_CLOSEST_LOWER_THEN_HIGHER
                )
            ).build()
        }

        val SCANNER_QR_OPTIONS by lazy {
            BarcodeReader.Options(
                formats = setOf(BarcodeReader.Format.QR_CODE),

                // Return qr code "content" as plain text
                textMode = BarcodeReader.TextMode.PLAIN,

                // Binarizer to prepare input image for qr code detection
                binarizer = BarcodeReader.Binarizer.LOCAL_AVERAGE,

                // Try harder to find qr codes. Better detection of bad qr codes (e.g. blurred or low-resolution), but
                // slower performance in general.
                tryHarder = true,

                // Support inverted qr codes
                tryInvert = true,

                // Downscale image to increase the detection rate with noisy input (e.g. monitor scans) and high-resolution images
                tryDownscale = true,

                maxNumberOfSymbols = 1,

                // Faster detection if input only contains perfectly aligned qr codes with no rotation, no skew, no noise.
                // Example: a raster image of a digitally generated qr code fed directly to BarcodeReader.read()
                isPure = false,

                // No effect for qr code detection according to https://github.com/zxing-cpp/zxing-cpp/issues/128#issue-635655065
                tryRotate = false,
            )
        }
    }
}
