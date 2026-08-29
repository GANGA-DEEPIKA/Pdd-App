package com.example.campusfix

import android.content.Intent
import android.net.Uri
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import android.util.Base64
import android.graphics.BitmapFactory
import android.graphics.Bitmap
import android.media.MediaPlayer
import java.io.File
import java.io.FileOutputStream

@Composable
fun ComplaintStatusScreen(
    complaint: ComplaintData,
    onBackClick: () -> Unit
) {
    var viewBitmap by remember { mutableStateOf<Bitmap?>(null) }
    val context = androidx.compose.ui.platform.LocalContext.current
    var mediaPlayer by remember { mutableStateOf<MediaPlayer?>(null) }
    var isPlaying by remember { mutableStateOf(false) }

    // Cleanup media player on dispose
    DisposableEffect(Unit) {
        onDispose {
            mediaPlayer?.release()
            mediaPlayer = null
        }
    }

    if (viewBitmap != null) {
        AlertDialog(
            onDismissRequest = { viewBitmap = null },
            confirmButton = {
                TextButton(onClick = { viewBitmap = null }) { Text("Close", color = Color(0xFF1565C0)) }
            },
            text = {
                Box(modifier = Modifier.fillMaxWidth().height(350.dp), contentAlignment = Alignment.Center) {
                    Image(
                        bitmap = viewBitmap!!.asImageBitmap(),
                        contentDescription = "Complaint Photo",
                        modifier = Modifier.fillMaxSize()
                    )
                }
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
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                IconButton(onClick = onBackClick) {
                    Text("←", fontSize = 24.sp, fontWeight = FontWeight.Bold)
                }
                Spacer(modifier = Modifier.width(16.dp))
                Text("Tracking Status", fontSize = 20.sp, fontWeight = FontWeight.Bold)
            }
        }

        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(20.dp)
        ) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 16.dp),
                contentAlignment = Alignment.Center
            ) {
                Surface(
                    shape = RoundedCornerShape(50),
                    color = Color(0xFFE3F2FD),
                    modifier = Modifier.padding(16.dp)
                ) {
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        modifier = Modifier.padding(24.dp)
                    ) {
                        Text(text = "🎫", fontSize = 48.sp)
                        Text(
                            text = complaint.ticketId,
                            fontSize = 20.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color(0xFF1565C0)
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            StatusItemM3("Subject", complaint.title, "📋")
            StatusItemM3("Category", complaint.category, "🏷️")
            
            val statusColor = when(complaint.status) {
                "Resolved" -> Color(0xFF4CAF50)
                "In Progress" -> Color(0xFFFF9800)
                else -> Color(0xFF1565C0)
            }
            
            StatusItemM3("Current Status", complaint.status, "⏳", statusColor)
            
            // Show attachment button for students to view their own attachments
            if (complaint.photoUrl.isNotEmpty() || complaint.videoUrl.isNotEmpty() || complaint.voiceUrl.isNotEmpty()) {
                Card(
                    modifier = Modifier.fillMaxWidth().padding(bottom = 12.dp),
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(containerColor = Color.White),
                    elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Text("Your Attachments", fontWeight = FontWeight.Bold, color = Color.Gray, fontSize = 12.sp)
                        Spacer(modifier = Modifier.height(12.dp))
                        
                        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            if (complaint.photoUrl.isNotEmpty()) {
                                AttachmentBadgeM3("📸 Photo", Modifier.weight(1f)) {
                                    if (complaint.photoUrl.startsWith("base64:")) {
                                        try {
                                            val pureBase64 = complaint.photoUrl.removePrefix("base64:")
                                            val decodedString = Base64.decode(pureBase64, Base64.DEFAULT)
                                            val bitmap = BitmapFactory.decodeByteArray(decodedString, 0, decodedString.size)
                                            if (bitmap != null) viewBitmap = bitmap
                                        } catch (e: Exception) {
                                            android.widget.Toast.makeText(context, "Error opening photo", android.widget.Toast.LENGTH_SHORT).show()
                                        }
                                    } else {
                                        try {
                                            val intent = Intent(Intent.ACTION_VIEW, Uri.parse(complaint.photoUrl))
                                            context.startActivity(intent)
                                        } catch (e: Exception) {
                                            android.widget.Toast.makeText(context, "Cannot open link", android.widget.Toast.LENGTH_SHORT).show()
                                        }
                                    }
                                }
                            }
                            
                            if (complaint.videoUrl.isNotEmpty()) {
                                AttachmentBadgeM3("🎥 Video", Modifier.weight(1f)) {
                                    try {
                                        val intent = Intent(Intent.ACTION_VIEW, Uri.parse(complaint.videoUrl))
                                        context.startActivity(intent)
                                    } catch (e: Exception) {
                                        android.widget.Toast.makeText(context, "Cannot open video", android.widget.Toast.LENGTH_SHORT).show()
                                    }
                                }
                            }
                            
                            if (complaint.voiceUrl.isNotEmpty()) {
                                AttachmentBadgeM3(if (isPlaying) "⏹ Stop" else "🎙 Voice", Modifier.weight(1f)) {
                                    if (isPlaying) {
                                        mediaPlayer?.stop()
                                        mediaPlayer?.release()
                                        mediaPlayer = null
                                        isPlaying = false
                                    } else {
                                        try {
                                            if (complaint.voiceUrl.startsWith("base64:")) {
                                                val pureBase64 = complaint.voiceUrl.removePrefix("base64:")
                                                val decodedBytes = Base64.decode(pureBase64, Base64.DEFAULT)
                                                val tempFile = File.createTempFile("temp_voice_student", ".mp4", context.cacheDir)
                                                FileOutputStream(tempFile).use { it.write(decodedBytes) }
                                                
                                                mediaPlayer = MediaPlayer().apply {
                                                    setDataSource(tempFile.absolutePath)
                                                    prepare()
                                                    start()
                                                    setOnCompletionListener {
                                                        isPlaying = false
                                                        mediaPlayer?.release()
                                                        mediaPlayer = null
                                                        tempFile.delete()
                                                    }
                                                }
                                                isPlaying = true
                                            } else {
                                                mediaPlayer = MediaPlayer().apply {
                                                    setDataSource(complaint.voiceUrl)
                                                    prepare()
                                                    start()
                                                    setOnCompletionListener {
                                                        isPlaying = false
                                                        mediaPlayer?.release()
                                                        mediaPlayer = null
                                                    }
                                                }
                                                isPlaying = true
                                            }
                                        } catch (e: Exception) {
                                            android.widget.Toast.makeText(context, "Cannot play voice note", android.widget.Toast.LENGTH_SHORT).show()
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }

            if (complaint.status == "Resolved" && (complaint.proofPhotoUrl.isNotEmpty() || complaint.proofVideoUrl.isNotEmpty())) {
                Spacer(modifier = Modifier.height(24.dp))
                Text("Resolution Proof from Staff", fontWeight = FontWeight.Bold, color = Color.DarkGray)
                Spacer(modifier = Modifier.height(12.dp))
                
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                    if (complaint.proofPhotoUrl.isNotEmpty()) {
                        Button(
                            onClick = {
                                if (complaint.proofPhotoUrl.startsWith("base64:")) {
                                    try {
                                        val pureBase64 = complaint.proofPhotoUrl.removePrefix("base64:")
                                        val decodedString = android.util.Base64.decode(pureBase64, android.util.Base64.DEFAULT)
                                        val bitmap = android.graphics.BitmapFactory.decodeByteArray(decodedString, 0, decodedString.size)
                                        if (bitmap != null) viewBitmap = bitmap
                                    } catch (e: Exception) {
                                        android.widget.Toast.makeText(context, "Error opening proof", android.widget.Toast.LENGTH_SHORT).show()
                                    }
                                } else {
                                    try {
                                        val intent = Intent(Intent.ACTION_VIEW, Uri.parse(complaint.proofPhotoUrl))
                                        context.startActivity(intent)
                                    } catch (e: Exception) {
                                        android.widget.Toast.makeText(context, "Cannot open link", android.widget.Toast.LENGTH_SHORT).show()
                                    }
                                }
                            },
                            modifier = Modifier.weight(1f),
                            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF4CAF50)),
                            shape = RoundedCornerShape(12.dp)
                        ) {
                            Text("📸 Proof Photo", fontSize = 10.sp)
                        }
                    }
                    
                    if (complaint.proofVideoUrl.isNotEmpty()) {
                        Button(
                            onClick = {
                                try {
                                    val intent = Intent(Intent.ACTION_VIEW, Uri.parse(complaint.proofVideoUrl))
                                    context.startActivity(intent)
                                } catch (e: Exception) {
                                    android.widget.Toast.makeText(context, "Cannot open video", android.widget.Toast.LENGTH_SHORT).show()
                                }
                            },
                            modifier = Modifier.weight(1f),
                            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF4CAF50)),
                            shape = RoundedCornerShape(12.dp)
                        ) {
                            Text("🎥 Proof Video", fontSize = 10.sp)
                        }
                    }
                }
            }
            
            Spacer(modifier = Modifier.height(32.dp))

            Button(
                onClick = onBackClick,
                modifier = Modifier.fillMaxWidth().height(56.dp),
                shape = RoundedCornerShape(16.dp),
                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF1565C0))
            ) {
                Text("Back To Home", fontSize = 18.sp, fontWeight = FontWeight.Bold)
            }
        }
    }
}

@Composable
fun AttachmentBadgeM3(text: String, modifier: Modifier = Modifier, onClick: () -> Unit = {}) {
    Surface(
        color = Color(0xFFF5F7FA),
        shape = RoundedCornerShape(8.dp),
        modifier = modifier.clickable { onClick() }
    ) {
        Text(
            text = text,
            modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp),
            fontSize = 12.sp,
            fontWeight = FontWeight.Medium,
            color = Color.DarkGray
        )
    }
}

@Composable
fun StatusItemM3(label: String, value: String, icon: String, valueColor: Color = Color.Black) {
    Card(
        modifier = Modifier.fillMaxWidth().padding(bottom = 12.dp),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
    ) {
        Row(
            modifier = Modifier.padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(icon, fontSize = 24.sp)
            Spacer(modifier = Modifier.width(16.dp))
            Column {
                Text(label, fontWeight = FontWeight.Bold, color = Color.Gray, fontSize = 12.sp)
                Text(text = value, fontSize = 16.sp, fontWeight = FontWeight.SemiBold, color = valueColor)
            }
        }
    }
}
