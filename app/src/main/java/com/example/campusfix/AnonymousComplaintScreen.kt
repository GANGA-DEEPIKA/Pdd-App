package com.example.campusfix

import android.widget.Toast
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
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

@Composable
fun AnonymousComplaintScreen(onBackClick: () -> Unit) {
    var title by remember { mutableStateOf("") }
    var details by remember { mutableStateOf("") }
    var location by remember { mutableStateOf("") }
    var isLoading by remember { mutableStateOf(false) }

    val scope = rememberCoroutineScope()
    val context = LocalContext.current

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFFF8F9FA))
    ) {
        // App Bar
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
                    text = "🕵 Anonymous Report",
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
            // Yellow Warning Banner
            Surface(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(12.dp),
                color = Color(0xFFFFF8E1),
                border = BorderStroke(1.dp, Color(0xFFFBBF24))
            ) {
                Row(
                    modifier = Modifier.padding(16.dp),
                    verticalAlignment = Alignment.Top
                ) {
                    Text("🕵", fontSize = 22.sp, modifier = Modifier.padding(top = 2.dp))
                    Spacer(modifier = Modifier.width(12.dp))
                    Column {
                        Text(
                            text = "Anonymous Submission",
                            fontSize = 14.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color(0xFFB45309)
                        )
                        Spacer(modifier = Modifier.height(2.dp))
                        Text(
                            text = "Your identity will be completely hidden. This complaint will show as \"anonymous@campus.edu\" in the system.",
                            fontSize = 12.sp,
                            color = Color(0xFFB45309),
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
                        color = Color(0xFF374151)
                    )

                    Spacer(modifier = Modifier.height(16.dp))

                    // Subject Field
                    Text(
                        text = "Subject *",
                        fontSize = 13.sp,
                        fontWeight = FontWeight.Medium,
                        color = Color(0xFF374151)
                    )
                    Spacer(modifier = Modifier.height(6.dp))
                    OutlinedTextField(
                        value = title,
                        onValueChange = { title = it },
                        placeholder = { Text("Brief title of the issue", color = Color(0xFF9CA3AF), fontSize = 14.sp) },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(10.dp),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = Color(0xFF2563EB),
                            unfocusedBorderColor = Color(0xFFD1D5DB),
                            focusedContainerColor = Color.White,
                            unfocusedContainerColor = Color.White,
                            focusedTextColor = Color.Black,
                            unfocusedTextColor = Color.Black
                        )
                    )

                    Spacer(modifier = Modifier.height(16.dp))

                    // Description Field
                    Text(
                        text = "Description",
                        fontSize = 13.sp,
                        fontWeight = FontWeight.Medium,
                        color = Color(0xFF374151)
                    )
                    Spacer(modifier = Modifier.height(6.dp))
                    OutlinedTextField(
                        value = details,
                        onValueChange = { details = it },
                        placeholder = { Text("Describe the issue...", color = Color(0xFF9CA3AF), fontSize = 14.sp) },
                        modifier = Modifier
                            .fillMaxWidth()
                            .heightIn(min = 100.dp),
                        shape = RoundedCornerShape(10.dp),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = Color(0xFF2563EB),
                            unfocusedBorderColor = Color(0xFFD1D5DB),
                            focusedContainerColor = Color.White,
                            unfocusedContainerColor = Color.White,
                            focusedTextColor = Color.Black,
                            unfocusedTextColor = Color.Black
                        )
                    )

                    Spacer(modifier = Modifier.height(16.dp))

                    // Location Field
                    Text(
                        text = "Location *",
                        fontSize = 13.sp,
                        fontWeight = FontWeight.Medium,
                        color = Color(0xFF374151)
                    )
                    Spacer(modifier = Modifier.height(6.dp))
                    OutlinedTextField(
                        value = location,
                        onValueChange = { location = it },
                        placeholder = { Text("Where is the issue?", color = Color(0xFF9CA3AF), fontSize = 14.sp) },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(10.dp),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = Color(0xFF2563EB),
                            unfocusedBorderColor = Color(0xFFD1D5DB),
                            focusedContainerColor = Color.White,
                            unfocusedContainerColor = Color.White,
                            focusedTextColor = Color.Black,
                            unfocusedTextColor = Color.Black
                        )
                    )
                }
            }

            Spacer(modifier = Modifier.height(24.dp))

            // Submit Button
            Button(
                onClick = {
                    if (title.isBlank() || location.isBlank()) {
                        Toast.makeText(context, "Please fill in required fields (*)", Toast.LENGTH_SHORT).show()
                        return@Button
                    }
                    isLoading = true
                    scope.launch {
                        val category = suggestCategory("$title $details")
                        val anonymousComplaint = ComplaintData(
                            title = title.trim(),
                            details = details.trim(),
                            location = location.trim(),
                            category = category,
                            priority = "Medium",
                            studentUid = "anonymous",
                            studentEmail = "anonymous@campus.edu",
                            status = "Pending"
                        )
                        val result = FirestoreService.submitComplaint(context, anonymousComplaint)
                        isLoading = false
                        if (result.isSuccess) {
                            Toast.makeText(context, "Anonymous complaint submitted! 🕵️", Toast.LENGTH_LONG).show()
                            onBackClick()
                        } else {
                            val errorMsg = result.exceptionOrNull()?.message ?: "Submission failed"
                            Toast.makeText(context, "Error: $errorMsg ❌", Toast.LENGTH_LONG).show()
                        }
                    }
                },
                enabled = !isLoading,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(52.dp),
                shape = RoundedCornerShape(12.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = Color(0xFF1565C0)
                )
            ) {
                if (isLoading) {
                    CircularProgressIndicator(color = Color.White, modifier = Modifier.size(24.dp))
                } else {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.Center
                    ) {
                        Text("🕵️ ", fontSize = 16.sp)
                        Text(
                            text = "Submit Anonymously",
                            fontSize = 16.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color.White
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(40.dp))
        }
    }
}
