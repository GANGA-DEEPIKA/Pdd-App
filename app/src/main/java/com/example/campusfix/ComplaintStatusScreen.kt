package com.example.campusfix

import android.content.Intent
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.media.MediaPlayer
import android.net.Uri
import android.util.Base64
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import java.io.File
import java.io.FileOutputStream
import java.text.SimpleDateFormat
import java.util.Locale

@Composable
fun ComplaintStatusScreen(
    complaint: ComplaintData,
    onBackClick: () -> Unit
) {
    val context = LocalContext.current
    var viewBitmap by remember { mutableStateOf<Bitmap?>(null) }
    var mediaPlayer by remember { mutableStateOf<MediaPlayer?>(null) }
    var isPlaying by remember { mutableStateOf(false) }

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
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(350.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Image(
                        bitmap = viewBitmap!!.asImageBitmap(),
                        contentDescription = "Complaint Photo",
                        modifier = Modifier.fillMaxSize()
                    )
                }
            }
        )
    }

    val hasPhoto = complaint.photoUrl.isNotEmpty() || complaint.photoUri.isNotEmpty()
    val hasVideo = complaint.videoUrl.isNotEmpty() || complaint.videoUri.isNotEmpty()
    val hasVoice = complaint.voiceUrl.isNotEmpty() || complaint.voicePath.isNotEmpty()
    val hasProof = complaint.proofPhotoUrl.isNotEmpty() || complaint.proofVideoUrl.isNotEmpty()

    val isAssigned = complaint.status != "Pending" && !complaint.status.startsWith("Waiting") && complaint.status.isNotBlank()
    val isInProgress = complaint.status == "In Progress" || complaint.status == "Resolved" || complaint.status == "Escalated"
    val isResolved = complaint.status == "Resolved"

    val steps = listOf(
        TimelineStep("Submitted", true, "📝"),
        TimelineStep("Assigned to Staff", isAssigned, "👤"),
        TimelineStep("In Progress", isInProgress, "🔧"),
        TimelineStep("Resolved", isResolved, "✅")
    )

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFFF5F7FA))
    ) {
        // App Bar Header
        Surface(
            modifier = Modifier.fillMaxWidth(),
            color = Color.White,
            shadowElevation = 2.dp
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 12.dp, vertical = 12.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                IconButton(onClick = onBackClick) {
                    Text("←", fontSize = 24.sp, fontWeight = FontWeight.Bold, color = Color.Black)
                }
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = "Complaint Status",
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color.Black
                )
            }
        }

        // Main Content Area
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp)
                .verticalScroll(rememberScrollState())
        ) {
            // Ticket Header Banner
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(20.dp),
                colors = CardDefaults.cardColors(containerColor = Color(0xFF103480))
            ) {
                Column(
                    modifier = Modifier.padding(20.dp)
                ) {
                    Text(
                        text = "TICKET ID",
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color.White.copy(alpha = 0.75f),
                        letterSpacing = 1.sp
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = if (complaint.ticketId.isNotBlank()) complaint.ticketId else "N/A",
                        fontSize = 22.sp,
                        fontWeight = FontWeight.ExtraBold,
                        color = Color.White
                    )
                    Spacer(modifier = Modifier.height(10.dp))
                    Surface(
                        shape = RoundedCornerShape(50),
                        color = Color.White
                    ) {
                        Text(
                            text = if (complaint.status.isNotBlank()) complaint.status else "Pending",
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color(0xFF103480),
                            modifier = Modifier.padding(horizontal = 14.dp, vertical = 5.dp)
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Complaint Details Card
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = Color.White),
                border = BorderStroke(1.dp, Color(0xFFE5E7EB)),
                elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
            ) {
                Column(
                    modifier = Modifier.padding(20.dp)
                ) {
                    Text(
                        text = if (complaint.title.isNotBlank()) complaint.title else "Complaint",
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFF1565C0)
                    )

                    Spacer(modifier = Modifier.height(12.dp))

                    DetailRowItem("Category", complaint.category)
                    DetailRowItem("Location", complaint.location)
                    DetailRowItem("Priority", complaint.priority)
                    DetailRowItem("College", complaint.collegeName)
                    DetailRowItem("Submitted On", formatComplaintDate(complaint.createdAt))
                    if (complaint.assignedStaffEmail.isNotBlank()) {
                        DetailRowItem("Assigned Staff", complaint.assignedStaffEmail)
                    }

                    if (complaint.details.isNotBlank()) {
                        Spacer(modifier = Modifier.height(12.dp))
                        Text(
                            text = "Description",
                            fontSize = 12.sp,
                            color = Color(0xFF757575)
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Surface(
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(8.dp),
                            color = Color(0xFFF5F7FA)
                        ) {
                            Text(
                                text = complaint.details,
                                fontSize = 13.sp,
                                color = Color(0xFF333333),
                                lineHeight = 18.sp,
                                modifier = Modifier.padding(12.dp)
                            )
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Progress Timeline Card
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = Color.White),
                border = BorderStroke(1.dp, Color(0xFFE5E7EB)),
                elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
            ) {
                Column(
                    modifier = Modifier.padding(20.dp)
                ) {
                    Text(
                        text = "Progress Timeline",
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFF555555)
                    )

                    Spacer(modifier = Modifier.height(16.dp))

                    steps.forEachIndexed { index, step ->
                        val isLast = index == steps.size - 1
                        val nextStepDone = if (!isLast) steps[index + 1].isDone else false

                        Row(
                            verticalAlignment = Alignment.Top
                        ) {
                            Column(
                                horizontalAlignment = Alignment.CenterHorizontally,
                                modifier = Modifier.width(36.dp)
                            ) {
                                Surface(
                                    shape = CircleShape,
                                    color = if (step.isDone) Color(0xFF1565C0) else Color(0xFFE0E0E0),
                                    modifier = Modifier.size(36.dp)
                                ) {
                                    Box(contentAlignment = Alignment.Center) {
                                        Text(
                                            text = if (step.isDone) step.icon else "○",
                                            fontSize = 14.sp,
                                            color = if (step.isDone) Color.White else Color.Gray
                                        )
                                    }
                                }

                                if (!isLast) {
                                    Box(
                                        modifier = Modifier
                                            .width(2.dp)
                                            .height(28.dp)
                                            .background(if (step.isDone && nextStepDone) Color(0xFF1565C0) else Color(0xFFE0E0E0))
                                    )
                                }
                            }

                            Spacer(modifier = Modifier.width(12.dp))

                            Text(
                                text = step.label,
                                fontSize = 14.sp,
                                fontWeight = if (step.isDone) FontWeight.Bold else FontWeight.Medium,
                                color = if (step.isDone) Color(0xFF1565C0) else Color(0xFFBDBDBD),
                                modifier = Modifier.padding(top = 8.dp)
                            )
                        }
                    }
                }
            }

            // Attachments Card (if any attachments exist)
            if (hasPhoto || hasVideo || hasVoice || hasProof) {
                Spacer(modifier = Modifier.height(16.dp))
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(containerColor = Color.White),
                    border = BorderStroke(1.dp, Color(0xFFE5E7EB)),
                    elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
                ) {
                    Column(
                        modifier = Modifier.padding(20.dp)
                    ) {
                        Text(
                            text = "Attachments",
                            fontSize = 14.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color(0xFF555555)
                        )

                        Spacer(modifier = Modifier.height(12.dp))

                        if (hasVoice) {
                            Surface(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clickable {
                                        if (isPlaying) {
                                            mediaPlayer?.stop()
                                            mediaPlayer?.release()
                                            mediaPlayer = null
                                            isPlaying = false
                                        } else {
                                            try {
                                                val rawVoice = if (complaint.voiceUrl.isNotBlank()) complaint.voiceUrl else complaint.voicePath
                                                if (rawVoice.startsWith("base64:")) {
                                                    val pureBase64 = rawVoice.removePrefix("base64:")
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
                                                } else if (rawVoice.isNotEmpty()) {
                                                    mediaPlayer = MediaPlayer().apply {
                                                        setDataSource(rawVoice)
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
                                    },
                                shape = RoundedCornerShape(10.dp),
                                color = Color(0xFFE3F2FD)
                            ) {
                                Row(
                                    modifier = Modifier.padding(12.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Text(
                                        text = if (isPlaying) "⏹ Voice recording attached (Playing...)" else "🎙 Voice recording attached",
                                        fontSize = 13.sp,
                                        color = Color(0xFF1565C0),
                                        fontWeight = FontWeight.Medium
                                    )
                                }
                            }
                            Spacer(modifier = Modifier.height(8.dp))
                        }

                        if (hasPhoto) {
                            Surface(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clickable {
                                        val rawPhoto = if (complaint.photoUrl.isNotBlank()) complaint.photoUrl else complaint.photoUri
                                        if (rawPhoto.startsWith("base64:")) {
                                            try {
                                                val pureBase64 = rawPhoto.removePrefix("base64:")
                                                val decodedString = Base64.decode(pureBase64, Base64.DEFAULT)
                                                val bitmap = BitmapFactory.decodeByteArray(decodedString, 0, decodedString.size)
                                                if (bitmap != null) viewBitmap = bitmap
                                            } catch (e: Exception) {
                                                android.widget.Toast.makeText(context, "Error opening photo", android.widget.Toast.LENGTH_SHORT).show()
                                            }
                                        } else if (rawPhoto.isNotEmpty()) {
                                            try {
                                                val intent = Intent(Intent.ACTION_VIEW, Uri.parse(rawPhoto))
                                                context.startActivity(intent)
                                            } catch (e: Exception) {
                                                android.widget.Toast.makeText(context, "Cannot open photo", android.widget.Toast.LENGTH_SHORT).show()
                                            }
                                        }
                                    },
                                shape = RoundedCornerShape(10.dp),
                                color = Color(0xFFE3F2FD)
                            ) {
                                Row(
                                    modifier = Modifier.padding(12.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Text(
                                        text = "📸 Photo attached (Tap to view)",
                                        fontSize = 13.sp,
                                        color = Color(0xFF1565C0),
                                        fontWeight = FontWeight.Medium
                                    )
                                }
                            }
                            Spacer(modifier = Modifier.height(8.dp))
                        }

                        if (hasProof) {
                            Surface(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clickable {
                                        if (complaint.proofPhotoUrl.startsWith("base64:")) {
                                            try {
                                                val pureBase64 = complaint.proofPhotoUrl.removePrefix("base64:")
                                                val decodedString = Base64.decode(pureBase64, Base64.DEFAULT)
                                                val bitmap = BitmapFactory.decodeByteArray(decodedString, 0, decodedString.size)
                                                if (bitmap != null) viewBitmap = bitmap
                                            } catch (e: Exception) {
                                                android.widget.Toast.makeText(context, "Error opening proof", android.widget.Toast.LENGTH_SHORT).show()
                                            }
                                        } else if (complaint.proofPhotoUrl.isNotEmpty()) {
                                            try {
                                                val intent = Intent(Intent.ACTION_VIEW, Uri.parse(complaint.proofPhotoUrl))
                                                context.startActivity(intent)
                                            } catch (e: Exception) {
                                                android.widget.Toast.makeText(context, "Cannot open proof", android.widget.Toast.LENGTH_SHORT).show()
                                            }
                                        }
                                    },
                                shape = RoundedCornerShape(10.dp),
                                color = Color(0xFFE8F5E9)
                            ) {
                                Row(
                                    modifier = Modifier.padding(12.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Text(
                                        text = "✅ Resolution Proof attached (Tap to view)",
                                        fontSize = 13.sp,
                                        color = Color(0xFF2E7D32),
                                        fontWeight = FontWeight.Medium
                                    )
                                }
                            }
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(30.dp))
        }
    }
}

data class TimelineStep(
    val label: String,
    val isDone: Boolean,
    val icon: String
)

@Composable
fun DetailRowItem(label: String, value: String) {
    if (value.isBlank()) return
    Column {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 10.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = label,
                fontSize = 13.sp,
                color = Color(0xFF757575)
            )
            Text(
                text = value,
                fontSize = 13.sp,
                fontWeight = FontWeight.SemiBold,
                color = Color(0xFF212121),
                textAlign = TextAlign.End,
                modifier = Modifier.widthIn(max = 220.dp)
            )
        }
        HorizontalDivider(color = Color(0xFFF5F5F5), thickness = 1.dp)
    }
}

fun formatComplaintDate(date: java.util.Date?): String {
    val targetDate = date ?: java.util.Date()
    return try {
        val formatter = SimpleDateFormat("dd MMM yyyy", Locale.getDefault())
        formatter.format(targetDate)
    } catch (e: Exception) {
        val formatter = SimpleDateFormat("dd MMM yyyy", Locale.getDefault())
        formatter.format(java.util.Date())
    }
}
