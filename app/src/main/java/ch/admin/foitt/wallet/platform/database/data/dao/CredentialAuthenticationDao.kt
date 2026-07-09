package ch.admin.foitt.wallet.platform.database.data.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import ch.admin.foitt.openid4vc.domain.model.TokenType
import ch.admin.foitt.wallet.platform.database.domain.model.CredentialAuthenticationEntity

@Dao
interface CredentialAuthenticationDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun insert(authentication: CredentialAuthenticationEntity): Long

    @Query(
        """
            UPDATE CredentialAuthenticationEntity
            SET accessToken = :accessToken,
                tokenType = :tokenType,
                refreshToken = COALESCE(:refreshToken, refreshToken)
            WHERE credentialId = :credentialId
        """
    )
    fun updateTokensByCredentialId(
        credentialId: Long,
        accessToken: String,
        tokenType: TokenType,
        refreshToken: String?,
    ): Int

    @Query("SELECT * FROM CredentialAuthenticationEntity WHERE credentialId = :credentialId LIMIT 1")
    fun getByCredentialId(credentialId: Long): CredentialAuthenticationEntity?
}
