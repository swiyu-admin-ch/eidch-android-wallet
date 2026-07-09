package ch.admin.foitt.wallet.platform.versionEnforcement.domain.usecase

fun interface GetOSVersion {
    operator fun invoke(): String
}
