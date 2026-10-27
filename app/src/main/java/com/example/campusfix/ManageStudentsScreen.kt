package com.example.campusfix

import android.widget.Toast
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
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
fun ManageStudentsScreen(college: String, onBackClick: () -> Unit) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    var studentList by remember { mutableStateOf<List<UserData>>(emptyList()) }
    var searchQuery by remember { mutableStateOf("") }
    var isLoading by remember { mutableStateOf(true) }

    val displayCollege = college.ifBlank { "Saveetha school of Engineering" }

    LaunchedEffect(displayCollege) {
        FirestoreService.getStudentsByCollege(displayCollege).collect { list ->
            studentList = list
            isLoading = false
        }
    }

    val filteredStudents = studentList.filter { student ->
        val q = searchQuery.trim().lowercase()
        q.isEmpty() || student.name.lowercase().contains(q) || student.email.lowercase().contains(q) || student.regNo.lowercase().contains(q)
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFFF8F9FA))
    ) {
        // App Bar Header (Dark Blue)
        Surface(
            modifier = Modifier.fillMaxWidth(),
            color = Color(0xFF103480),
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
                    text = "Manage Students",
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color.White
                )
            }
        }

        // Main Content Area
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp)
        ) {
            // Count subtitle
            Text(
                text = "${studentList.size} registered students at $displayCollege",
                fontSize = 13.sp,
                color = Color(0xFF6B7280),
                modifier = Modifier.padding(bottom = 12.dp)
            )

            // Search input field
            OutlinedTextField(
                value = searchQuery,
                onValueChange = { searchQuery = it },
                placeholder = { Text("🔍 Search by name or email...", color = Color(0xFF9CA3AF), fontSize = 14.sp) },
                singleLine = true,
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(12.dp),
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

            if (isLoading) {
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    CircularProgressIndicator(color = Color(0xFF1565C0))
                }
            } else if (filteredStudents.isEmpty()) {
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text("👥", fontSize = 48.sp)
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            text = if (searchQuery.isNotBlank()) "No matching students found" else "No students registered yet",
                            fontSize = 15.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color(0xFF374151)
                        )
                    }
                }
            } else {
                LazyColumn(
                    modifier = Modifier.fillMaxSize(),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    items(filteredStudents, key = { it.uid.ifEmpty { it.email } }) { student ->
                        StudentCardItem(
                            student = student,
                            onRemove = {
                                scope.launch {
                                    val result = FirestoreService.removeUser(student.uid)
                                    if (result.isSuccess) {
                                        Toast.makeText(context, "${student.name} removed", Toast.LENGTH_SHORT).show()
                                    } else {
                                        Toast.makeText(context, "Remove failed", Toast.LENGTH_SHORT).show()
                                    }
                                }
                            }
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun StudentCardItem(
    student: UserData,
    onRemove: () -> Unit
) {
    var isRemoving by remember { mutableStateOf(false) }

    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        border = BorderStroke(1.dp, Color(0xFFE5E7EB)),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
    ) {
        Row(
            modifier = Modifier.padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Initial Circle Avatar
            Surface(
                shape = CircleShape,
                color = Color(0xFF1565C0),
                modifier = Modifier.size(44.dp)
            ) {
                Box(contentAlignment = Alignment.Center) {
                    val initial = (student.name.trim().take(1).ifEmpty { "S" }).uppercase()
                    Text(
                        text = initial,
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color.White
                    )
                }
            }

            Spacer(modifier = Modifier.width(12.dp))

            // Student details
            Column(
                modifier = Modifier.weight(1f)
            ) {
                Text(
                    text = student.name.ifBlank { "Student" },
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color(0xFF1F2937)
                )

                Spacer(modifier = Modifier.height(2.dp))

                Text(
                    text = student.email,
                    fontSize = 12.sp,
                    color = Color(0xFF6B7280)
                )

                val detailsSub = listOfNotNull(
                    student.regNo.takeIf { it.isNotBlank() },
                    student.year.takeIf { it.isNotBlank() }
                ).joinToString(" • ")

                if (detailsSub.isNotBlank()) {
                    Spacer(modifier = Modifier.height(2.dp))
                    Text(
                        text = detailsSub,
                        fontSize = 11.sp,
                        color = Color(0xFF9CA3AF)
                    )
                }
            }

            Spacer(modifier = Modifier.width(8.dp))

            // Remove Button
            Button(
                onClick = {
                    isRemoving = true
                    onRemove()
                },
                enabled = !isRemoving,
                shape = RoundedCornerShape(8.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = Color(0xFFFFEBEE),
                    contentColor = Color(0xFFDC2626)
                ),
                contentPadding = PaddingValues(horizontal = 12.dp, vertical = 6.dp),
                modifier = Modifier.height(34.dp)
            ) {
                Text(
                    text = if (isRemoving) "…" else "Remove",
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color(0xFFDC2626)
                )
            }
        }
    }
}
