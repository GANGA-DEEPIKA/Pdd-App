package com.example.campusfix

import android.Manifest
import android.net.Uri
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
import kotlinx.coroutines.launch
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.graphics.PathEffect
import androidx.compose.ui.graphics.drawscope.Stroke

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun UploadProofScreen(
    complaintList: List<ComplaintData>,
    initialTicketId: String = "",
    onBackClick: () -> Unit
) {
    var selectedTicketId by remember { mutableStateOf(initialTicketId) }
    var expanded by remember { mutableStateOf(false) }
    var message by remember { mutableStateOf("") }
    var selectedPhoto by remember { mutableStateOf<Uri?>(null) }
    var cameraPhotoUri by remember { mutableStateOf<Uri?>(null) }
    
    var isUploading by remember { mutableStateOf(false) }
    val scope = rememberCoroutineScope()
    val context = LocalContext.current

    val selectedComplaint = complaintList.find { it.ticketId.trim() == selectedTicketId.trim() }

    val photoGalleryLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.GetContent()
    ) { uri -> if (uri != null) selectedPhoto = uri }

    val photoCameraLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.TakePicture()
    ) { success -> if (success) selectedPhoto = cameraPhotoUri }

    val cameraPermissionLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { granted ->
        if (granted) {
            val uri = createImageUri(context)
            cameraPhotoUri = uri
            photoCameraLauncher.launch(uri)
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFFF5F7FA))
    ) {
        // Header
        Surface(
            modifier = Modifier.fillMaxWidth(),
            color = Color.White,
            shadowElevation = 2.dp
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
                Text("Upload Proof", fontSize = 18.sp, fontWeight = FontWeight.Bold)
            }
        }

        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp)
                .verticalScroll(rememberScrollState())
        ) {
            // Select Complaint Section
            Text(
                "Select or Enter Complaint ID",
                fontSize = 12.sp,
                color = Color.Gray,
                modifier = Modifier.padding(bottom = 8.dp)
            )

            ExposedDropdownMenuBox(
                expanded = expanded,
                onExpandedChange = { expanded = !expanded },
                modifier = Modifier.fillMaxWidth()
            ) {
                OutlinedTextField(
                    value = selectedTicketId,
                    onValueChange = { 
                        selectedTicketId = it
                        expanded = true 
                    },
                    label = { Text("Complaint Ticket ID") },
                    placeholder = { Text("CMPXXXXXX") },
                    trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = expanded) },
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedTextColor = Color.Black,
                        unfocusedTextColor = Color.Black,
                        focusedBorderColor = Color(0xFF1565C0),
                        unfocusedBorderColor = Color.LightGray,
                        focusedContainerColor = Color.White,
                        unfocusedContainerColor = Color.White
                    ),
                    modifier = Modifier.menuAnchor(ExposedDropdownMenuAnchorType.PrimaryEditable).fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp)
                )

                val filteredList = complaintList.filter { 
                    it.ticketId.contains(selectedTicketId, ignoreCase = true) || 
                    it.title.contains(selectedTicketId, ignoreCase = true) 
                }

                if (filteredList.isNotEmpty()) {
                    ExposedDropdownMenu(
                        expanded = expanded,
                        onDismissRequest = { expanded = false },
                        modifier = Modifier.background(Color.White)
                    ) {
                        filteredList.forEach { complaint ->
                            DropdownMenuItem(
                                text = { Text("${complaint.ticketId} — ${complaint.title}") },
                                onClick = {
                                    selectedTicketId = complaint.ticketId
                                    expanded = false
                                }
                            )
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(24.dp))

            // Complaint Details Card
            if (selectedComplaint != null) {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp),
                    colors = CardDefaults.cardColors(containerColor = Color.White),
                    elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Text(
                            text = selectedComplaint.title,
                            fontSize = 18.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color.Black
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = "${selectedComplaint.category} • ${selectedComplaint.location}",
                            fontSize = 13.sp,
                            color = Color.Gray
                        )
                        Spacer(modifier = Modifier.height(12.dp))
                        Surface(
                            color = if (selectedComplaint.status == "Resolved") Color(0xFFE8F5E9) else Color(0xFFE3F2FD),
                            shape = RoundedCornerShape(8.dp)
                        ) {
                            Text(
                                text = selectedComplaint.status,
                                modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp),
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold,
                                color = if (selectedComplaint.status == "Resolved") Color(0xFF2E7D32) else Color(0xFF1565C0)
                            )
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(32.dp))

            // Proof Photo Section
            Text(
                "Proof Photo",
                fontSize = 12.sp,
                color = Color.Gray,
                modifier = Modifier.padding(bottom = 8.dp)
            )

            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(150.dp)
                    .drawBehind {
                        drawRoundRect(
                            color = Color.LightGray,
                            style = Stroke(
                                width = 2f,
                                pathEffect = PathEffect.dashPathEffect(floatArrayOf(10f, 10f), 0f)
                            ),
                            cornerRadius = CornerRadius(12.dp.toPx())
                        )
                    }
                    .clickable { 
                        showPhotoSourceDialog(context, { photoGalleryLauncher.launch("image/*") }, { cameraPermissionLauncher.launch(Manifest.permission.CAMERA) })
                    },
                contentAlignment = Alignment.Center
            ) {
                if (selectedPhoto != null) {
                    Text("✅ Photo Attached", color = Color(0xFF2E7D32), fontWeight = FontWeight.Bold)
                } else {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text("📷", fontSize = 24.sp)
                        Spacer(modifier = Modifier.height(8.dp))
                        Text("Tap to attach proof photo", fontSize = 12.sp, color = Color.Gray)
                    }
                }
            }

            Spacer(modifier = Modifier.height(24.dp))

            // Information Banner
            Surface(
                modifier = Modifier.fillMaxWidth(),
                color = Color(0xFFE3F2FD),
                shape = RoundedCornerShape(8.dp)
            ) {
                Row(
                    modifier = Modifier.padding(12.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text("ℹ️", fontSize = 16.sp)
                    Spacer(modifier = Modifier.width(12.dp))
                    Text(
                        "Uploading proof will automatically mark the complaint as Resolved",
                        fontSize = 12.sp,
                        color = Color(0xFF1565C0),
                        fontWeight = FontWeight.Medium
                    )
                }
            }

            Spacer(modifier = Modifier.height(32.dp))

            // Submit Button
            if (isUploading) {
                Box(modifier = Modifier.fillMaxWidth(), contentAlignment = Alignment.Center) {
                    CircularProgressIndicator(color = Color(0xFF1565C0))
                }
            } else {
                Button(
                    onClick = {
                        if (selectedTicketId.isNotEmpty() && selectedPhoto != null) {
                            isUploading = true
                            scope.launch {
                                val result = FirestoreService.uploadStaffProof(context, selectedTicketId.trim(), selectedPhoto, null)
                                isUploading = false
                                if (result.isSuccess) {
                                    message = "Success! Marked as Resolved ✅"
                                    selectedPhoto = null
                                    android.os.Handler(android.os.Looper.getMainLooper()).postDelayed({
                                        onBackClick()
                                    }, 2000)
                                } else {
                                    message = "Error: ${result.exceptionOrNull()?.message}"
                                }
                            }
                        } else {
                            message = "Please select a ticket and attach a photo"
                        }
                    },
                    modifier = Modifier.fillMaxWidth().height(56.dp),
                    shape = RoundedCornerShape(12.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF1565C0)),
                    enabled = selectedTicketId.isNotEmpty() && selectedPhoto != null
                ) {
                    Text("Upload & Resolve", fontSize = 16.sp, fontWeight = FontWeight.Bold)
                }
            }

            if (message.isNotEmpty()) {
                Text(
                    text = message,
                    modifier = Modifier.padding(top = 16.dp).fillMaxWidth(),
                    textAlign = androidx.compose.ui.text.style.TextAlign.Center,
                    color = if (message.contains("Success")) Color(0xFF2E7D32) else Color.Red,
                    fontWeight = FontWeight.Bold
                )
            }
        }
    }
}

private fun showPhotoSourceDialog(context: android.content.Context, onGallery: () -> Unit, onCamera: () -> Unit) {
    val options = arrayOf("Choose from Gallery", "Take Photo")
    val builder = android.app.AlertDialog.Builder(context)
    builder.setTitle("Select Source")
    builder.setItems(options) { _, which ->
        if (which == 0) onGallery() else onCamera()
    }
    builder.show()
}
