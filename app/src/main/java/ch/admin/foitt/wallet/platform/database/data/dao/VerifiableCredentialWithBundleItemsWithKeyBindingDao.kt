package ch.admin.foitt.wallet.platform.database.data.dao

import androidx.room.Dao
import androidx.room.Query
import androidx.room.Transaction
import ch.admin.foitt.wallet.platform.database.domain.model.VerifiableCredentialWithBundleItemsWithKeyBinding

@Dao
interface VerifiableCredentialWithBundleItemsWithKeyBindingDao {
    @Transaction
    @Query("SELECT * FROM verifiableCredentialEntity")
    fun getAll(): List<VerifiableCredentialWithBundleItemsWithKeyBinding>

    @Transaction
    @Query("SELECT * FROM verifiableCredentialEntity WHERE credentialId = :id")
    fun getCredentialWithKeyBindingById(id: Long): VerifiableCredentialWithBundleItemsWithKeyBinding
}
