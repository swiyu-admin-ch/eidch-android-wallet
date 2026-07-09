package ch.admin.foitt.wallet.platform.database.data.migrations

import androidx.core.database.getStringOrNull
import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase
import ch.admin.foitt.openid4vc.domain.model.claimsPathPointer.toPointerString
import ch.admin.foitt.wallet.platform.oca.domain.util.naiveJsonPathToClaimsPathPointer

internal val Migration27to28 = object : Migration(27, 28) {
    override fun migrate(db: SupportSQLiteDatabase) {
        db.query("SELECT `id`, `description` FROM `CredentialDisplay`").use { cursor ->
            while (cursor.moveToNext()) {
                val id = cursor.getLong(cursor.getColumnIndexOrThrow("id"))
                val description = cursor.getStringOrNull(cursor.getColumnIndexOrThrow("description"))
                description?.let {
                    val jsonPathFinder = Regex("""\{\{(?<jsonPath>\$\.[\w.\[\]]+?)\}\}""")
                    val newDescription = jsonPathFinder.replace(description) { match ->
                        val jsonPath = match.groups["jsonPath"]?.value ?: return@replace ""
                        val path = naiveJsonPathToClaimsPathPointer(jsonPath)
                        "{{${path.toPointerString()}}}"
                    }

                    db.execSQL("UPDATE `CredentialDisplay` SET `description` = '$newDescription' WHERE `id` = $id")
                }
            }
        }
    }
}
