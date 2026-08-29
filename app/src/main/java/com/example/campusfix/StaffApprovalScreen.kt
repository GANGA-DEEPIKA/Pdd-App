package com.example.campusfix

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
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
import kotlinx.coroutines.flow.collect

@Composable
fun StaffApprovalScreen(college: String, onBackClick: () -> Unit) {
    val scope = rememberCoroutineScope()
    var pendingRequests by remember { mutableStateOf<List<UserData>>(emptyList()) }
    var errorMessage by remember { mutableStateOf("") }
    var isLoading by remember { mutableStateOf(true) }

    var selectedUser by remember { mutableStateOf<UserData?>(null) }
    var selectedCategories by remember { mutableStateOf(setOf<String>()) }
    var isProcessing by remember { mutableStateOf(false) }

    val categories = listOf(
        "Electrical", "Plumbing", "Network/WiFi", "Cleanliness",
        "Furniture", "Hostel", "Security", "Transport",
        "Laboratory", "General Maintenance"
    )

    LaunchedEffect(college) {
        try {
            FirestoreService.getPendingStaffRequests(college).collect { list ->
                pendingRequests = list
                isLoading = false
            }
        } catch (e: Exception) {
            errorMessage = e.message ?: "Database error"
            isLoading = false
        }
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
            Column {
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
                        Text("Staff Approvals", fontSize = 20.sp, fontWeight = FontWeight.Bold)
                        Text("College Filter: $college", fontSize = 11.sp, color = Color.Gray)
                    }
                }
            }
        }

        if (isLoading) {
            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                CircularProgressIndicator()
            }
        } else if (errorMessage.isNotEmpty()) {
            Box(modifier = Modifier.fillMaxSize().padding(24.dp), contentAlignment = Alignment.Center) {
                Text("Error: $errorMessage", color = Color.Red, textAlign = androidx.compose.ui.text.style.TextAlign.Center)
            }
        } else if (pendingRequests.isEmpty()) {
            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                Column(horizontalAlignment = Alignment.CenterHorizontally, modifier = Modifier.padding(24.dp)) {
                    Text("🎉", fontSize = 40.sp)
                    Spacer(modifier = Modifier.height(8.dp))
                    Text("No pending requests", fontWeight = FontWeight.Bold)
                    Spacer(modifier = Modifier.height(16.dp))
                    Text(
                        "If you registered a staff member and they don't appear, ensure they registered for college '$college' exactly.",
                        fontSize = 12.sp,
                        color = Color.Gray,
                        textAlign = androidx.compose.ui.text.style.TextAlign.Center
                    )
                }
            }
        } else {
            LazyColumn(
                modifier = Modifier.weight(1f).padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                items(pendingRequests) { user ->
                    StaffRequestCard(
                        user = user,
                        isSelected = selectedUser?.uid == user.uid,
                        onClick = {
                            selectedUser = user
                            selectedCategories = user.assignedCategories.toSet()
                        }
                    )
                }
            }
        }

        if (selectedUser != null) {
            Card(
                modifier = Modifier.fillMaxWidth().padding(16.dp),
                shape = RoundedCornerShape(24.dp),
                colors = CardDefaults.cardColors(containerColor = Color.White),
                elevation = CardDefaults.cardElevation(8.dp)
            ) {
                Column(modifier = Modifier.padding(20.dp)) {
                    Text("Approve ${selectedUser?.name}", fontWeight = FontWeight.Bold)
                    Text(selectedUser?.email ?: "", fontSize = 12.sp, color = Color.Gray)
                    Spacer(modifier = Modifier.height(12.dp))
                    
                    Box(modifier = Modifier.height(150.dp)) {
                        LazyVerticalGrid(
                            columns = GridCells.Fixed(2),
                            horizontalArrangement = Arrangement.spacedBy(8.dp),
                            verticalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            items(categories) { cat ->
                                FilterChip(
                                    selected = cat in selectedCategories,
                                    onClick = {
                                        selectedCategories = if (cat in selectedCategories) {
                                            selectedCategories - cat
                                        } else {
                                            selectedCategories + cat
                                        }
                                    },
                                    label = { Text(cat, fontSize = 11.sp) }
                                )
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(16.dp))

                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                        Button(
                            onClick = {
                                scope.launch {
                                    isProcessing = true
                                    FirestoreService.approveStaffRequest(selectedUser!!, selectedCategories.toList())
                                    selectedUser = null
                                    isProcessing = false
                                }
                            },
                            modifier = Modifier.weight(1f),
                            enabled = selectedCategories.isNotEmpty() && !isProcessing,
                            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF4CAF50))
                        ) {
                            Text("Approve")
                        }
                        
                        OutlinedButton(
                            onClick = {
                                scope.launch {
                                    isProcessing = true
                                    FirestoreService.rejectStaffRequest(selectedUser!!.uid)
                                    selectedUser = null
                                    isProcessing = false
                                }
                            },
                            modifier = Modifier.weight(1f),
                            enabled = !isProcessing,
                            colors = ButtonDefaults.outlinedButtonColors(contentColor = Color.Red)
                        ) {
                            Text("Reject")
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun StaffRequestCard(user: UserData, isSelected: Boolean, onClick: () -> Unit) {
    Card(
        modifier = Modifier.fillMaxWidth().clickable { onClick() },
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(
            containerColor = if (isSelected) Color(0xFFE3F2FD) else Color.White
        ),
        border = if (isSelected) androidx.compose.foundation.BorderStroke(2.dp, Color(0xFF1565C0)) else null,
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(user.name, fontWeight = FontWeight.Bold, fontSize = 18.sp)
            Text(user.email, fontSize = 14.sp, color = Color.Gray)
            Text("College: ${user.collegeName}", fontSize = 12.sp, color = Color.Gray)
        }
    }
}
