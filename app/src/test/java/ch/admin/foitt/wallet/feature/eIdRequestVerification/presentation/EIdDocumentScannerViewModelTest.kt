package ch.admin.foitt.wallet.feature.eIdRequestVerification.presentation

import androidx.appcompat.app.AppCompatActivity
import ch.admin.foitt.avwrapper.AVBeam
import ch.admin.foitt.avwrapper.AVBeamError
import ch.admin.foitt.avwrapper.AVBeamStatus
import ch.admin.foitt.avwrapper.AvBeamNotification
import ch.admin.foitt.avwrapper.AvBeamScanDocumentNotification
import ch.admin.foitt.avwrapper.DocumentScanPackageResult
import ch.admin.foitt.wallet.feature.eIdRequestVerification.domain.model.TextKeyType
import ch.admin.foitt.wallet.feature.eIdRequestVerification.domain.usecase.AreEIdDocumentsEqual
import ch.admin.foitt.wallet.feature.eIdRequestVerification.domain.usecase.CreateSDKErrorTextKeys
import ch.admin.foitt.wallet.feature.eIdRequestVerification.domain.usecase.GetEIdRequestCase
import ch.admin.foitt.wallet.platform.cameraPermissionHandler.domain.model.PermissionState
import ch.admin.foitt.wallet.platform.cameraPermissionHandler.infra.PermissionStateHandler
import ch.admin.foitt.wallet.platform.eIdApplicationProcess.domain.model.EIdUiDocumentType
import ch.admin.foitt.wallet.platform.eIdApplicationProcess.domain.usecase.GetDocumentType
import ch.admin.foitt.wallet.platform.eIdApplicationProcess.domain.usecase.SetDocumentScanResult
import ch.admin.foitt.wallet.platform.environmentSetup.domain.repository.EnvironmentSetupRepository
import ch.admin.foitt.wallet.platform.navigation.DestinationScopedComponentManager
import ch.admin.foitt.wallet.platform.navigation.NavigationManager
import ch.admin.foitt.wallet.platform.scaffold.domain.usecase.SetTopBarState
import ch.admin.foitt.wallet.platform.scanning.di.AvBeamSdkEntryPoint
import ch.admin.foitt.wallet.platform.scanning.domain.repository.AvBeamRepository
import com.github.michaelbull.result.Ok
import io.mockk.MockKAnnotations
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.every
import io.mockk.impl.annotations.MockK
import io.mockk.junit5.MockKExtension
import io.mockk.mockk
import io.mockk.unmockkAll
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith

@OptIn(ExperimentalCoroutinesApi::class)
@ExtendWith(MockKExtension::class)
class EIdDocumentScannerViewModelTest {

    private val testDispatcher = StandardTestDispatcher()

    @MockK(relaxed = true)
    lateinit var avBeam: AVBeam

    @MockK(relaxed = true)
    lateinit var navManager: NavigationManager

    @MockK(relaxed = true)
    lateinit var setDocumentScanResult: SetDocumentScanResult

    @MockK(relaxed = true)
    lateinit var setTopBarState: SetTopBarState

    @MockK(relaxed = true)
    lateinit var environmentSetupRepository: EnvironmentSetupRepository

    @MockK(relaxed = true)
    lateinit var activity: AppCompatActivity

    @MockK(relaxed = true)
    lateinit var areEIdDocumentsEqual: AreEIdDocumentsEqual

    @MockK(relaxed = true)
    lateinit var permissionStateHandler: PermissionStateHandler

    @MockK(relaxed = true)
    lateinit var createSDKErrorTextKeys: CreateSDKErrorTextKeys

    val mockPermissionState: StateFlow<PermissionState> = MutableStateFlow(PermissionState.Granted)

    @MockK(relaxed = true)
    lateinit var getDocumentType: GetDocumentType

    @MockK(relaxed = true)
    lateinit var getEIdRequestCase: GetEIdRequestCase

    @MockK
    private lateinit var mockDestinationScopedComponentManager: DestinationScopedComponentManager

    @MockK
    private lateinit var mockAvBeamSdkEntryPoint: AvBeamSdkEntryPoint

    @MockK(relaxed = true)
    private lateinit var mockAvBeamRepository: AvBeamRepository

    private lateinit var scanDocumentFlow: MutableStateFlow<AvBeamScanDocumentNotification>
    private lateinit var statusFlow: MutableStateFlow<AVBeamStatus>
    private lateinit var errorFlow: MutableStateFlow<AVBeamError>
    private lateinit var viewModel: EIdDocumentScannerViewModel
    private lateinit var documentTypeFlow: MutableStateFlow<EIdUiDocumentType>

    @BeforeEach
    fun setup() {
        MockKAnnotations.init(this)
        Dispatchers.setMain(testDispatcher)

        scanDocumentFlow = MutableStateFlow(AvBeamNotification.Empty)
        statusFlow = MutableStateFlow(AVBeamStatus.Init)
        errorFlow = MutableStateFlow(AVBeamError.None)
        documentTypeFlow = MutableStateFlow(EIdUiDocumentType.IDENTITY_CARD)

        every { avBeam.initializedFlow } returns MutableStateFlow(true)
        every { avBeam.getGLView(any(), any()) } returns mockk {
            every { width } returns 100
            every { height } returns 200
        }
        every { avBeam.scanDocumentFlow } returns scanDocumentFlow
        every { avBeam.statusFlow } returns statusFlow
        every { avBeam.errorFlow } returns errorFlow

        every {
            mockDestinationScopedComponentManager.getEntryPoint(
                AvBeamSdkEntryPoint::class.java,
                componentScope = any()
            )
        } returns mockAvBeamSdkEntryPoint

        every {
            mockAvBeamSdkEntryPoint.avBeamRepository()
        } returns mockAvBeamRepository

        every {
            mockAvBeamRepository.getBeam()
        } returns avBeam

        coEvery { areEIdDocumentsEqual(any(), any()) } returns Ok(true)
        every { getDocumentType() } returns documentTypeFlow
        coEvery { permissionStateHandler.permissionState } returns mockPermissionState
        coEvery { createSDKErrorTextKeys(AVBeamError.MrzNotDetected, TextKeyType.TITLE) } returns 1

        viewModel = EIdDocumentScannerViewModel(
            navManager = navManager,
            setDocumentScanResult = setDocumentScanResult,
            setTopBarState = setTopBarState,
            areEIdDocumentsEqual = areEIdDocumentsEqual,
            caseId = "",
            getDocumentType = getDocumentType,
            getEIdRequestCase = getEIdRequestCase,
            destinationScopedComponentManager = mockDestinationScopedComponentManager,
            permissionStateHandler = permissionStateHandler,
            appContext = mockk(),
            createSDKErrorTextKeys = createSDKErrorTextKeys
        )
    }

    @AfterEach
    fun tearDown() {
        unmockkAll()
        Dispatchers.resetMain()
    }

    @Test
    fun `calls stopCamera and setDocumentScanResult when DocumentScanCompleted is emitted`() = runTest(testDispatcher) {
        // Given
        val fakePackageData = mockk<DocumentScanPackageResult>(relaxed = true)
        val startedCamera = AVBeamStatus.StreamingStarted
        val notification = AvBeamNotification.DocumentScanCompleted(fakePackageData)

        // When
        viewModel.initScannerSdk(activity = activity)
        viewModel.onResumeScan()
        viewModel.onAfterViewLayout(100, 100)
        statusFlow.update { startedCamera }
        scanDocumentFlow.update { notification }
        advanceUntilIdle()

        // Then
        coVerify {
            avBeam.stopCamera()
            setDocumentScanResult.invoke(fakePackageData)
        }
    }
}
