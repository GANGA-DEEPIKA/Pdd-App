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
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlinx.coroutines.launch

@Composable
fun FeedbackScreen(collegeName: String, onBackClick: () -> Unit) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    var feedback by remember { mutableStateOf("") }
    var rating by remember { mutableStateOf(0) }
    var isLoading by remember { mutableStateOf(false) }

    val ratingLabels = listOf("", "Poor", "Fair", "Good", "Very Good", "Excellent")

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
                    text = "Feedback",
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color.Black
                )
            }
        }

        // Main Content Area
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp)
                .verticalScroll(rememberScrollState()),
            contentAlignment = Alignment.Center
        ) {
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(20.dp),
                colors = CardDefaults.cardColors(containerColor = Color.White),
                border = BorderStroke(1.dp, Color(0xFFE5E7EB)),
                elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
            ) {
                Column(
                    modifier = Modifier.padding(24.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text("⭐", fontSize = 48.sp)

                    Spacer(modifier = Modifier.height(8.dp))

                    Text(
                        text = "Rate Your Experience",
                        fontSize = 20.sp,
                        fontWeight = FontWeight.ExtraBold,
                        color = Color(0xFF1565C0)
                    )

                    Spacer(modifier = Modifier.height(4.dp))

                    Text(
                        text = "Your feedback helps us improve CampusFix",
                        fontSize = 13.sp,
                        color = Color(0xFF6B7280),
                        textAlign = TextAlign.Center
                    )

                    Spacer(modifier = Modifier.height(20.dp))

                    // 5 Star Buttons
                    Row(
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        for (i in 1..5) {
                            Text(
                                text = "⭐",
                                fontSize = 36.sp,
                                modifier = Modifier
                                    .clickable { rating = i }
                                    .padding(4.dp),
                                color = if (i <= rating) Color.Unspecified else Color.Gray.copy(alpha = 0.35f)
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(8.dp))

                    Text(
                        text = if (rating in 1..5) ratingLabels[rating] else "Tap a star to rate",
                        fontSize = 13.sp,
                        fontWeight = if (rating > 0) FontWeight.Bold else FontWeight.Normal,
                        color = if (rating > 0) Color(0xFFD97706) else Color(0xFF6B7280)
                    )

                    Spacer(modifier = Modifier.height(20.dp))

                    // Comments Field
                    Column(modifier = Modifier.fillMaxWidth()) {
                        Text(
                            text = "Comments (Optional)",
                            fontSize = 13.sp,
                            fontWeight = FontWeight.Medium,
                            color = Color(0xFF374151)
                        )
                        Spacer(modifier = Modifier.height(6.dp))
                        OutlinedTextField(
                            value = feedback,
                            onValueChange = { feedback = it },
                            placeholder = { Text("Tell us about your experience...", color = Color(0xFF9CA3AF), fontSize = 14.sp) },
                            modifier = Modifier
                                .fillMaxWidth()
                                .heightIn(min = 110.dp),
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

                    Spacer(modifier = Modifier.height(24.dp))

                    // Submit Button
                    Button(
                        onClick = {
                            if (rating == 0) {
                                Toast.makeText(context, "Please tap a star to rate first", Toast.LENGTH_SHORT).show()
                                return@Button
                            }
                            isLoading = true
                            scope.launch {
                                val result = FirestoreService.submitFeedback(rating, feedback.trim(), collegeName)
                                isLoading = false
                                if (result.isSuccess) {
                                    Toast.makeText(context, "Thank you for your feedback! ⭐", Toast.LENGTH_LONG).show()
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
                            Text(
                                text = "Submit Feedback",
                                fontSize = 16.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color.White
                            )
                        }
                    }
                }
            }
        }
    }
}
