package com.example.campusfix

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

@Composable
fun NotificationScreen(
    complaintList: List<ComplaintData> = emptyList(),
    onBackClick: () -> Unit
) {
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
                    text = "Notifications",
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color.Black,
                    modifier = Modifier.weight(1f)
                )

                if (complaintList.isNotEmpty()) {
                    Surface(
                        shape = CircleShape,
                        color = Color(0xFFEF4444),
                        modifier = Modifier.padding(end = 8.dp)
                    ) {
                        Text(
                            text = "${complaintList.size}",
                            color = Color.White,
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold,
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp)
                        )
                    }
                }
            }
        }

        if (complaintList.isEmpty()) {
            Box(
                modifier = Modifier.fillMaxSize(),
                contentAlignment = Alignment.Center
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text("🔔", fontSize = 48.sp)
                    Spacer(modifier = Modifier.height(12.dp))
                    Text(
                        "No Notifications Yet",
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFF374151)
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        "Your complaint status updates will appear here.",
                        fontSize = 13.sp,
                        color = Color(0xFF9CA3AF)
                    )
                }
            }
        } else {
            LazyColumn(
                contentPadding = PaddingValues(16.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                items(complaintList) { complaint ->
                    NotificationCardItem(complaint)
                }
            }
        }
    }
}

@Composable
fun NotificationCardItem(complaint: ComplaintData) {
    val status = complaint.status
    val icon = when (status) {
        "Resolved" -> "✅"
        "In Progress" -> "🔧"
        "Assigned" -> "👤"
        else -> "📝"
    }

    val message = when (status) {
        "Resolved" -> "Your complaint \"${complaint.title}\" has been resolved!"
        "In Progress" -> "Staff is working on \"${complaint.title}\""
        "Assigned" -> "\"${complaint.title}\" has been assigned to staff"
        else -> "\"${complaint.title}\" is pending"
    }

    val badgeBg = when (status) {
        "Resolved" -> Color(0xFFDCFCE7)
        "In Progress" -> Color(0xFFFFEDD5)
        "Assigned" -> Color(0xFFE0F2FE)
        else -> Color(0xFFFEF3C7)
    }

    val badgeTextColor = when (status) {
        "Resolved" -> Color(0xFF15803D)
        "In Progress" -> Color(0xFFC2410C)
        "Assigned" -> Color(0xFF1D4ED8)
        else -> Color(0xFFB45309)
    }

    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        border = BorderStroke(1.dp, Color(0xFFE5E7EB)),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
    ) {
        Row(
            modifier = Modifier.padding(16.dp),
            verticalAlignment = Alignment.Top
        ) {
            Surface(
                shape = CircleShape,
                color = Color(0xFFF3F4F6),
                modifier = Modifier.size(40.dp)
            ) {
                Box(contentAlignment = Alignment.Center) {
                    Text(text = icon, fontSize = 20.sp)
                }
            }

            Spacer(modifier = Modifier.width(12.dp))

            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = message,
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color(0xFF1F2937)
                )

                Spacer(modifier = Modifier.height(6.dp))

                Row(
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Surface(
                        shape = RoundedCornerShape(50),
                        color = badgeBg
                    ) {
                        Text(
                            text = if (status.isNotBlank()) status else "Pending",
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            color = badgeTextColor,
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 2.dp)
                        )
                    }

                    Spacer(modifier = Modifier.width(8.dp))

                    Text(
                        text = formatComplaintDate(complaint.createdAt),
                        fontSize = 12.sp,
                        color = Color(0xFF9CA3AF)
                    )
                }

                if (complaint.ticketId.isNotBlank()) {
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = "✔ ${complaint.ticketId}",
                        fontSize = 11.sp,
                        color = Color(0xFF9CA3AF)
                    )
                }
            }
        }
    }
}
