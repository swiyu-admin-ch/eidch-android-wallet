package ch.admin.foitt.wallet.platform.versionEnforcement.domain.usecase

fun interface GetDeviceModel {
    operator fun invoke(): String
}
