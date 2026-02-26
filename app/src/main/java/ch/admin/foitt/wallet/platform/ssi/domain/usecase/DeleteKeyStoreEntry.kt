package ch.admin.foitt.wallet.platform.ssi.domain.usecase

interface DeleteKeyStoreEntry {
    suspend operator fun invoke(keyIdentifier: String)
}
