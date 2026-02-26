package ch.admin.foitt.wallet.platform.database.data.dao

import androidx.room.Dao
import androidx.room.Query
import androidx.room.Transaction
import ch.admin.foitt.wallet.platform.database.domain.model.BundleItemWithKeyBinding

@Dao
interface BundleItemWithKeyBindingDao {
    @Transaction
    @Query("SELECT * FROM bundleitementity")
    fun getAll(): List<BundleItemWithKeyBinding>

    @Transaction
    @Query("SELECT * FROM bundleitementity WHERE id IN (:ids)")
    fun getBundleItemWithKeyBindingByIds(ids: List<Long>): List<BundleItemWithKeyBinding>

    @Transaction
    @Query("SELECT * FROM bundleitementity WHERE credentialId = :credentialId ORDER BY presented DESC LIMIT :amount")
    fun getBundleItemsWithKeyBindingsToDelete(credentialId: Long, amount: Int): List<BundleItemWithKeyBinding>
}
