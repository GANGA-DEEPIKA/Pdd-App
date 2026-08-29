package com.example.campusfix

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
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
import kotlinx.coroutines.launch

@Composable
fun AssignedComplaintsScreen(
    title: String = "Assigned Tasks",
    complaintList: List<ComplaintData>,
    onComplaintClick: (ComplaintData) -> Unit,
    onBackClick: () -> Unit
) {
    var viewBitmap by remember { mutableStateOf<Bitmap?>(null) }

    if (viewBitmap != null) {
        AlertDialog(
            onDismissRequest = { viewBitmap = null },
            confirmButton = {
                TextButton(onClick = { viewBitmap = null }) { Text("Close", color = Color(0xFF1565C0)) }
            },
            title = { Text("Student Photo Evidence", fontSize = 16.sp, fontWeight = FontWeight.Bold) },
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
                Column {
                    Text(title, fontSize = 20.sp, fontWeight = FontWeight.Bold)
                    Text("${complaintList.size} tasks found", fontSize = 12.sp, color = Color.Gray)
                }
            }
        }

        if (complaintList.isEmpty()) {
            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text("🎉", fontSize = 60.sp)
                    Spacer(modifier = Modifier.height(16.dp))
                    Text("No tasks assigned yet", color = Color.Gray, fontWeight = FontWeight.Medium)
                }
            }
        } else {
            LazyColumn(
                contentPadding = PaddingValues(16.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                items(complaintList) { complaint ->
                    StaffComplaintCardM3(
                        complaint = complaint, 
                        onShowPhoto = { bitmap -> viewBitmap = bitmap }, 
                        onClick = { onComplaintClick(complaint) }
                    )
                }
            }
        }
    }
}

@Composable
fun StaffComplaintCardM3(complaint: ComplaintData, onShowPhoto: (Bitmap) -> Unit, onClick: () -> Unit) {
    val context = androidx.compose.ui.platform.LocalContext.current
    Card(
        modifier = Modifier.fillMaxWidth().clickable { onClick() },
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(modifier = Modifier.padding(20.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Surface(
                    color = Color(0xFFE3F2FD),
                    shape = RoundedCornerShape(8.dp)
                ) {
                    Text(
                        text = complaint.ticketId,
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFF1565C0)
                    )
                }
                
                val statusColor = when(complaint.status) {
                    "Resolved" -> Color(0xFF4CAF50)
                    "In Progress" -> Color(0xFFFF9800)
                    else -> Color(0xFF1565C0)
                }
                
                Text(
                    text = complaint.status,
                    color = statusColor,
                    fontWeight = FontWeight.Bold,
                    fontSize = 14.sp
                )
            }
            
            Spacer(modifier = Modifier.height(12.dp))
            
            Text(
                text = complaint.title,
                fontSize = 18.sp,
                fontWeight = FontWeight.Bold,
                color = Color.Black
            )
            
            Spacer(modifier = Modifier.height(8.dp))
            
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text("📍", fontSize = 14.sp)
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = if (complaint.location.isBlank()) "Location: Not specified" else complaint.location,
                    fontSize = 14.sp,
                    color = Color.Gray
                )
            }
            
            Spacer(modifier = Modifier.height(4.dp))
            
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text("🏷️", fontSize = 14.sp)
                Spacer(modifier = Modifier.width(8.dp))
                Text(text = complaint.category, fontSize = 14.sp, color = Color.Gray)
            }

            if (complaint.details.isNotEmpty()) {
                Spacer(modifier = Modifier.height(12.dp))
                Surface(
                    color = Color(0xFFF5F7FA),
                    shape = RoundedCornerShape(8.dp)
                ) {
                    Text(
                        text = complaint.details,
                        modifier = Modifier.padding(12.dp),
                        fontSize = 13.sp,
                        color = Color.DarkGray,
                        lineHeight = 18.sp
                    )
                }
            }
            
            val hasPhoto = complaint.photoUrl.isNotEmpty()
            val hasVoice = complaint.voiceUrl.isNotEmpty()
            var mediaPlayer by remember { mutableStateOf<MediaPlayer?>(null) }
            var isPlaying by remember { mutableStateOf(false) }

            if (hasPhoto || hasVoice) {
                HorizontalDivider(modifier = Modifier.padding(vertical = 12.dp), color = Color(0xFFEEEEEE))
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    if (hasPhoto) {
                        AttachmentBadge("📸 View Photo") {
                            if (complaint.photoUrl.startsWith("base64:")) {
                                try {
                                    val pureBase64 = complaint.photoUrl.removePrefix("base64:")
                                    val decodedString = Base64.decode(pureBase64, Base64.DEFAULT)
                                    val bitmap = BitmapFactory.decodeByteArray(decodedString, 0, decodedString.size)
                                    if (bitmap != null) {
                                        onShowPhoto(bitmap)
                                    } else {
                                        android.widget.Toast.makeText(context, "Photo data corrupted", android.widget.Toast.LENGTH_SHORT).show()
                                    }
                                } catch (e: Exception) {
                                    android.widget.Toast.makeText(context, "Error decoding photo", android.widget.Toast.LENGTH_SHORT).show()
                                }
                            } else {
                                // Fallback for old cloud URLs
                                try {
                                    val intent = android.content.Intent(android.content.Intent.ACTION_VIEW, android.net.Uri.parse(complaint.photoUrl))
                                    context.startActivity(intent)
                                } catch (e: Exception) {
                                    android.widget.Toast.makeText(context, "Cannot open link", android.widget.Toast.LENGTH_SHORT).show()
                                }
                            }
                        }
                    }
                    
                    if (hasVoice) {
                        AttachmentBadge(if (isPlaying) "⏹ Stop Voice" else "🎙 Hear Voice") {
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
                                        
                                        // Create temp file to play
                                        val tempFile = File.createTempFile("temp_voice", ".mp4", context.cacheDir)
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
                                        // Fallback for cloud URLs
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
}

@Composable
fun AttachmentBadge(text: String, onClick: () -> Unit = {}) {
    Surface(
        color = Color(0xFFF5F7FA),
        shape = RoundedCornerShape(6.dp),
        modifier = Modifier.clickable { onClick() }
    ) {
        Text(
            text = text,
            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
            fontSize = 10.sp,
            fontWeight = FontWeight.Medium,
            color = Color.DarkGray
        )
    }
}
