package com.example.campusfix

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
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
fun StudentDashboardScreen(
    complaintList: List<ComplaintData>,
    onFilterClick: (String) -> Unit,
    onNewComplaintClick: () -> Unit,
    onTrackClick: () -> Unit,
    onHistoryClick: () -> Unit,
    onEmergencyClick: () -> Unit,
    onAnonymousClick: () -> Unit,
    onNotificationClick: () -> Unit,
    onFeedbackClick: () -> Unit,
    onProfileClick: () -> Unit,
    onBackClick: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFFF5F7FA))
            .padding(horizontal = 16.dp, vertical = 8.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            IconButton(onClick = onBackClick) {
                Text("←", fontSize = 24.sp, fontWeight = FontWeight.Bold, color = Color(0xFF1565C0))
            }
            Spacer(modifier = Modifier.width(8.dp))
        }

        Spacer(modifier = Modifier.height(2.dp))
        
        Text(
            text = "Student Dashboard",
            fontSize = 26.sp,
            fontWeight = FontWeight.Bold,
            color = Color(0xFF1565C0)
        )
        Text(
            text = "Welcome back to CampusFix",
            fontSize = 14.sp,
            color = Color.Gray
        )

        Spacer(modifier = Modifier.height(10.dp))

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            SummaryCard(
                title = "Active", 
                value = "${complaintList.count { it.status == "Pending" || it.status == "In Progress" || it.status == "Assigned" || it.status.startsWith("Waiting") }}", 
                color = Color(0xFFFF9800), 
                modifier = Modifier.weight(1f).clickable { onFilterClick("active") }
            )
            SummaryCard(
                title = "Resolved", 
                value = "${complaintList.count { it.status == "Resolved" }}", 
                color = Color(0xFF4CAF50), 
                modifier = Modifier.weight(1f).clickable { onFilterClick("Resolved") }
            )
            SummaryCard(
                title = "Total", 
                value = "${complaintList.size}", 
                color = Color(0xFF1565C0), 
                modifier = Modifier.weight(1f).clickable { onFilterClick("all") }
            )
        }

        Spacer(modifier = Modifier.height(12.dp))

        Text(
            text = "Services",
            fontSize = 18.sp,
            fontWeight = FontWeight.Bold,
            modifier = Modifier.padding(bottom = 8.dp)
        )

        val menuItems = listOf(
            DashboardMenuItem("New Complaint", "📝", onNewComplaintClick),
            DashboardMenuItem("Track Status", "🔍", onTrackClick),
            DashboardMenuItem("History", "📚", onHistoryClick),
            DashboardMenuItem("Emergency", "🚨", onEmergencyClick),
            DashboardMenuItem("Anonymous", "🕵️", onAnonymousClick),
            DashboardMenuItem("Notifications", "🔔", onNotificationClick),
            DashboardMenuItem("Feedback", "⭐", onFeedbackClick),
            DashboardMenuItem("My Profile", "👤", onProfileClick)
        )

        LazyVerticalGrid(
            columns = GridCells.Fixed(2),
            horizontalArrangement = Arrangement.spacedBy(12.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp),
            modifier = Modifier.weight(1f)
        ) {
            items(menuItems) { item ->
                ServiceCard(item)
            }
        }
    }
}

data class DashboardMenuItem(val title: String, val icon: String, val onClick: () -> Unit)

@Composable
fun ServiceCard(item: DashboardMenuItem) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .height(98.dp)
            .clickable { item.onClick() },
        shape = RoundedCornerShape(18.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(6.dp),
            verticalArrangement = Arrangement.Center,
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Surface(
                modifier = Modifier.size(42.dp),
                shape = RoundedCornerShape(12.dp),
                color = Color(0xFFF5F7FA)
            ) {
                Box(contentAlignment = Alignment.Center) {
                    Text(item.icon, fontSize = 22.sp)
                }
            }
            Spacer(modifier = Modifier.height(6.dp))
            Text(
                text = item.title,
                fontSize = 12.sp,
                fontWeight = FontWeight.Bold,
                color = Color(0xFF333333),
                textAlign = androidx.compose.ui.text.style.TextAlign.Center
            )
        }
    }
}
