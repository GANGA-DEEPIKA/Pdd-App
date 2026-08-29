package com.example.campusfix

import android.content.Context
import android.net.Uri
import androidx.core.content.FileProvider
import java.io.File

fun createImageUri(context: Context): Uri {
    val directory = File(context.cacheDir, "complaints")
    if (!directory.exists()) directory.mkdirs()
    val file = File(directory, "complaint_photo_${System.currentTimeMillis()}.jpg")
    return FileProvider.getUriForFile(context, "com.example.campusfix.provider", file)
}

fun createVideoUri(context: Context): Uri {
    val directory = File(context.externalCacheDir, "complaints")
    if (!directory.exists()) directory.mkdirs()
    val file = File(directory, "complaint_video_${System.currentTimeMillis()}.mp4")
    return FileProvider.getUriForFile(context, "com.example.campusfix.provider", file)
}

fun createAudioFile(context: Context): File {
    val directory = File(context.cacheDir, "complaints")
    if (!directory.exists()) directory.mkdirs()
    return File(directory, "complaint_voice_${System.currentTimeMillis()}.mp4")
}
