package ch.admin.foitt.wallet.platform.database.data.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import ch.admin.foitt.wallet.platform.database.domain.model.Credential

@Dao
interface CredentialDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun insert(credential: Credential): Long

    @Query("DELETE FROM Credential WHERE id = :id")
    fun deleteById(id: Long)

    @Query("SELECT * FROM Credential WHERE id = :id")
    fun getById(id: Long): Credential

    @Query("SELECT * FROM Credential ORDER BY createdAt DESC")
    fun getAll(): List<Credential>
}
