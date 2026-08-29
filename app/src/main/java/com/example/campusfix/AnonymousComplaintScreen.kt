package com.example.campusfix

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlinx.coroutines.launch

@Composable
fun AnonymousComplaintScreen(onBackClick: () -> Unit) {
    var complaintText by remember { mutableStateOf("") }
    var message by remember { mutableStateOf("") }
    var isLoading by remember { mutableStateOf(false) }
    val scope = rememberCoroutineScope()
    val context = androidx.compose.ui.platform.LocalContext.current

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
                Text("Incognito Report", fontSize = 20.sp, fontWeight = FontWeight.Bold)
            }
        }

        Column(modifier = Modifier.padding(24.dp)) {
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(20.dp),
                colors = CardDefaults.cardColors(containerColor = Color.White),
                elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
            ) {
                Column(modifier = Modifier.padding(24.dp)) {
                    Text(
                        "Submit Anonymously",
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFF1565C0)
                    )
                    
                    Spacer(modifier = Modifier.height(8.dp))
                    
                    Text(
                        "Your identity will not be shared with anyone. Report freely.",
                        fontSize = 13.sp,
                        color = Color.Gray
                    )
                    
                    Spacer(modifier = Modifier.height(24.dp))

                    OutlinedTextField(
                        value = complaintText,
                        onValueChange = { complaintText = it },
                        label = { Text("Report Details") },
                        placeholder = { Text("Describe the issue without revealing your identity...") },
                        modifier = Modifier.fillMaxWidth().height(180.dp),
                        shape = RoundedCornerShape(12.dp),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedTextColor = Color.Black,
                            unfocusedTextColor = Color.Black
                        )
                    )

                    Spacer(modifier = Modifier.height(24.dp))

                    if (isLoading) {
                        Box(modifier = Modifier.fillMaxWidth(), contentAlignment = Alignment.Center) {
                            CircularProgressIndicator()
                        }
                    } else {
                        Button(
                            onClick = {
                                if (complaintText.isBlank()) return@Button
                                isLoading = true
                                scope.launch {
                                    val anonymous = ComplaintData(
                                        title = "ANONYMOUS REPORT",
                                        details = complaintText,
                                        category = "General Maintenance",
                                        priority = "High",
                                        studentUid = "anonymous", // Flag as anonymous
                                        studentEmail = "anonymous@campus.edu",
                                        status = "Pending"
                                    )
                                    val result = FirestoreService.submitComplaint(context, anonymous)
                                    isLoading = false
                                    if (result.isSuccess) {
                                        message = "Anonymous report sent to admin ✅"
                                        complaintText = ""
                                        android.widget.Toast.makeText(context, "Report submitted! ✅", android.widget.Toast.LENGTH_SHORT).show()
                                    } else {
                                        message = "Failed to send report. Try again later."
                                        android.widget.Toast.makeText(context, "Submission failed! ❌", android.widget.Toast.LENGTH_SHORT).show()
                                    }
                                }
                            },
                            modifier = Modifier.fillMaxWidth().height(56.dp),
                            shape = RoundedCornerShape(16.dp),
                            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF1565C0))
                        ) {
                            Text("Send Anonymously", fontSize = 16.sp, fontWeight = FontWeight.Bold)
                        }
                    }
                }
            }

            if (message.isNotEmpty()) {
                Spacer(modifier = Modifier.height(24.dp))
                Card(
                    colors = CardDefaults.cardColors(
                        containerColor = if (message.contains("sent")) Color(0xFFE8F5E9) else Color(0xFFFFEBEE)
                    ),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Text(
                        text = message,
                        color = if (message.contains("sent")) Color(0xFF2E7D32) else Color.Red,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.padding(16.dp),
                        textAlign = androidx.compose.ui.text.style.TextAlign.Center
                    )
                }
            }
        }
    }
}
