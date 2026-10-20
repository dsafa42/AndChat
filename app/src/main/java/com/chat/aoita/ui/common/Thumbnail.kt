package com.chat.aoita.ui.common

import android.content.ContentResolver
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Matrix
import android.media.ExifInterface
import android.net.Uri
import androidx.compose.runtime.Composable
import androidx.compose.runtime.produceState
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.platform.LocalContext
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

/** 在后台线程从 Uri 解码缩略图（按 EXIF 旋转，只读取需要的分辨率），解码完成前为 null。 */
@Composable
fun rememberThumbnail(uri: Uri, maxSizePx: Int): ImageBitmap? {
    val resolver = LocalContext.current.contentResolver
    return produceState<ImageBitmap?>(null, uri, maxSizePx) {
        value = withContext(Dispatchers.IO) { decodeThumbnail(resolver, uri, maxSizePx) }
    }.value
}

private fun decodeThumbnail(resolver: ContentResolver, uri: Uri, maxSizePx: Int): ImageBitmap? {
    return try {
        val bounds = BitmapFactory.Options().apply { inJustDecodeBounds = true }
        resolver.openInputStream(uri)?.use { BitmapFactory.decodeStream(it, null, bounds) }
        var sample = 1
        while (bounds.outWidth / sample > maxSizePx * 2 || bounds.outHeight / sample > maxSizePx * 2) sample *= 2
        val bitmap = resolver.openInputStream(uri)?.use {
            BitmapFactory.decodeStream(it, null, BitmapFactory.Options().apply { inSampleSize = sample })
        } ?: return null
        val orientation = resolver.openInputStream(uri)?.use {
            ExifInterface(it).getAttributeInt(ExifInterface.TAG_ORIENTATION, ExifInterface.ORIENTATION_NORMAL)
        }
        val degrees = when (orientation) {
            ExifInterface.ORIENTATION_ROTATE_90 -> 90f
            ExifInterface.ORIENTATION_ROTATE_180 -> 180f
            ExifInterface.ORIENTATION_ROTATE_270 -> 270f
            else -> 0f
        }
        val result = if (degrees == 0f) bitmap else Bitmap.createBitmap(
            bitmap, 0, 0, bitmap.width, bitmap.height, Matrix().apply { postRotate(degrees) }, true
        )
        result.asImageBitmap()
    } catch (_: Exception) {
        null
    }
}
