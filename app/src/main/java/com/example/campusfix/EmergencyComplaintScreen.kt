package com.example.campusfix

import android.widget.Toast
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
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlinx.coroutines.launch

@Composable
fun EmergencyComplaintScreen(onBackClick: () -> Unit) {
    var title by remember { mutableStateOf("") }
    var details by remember { mutableStateOf("") }
    var location by remember { mutableStateOf("") }
    var category by remember { mutableStateOf("Electrical") }
    var expandedCategory by remember { mutableStateOf(false) }
    var isLoading by remember { mutableStateOf(false) }

    val categories = listOf("Electrical", "Plumbing", "Security", "Fire/Safety", "Medical", "General Maintenance")
    val scope = rememberCoroutineScope()
    val context = LocalContext.current

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFFF8F9FA))
    ) {
        // Top Header
        Surface(
            modifier = Modifier.fillMaxWidth(),
            color = Color(0xFFC62828),
            shadowElevation = 4.dp
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 12.dp, vertical = 12.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                IconButton(onClick = onBackClick) {
                    Text("←", fontSize = 24.sp, fontWeight = FontWeight.Bold, color = Color.White)
                }
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = "🚨 Emergency Report",
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color.White
                )
            }
        }

        // Body Content
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp)
                .verticalScroll(rememberScrollState())
        ) {
            // Emergency Priority Banner
            Surface(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(12.dp),
                color = Color(0xFFFFF0F0),
                border = BorderStroke(1.dp, Color(0xFFFCA5A5))
            ) {
                Row(
                    modifier = Modifier.padding(16.dp),
                    verticalAlignment = Alignment.Top
                ) {
                    Text("🚨", fontSize = 20.sp, modifier = Modifier.padding(top = 2.dp))
                    Spacer(modifier = Modifier.width(12.dp))
                    Column {
                        Text(
                            text = "Emergency Priority",
                            fontSize = 14.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color(0xFF991B1B)
                        )
                        Spacer(modifier = Modifier.height(2.dp))
                        Text(
                            text = "This complaint will be flagged as high-priority and escalated immediately to the admin and available staff.",
                            fontSize = 12.sp,
                            color = Color(0xFF991B1B),
                            lineHeight = 16.sp
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Emergency Details Form Card
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
                        text = "Emergency Details",
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFF800000)
                    )

                    Spacer(modifier = Modifier.height(16.dp))

                    // Title Field
                    Text(
                        text = "Emergency Title *",
                        fontSize = 13.sp,
                        fontWeight = FontWeight.Medium,
                        color = Color(0xFF374151)
                    )
                    Spacer(modifier = Modifier.height(6.dp))
                    OutlinedTextField(
                        value = title,
                        onValueChange = { title = it },
                        placeholder = { Text("e.g. Electrical short circuit", color = Color(0xFF9CA3AF), fontSize = 14.sp) },
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
                        placeholder = { Text("Describe the emergency clearly...", color = Color(0xFF9CA3AF), fontSize = 14.sp) },
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
                        placeholder = { Text("Where is the emergency?", color = Color(0xFF9CA3AF), fontSize = 14.sp) },
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

                    // Category Dropdown
                    Text(
                        text = "Category",
                        fontSize = 13.sp,
                        fontWeight = FontWeight.Medium,
                        color = Color(0xFF374151)
                    )
                    Spacer(modifier = Modifier.height(6.dp))
                    Box(modifier = Modifier.fillMaxWidth()) {
                        OutlinedTextField(
                            value = category,
                            onValueChange = {},
                            readOnly = true,
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(10.dp),
                            trailingIcon = {
                                Text(
                                    text = if (expandedCategory) "▲" else "▼",
                                    fontSize = 11.sp,
                                    color = Color.Gray,
                                    modifier = Modifier.clickable { expandedCategory = !expandedCategory }
                                )
                            },
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedBorderColor = Color(0xFFD1D5DB),
                                unfocusedBorderColor = Color(0xFFD1D5DB),
                                focusedContainerColor = Color.White,
                                unfocusedContainerColor = Color.White,
                                focusedTextColor = Color.Black,
                                unfocusedTextColor = Color.Black
                            )
                        )
                        Box(
                            modifier = Modifier
                                .matchParentSize()
                                .clickable { expandedCategory = true }
                        )
                        DropdownMenu(
                            expanded = expandedCategory,
                            onDismissRequest = { expandedCategory = false },
                            modifier = Modifier
                                .fillMaxWidth(0.85f)
                                .background(Color.White)
                        ) {
                            categories.forEach { item ->
                                DropdownMenuItem(
                                    text = { Text(item, color = Color.Black) },
                                    onClick = {
                                        category = item
                                        expandedCategory = false
                                    }
                                )
                            }
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(24.dp))

            // Submit Button
            Button(
                onClick = {
                    if (title.isBlank() || location.isBlank()) {
                        Toast.makeText(context, "Please fill in all required fields (*)", Toast.LENGTH_SHORT).show()
                        return@Button
                    }
                    isLoading = true
                    scope.launch {
                        val emergencyComplaint = ComplaintData(
                            title = title.trim(),
                            details = details.trim(),
                            location = location.trim(),
                            category = category,
                            priority = "High",
                            status = "Pending"
                        )
                        val result = FirestoreService.submitComplaint(context, emergencyComplaint)
                        isLoading = false
                        if (result.isSuccess) {
                            Toast.makeText(context, "🚨 Emergency reported successfully!", Toast.LENGTH_LONG).show()
                            onBackClick()
                        } else {
                            val errorMsg = result.exceptionOrNull()?.message ?: "Failed to submit emergency report"
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
                    containerColor = Color(0xFFD32F2F)
                )
            ) {
                if (isLoading) {
                    CircularProgressIndicator(color = Color.White, modifier = Modifier.size(24.dp))
                } else {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.Center
                    ) {
                        Text("🚨 ", fontSize = 16.sp)
                        Text(
                            text = "Submit Emergency Report",
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
