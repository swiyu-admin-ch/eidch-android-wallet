package ch.admin.foitt.wallet.platform.database.data.migrations

import androidx.room.DeleteColumn
import androidx.room.migration.AutoMigrationSpec

// AutoMigration spec for DB schema 17 to 18
// Deletes the deprecated columns from VerifiableCredentialEntity which were
// moved to BundleItemEntity in a previous step.
@DeleteColumn.Entries(
    DeleteColumn(
        tableName = "VerifiableCredentialEntity",
        columnName = "status",
    ),
    DeleteColumn(
        tableName = "VerifiableCredentialEntity",
        columnName = "payload",
    ),
)
class Migration17to18 : AutoMigrationSpec
