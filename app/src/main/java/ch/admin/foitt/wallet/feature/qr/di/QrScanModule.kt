package ch.admin.foitt.wallet.feature.qr.di

import ch.admin.foitt.wallet.feature.qr.infra.qrscan.QrScanner
import ch.admin.foitt.wallet.feature.qr.infra.qrscan.implementation.QrScannerImpl
import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.android.components.ActivityRetainedComponent

@Module
@InstallIn(ActivityRetainedComponent::class)
internal interface QrScanModule {

    @Binds
    fun bindQrScanner(
        qrScanner: QrScannerImpl
    ): QrScanner
}
