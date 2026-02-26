package ch.admin.foitt.wallet.feature.eIdRequestVerification.di

import ch.admin.foitt.avwrapper.AVBeam
import ch.admin.foitt.avwrapper.AVBeamImpl
import ch.admin.foitt.wallet.feature.eIdRequestVerification.domain.usecase.AreEIdDocumentsEqual
import ch.admin.foitt.wallet.feature.eIdRequestVerification.domain.usecase.FetchSIdCase
import ch.admin.foitt.wallet.feature.eIdRequestVerification.domain.usecase.GetDocumentScanData
import ch.admin.foitt.wallet.feature.eIdRequestVerification.domain.usecase.GetEIdMrzValues
import ch.admin.foitt.wallet.feature.eIdRequestVerification.domain.usecase.GetEIdRequestCase
import ch.admin.foitt.wallet.feature.eIdRequestVerification.domain.usecase.SaveEIdRequestCase
import ch.admin.foitt.wallet.feature.eIdRequestVerification.domain.usecase.SaveEIdRequestFiles
import ch.admin.foitt.wallet.feature.eIdRequestVerification.domain.usecase.SaveEIdRequestState
import ch.admin.foitt.wallet.feature.eIdRequestVerification.domain.usecase.SaveMetadataFile
import ch.admin.foitt.wallet.feature.eIdRequestVerification.domain.usecase.SubmitCaseId
import ch.admin.foitt.wallet.feature.eIdRequestVerification.domain.usecase.UploadAllFiles
import ch.admin.foitt.wallet.feature.eIdRequestVerification.domain.usecase.implementation.AreEIdDocumentsEqualImpl
import ch.admin.foitt.wallet.feature.eIdRequestVerification.domain.usecase.implementation.FetchSIdCaseImpl
import ch.admin.foitt.wallet.feature.eIdRequestVerification.domain.usecase.implementation.GetDocumentScanDataImpl
import ch.admin.foitt.wallet.feature.eIdRequestVerification.domain.usecase.implementation.GetEIdMrzValuesImpl
import ch.admin.foitt.wallet.feature.eIdRequestVerification.domain.usecase.implementation.GetEIdRequestCaseImpl
import ch.admin.foitt.wallet.feature.eIdRequestVerification.domain.usecase.implementation.SaveEIdRequestCaseImpl
import ch.admin.foitt.wallet.feature.eIdRequestVerification.domain.usecase.implementation.SaveEIdRequestFilesImpl
import ch.admin.foitt.wallet.feature.eIdRequestVerification.domain.usecase.implementation.SaveEIdRequestStateImpl
import ch.admin.foitt.wallet.feature.eIdRequestVerification.domain.usecase.implementation.SaveMetadataFileImpl
import ch.admin.foitt.wallet.feature.eIdRequestVerification.domain.usecase.implementation.SubmitCaseIdImpl
import ch.admin.foitt.wallet.feature.eIdRequestVerification.domain.usecase.implementation.UploadAllFilesImpl
import dagger.Binds
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.components.ActivityRetainedComponent
import dagger.hilt.android.scopes.ActivityRetainedScoped

@Module
@InstallIn(ActivityRetainedComponent::class)
interface EIdRequestVerificationModule {
    @Binds
    fun bindSaveEIdRequestCase(
        useCase: SaveEIdRequestCaseImpl
    ): SaveEIdRequestCase

    @Binds
    fun bindSaveEIdRequestState(
        useCase: SaveEIdRequestStateImpl
    ): SaveEIdRequestState

    @Binds
    fun bindFetchSIdCase(
        useCase: FetchSIdCaseImpl
    ): FetchSIdCase

    @Binds
    fun bindSaveEIdRequestFiles(
        useCase: SaveEIdRequestFilesImpl
    ): SaveEIdRequestFiles

    @Binds
    fun bindUploadAllFiles(
        useCase: UploadAllFilesImpl
    ): UploadAllFiles

    @Binds
    fun bindGetDocumentScanData(
        useCase: GetDocumentScanDataImpl
    ): GetDocumentScanData

    @Binds
    fun bindSubmitCaseId(
        useCase: SubmitCaseIdImpl
    ): SubmitCaseId

    @Binds
    fun bindSaveMetadataFile(
        useCase: SaveMetadataFileImpl
    ): SaveMetadataFile

    @Binds
    fun bindAreEIdDocumentsEqual(
        useCase: AreEIdDocumentsEqualImpl
    ): AreEIdDocumentsEqual

    @Binds
    fun bindFindEIdValues(
        useCase: GetEIdMrzValuesImpl
    ): GetEIdMrzValues

    @Binds
    fun bindGetIdRequestCase(
        useCase: GetEIdRequestCaseImpl
    ): GetEIdRequestCase
}

@Module
@InstallIn(ActivityRetainedComponent::class)
object AVBeamModule {

    @Provides
    @ActivityRetainedScoped
    fun provideAVBeam(): AVBeam {
        return AVBeamImpl()
    }
}
