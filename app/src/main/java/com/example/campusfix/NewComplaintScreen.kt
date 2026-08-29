package com.example.campusfix

import android.Manifest
import android.content.Context
import android.media.MediaPlayer
import android.util.Log
import android.media.MediaRecorder
import android.net.Uri
import android.os.Build
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.content.FileProvider
import java.io.File

@Composable
fun NewComplaintScreen(
    userCollege: String,
    onBackClick: () -> Unit,
    onReviewClick: (ComplaintData) -> Unit
) {
    val context = LocalContext.current

    var title by remember { mutableStateOf("") }
    var details by remember { mutableStateOf("") }
    var location by remember { mutableStateOf("") }
    var category by remember { mutableStateOf("") }

    var selectedPhoto by remember { mutableStateOf<Uri?>(null) }
    var selectedVideo by remember { mutableStateOf<Uri?>(null) }
    var cameraPhotoUri by remember { mutableStateOf<Uri?>(null) }
    var cameraVideoUri by remember { mutableStateOf<Uri?>(null) }

    var showPhotoOptions by remember { mutableStateOf(false) }
    var showVideoOptions by remember { mutableStateOf(false) }
    var showAttachmentAction by remember { mutableStateOf<String?>(null) } // "photo", "video", "voice"

    var recorder by remember { mutableStateOf<MediaRecorder?>(null) }
    var audioFilePath by remember { mutableStateOf("") }
    var isRecording by remember { mutableStateOf(false) }
    var message by remember { mutableStateOf("") }
    var mediaPlayer by remember { mutableStateOf<MediaPlayer?>(null) }
    var isPlaying by remember { mutableStateOf(false) }

    val photoGalleryLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.GetContent()
    ) { uri -> if (uri != null) selectedPhoto = uri }

    val videoGalleryLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.GetContent()
    ) { uri -> if (uri != null) selectedVideo = uri }

    val photoCameraLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.TakePicture()
    ) { success -> if (success) selectedPhoto = cameraPhotoUri }

    val videoCameraLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.CaptureVideo()
    ) { success -> if (success) selectedVideo = cameraVideoUri }

    val cameraPermissionLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { granted ->
        if (granted) {
            val uri = createImageUri(context)
            cameraPhotoUri = uri
            photoCameraLauncher.launch(uri)
        }
    }

    val videoPermissionLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.RequestMultiplePermissions()
    ) { permissions ->
        val cameraGranted = permissions[Manifest.permission.CAMERA] ?: false
        val audioGranted = permissions[Manifest.permission.RECORD_AUDIO] ?: false
        if (cameraGranted && audioGranted) {
            val uri = createVideoUri(context)
            cameraVideoUri = uri
            videoCameraLauncher.launch(uri)
        }
    }

    val audioPermissionLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { granted ->
        if (granted) {
            if (!isRecording) {
                val file = createAudioFile(context)
                audioFilePath = file.absolutePath
                recorder = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) MediaRecorder(context) else MediaRecorder()
                recorder?.apply {
                    setAudioSource(MediaRecorder.AudioSource.MIC)
                    setOutputFormat(MediaRecorder.OutputFormat.MPEG_4)
                    setAudioEncoder(MediaRecorder.AudioEncoder.AAC)
                    setOutputFile(audioFilePath)
                    prepare()
                    start()
                }
                isRecording = true
                message = "Voice recording started..."
            }
        }
    }

    if (showPhotoOptions) {
        AlertDialog(
            onDismissRequest = { showPhotoOptions = false },
            title = { Text("Add Photo") },
            text = { Text("Select photo source") },
            confirmButton = {
                TextButton(onClick = {
                    showPhotoOptions = false
                    cameraPermissionLauncher.launch(Manifest.permission.CAMERA)
                }) { Text("Capture Photo") }
            },
            dismissButton = {
                TextButton(onClick = {
                    showPhotoOptions = false
                    photoGalleryLauncher.launch("image/*")
                }) { Text("Choose Gallery") }
            }
        )
    }

    if (showVideoOptions) {
        AlertDialog(
            onDismissRequest = { showVideoOptions = false },
            title = { Text("Add Video") },
            text = { Text("Select video source") },
            confirmButton = {
                TextButton(onClick = {
                    showVideoOptions = false
                    videoPermissionLauncher.launch(arrayOf(Manifest.permission.CAMERA, Manifest.permission.RECORD_AUDIO))
                }) { Text("Record Video") }
            },
            dismissButton = {
                TextButton(onClick = {
                    showVideoOptions = false
                    videoGalleryLauncher.launch("video/*")
                }) { Text("Choose Gallery") }
            }
        )
    }

    if (showAttachmentAction != null) {
        val type = showAttachmentAction
        AlertDialog(
            onDismissRequest = { 
                showAttachmentAction = null
                mediaPlayer?.release()
                mediaPlayer = null
                isPlaying = false
            },
            title = { Text("Manage ${type?.replaceFirstChar { it.uppercase() }}") },
            text = { 
                Column {
                    Text("What would you like to do with this attachment?")
                    if (type == "voice" && isPlaying) {
                        Spacer(modifier = Modifier.height(8.dp))
                        Text("🔊 Playing...", color = Color(0xFF4CAF50), fontWeight = FontWeight.Bold)
                    }
                }
            },
            confirmButton = {
                TextButton(onClick = {
                    if (type == "voice") {
                        if (isPlaying) {
                            mediaPlayer?.stop()
                            mediaPlayer?.release()
                            mediaPlayer = null
                            isPlaying = false
                        } else {
                            try {
                                mediaPlayer = MediaPlayer().apply {
                                    setDataSource(audioFilePath)
                                    prepare()
                                    start()
                                    setOnCompletionListener {
                                        isPlaying = false
                                        mediaPlayer?.release()
                                        mediaPlayer = null
                                    }
                                }
                                isPlaying = true
                            } catch (e: Exception) {
                                android.widget.Toast.makeText(context, "Cannot play audio", android.widget.Toast.LENGTH_SHORT).show()
                            }
                        }
                    } else {
                        val uriToView = when (type) {
                            "photo" -> selectedPhoto
                            "video" -> selectedVideo
                            else -> null
                        }
                        if (uriToView != null) {
                            try {
                                val intent = android.content.Intent(android.content.Intent.ACTION_VIEW).apply {
                                    setDataAndType(uriToView, when(type) {
                                        "photo" -> "image/*"
                                        else -> "video/*"
                                    })
                                    addFlags(android.content.Intent.FLAG_GRANT_READ_URI_PERMISSION)
                                }
                                context.startActivity(intent)
                            } catch (e: Exception) {
                                android.widget.Toast.makeText(context, "No app found to open this", android.widget.Toast.LENGTH_SHORT).show()
                            }
                        }
                        showAttachmentAction = null
                    }
                }) { Text(if (type == "voice") (if (isPlaying) "Stop" else "Listen") else "View") }
            },
            dismissButton = {
                TextButton(onClick = {
                    when (type) {
                        "photo" -> selectedPhoto = null
                        "video" -> selectedVideo = null
                        "voice" -> {
                            mediaPlayer?.stop()
                            mediaPlayer?.release()
                            mediaPlayer = null
                            isPlaying = false
                            audioFilePath = ""
                        }
                    }
                    showAttachmentAction = null
                }) { Text("Remove", color = Color.Red) }
            }
        )
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFFF5F7FA))
    ) {
        Surface(
            modifier = Modifier.fillMaxWidth(),
            color = Color.White,
            shadowElevation = 4.dp
        ) {
            Row(
                modifier = Modifier.fillMaxWidth().padding(16.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                IconButton(onClick = onBackClick) {
                    Text("←", fontSize = 24.sp, fontWeight = FontWeight.Bold)
                }
                Spacer(modifier = Modifier.width(16.dp))
                Text("New Complaint", fontSize = 20.sp, fontWeight = FontWeight.Bold)
            }
        }

        if (isRecording) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(Color.Red.copy(alpha = 0.8f))
                    .padding(8.dp),
                contentAlignment = Alignment.Center
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text("● Recording Audio...", color = Color.White, fontWeight = FontWeight.Bold)
                    Spacer(modifier = Modifier.width(16.dp))
                    Text("Tap stop icon to save", color = Color.White.copy(alpha = 0.8f), fontSize = 12.sp)
                }
            }
        }

        Column(
            modifier = Modifier.fillMaxSize().padding(20.dp).verticalScroll(rememberScrollState())
        ) {
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(20.dp),
                colors = CardDefaults.cardColors(containerColor = Color.White),
                elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
            ) {
                Column(modifier = Modifier.padding(20.dp)) {
                    Text("Problem Details", fontWeight = FontWeight.Bold, color = Color(0xFF1565C0))
                    Spacer(modifier = Modifier.height(16.dp))
                    OutlinedTextField(
                        value = title,
                        onValueChange = { title = it },
                        label = { Text("Subject") },
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(12.dp),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedTextColor = Color.Black,
                            unfocusedTextColor = Color.Black
                        )
                    )
                    Spacer(modifier = Modifier.height(16.dp))
                    OutlinedTextField(
                        value = details,
                        onValueChange = { details = it },
                        label = { Text("Description") },
                        modifier = Modifier.fillMaxWidth().height(150.dp),
                        shape = RoundedCornerShape(12.dp),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedTextColor = Color.Black,
                            unfocusedTextColor = Color.Black
                        )
                    )
                    
                    // Update category only when text changes
                    LaunchedEffect(title, details) {
                        if (title.isNotEmpty() || details.isNotEmpty()) {
                            category = suggestCategory("$title $details")
                        }
                    }
                    Spacer(modifier = Modifier.height(12.dp))
                    Surface(color = Color(0xFFE3F2FD), shape = RoundedCornerShape(8.dp)) {
                        Text(
                            text = "Category: $category",
                            color = Color(0xFF1565C0),
                            fontWeight = FontWeight.Bold,
                            modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp),
                            fontSize = 12.sp
                        )
                    }
                    Spacer(modifier = Modifier.height(16.dp))
                    OutlinedTextField(
                        value = location,
                        onValueChange = { location = it },
                        label = { Text("Location (e.g. Block A)") },
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(12.dp),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedTextColor = Color.Black,
                            unfocusedTextColor = Color.Black
                        )
                    )
                }
            }

            Spacer(modifier = Modifier.height(24.dp))
            Text("Attachments", fontWeight = FontWeight.Bold, color = Color.DarkGray)
            Spacer(modifier = Modifier.height(12.dp))
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                AttachmentBoxM3("📸", "Photo", selectedPhoto != null, Modifier.weight(1f)) { 
                    if (selectedPhoto != null) showAttachmentAction = "photo" else showPhotoOptions = true 
                }
                AttachmentBoxM3("🎥", "Video", selectedVideo != null, Modifier.weight(1f)) { 
                    if (selectedVideo != null) showAttachmentAction = "video" else showVideoOptions = true 
                }
                AttachmentBoxM3(if (isRecording) "⏹" else "🎙", if (isRecording) "Stop" else "Voice", audioFilePath.isNotEmpty(), Modifier.weight(1f)) {
                    if (isRecording) {
                        try {
                            recorder?.stop()
                            recorder?.reset()
                            recorder?.release()
                        } catch (e: Exception) {
                            Log.e("NewComplaint", "Recorder stop error", e)
                        } finally {
                            recorder = null
                            isRecording = false
                            message = "Recording saved ✅"
                        }
                    } else if (audioFilePath.isNotEmpty()) {
                        showAttachmentAction = "voice"
                    } else {
                        audioPermissionLauncher.launch(Manifest.permission.RECORD_AUDIO)
                    }
                }
            }

            if (selectedPhoto != null || selectedVideo != null || audioFilePath.isNotEmpty()) {
                Spacer(modifier = Modifier.height(16.dp))
                Card(modifier = Modifier.fillMaxWidth(), shape = RoundedCornerShape(12.dp), colors = CardDefaults.cardColors(containerColor = Color(0xFFE8F5E9))) {
                    Column(modifier = Modifier.padding(12.dp)) {
                        if (selectedPhoto != null) Text("✅ Photo attached", fontSize = 12.sp, color = Color(0xFF2E7D32))
                        if (selectedVideo != null) Text("✅ Video attached", fontSize = 12.sp, color = Color(0xFF2E7D32))
                        if (audioFilePath.isNotEmpty()) Text("✅ Voice attached", fontSize = 12.sp, color = Color(0xFF2E7D32))
                    }
                }
            }

            Spacer(modifier = Modifier.height(32.dp))
            Button(
                onClick = {
                    val ticket = "CMP" + System.currentTimeMillis().toString().takeLast(6)
                    val complaint = ComplaintData(
                        title = title, details = details, location = location,
                        category = category, ticketId = ticket, status = "Pending",
                        photoUri = selectedPhoto?.toString() ?: "",
                        videoUri = selectedVideo?.toString() ?: "",
                        voicePath = audioFilePath,
                        collegeName = userCollege
                    )
                    onReviewClick(complaint)
                },
                modifier = Modifier.fillMaxWidth().height(56.dp),
                shape = RoundedCornerShape(16.dp),
                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF1565C0)),
                enabled = title.isNotBlank() && location.isNotBlank()
            ) {
                Text("Review & Submit", fontSize = 18.sp, fontWeight = FontWeight.Bold)
            }
            Spacer(modifier = Modifier.height(50.dp))
        }
    }
}

@Composable
fun AttachmentBoxM3(icon: String, text: String, isSelected: Boolean, modifier: Modifier, onClick: () -> Unit) {
    Card(
        modifier = modifier.height(90.dp).clickable { onClick() },
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = if (isSelected) Color(0xFFE8F5E9) else Color.White),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
    ) {
        Column(modifier = Modifier.fillMaxSize(), verticalArrangement = Arrangement.Center, horizontalAlignment = Alignment.CenterHorizontally) {
            Text(icon, fontSize = 28.sp)
            Text(text = text, fontSize = 11.sp, fontWeight = FontWeight.Bold, color = if (isSelected) Color(0xFF2E7D32) else Color.Gray)
        }
    }
}

fun suggestCategory(text: String): String {
    val input = text.lowercase()
    return when {
        "wifi" in input || "internet" in input || "network" in input -> "Network/WiFi"
        "fan" in input || "light" in input || "current" in input || "electrical" in input -> "Electrical"
        "water" in input || "tap" in input || "leakage" in input || "plumbing" in input -> "Plumbing"
        "dirty" in input || "dust" in input || "clean" in input -> "Cleanliness"
        "furniture" in input || "chair" in input || "desk" in input -> "Furniture"
        "hostel" in input || "room" in input -> "Hostel"
        "security" in input || "safe" in input || "gate" in input -> "Security"
        "transport" in input || "bus" in input -> "Transport"
        "lab" in input || "experiment" in input -> "Laboratory"
        else -> "General Maintenance"
    }
}
