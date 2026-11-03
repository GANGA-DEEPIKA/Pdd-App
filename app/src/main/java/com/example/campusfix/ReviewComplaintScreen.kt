package com.example.campusfix

import android.content.Context
import android.net.Uri
import androidx.compose.foundation.BorderStroke
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
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlinx.coroutines.launch
import java.io.File

fun viewAttachment(context: Context, uri: Uri, mimeType: String) {
    try {
        val intent = android.content.Intent(android.content.Intent.ACTION_VIEW).apply {
            setDataAndType(uri, mimeType)
            addFlags(android.content.Intent.FLAG_GRANT_READ_URI_PERMISSION)
        }
        context.startActivity(intent)
    } catch (e: Exception) {
        android.widget.Toast.makeText(context, "No app found to open this file", android.widget.Toast.LENGTH_SHORT).show()
    }
}

@Composable
fun ReviewComplaintScreen(
    complaint: ComplaintData,
    onBackClick: () -> Unit,
    onViewExistingClick: (ComplaintData) -> Unit = {},
    onSubmitClick: suspend () -> Unit
) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    var isSubmitting by remember { mutableStateOf(false) }
    var activeDuplicate by remember { mutableStateOf<DuplicateCheckResult?>(null) }
    var showDuplicateDialog by remember { mutableStateOf(false) }

    val hasPhoto = complaint.photoUri.isNotEmpty() || complaint.photoUrl.isNotEmpty()
    val hasVideo = complaint.videoUri.isNotEmpty() || complaint.videoUrl.isNotEmpty()
    val hasVoice = complaint.voicePath.isNotEmpty() || complaint.voiceUrl.isNotEmpty()

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFFF8F9FA))
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
                    text = "Review Complaint",
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
            // Top Blue Info Banner
            Surface(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(12.dp),
                color = Color(0xFFE3F2FD)
            ) {
                Row(
                    modifier = Modifier.padding(16.dp),
                    verticalAlignment = Alignment.Top
                ) {
                    Text("ℹ️", fontSize = 22.sp, modifier = Modifier.padding(top = 2.dp))
                    Spacer(modifier = Modifier.width(12.dp))
                    Column {
                        Text(
                            text = "Please review before submitting",
                            fontSize = 14.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color(0xFF1565C0)
                        )
                        Spacer(modifier = Modifier.height(2.dp))
                        Text(
                            text = "Once submitted, you'll receive a ticket ID for tracking.",
                            fontSize = 12.sp,
                            color = Color(0xFF1565C0).copy(alpha = 0.85f),
                            lineHeight = 16.sp
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
                        text = "Complaint Details",
                        fontSize = 15.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFF1565C0)
                    )

                    Spacer(modifier = Modifier.height(12.dp))

                    ReviewRowItem("Subject", complaint.title)
                    ReviewRowItem("Category", complaint.category)
                    if (complaint.department.isNotBlank()) {
                        ReviewRowItem("Department", complaint.department)
                    }
                    ReviewRowItem("Location", complaint.location)
                    ReviewRowItem("Priority", if (complaint.priority.isNotBlank()) complaint.priority else "Medium")
                    ReviewRowItem("College", complaint.collegeName)

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

            // Attachments Card
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

                    if (hasPhoto) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable {
                                    if (complaint.photoUri.isNotEmpty()) {
                                        viewAttachment(context, Uri.parse(complaint.photoUri), "image/*")
                                    }
                                }
                                .padding(vertical = 4.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = "✅ Photo attached",
                                fontSize = 13.sp,
                                fontWeight = FontWeight.Medium,
                                color = Color(0xFF2E7D32)
                            )
                        }
                    }

                    if (hasVideo) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable {
                                    if (complaint.videoUri.isNotEmpty()) {
                                        viewAttachment(context, Uri.parse(complaint.videoUri), "video/*")
                                    }
                                }
                                .padding(vertical = 4.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = "✅ Video attached",
                                fontSize = 13.sp,
                                fontWeight = FontWeight.Medium,
                                color = Color(0xFF2E7D32)
                            )
                        }
                    }

                    if (hasVoice) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable {
                                    if (complaint.voicePath.isNotEmpty()) {
                                        viewAttachment(context, Uri.fromFile(File(complaint.voicePath)), "audio/*")
                                    }
                                }
                                .padding(vertical = 4.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = "✅ Voice recording attached",
                                fontSize = 13.sp,
                                fontWeight = FontWeight.Medium,
                                color = Color(0xFF2E7D32)
                            )
                        }
                    }

                    if (!hasPhoto && !hasVideo && !hasVoice) {
                        Text(
                            text = "No attachments",
                            fontSize = 13.sp,
                            color = Color(0xFF9E9E9E)
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(24.dp))

            // Submit Button
            Button(
                onClick = {
                    isSubmitting = true
                    scope.launch {
                        try {
                            // Check for duplicates using AI backend service
                            val dupResult = FirestoreService.checkDuplicateComplaint(complaint)
                            if (dupResult.isDuplicate && dupResult.matchedComplaint != null) {
                                activeDuplicate = dupResult
                                showDuplicateDialog = true
                                isSubmitting = false
                                return@launch
                            }
                            onSubmitClick()
                        } catch (e: Exception) {
                            android.widget.Toast.makeText(context, "Error: ${e.message}", android.widget.Toast.LENGTH_SHORT).show()
                        } finally {
                            isSubmitting = false
                        }
                    }
                },
                enabled = !isSubmitting,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(52.dp),
                shape = RoundedCornerShape(12.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = Color(0xFF1565C0)
                )
            ) {
                if (isSubmitting) {
                    CircularProgressIndicator(color = Color.White, modifier = Modifier.size(24.dp))
                } else {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.Center
                    ) {
                        Text("✅ ", fontSize = 16.sp)
                        Text(
                            text = "Confirm & Submit",
                            fontSize = 16.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color.White
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            // Edit Button
            Button(
                onClick = onBackClick,
                enabled = !isSubmitting,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(52.dp),
                shape = RoundedCornerShape(12.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = Color(0xFFE3F2FD),
                    contentColor = Color(0xFF1565C0)
                )
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.Center
                ) {
                    Text("✏️ ", fontSize = 16.sp)
                    Text(
                        text = "Edit Complaint",
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFF1565C0)
                    )
                }
            }

            Spacer(modifier = Modifier.height(40.dp))
        }
    }

    // AI-Based Duplicate Complaint Alert Dialog
    if (showDuplicateDialog && activeDuplicate != null) {
        val dup = activeDuplicate!!.matchedComplaint
        AlertDialog(
            onDismissRequest = { showDuplicateDialog = false },
            title = {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Text("⚠️", fontSize = 24.sp)
                    Text(
                        text = "Potential Duplicate Detected",
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFFD84315)
                    )
                }
            },
            text = {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .verticalScroll(rememberScrollState()),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    // AI Match Badge
                    Surface(
                        shape = RoundedCornerShape(12.dp),
                        color = Color(0xFFFFF3E0),
                        border = BorderStroke(1.dp, Color(0xFFFFB74D))
                    ) {
                        Text(
                            text = "🤖 AI Match: ${activeDuplicate!!.confidence}% Confidence",
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color(0xFFE65100),
                            modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp)
                        )
                    }

                    Text(
                        text = if (activeDuplicate!!.matchReason.isNotEmpty()) activeDuplicate!!.matchReason
                               else "A similar complaint has already been submitted for this location and category:",
                        fontSize = 13.sp,
                        color = Color(0xFF616161),
                        lineHeight = 17.sp
                    )

                    // Existing Complaint Details Preview Card
                    if (dup != null) {
                        Surface(
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(12.dp),
                            color = Color(0xFFF8F9FA),
                            border = BorderStroke(1.dp, Color(0xFFE0E0E0))
                        ) {
                            Column(
                                modifier = Modifier.padding(12.dp),
                                verticalArrangement = Arrangement.spacedBy(4.dp)
                            ) {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Text(
                                        text = "Ticket: ${dup.ticketId}",
                                        fontSize = 12.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = Color(0xFF1565C0)
                                    )
                                    Surface(
                                        shape = RoundedCornerShape(8.dp),
                                        color = when (dup.status.lowercase()) {
                                            "resolved" -> Color(0xFFE8F5E9)
                                            "assigned", "in progress" -> Color(0xFFE3F2FD)
                                            else -> Color(0xFFFFF3E0)
                                        }
                                    ) {
                                        Text(
                                            text = dup.status.ifEmpty { "Pending" },
                                            fontSize = 11.sp,
                                            fontWeight = FontWeight.Bold,
                                            color = when (dup.status.lowercase()) {
                                                "resolved" -> Color(0xFF2E7D32)
                                                "assigned", "in progress" -> Color(0xFF1565C0)
                                                else -> Color(0xFFE65100)
                                            },
                                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 2.dp)
                                        )
                                    }
                                }

                                Text(
                                    text = dup.title.ifEmpty { "Complaint" },
                                    fontSize = 14.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = Color(0xFF212121)
                                )

                                Text(
                                    text = "📍 Location: ${dup.location}",
                                    fontSize = 12.sp,
                                    color = Color(0xFF555555)
                                )

                                Text(
                                    text = "🏷️ Category: ${dup.category}",
                                    fontSize = 12.sp,
                                    color = Color(0xFF555555)
                                )

                                if (dup.details.isNotBlank()) {
                                    Surface(
                                        modifier = Modifier.fillMaxWidth(),
                                        shape = RoundedCornerShape(6.dp),
                                        color = Color.White,
                                        border = BorderStroke(1.dp, Color(0xFFEEEEEE))
                                    ) {
                                        Text(
                                            text = dup.details,
                                            fontSize = 11.sp,
                                            color = Color(0xFF666666),
                                            lineHeight = 15.sp,
                                            maxLines = 3,
                                            modifier = Modifier.padding(8.dp)
                                        )
                                    }
                                }
                            }
                        }
                    }
                }
            },
            confirmButton = {
                Column(
                    modifier = Modifier.fillMaxWidth(),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    // Option 1: View Existing Complaint
                    Button(
                        onClick = {
                            showDuplicateDialog = false
                            if (dup != null) {
                                onViewExistingClick(dup)
                            }
                        },
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(10.dp),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = Color(0xFFE3F2FD),
                            contentColor = Color(0xFF1565C0)
                        ),
                        border = BorderStroke(1.dp, Color(0xFF1565C0))
                    ) {
                        Text("👁️ View Existing Complaint", fontWeight = FontWeight.Bold, fontSize = 14.sp)
                    }

                    // Option 2: Submit Anyway
                    Button(
                        onClick = {
                            showDuplicateDialog = false
                            isSubmitting = true
                            scope.launch {
                                try {
                                    onSubmitClick()
                                } catch (e: Exception) {
                                    android.widget.Toast.makeText(context, "Error: ${e.message}", android.widget.Toast.LENGTH_SHORT).show()
                                } finally {
                                    isSubmitting = false
                                }
                            }
                        },
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(10.dp),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = Color(0xFFE65100),
                            contentColor = Color.White
                        )
                    ) {
                        Text("⚠️ Submit Anyway", fontWeight = FontWeight.Bold, fontSize = 14.sp)
                    }

                    // Option 3: Edit My Complaint
                    TextButton(
                        onClick = { showDuplicateDialog = false },
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text("✏️ Edit My Complaint", color = Color(0xFF757575), fontSize = 13.sp)
                    }
                }
            },
            dismissButton = null,
            shape = RoundedCornerShape(16.dp),
            containerColor = Color.White
        )
    }
}

@Composable
fun ReviewRowItem(label: String, value: String) {
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
                text = if (value.isNotBlank()) value else "—",
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
