package com.example.campusfix

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

@Composable
fun AdminDashboardScreen(
    collegeName: String,
    complaintList: List<ComplaintData>,
    onFilterClick: (String) -> Unit,
    onBackClick: () -> Unit,
    onManageStudentsClick: () -> Unit,
    onStaffApprovalClick: () -> Unit,
    onAnalyticsClick: () -> Unit,
    onSettingsClick: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFFF5F7FA))
    ) {
        Surface(
            modifier = Modifier.fillMaxWidth(),
            color = Color(0xFF1A237E),
            shadowElevation = 8.dp
        ) {
            Column(modifier = Modifier.padding(24.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    IconButton(onClick = onBackClick) {
                        Text("←", color = Color.White, fontSize = 24.sp, fontWeight = FontWeight.Bold)
                    }
                    Surface(
                        shape = RoundedCornerShape(8.dp),
                        color = Color.White.copy(alpha = 0.2f)
                    ) {
                        Text(
                            text = "ADMIN: $collegeName",
                            modifier = Modifier.padding(horizontal = 12.dp, vertical = 4.dp),
                            color = Color.White,
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
                
                Spacer(modifier = Modifier.height(16.dp))
                
                Text(
                    text = "Control Center",
                    fontSize = 30.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color.White
                )
            }
        }

        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(20.dp)
                .verticalScroll(rememberScrollState())
        ) {
            Text(
                text = "College Statistics",
                fontSize = 18.sp,
                fontWeight = FontWeight.Bold,
                color = Color.DarkGray
            )
            
            Spacer(modifier = Modifier.height(16.dp))

            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                AdminStatCard("Total Tickets", "${complaintList.size}", Color(0xFF1565C0), Modifier.weight(1f).clickable { onFilterClick("all") })
                AdminStatCard("Pending", "${complaintList.count { it.status == "Pending" || it.status == "Assigned" || it.status.startsWith("Waiting") }}", Color(0xFFFF9800), Modifier.weight(1f).clickable { onFilterClick("Pending") })
            }
            
            Spacer(modifier = Modifier.height(12.dp))

            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                AdminStatCard("In Progress", "${complaintList.count { it.status == "In Progress" }}", Color(0xFF7B1FA2), Modifier.weight(1f).clickable { onFilterClick("In Progress") })
                AdminStatCard("Resolved", "${complaintList.count { it.status == "Resolved" }}", Color(0xFF4CAF50), Modifier.weight(1f).clickable { onFilterClick("Resolved") })
            }

            Spacer(modifier = Modifier.height(32.dp))

            Text(
                text = "Administration",
                fontSize = 18.sp,
                fontWeight = FontWeight.Bold,
                color = Color.DarkGray
            )

            Spacer(modifier = Modifier.height(16.dp))

            AdminActionRow("Manage Students", "View, add or remove users", "👥", onManageStudentsClick)
            AdminActionRow("Manage Staff", "Assign teams and departments", "👷", onStaffApprovalClick)
            AdminActionRow("System Analytics", "Generate performance reports", "📈", onAnalyticsClick)
            AdminActionRow("Global Settings", "App-wide configuration", "⚙️", onSettingsClick)
            
            Spacer(modifier = Modifier.height(40.dp))
        }
    }
}

@Composable
fun AdminStatCard(label: String, value: String, color: Color, modifier: Modifier) {
    Card(
        modifier = modifier,
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = color.copy(alpha = 0.08f))
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(value, fontSize = 26.sp, fontWeight = FontWeight.ExtraBold, color = color)
            Text(label, fontSize = 12.sp, fontWeight = FontWeight.Bold, color = Color.Gray)
        }
    }
}

@Composable
fun AdminActionRow(title: String, subtitle: String, icon: String, onClick: () -> Unit) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(bottom = 12.dp)
            .clickable { onClick() },
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
    ) {
        Row(
            modifier = Modifier.padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Surface(
                modifier = Modifier.size(44.dp),
                shape = RoundedCornerShape(10.dp),
                color = Color(0xFFF5F7FA)
            ) {
                Box(contentAlignment = Alignment.Center) {
                    Text(text = icon, fontSize = 22.sp)
                }
            }
            Spacer(modifier = Modifier.width(16.dp))
            Column {
                Text(text = title, fontSize = 15.sp, fontWeight = FontWeight.Bold)
                Text(text = subtitle, fontSize = 12.sp, color = Color.Gray)
            }
        }
    }
}
