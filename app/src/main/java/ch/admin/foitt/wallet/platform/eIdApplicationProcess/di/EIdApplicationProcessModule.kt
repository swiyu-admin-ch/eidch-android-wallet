package ch.admin.foitt.wallet.platform.eIdApplicationProcess.di

import ch.admin.foitt.wallet.platform.eIdApplicationProcess.data.repository.EIdAvRepositoryImpl
import ch.admin.foitt.wallet.platform.eIdApplicationProcess.data.repository.EIdDocumentScanRepositoryImpl
import ch.admin.foitt.wallet.platform.eIdApplicationProcess.data.repository.EIdRequestCaseRepositoryImpl
import ch.admin.foitt.wallet.platform.eIdApplicationProcess.data.repository.EIdRequestCaseWithStateRepositoryImpl
import ch.admin.foitt.wallet.platform.eIdApplicationProcess.data.repository.EIdRequestFileRepositoryImpl
import ch.admin.foitt.wallet.platform.eIdApplicationProcess.data.repository.EIdRequestStateRepositoryImpl
import ch.admin.foitt.wallet.platform.eIdApplicationProcess.data.repository.EIdStartAutoVerificationRepositoryImpl
import ch.admin.foitt.wallet.platform.eIdApplicationProcess.data.repository.EidApplicationProcessRepositoryImpl
import ch.admin.foitt.wallet.platform.eIdApplicationProcess.data.repository.SIdRepositoryImpl
import ch.admin.foitt.wallet.platform.eIdApplicationProcess.domain.repository.EIdAvRepository
import ch.admin.foitt.wallet.platform.eIdApplicationProcess.domain.repository.EIdDocumentScanRepository
import ch.admin.foitt.wallet.platform.eIdApplicationProcess.domain.repository.EIdRequestCaseRepository
import ch.admin.foitt.wallet.platform.eIdApplicationProcess.domain.repository.EIdRequestCaseWithStateRepository
import ch.admin.foitt.wallet.platform.eIdApplicationProcess.domain.repository.EIdRequestFileRepository
import ch.admin.foitt.wallet.platform.eIdApplicationProcess.domain.repository.EIdRequestStateRepository
import ch.admin.foitt.wallet.platform.eIdApplicationProcess.domain.repository.EIdStartAutoVerificationRepository
import ch.admin.foitt.wallet.platform.eIdApplicationProcess.domain.repository.EidApplicationProcessRepository
import ch.admin.foitt.wallet.platform.eIdApplicationProcess.domain.repository.SIdRepository
import ch.admin.foitt.wallet.platform.eIdApplicationProcess.domain.usecase.FetchGuardianVerification
import ch.admin.foitt.wallet.platform.eIdApplicationProcess.domain.usecase.FetchSIdStatus
import ch.admin.foitt.wallet.platform.eIdApplicationProcess.domain.usecase.GetDocumentScanResult
import ch.admin.foitt.wallet.platform.eIdApplicationProcess.domain.usecase.GetDocumentType
import ch.admin.foitt.wallet.platform.eIdApplicationProcess.domain.usecase.GetHasLegalGuardian
import ch.admin.foitt.wallet.platform.eIdApplicationProcess.domain.usecase.GetStartAutoVerificationResult
import ch.admin.foitt.wallet.platform.eIdApplicationProcess.domain.usecase.PairCurrentWallet
import ch.admin.foitt.wallet.platform.eIdApplicationProcess.domain.usecase.PairWallet
import ch.admin.foitt.wallet.platform.eIdApplicationProcess.domain.usecase.SetDocumentScanResult
import ch.admin.foitt.wallet.platform.eIdApplicationProcess.domain.usecase.SetDocumentType
import ch.admin.foitt.wallet.platform.eIdApplicationProcess.domain.usecase.SetHasLegalGuardian
import ch.admin.foitt.wallet.platform.eIdApplicationProcess.domain.usecase.SetStartAutoVerificationResult
import ch.admin.foitt.wallet.platform.eIdApplicationProcess.domain.usecase.StartAutoVerification
import ch.admin.foitt.wallet.platform.eIdApplicationProcess.domain.usecase.StartOnlineSession
import ch.admin.foitt.wallet.platform.eIdApplicationProcess.domain.usecase.UpdateAllSIdStatuses
import ch.admin.foitt.wallet.platform.eIdApplicationProcess.domain.usecase.UpdateSIdStatusByCaseId
import ch.admin.foitt.wallet.platform.eIdApplicationProcess.domain.usecase.UploadFileToCase
import ch.admin.foitt.wallet.platform.eIdApplicationProcess.domain.usecase.ValidateAttestations
import ch.admin.foitt.wallet.platform.eIdApplicationProcess.domain.usecase.WalletPairingStatus
import ch.admin.foitt.wallet.platform.eIdApplicationProcess.domain.usecase.implementation.FetchGuardianVerificationImpl
import ch.admin.foitt.wallet.platform.eIdApplicationProcess.domain.usecase.implementation.FetchSIdStatusImpl
import ch.admin.foitt.wallet.platform.eIdApplicationProcess.domain.usecase.implementation.GetDocumentScanResultImpl
import ch.admin.foitt.wallet.platform.eIdApplicationProcess.domain.usecase.implementation.GetDocumentTypeImpl
import ch.admin.foitt.wallet.platform.eIdApplicationProcess.domain.usecase.implementation.GetHasLegalGuardianImpl
import ch.admin.foitt.wallet.platform.eIdApplicationProcess.domain.usecase.implementation.GetStartAutoVerificationResultImpl
import ch.admin.foitt.wallet.platform.eIdApplicationProcess.domain.usecase.implementation.PairCurrentWalletImpl
import ch.admin.foitt.wallet.platform.eIdApplicationProcess.domain.usecase.implementation.PairWalletImpl
import ch.admin.foitt.wallet.platform.eIdApplicationProcess.domain.usecase.implementation.SetDocumentScanResultImpl
import ch.admin.foitt.wallet.platform.eIdApplicationProcess.domain.usecase.implementation.SetDocumentTypeImpl
import ch.admin.foitt.wallet.platform.eIdApplicationProcess.domain.usecase.implementation.SetHasLegalGuardianImpl
import ch.admin.foitt.wallet.platform.eIdApplicationProcess.domain.usecase.implementation.SetStartAutoVerificationResultImpl
import ch.admin.foitt.wallet.platform.eIdApplicationProcess.domain.usecase.implementation.StartAutoVerificationImpl
import ch.admin.foitt.wallet.platform.eIdApplicationProcess.domain.usecase.implementation.StartOnlineSessionImpl
import ch.admin.foitt.wallet.platform.eIdApplicationProcess.domain.usecase.implementation.UpdateAllSIdStatusesImpl
import ch.admin.foitt.wallet.platform.eIdApplicationProcess.domain.usecase.implementation.UpdateSIdStatusByCaseIdImpl
import ch.admin.foitt.wallet.platform.eIdApplicationProcess.domain.usecase.implementation.UploadFileToCaseImpl
import ch.admin.foitt.wallet.platform.eIdApplicationProcess.domain.usecase.implementation.ValidateAttestationsImpl
import ch.admin.foitt.wallet.platform.eIdApplicationProcess.domain.usecase.implementation.WalletPairingStatusImpl
import ch.admin.foitt.wallet.platform.navigation.DestinationScopedComponent
import ch.admin.foitt.wallet.platform.navigation.DestinationsScoped
import dagger.Binds
import dagger.Module
import dagger.hilt.EntryPoint
import dagger.hilt.InstallIn
import dagger.hilt.android.components.ActivityRetainedComponent
import dagger.hilt.android.scopes.ActivityRetainedScoped

@Module
@InstallIn(ActivityRetainedComponent::class)
interface EIdApplicationProcessModule {

    @Binds
    @ActivityRetainedScoped
    fun bindEIdRequestCaseRepository(
        repo: EIdRequestCaseRepositoryImpl
    ): EIdRequestCaseRepository

    @Binds
    @ActivityRetainedScoped
    fun bindEIdRequestStateRepository(
        repo: EIdRequestStateRepositoryImpl
    ): EIdRequestStateRepository

    @Binds
    @ActivityRetainedScoped
    fun bindSIdRepository(
        repo: SIdRepositoryImpl
    ): SIdRepository

    @Binds
    @ActivityRetainedScoped
    fun bindEIdRequestCaseWithStateRepository(
        repo: EIdRequestCaseWithStateRepositoryImpl
    ): EIdRequestCaseWithStateRepository

    @Binds
    @ActivityRetainedScoped
    fun bindEIdRequestFileRepository(
        repo: EIdRequestFileRepositoryImpl
    ): EIdRequestFileRepository

    @Binds
    @ActivityRetainedScoped
    fun bindEIdAvRepository(
        repo: EIdAvRepositoryImpl
    ): EIdAvRepository

    @Binds
    fun bindFetchSIdStatus(
        useCase: FetchSIdStatusImpl
    ): FetchSIdStatus

    @Binds
    fun bindFetchGuardianVerification(
        useCase: FetchGuardianVerificationImpl
    ): FetchGuardianVerification

    @Binds
    fun bindUpdateAllSIdStatuses(
        useCase: UpdateAllSIdStatusesImpl
    ): UpdateAllSIdStatuses

    @Binds
    fun bindSetHasLegalGuardian(
        useCase: SetHasLegalGuardianImpl
    ): SetHasLegalGuardian

    @Binds
    fun bindGetHasLegualGuardian(
        useCase: GetHasLegalGuardianImpl
    ): GetHasLegalGuardian

    @Binds
    fun bindUpdateSidStatusByCaseId(
        useCase: UpdateSIdStatusByCaseIdImpl
    ): UpdateSIdStatusByCaseId

    @Binds
    fun bindSetDocumentType(
        useCase: SetDocumentTypeImpl
    ): SetDocumentType

    @Binds
    fun bindGetDocumentType(
        useCase: GetDocumentTypeImpl
    ): GetDocumentType

    @Binds
    fun bindValidateAttestations(
        useCase: ValidateAttestationsImpl
    ): ValidateAttestations

    @Binds
    fun bindGetDocumentScanResult(
        useCase: GetDocumentScanResultImpl
    ): GetDocumentScanResult

    @Binds
    fun bindSetDocumentScanResult(
        useCase: SetDocumentScanResultImpl
    ): SetDocumentScanResult

    @Binds
    fun bindStartOnlineSession(
        useCase: StartOnlineSessionImpl
    ): StartOnlineSession

    @Binds
    fun bindPairWallet(
        useCase: PairWalletImpl
    ): PairWallet

    @Binds
    fun bindPairCurrentWallet(
        useCase: PairCurrentWalletImpl
    ): PairCurrentWallet

    @Binds
    fun bindStartAutoVerification(
        useCase: StartAutoVerificationImpl
    ): StartAutoVerification

    @Binds
    fun bindSetStartAutoVerificationResult(
        useCase: SetStartAutoVerificationResultImpl
    ): SetStartAutoVerificationResult

    @Binds
    fun bindGetStartAutoVerificationResult(
        useCase: GetStartAutoVerificationResultImpl
    ): GetStartAutoVerificationResult

    @Binds
    fun bindUploadFileToCase(
        useCase: UploadFileToCaseImpl
    ): UploadFileToCase

    @Binds
    fun walletPairingStatus(
        useCase: WalletPairingStatusImpl
    ): WalletPairingStatus
}

@Module
@InstallIn(DestinationScopedComponent::class)
internal interface EidApplicationRepositoryModule {
    @Binds
    @DestinationsScoped
    fun bindEidApplicationProcessRepository(
        repo: EidApplicationProcessRepositoryImpl
    ): EidApplicationProcessRepository

    @Binds
    @DestinationsScoped
    fun bindEIdDocumentScanRepository(
        repo: EIdDocumentScanRepositoryImpl
    ): EIdDocumentScanRepository

    @Binds
    @DestinationsScoped
    fun bindEIdStartAutoVerificationRepository(
        repo: EIdStartAutoVerificationRepositoryImpl
    ): EIdStartAutoVerificationRepository
}

@EntryPoint
@InstallIn(DestinationScopedComponent::class)
interface EidApplicationProcessEntryPoint {
    fun eidApplicationProcessRepository(): EidApplicationProcessRepository
    fun eidDocumentScanRepository(): EIdDocumentScanRepository
    fun eidStartAutoVerificationRepository(): EIdStartAutoVerificationRepository
}
