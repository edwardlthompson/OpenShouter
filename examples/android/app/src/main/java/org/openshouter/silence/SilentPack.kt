package org.openshouter.silence

import android.content.ContentUris
import android.content.ContentValues
import android.content.Context
import android.net.Uri
import android.os.Build
import android.provider.MediaStore

object SilentPack {
    fun existingUri(context: Context): Uri? {
        val resolver = context.contentResolver
        val table = MediaStore.Audio.Media.EXTERNAL_CONTENT_URI
        return resolver.query(
            table,
            arrayOf(MediaStore.Audio.Media._ID),
            "${MediaStore.Audio.Media.DISPLAY_NAME} = ?",
            arrayOf(SilentWav.DISPLAY_NAME),
            null,
        )?.use { cursor ->
            if (!cursor.moveToFirst()) return@use null
            ContentUris.withAppendedId(table, cursor.getLong(0))
        }
    }

    fun install(context: Context): Uri? = runCatching {
        existingUri(context)?.let { return@runCatching it }
        val values = ContentValues().apply {
            put(MediaStore.Audio.Media.DISPLAY_NAME, SilentWav.DISPLAY_NAME)
            put(MediaStore.Audio.Media.TITLE, SilentWav.TITLE)
            put(MediaStore.Audio.Media.MIME_TYPE, SilentWav.MIME)
            put(MediaStore.Audio.Media.IS_NOTIFICATION, 1)
            put(MediaStore.Audio.Media.IS_RINGTONE, 1)
            put(MediaStore.Audio.Media.IS_ALARM, 1)
            if (Build.VERSION.SDK_INT >= 29) put(MediaStore.Audio.Media.IS_PENDING, 1)
        }
        val uri = context.contentResolver.insert(MediaStore.Audio.Media.EXTERNAL_CONTENT_URI, values)
            ?: return@runCatching null
        context.contentResolver.openOutputStream(uri)?.use { stream -> stream.write(SilentWav.bytes()) }
            ?: return@runCatching null
        if (Build.VERSION.SDK_INT >= 29) {
            context.contentResolver.update(
                uri,
                ContentValues().apply { put(MediaStore.Audio.Media.IS_PENDING, 0) },
                null,
                null,
            )
        }
        uri
    }.getOrNull()

    fun installed(context: Context): Boolean = existingUri(context) != null
}
