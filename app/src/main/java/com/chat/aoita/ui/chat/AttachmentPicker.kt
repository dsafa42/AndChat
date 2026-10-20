package com.chat.aoita.ui.chat

import android.Manifest
import android.content.Context
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Build
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.platform.LocalContext
import androidx.core.content.ContextCompat
import androidx.core.content.FileProvider
import java.io.File

/**
 * 附件选择：相机拍照 / 相册选图 / 文件选择。
 *
 * - 照片：用 Photo Picker（Android 13+ 免权限；低版本自动回退）。
 * - 文件：用 OpenDocument，无需权限。
 * - 相机：需要 CAMERA 运行时权限，拍摄结果写入 filesDir/images 并通过 FileProvider 共享。
 */
class AttachPicker(
    val onPickCamera: () -> Unit,
    val onPickPhoto: () -> Unit,
    val onPickFile: () -> Unit,
)

@Composable
fun rememberAttachPicker(
    onPicked: (Uri) -> Unit,
    onError: (String) -> Unit,
): AttachPicker {
    val context = LocalContext.current

    // 相机临时文件 Uri（拍照前生成，拍完从结果里读回）
    var cameraUri by remember { mutableStateOf<Uri?>(null) }

    // 相册选图（Photo Picker，无需存储权限）
    val photoLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.PickVisualMedia()
    ) { uri -> if (uri != null) onPicked(uri) }

    // 文件选择（无需权限）
    val fileLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.OpenDocument()
    ) { uri -> if (uri != null) onPicked(uri) }

    // 相机拍照
    val cameraLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.TakePicture()
    ) { ok ->
        val uri = cameraUri
        if (ok && uri != null) onPicked(uri)
    }

    fun launchCamera() {
        val uri = createImageUri(context)
        if (uri == null) {
            onError("无法创建照片文件")
            return
        }
        cameraUri = uri
        try {
            cameraLauncher.launch(uri)
        } catch (_: Exception) {
            onError("无法打开相机")
        }
    }

    // CAMERA 权限请求
    val cameraPermLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { granted ->
        if (granted) launchCamera() else onError("未授予相机权限")
    }

    return remember(context) {
        AttachPicker(
            onPickCamera = {
                if (hasCameraPermission(context)) launchCamera()
                else cameraPermLauncher.launch(Manifest.permission.CAMERA)
            },
            onPickPhoto = {
                photoLauncher.launch(
                    PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly)
                )
            },
            onPickFile = {
                fileLauncher.launch(arrayOf("*/*"))
            },
        )
    }
}

private fun hasCameraPermission(context: Context): Boolean =
    ContextCompat.checkSelfPermission(context, Manifest.permission.CAMERA) ==
        PackageManager.PERMISSION_GRANTED

/** 在 filesDir/images 下创建一张待写入的照片文件，返回可共享的 content:// Uri。 */
private fun createImageUri(context: Context): Uri? = try {
    val dir = File(context.filesDir, "images").apply { if (!exists()) mkdirs() }
    val file = File(dir, "IMG_${System.currentTimeMillis()}.jpg")
    if (!file.exists()) file.createNewFile()
    FileProvider.getUriForFile(context, "${context.packageName}.fileprovider", file)
} catch (_: Exception) {
    null
}

/** Android 13+ 用 READ_MEDIA_IMAGES，否则用 READ_EXTERNAL_STORAGE（仅提示用，Photo Picker 本身免权限）。 */
@Suppress("unused")
private fun mediaPermission(): String =
    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
        Manifest.permission.READ_MEDIA_IMAGES
    } else {
        Manifest.permission.READ_EXTERNAL_STORAGE
    }
