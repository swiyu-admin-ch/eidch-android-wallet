package ch.admin.foitt.wallet.platform.database.util

import android.database.Cursor
import androidx.core.database.getBlobOrNull
import androidx.core.database.getLongOrNull
import androidx.core.database.getStringOrNull

fun Cursor.getStringColumn(columnName: String) = getString(getColumnIndexOrThrow(columnName))
fun Cursor.getStringOrNullColumn(columnName: String) = getStringOrNull(getColumnIndexOrThrow(columnName))
fun Cursor.getIntColumn(columnName: String) = getInt(getColumnIndexOrThrow(columnName))
fun Cursor.getLongColumn(columnName: String) = getLong(getColumnIndexOrThrow(columnName))
fun Cursor.getLongOrNullColumn(columnName: String) = getLongOrNull(getColumnIndexOrThrow(columnName))
fun Cursor.getBlobOrNullColumn(columnName: String) = getBlobOrNull(getColumnIndexOrThrow(columnName))
fun Cursor.getBooleanColumn(columnName: String) = getInt(getColumnIndex(columnName)) == 1
