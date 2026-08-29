package com.example.campusfix

import android.content.Intent
import android.net.Uri
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
import kotlinx.coroutines.launch

@Composable
fun UpdateStatusScreen(
    complaintList: List<ComplaintData>,
    initialTicketId: String = "",
    onBackClick: () -> Unit
) {
    var selectedTicketId by remember { mutableStateOf(initialTicketId) }
    var status by remember { 
        mutableStateOf(complaintList.find { it.ticketId == initialTicketId }?.status ?: "Pending") 
    }
    val scope = rememberCoroutineScope()
    var message by remember { mutableStateOf("") }
    val context = androidx.compose.ui.platform.LocalContext.current

    val selectedComplaint = complaintList.find { it.ticketId == selectedTicketId }
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
                Text("Update Ticket Status", fontSize = 20.sp, fontWeight = FontWeight.Bold)
            }
        }

        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(20.dp)
        ) {
            item {
                Text(
                    "Select a Ticket",
                    fontWeight = FontWeight.Bold,
                    color = Color.DarkGray,
                    modifier = Modifier.padding(bottom = 12.dp)
                )
            }

            if (complaintList.isEmpty()) {
                item {
                    Text("No tickets found", color = Color.Gray, modifier = Modifier.padding(vertical = 20.dp))
                }
            } else {
                items(complaintList) { complaint ->
                    SelectableTicketCard(
                        complaint = complaint,
                        isSelected = selectedTicketId == complaint.ticketId,
                        onSelect = { 
                            selectedTicketId = complaint.ticketId
                            status = complaint.status 
                        }
                    )
                }
            }

            if (selectedTicketId.isNotEmpty()) {
                item {
                    Spacer(modifier = Modifier.height(24.dp))
                    Text("Complaint Details", fontWeight = FontWeight.Bold, color = Color.DarkGray)
                    Card(
                        modifier = Modifier.fillMaxWidth().padding(top = 8.dp),
                        shape = RoundedCornerShape(12.dp),
                        colors = CardDefaults.cardColors(containerColor = Color.White),
                        border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFFEEEEEE))
                    ) {
                        Text(
                            text = selectedComplaint?.details ?: "No details provided.",
                            modifier = Modifier.padding(16.dp),
                            fontSize = 14.sp,
                            color = Color.Black
                        )
                    }

                    Spacer(modifier = Modifier.height(24.dp))
                    Text("Choose New Status", fontWeight = FontWeight.Bold, color = Color.DarkGray)
                    Spacer(modifier = Modifier.height(12.dp))
                    
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        StatusChip("Assigned", status == "Assigned", Modifier.weight(1f)) { status = "Assigned" }
                        StatusChip("In Progress", status == "In Progress", Modifier.weight(1f)) { status = "In Progress" }
                        StatusChip("Resolved", status == "Resolved", Modifier.weight(1f)) { status = "Resolved" }
                    }

                    if (selectedComplaint != null && (selectedComplaint.photoUrl.isNotEmpty() || selectedComplaint.videoUrl.isNotEmpty() || selectedComplaint.voiceUrl.isNotEmpty())) {
                        Spacer(modifier = Modifier.height(24.dp))
                        Text("Attachments from Student", fontWeight = FontWeight.Bold, color = Color.DarkGray)
                        Spacer(modifier = Modifier.height(12.dp))
                        
                        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                            if (selectedComplaint.photoUrl.isNotEmpty()) {
                                Button(
                                    onClick = {
                                        if (selectedComplaint.photoUrl.startsWith("base64:")) {
                                            try {
                                                val pureBase64 = selectedComplaint.photoUrl.removePrefix("base64:")
                                                val decodedString = Base64.decode(pureBase64, Base64.DEFAULT)
                                                val bitmap = BitmapFactory.decodeByteArray(decodedString, 0, decodedString.size)
                                                if (bitmap != null) viewBitmap = bitmap
                                            } catch (e: Exception) {
                                                android.widget.Toast.makeText(context, "Error decoding photo", android.widget.Toast.LENGTH_SHORT).show()
                                            }
                                        } else {
                                            try {
                                                val intent = Intent(Intent.ACTION_VIEW, Uri.parse(selectedComplaint.photoUrl))
                                                context.startActivity(intent)
                                            } catch (e: Exception) {
                                                android.widget.Toast.makeText(context, "Cannot open link", android.widget.Toast.LENGTH_SHORT).show()
                                            }
                                        }
                                    },
                                    modifier = Modifier.weight(1f),
                                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF607D8B)),
                                    shape = RoundedCornerShape(12.dp)
                                ) {
                                    Text("📸 Photo", fontSize = 10.sp)
                                }
                            }
                            
                            if (selectedComplaint.videoUrl.isNotEmpty()) {
                                Button(
                                    onClick = {
                                        try {
                                            val intent = Intent(Intent.ACTION_VIEW, Uri.parse(selectedComplaint.videoUrl))
                                            context.startActivity(intent)
                                        } catch (e: Exception) {
                                            android.widget.Toast.makeText(context, "Cannot open video", android.widget.Toast.LENGTH_SHORT).show()
                                        }
                                    },
                                    modifier = Modifier.weight(1f),
                                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF607D8B)),
                                    shape = RoundedCornerShape(12.dp)
                                ) {
                                    Text("🎥 Video", fontSize = 10.sp)
                                }
                            }

                            if (selectedComplaint.voiceUrl.isNotEmpty()) {
                                Button(
                                    onClick = {
                                        if (isPlaying) {
                                            mediaPlayer?.stop()
                                            mediaPlayer?.release()
                                            mediaPlayer = null
                                            isPlaying = false
                                        } else {
                                            try {
                                                if (selectedComplaint.voiceUrl.startsWith("base64:")) {
                                                    val pureBase64 = selectedComplaint.voiceUrl.removePrefix("base64:")
                                                    val decodedBytes = Base64.decode(pureBase64, Base64.DEFAULT)
                                                    val tempFile = java.io.File.createTempFile("temp_voice_staff", ".mp4", context.cacheDir)
                                                    java.io.FileOutputStream(tempFile).use { it.write(decodedBytes) }
                                                    
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
                                                        setDataSource(selectedComplaint.voiceUrl)
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
                                                android.widget.Toast.makeText(context, "Cannot play voice", android.widget.Toast.LENGTH_SHORT).show()
                                            }
                                        }
                                    },
                                    modifier = Modifier.weight(1f),
                                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF607D8B)),
                                    shape = RoundedCornerShape(12.dp)
                                ) {
                                    Text(if (isPlaying) "⏹ Stop" else "🎙 Voice", fontSize = 10.sp)
                                }
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(32.dp))

                    Button(
                        onClick = {
                            scope.launch {
                        val college = complaintList.find { it.ticketId == selectedTicketId }?.collegeName ?: ""
                        val result = FirestoreService.updateComplaintStatus(selectedTicketId, college, status)
                        message = if (result.isSuccess) "Status updated successfully! ✅" else "Failed to update ❌"
                    }
                        },
                        modifier = Modifier.fillMaxWidth().height(56.dp),
                        shape = RoundedCornerShape(16.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF1565C0))
                    ) {
                        Text("Confirm Update", fontSize = 18.sp, fontWeight = FontWeight.Bold)
                    }

                    if (message.isNotEmpty()) {
                        Text(
                            text = message,
                            modifier = Modifier.padding(top = 16.dp).fillMaxWidth(),
                            textAlign = androidx.compose.ui.text.style.TextAlign.Center,
                            color = if (message.contains("success")) Color(0xFF2E7D32) else Color.Red,
                            fontWeight = FontWeight.Medium
                        )
                    }
                }
            }
            
            item { Spacer(modifier = Modifier.height(40.dp)) }
        }
    }
}

@Composable
fun SelectableTicketCard(complaint: ComplaintData, isSelected: Boolean, onSelect: () -> Unit) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(bottom = 10.dp)
            .clickable { onSelect() },
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(
            containerColor = if (isSelected) Color(0xFFE3F2FD) else Color.White
        ),
        border = if (isSelected) androidx.compose.foundation.BorderStroke(2.dp, Color(0xFF1565C0)) else null,
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
    ) {
        Row(
            modifier = Modifier.padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            RadioButton(selected = isSelected, onClick = onSelect)
            Spacer(modifier = Modifier.width(12.dp))
            Column {
                Text(text = complaint.ticketId, fontWeight = FontWeight.Bold, color = if (isSelected) Color(0xFF1565C0) else Color.Black)
                Text(text = complaint.title, fontSize = 14.sp, color = Color.Gray, maxLines = 1)
            }
        }
    }
}

@Composable
fun StatusChip(label: String, isSelected: Boolean, modifier: Modifier, onClick: () -> Unit) {
    Surface(
        modifier = modifier
            .height(48.dp)
            .clickable { onClick() },
        shape = RoundedCornerShape(12.dp),
        color = if (isSelected) Color(0xFF1565C0) else Color.White,
        border = if (!isSelected) androidx.compose.foundation.BorderStroke(1.dp, Color.LightGray) else null,
        shadowElevation = if (isSelected) 4.dp else 0.dp
    ) {
        Box(contentAlignment = Alignment.Center) {
            Text(
                text = label,
                color = if (isSelected) Color.White else Color.Gray,
                fontWeight = FontWeight.Bold,
                fontSize = 13.sp
            )
        }
    }
}
