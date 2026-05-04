package ch.admin.foitt.wallet.feature.eIdRequestVerification.presentation

import androidx.appcompat.app.AppCompatActivity
import ch.admin.foitt.avwrapper.AVBeam
import ch.admin.foitt.avwrapper.AVBeamError
import ch.admin.foitt.avwrapper.AVBeamStatus
import ch.admin.foitt.avwrapper.AvBeamNotification
import ch.admin.foitt.avwrapper.AvBeamScanDocumentNotification
import ch.admin.foitt.avwrapper.DocumentScanPackageResult
import ch.admin.foitt.wallet.feature.eIdRequestVerification.domain.usecase.AreEIdDocumentsEqual
import ch.admin.foitt.wallet.platform.eIdApplicationProcess.domain.model.AutoVerificationResponse
import ch.admin.foitt.wallet.platform.eIdApplicationProcess.domain.model.EIdDocumentType
import ch.admin.foitt.wallet.platform.eIdApplicationProcess.domain.usecase.GetDocumentType
import ch.admin.foitt.wallet.platform.eIdApplicationProcess.domain.usecase.SetDocumentScanResult
import ch.admin.foitt.wallet.platform.environmentSetup.domain.repository.EnvironmentSetupRepository
import ch.admin.foitt.wallet.platform.navigation.NavigationManager
import ch.admin.foitt.wallet.platform.scaffold.domain.usecase.SetTopBarState
import com.github.michaelbull.result.Ok
import io.mockk.MockKAnnotations
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.every
import io.mockk.impl.annotations.MockK
import io.mockk.junit5.MockKExtension
import io.mockk.mockk
import io.mockk.unmockkAll
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableStateFlow
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
    lateinit var autoVerificationResponse: AutoVerificationResponse

    @MockK(relaxed = true)
    lateinit var areEIdDocumentsEqual: AreEIdDocumentsEqual

    @MockK(relaxed = true)
    lateinit var getDocumentType: GetDocumentType
    private lateinit var scanDocumentFlow: MutableStateFlow<AvBeamScanDocumentNotification>
    private lateinit var statusFlow: MutableStateFlow<AVBeamStatus>
    private lateinit var errorFlow: MutableStateFlow<AVBeamError>
    private lateinit var viewModel: EIdDocumentScannerViewModel
    private lateinit var documentTypeFlow: MutableStateFlow<EIdDocumentType>

    @BeforeEach
    fun setup() {
        MockKAnnotations.init(this)
        Dispatchers.setMain(testDispatcher)

        scanDocumentFlow = MutableStateFlow(AvBeamNotification.Empty)
        statusFlow = MutableStateFlow(AVBeamStatus.Init)
        errorFlow = MutableStateFlow(AVBeamError.None)
        documentTypeFlow = MutableStateFlow(EIdDocumentType.IDENTITY_CARD)

        coEvery { avBeam.initializedFlow } returns MutableStateFlow(true)
        every { avBeam.getGLView(any(), any()) } returns mockk {
            every { width } returns 100
            every { height } returns 200
        }
        every { avBeam.scanDocumentFlow } returns scanDocumentFlow
        every { avBeam.statusFlow } returns statusFlow
        every { avBeam.errorFlow } returns errorFlow
        coEvery { areEIdDocumentsEqual(any(), any()) } returns Ok(true)
        every { getDocumentType() } returns documentTypeFlow

        viewModel = EIdDocumentScannerViewModel(
            navManager = navManager,
            avBeam = avBeam,
            ioDispatcherScope = CoroutineScope(testDispatcher),
            setDocumentScanResult = setDocumentScanResult,
            environmentSetupRepository = environmentSetupRepository,
            setTopBarState = setTopBarState,
            areEIdDocumentsEqual = areEIdDocumentsEqual,
            caseId = "",
            getDocumentType = getDocumentType,
            appContext = mockk()
        )
    }

    @AfterEach
    fun tearDown() {
        unmockkAll()
        Dispatchers.resetMain()
    }

    @Test
    fun `calls avBeam_shutDown and stopCamera when DocumentScanCompleted is emitted`() = runTest(testDispatcher) {
        // Given
        val fakePackageData = mockk<DocumentScanPackageResult>(relaxed = true)
        val startedCamera = AVBeamStatus.StreamingStarted
        val notification = AvBeamNotification.DocumentScanCompleted(fakePackageData)

        // When
        viewModel.initScannerSdk(activity = activity)
        viewModel.onResume()
        viewModel.onAfterViewLayout(100, 100)
        statusFlow.update { startedCamera }
        scanDocumentFlow.update { notification }
        advanceUntilIdle()

        // Then
        coVerify {
            avBeam.stopCamera()
            avBeam.shutDown()
            setDocumentScanResult.invoke(fakePackageData)
        }
    }
}
