package com.example.campusfix

import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.border
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
fun DuplicateGroupsScreen(
    complaintList: List<ComplaintData>,
    onBackClick: () -> Unit
) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    var filterType by remember { mutableStateOf("all") } // "all", "suggested", "confirmed"
    var isProcessing by remember { mutableStateOf(false) }

    // Run AI grouping on complaint list
    val allGroups = remember(complaintList) {
        AIDuplicateEngine.groupComplaintsWithAI(complaintList)
    }

    val filteredGroups = remember(allGroups, filterType) {
        when (filterType) {
            "suggested" -> allGroups.filter { !it.isConfirmed }
            "confirmed" -> allGroups.filter { it.isConfirmed }
            else -> allGroups
        }
    }

    val totalGroups = allGroups.size
    val totalAffected = allGroups.sumOf { it.totalStudentsAffected }
    val confirmedCount = allGroups.count { it.isConfirmed }
    val suggestedCount = totalGroups - confirmedCount

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFFF5F7FA))
    ) {
        // App Bar
        Surface(
            modifier = Modifier.fillMaxWidth(),
            color = Color.White,
            shadowElevation = 4.dp
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 14.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                IconButton(onClick = onBackClick) {
                    Text("←", fontSize = 24.sp, fontWeight = FontWeight.Bold, color = Color(0xFF1565C0))
                }
                Spacer(modifier = Modifier.width(8.dp))
                Column {
                    Text(
                        text = "AI Duplicate Groups",
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFF1565C0)
                    )
                    Text(
                        text = "Automatic similarity detection & student impact",
                        fontSize = 11.sp,
                        color = Color.Gray
                    )
                }
            }
        }

        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp)
                .verticalScroll(rememberScrollState())
        ) {
            // Summary Overview Banner
            Surface(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(16.dp),
                color = Color(0xFF1565C0),
                shadowElevation = 3.dp
            ) {
                Column(modifier = Modifier.padding(18.dp)) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        Surface(
                            modifier = Modifier.size(38.dp),
                            shape = RoundedCornerShape(10.dp),
                            color = Color.White.copy(alpha = 0.2f)
                        ) {
                            Box(contentAlignment = Alignment.Center) {
                                Text("👥", fontSize = 20.sp)
                            }
                        }
                        Column {
                            Text(
                                text = "Duplicate Complaints Overview",
                                fontSize = 16.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color.White
                            )
                            Text(
                                text = "Grouped by AI based on Title, Category, Location & Details",
                                fontSize = 11.sp,
                                color = Color.White.copy(alpha = 0.85f)
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(16.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        Surface(
                            modifier = Modifier.weight(1f),
                            shape = RoundedCornerShape(12.dp),
                            color = Color.White.copy(alpha = 0.15f)
                        ) {
                            Column(
                                modifier = Modifier.padding(vertical = 10.dp),
                                horizontalAlignment = Alignment.CenterHorizontally
                            ) {
                                Text("$totalGroups", fontSize = 20.sp, fontWeight = FontWeight.Bold, color = Color.White)
                                Text("Groups", fontSize = 11.sp, color = Color.White.copy(alpha = 0.85f))
                            }
                        }

                        Surface(
                            modifier = Modifier.weight(1f),
                            shape = RoundedCornerShape(12.dp),
                            color = Color.White.copy(alpha = 0.15f)
                        ) {
                            Column(
                                modifier = Modifier.padding(vertical = 10.dp),
                                horizontalAlignment = Alignment.CenterHorizontally
                            ) {
                                Text("$totalAffected", fontSize = 20.sp, fontWeight = FontWeight.Bold, color = Color.White)
                                Text("Students", fontSize = 11.sp, color = Color.White.copy(alpha = 0.85f))
                            }
                        }

                        Surface(
                            modifier = Modifier.weight(1f),
                            shape = RoundedCornerShape(12.dp),
                            color = Color.White.copy(alpha = 0.15f)
                        ) {
                            Column(
                                modifier = Modifier.padding(vertical = 10.dp),
                                horizontalAlignment = Alignment.CenterHorizontally
                            ) {
                                Text("$confirmedCount", fontSize = 20.sp, fontWeight = FontWeight.Bold, color = Color.White)
                                Text("Confirmed", fontSize = 11.sp, color = Color.White.copy(alpha = 0.85f))
                            }
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Filter Tabs
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                FilterTabButton(
                    text = "All ($totalGroups)",
                    isSelected = filterType == "all",
                    onClick = { filterType = "all" },
                    modifier = Modifier.weight(1f)
                )
                FilterTabButton(
                    text = "🤖 AI ($suggestedCount)",
                    isSelected = filterType == "suggested",
                    onClick = { filterType = "suggested" },
                    modifier = Modifier.weight(1f)
                )
                FilterTabButton(
                    text = "✔ Done ($confirmedCount)",
                    isSelected = filterType == "confirmed",
                    onClick = { filterType = "confirmed" },
                    modifier = Modifier.weight(1f)
                )
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Groups Content
            if (filteredGroups.isEmpty()) {
                Card(
                    modifier = Modifier.fillMaxWidth().padding(top = 20.dp),
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(containerColor = Color.White),
                    elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
                ) {
                    Column(
                        modifier = Modifier.fillMaxWidth().padding(32.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Text("🎉", fontSize = 42.sp)
                        Spacer(modifier = Modifier.height(8.dp))
                        Text("No Duplicate Complaints", fontWeight = FontWeight.Bold, fontSize = 16.sp, color = Color.DarkGray)
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = "All complaints in your department are currently unique.",
                            fontSize = 12.sp,
                            color = Color.Gray,
                            textAlign = androidx.compose.ui.text.style.TextAlign.Center
                        )
                    }
                }
            } else {
                for (group in filteredGroups) {
                    val main = group.mainComplaint
                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(bottom = 18.dp),
                        shape = RoundedCornerShape(16.dp),
                        colors = CardDefaults.cardColors(containerColor = Color.White),
                        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
                    ) {
                        Column(modifier = Modifier.fillMaxWidth()) {
                            
                            // Group Header
                            Surface(
                                modifier = Modifier.fillMaxWidth(),
                                color = Color(0xFFF0F7FF)
                            ) {
                                Row(
                                    modifier = Modifier.fillMaxWidth().padding(14.dp),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Surface(
                                        shape = RoundedCornerShape(12.dp),
                                        color = Color(0xFF1565C0)
                                    ) {
                                        Text(
                                            text = "👥 ${group.totalStudentsAffected} Students Affected",
                                            modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp),
                                            color = Color.White,
                                            fontWeight = FontWeight.Bold,
                                            fontSize = 11.sp
                                        )
                                    }

                                    if (group.isConfirmed) {
                                        Surface(
                                            shape = RoundedCornerShape(10.dp),
                                            color = Color(0xFFE8F5E9)
                                        ) {
                                            Text(
                                                text = "✔ Confirmed Group",
                                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp),
                                                color = Color(0xFF2E7D32),
                                                fontWeight = FontWeight.Bold,
                                                fontSize = 10.sp
                                            )
                                        }
                                    } else {
                                        Surface(
                                            shape = RoundedCornerShape(10.dp),
                                            color = Color(0xFFFFF3E0)
                                        ) {
                                            Text(
                                                text = "🤖 AI Suggested (${group.confidence}%)",
                                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp),
                                                color = Color(0xFFE65100),
                                                fontWeight = FontWeight.Bold,
                                                fontSize = 10.sp
                                            )
                                        }
                                    }
                                }
                            }

                            // AI Reason info
                            Surface(
                                modifier = Modifier.fillMaxWidth(),
                                color = Color(0xFFFAFAFA)
                            ) {
                                Row(
                                    modifier = Modifier.fillMaxWidth().padding(horizontal = 14.dp, vertical = 8.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Text("💡", fontSize = 13.sp)
                                    Spacer(modifier = Modifier.width(6.dp))
                                    Text(
                                        text = group.matchReason,
                                        fontSize = 11.sp,
                                        color = Color.DarkGray
                                    )
                                }
                            }

                            Column(modifier = Modifier.padding(14.dp)) {
                                // Primary Complaint Box
                                Surface(
                                    modifier = Modifier.fillMaxWidth(),
                                    shape = RoundedCornerShape(10.dp),
                                    color = Color(0xFFF9FBFF),
                                    border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFFBBDEFB))
                                ) {
                                    Column(modifier = Modifier.padding(12.dp)) {
                                        Row(
                                            modifier = Modifier.fillMaxWidth(),
                                            horizontalArrangement = Arrangement.SpaceBetween,
                                            verticalAlignment = Alignment.CenterVertically
                                        ) {
                                            Row(verticalAlignment = Alignment.CenterVertically) {
                                                Surface(
                                                    shape = RoundedCornerShape(6.dp),
                                                    color = Color(0xFF1565C0)
                                                ) {
                                                    Text(
                                                        text = "PRIMARY",
                                                        modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp),
                                                        color = Color.White,
                                                        fontSize = 9.sp,
                                                        fontWeight = FontWeight.Bold
                                                    )
                                                }
                                                Spacer(modifier = Modifier.width(6.dp))
                                                Text(main.ticketId, fontWeight = FontWeight.Bold, fontSize = 12.sp, color = Color(0xFF1565C0))
                                            }
                                            Surface(
                                                shape = RoundedCornerShape(6.dp),
                                                color = when (main.status) {
                                                    "Resolved" -> Color(0xFFE8F5E9)
                                                    "In Progress" -> Color(0xFFFFF3E0)
                                                    else -> Color(0xFFE3F2FD)
                                                }
                                            ) {
                                                Text(
                                                    text = main.status,
                                                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 2.dp),
                                                    color = when (main.status) {
                                                        "Resolved" -> Color(0xFF2E7D32)
                                                        "In Progress" -> Color(0xFFE65100)
                                                        else -> Color(0xFF1565C0)
                                                    },
                                                    fontWeight = FontWeight.Bold,
                                                    fontSize = 10.sp
                                                )
                                            }
                                        }

                                        Spacer(modifier = Modifier.height(6.dp))
                                        Text(main.title, fontWeight = FontWeight.Bold, fontSize = 14.sp, color = Color(0xFF212121))
                                        if (main.details.isNotEmpty()) {
                                            Spacer(modifier = Modifier.height(2.dp))
                                            Text(main.details, fontSize = 12.sp, color = Color.Gray, maxLines = 2)
                                        }
                                        Spacer(modifier = Modifier.height(6.dp))
                                        Row(
                                            modifier = Modifier.fillMaxWidth(),
                                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                                        ) {
                                            Surface(shape = RoundedCornerShape(6.dp), color = Color(0xFFE3F2FD)) {
                                                Text("🏷️ ${main.category}", modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp), fontSize = 10.sp, color = Color(0xFF1565C0), fontWeight = FontWeight.Bold)
                                            }
                                            Text("📍 ${main.location}", fontSize = 11.sp, color = Color.DarkGray)
                                        }
                                    }
                                }

                                Spacer(modifier = Modifier.height(14.dp))

                                // Linked Complaints
                                Text(
                                    text = "🔗 Linked Duplicate Complaints (${group.linkedComplaints.size})",
                                    fontSize = 12.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = Color.DarkGray
                                )

                                Spacer(modifier = Modifier.height(8.dp))

                                for (linked in group.linkedComplaints) {
                                    Surface(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .padding(bottom = 8.dp),
                                        shape = RoundedCornerShape(10.dp),
                                        color = Color(0xFFF5F7FA),
                                        border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFFE0E0E0))
                                    ) {
                                        Row(
                                            modifier = Modifier.fillMaxWidth().padding(10.dp),
                                            horizontalArrangement = Arrangement.SpaceBetween,
                                            verticalAlignment = Alignment.CenterVertically
                                        ) {
                                            Column(modifier = Modifier.weight(1f)) {
                                                Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                                                    Text(linked.ticketId, fontWeight = FontWeight.Bold, fontSize = 11.sp, color = Color(0xFF1565C0))
                                                    Text("•", color = Color.Gray, fontSize = 10.sp)
                                                    Text(linked.status, fontSize = 10.sp, color = Color.DarkGray, fontWeight = FontWeight.Medium)
                                                    if (linked.studentEmail.isNotEmpty()) {
                                                        Text("• ${linked.studentEmail}", fontSize = 10.sp, color = Color.Gray)
                                                    }
                                                }
                                                Text(linked.title, fontSize = 12.sp, fontWeight = FontWeight.SemiBold, color = Color.Black)
                                                Text("📍 ${linked.location}", fontSize = 10.sp, color = Color.Gray)
                                            }

                                            Spacer(modifier = Modifier.width(8.dp))

                                            OutlinedButton(
                                                onClick = {
                                                    scope.launch {
                                                        isProcessing = true
                                                        val res = FirestoreService.separateDuplicateComplaint(main.ticketId, linked.ticketId)
                                                        isProcessing = false
                                                        if (res.isSuccess) {
                                                            Toast.makeText(context, "Complaint separated successfully ✅", Toast.LENGTH_SHORT).show()
                                                        } else {
                                                            Toast.makeText(context, "Failed to separate: ${res.exceptionOrNull()?.message}", Toast.LENGTH_SHORT).show()
                                                        }
                                                    }
                                                },
                                                contentPadding = PaddingValues(horizontal = 8.dp, vertical = 2.dp),
                                                shape = RoundedCornerShape(8.dp),
                                                colors = ButtonDefaults.outlinedButtonColors(contentColor = Color(0xFFD32F2F))
                                            ) {
                                                Text("✂️ Separate", fontSize = 10.sp, fontWeight = FontWeight.Bold)
                                            }
                                        }
                                    }
                                }

                                Spacer(modifier = Modifier.height(10.dp))

                                // Group Actions Footer
                                Surface(
                                    modifier = Modifier.fillMaxWidth(),
                                    shape = RoundedCornerShape(10.dp),
                                    color = Color(0xFFFAFAFA),
                                    border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFFEEEEEE))
                                ) {
                                    Column(modifier = Modifier.padding(10.dp)) {
                                        Text("Update Group Status (All Linked):", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = Color.DarkGray)
                                        Spacer(modifier = Modifier.height(6.dp))
                                        Row(
                                            modifier = Modifier.fillMaxWidth(),
                                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                                        ) {
                                            Button(
                                                onClick = {
                                                    scope.launch {
                                                        isProcessing = true
                                                        val res = FirestoreService.updateComplaintStatus(main.ticketId, main.collegeName, "In Progress")
                                                        isProcessing = false
                                                        if (res.isSuccess) {
                                                            Toast.makeText(context, "Group marked In Progress! ✅", Toast.LENGTH_SHORT).show()
                                                        }
                                                    }
                                                },
                                                modifier = Modifier.weight(1f),
                                                shape = RoundedCornerShape(8.dp),
                                                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFFF9800)),
                                                contentPadding = PaddingValues(vertical = 6.dp)
                                            ) {
                                                Text("🔄 In Progress", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                                            }

                                            Button(
                                                onClick = {
                                                    scope.launch {
                                                        isProcessing = true
                                                        val res = FirestoreService.updateComplaintStatus(main.ticketId, main.collegeName, "Resolved")
                                                        isProcessing = false
                                                        if (res.isSuccess) {
                                                            Toast.makeText(context, "Group marked Resolved! ✅", Toast.LENGTH_SHORT).show()
                                                        }
                                                    }
                                                },
                                                modifier = Modifier.weight(1f),
                                                shape = RoundedCornerShape(8.dp),
                                                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF4CAF50)),
                                                contentPadding = PaddingValues(vertical = 6.dp)
                                            ) {
                                                Text("✅ Resolved", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                                            }
                                        }

                                        Spacer(modifier = Modifier.height(8.dp))

                                        Row(
                                            modifier = Modifier.fillMaxWidth(),
                                            horizontalArrangement = Arrangement.End,
                                            verticalAlignment = Alignment.CenterVertically
                                        ) {
                                            if (!group.isConfirmed) {
                                                Button(
                                                    onClick = {
                                                        scope.launch {
                                                            isProcessing = true
                                                            val linkedIds = group.linkedComplaints.map { it.ticketId }
                                                            val res = FirestoreService.confirmDuplicateGroup(main.ticketId, linkedIds, group.matchReason)
                                                            isProcessing = false
                                                            if (res.isSuccess) {
                                                                Toast.makeText(context, "Group confirmed! ✅", Toast.LENGTH_SHORT).show()
                                                            }
                                                        }
                                                    },
                                                    shape = RoundedCornerShape(8.dp),
                                                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF1565C0)),
                                                    contentPadding = PaddingValues(horizontal = 12.dp, vertical = 6.dp)
                                                ) {
                                                    Text("✔ Confirm Group", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                                                }
                                                Spacer(modifier = Modifier.width(8.dp))
                                            }

                                            OutlinedButton(
                                                onClick = {
                                                    scope.launch {
                                                        isProcessing = true
                                                        val linkedIds = group.linkedComplaints.map { it.ticketId }
                                                        val res = FirestoreService.separateAllInGroup(main.ticketId, linkedIds)
                                                        isProcessing = false
                                                        if (res.isSuccess) {
                                                            Toast.makeText(context, "Group separated into individual tickets! ✅", Toast.LENGTH_SHORT).show()
                                                        }
                                                    }
                                                },
                                                shape = RoundedCornerShape(8.dp),
                                                contentPadding = PaddingValues(horizontal = 12.dp, vertical = 6.dp)
                                            ) {
                                                Text("Separate All", fontSize = 11.sp, color = Color.DarkGray)
                                            }
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(40.dp))
        }
    }
}

@Composable
fun FilterTabButton(
    text: String,
    isSelected: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Surface(
        modifier = modifier.clickable { onClick() },
        shape = RoundedCornerShape(20.dp),
        color = if (isSelected) Color(0xFF1565C0) else Color(0xFFE0E0E0)
    ) {
        Box(
            modifier = Modifier.padding(vertical = 8.dp),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = text,
                color = if (isSelected) Color.White else Color(0xFF555555),
                fontSize = 11.sp,
                fontWeight = FontWeight.Bold
            )
        }
    }
}
