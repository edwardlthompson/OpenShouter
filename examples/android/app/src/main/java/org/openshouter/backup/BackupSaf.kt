package org.openshouter.backup

import android.content.Context
import android.net.Uri

object BackupSaf {
    const val MIME_ZIP = "application/zip"
    const val DEFAULT_NAME = "openshouter-backup.zip"

    fun write(context: Context, uri: Uri, bytes: ByteArray): Boolean = runCatching {
        context.contentResolver.openOutputStream(uri)?.use { it.write(bytes) } != null
    }.getOrDefault(false)

    fun read(context: Context, uri: Uri): ByteArray? = runCatching {
        context.contentResolver.openInputStream(uri)?.use { stream ->
            BackupLimits.readBounded(stream, BackupLimits.MAX_ZIP_BYTES)
        }
    }.getOrNull()
}
