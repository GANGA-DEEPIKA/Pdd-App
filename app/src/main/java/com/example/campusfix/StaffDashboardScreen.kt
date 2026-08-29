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
fun StaffDashboardScreen(
    onBackClick: () -> Unit,
    onFilterClick: (String) -> Unit,
    onAssignedClick: () -> Unit,
    onUpdateStatusClick: () -> Unit,
    onResolutionNotesClick: () -> Unit,
    onUploadProofClick: () -> Unit,
    onEscalateClick: () -> Unit,
    onPerformanceClick: () -> Unit,
    complaintList: List<ComplaintData>,
    staffInfo: StaffData = StaffData(
        name = "Robert Wilson",
        staffId = "EMP1024",
        department = "Facilities & Maintenance",
        email = "r.wilson@campus.edu",
        phone = "+1 234 567 890"
    )
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFFF5F7FA))
    ) {
        Surface(
            modifier = Modifier.fillMaxWidth(),
            color = Color(0xFF0D47A1),
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
                            text = "STAFF",
                            modifier = Modifier.padding(horizontal = 12.dp, vertical = 4.dp),
                            color = Color.White,
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
                
                Spacer(modifier = Modifier.height(16.dp))
                
                Text(
                    text = "Welcome, ${staffInfo.name}",
                    fontSize = 28.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color.White
                )
                
                val categoryText = if (staffInfo.assignedCategories.isNotEmpty()) 
                    "${staffInfo.assignedCategories.joinToString(", ")} Staff" 
                else staffInfo.department
                
                Text(
                    text = categoryText,
                    fontSize = 14.sp,
                    color = Color.White.copy(alpha = 0.8f)
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
                text = "Performance Overview",
                fontSize = 18.sp,
                fontWeight = FontWeight.Bold,
                color = Color.DarkGray
            )
            
            Spacer(modifier = Modifier.height(16.dp))

            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                SummaryCard("Assigned", "${complaintList.size}", Color(0xFF1565C0), Modifier.weight(1f).clickable { onFilterClick("all") })
                SummaryCard("In-Progress", "${complaintList.count { it.status == "In Progress" }}", Color(0xFFFF9800), Modifier.weight(1f).clickable { onFilterClick("In Progress") })
                SummaryCard("Resolved", "${complaintList.count { it.status == "Resolved" }}", Color(0xFF4CAF50), Modifier.weight(1f).clickable { onFilterClick("Resolved") })
            }

            Spacer(modifier = Modifier.height(32.dp))

            Text(
                text = "Actions",
                fontSize = 18.sp,
                fontWeight = FontWeight.Bold,
                color = Color.DarkGray
            )

            Spacer(modifier = Modifier.height(16.dp))

            StaffActionCard("Assigned Complaints", "View and process task list", "📂", onAssignedClick)
            StaffActionCard("Update Status", "Change ticket progress state", "🔄", onUpdateStatusClick)
            StaffActionCard("Resolution Notes", "Add details about the fix", "✍️", onResolutionNotesClick)
            StaffActionCard("Upload Proof", "Attach completion photos", "📸", onUploadProofClick)
            StaffActionCard("Escalate Issue", "Request additional support", "⬆️", onEscalateClick)
            StaffActionCard("Performance", "View your service metrics", "📊", onPerformanceClick)
            
            Spacer(modifier = Modifier.height(40.dp))
        }
    }
}

@Composable
fun StaffActionCard(title: String, subtitle: String, icon: String, onClick: () -> Unit) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(bottom = 12.dp),
        onClick = onClick,
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
    ) {
        Row(
            modifier = Modifier.padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Surface(
                modifier = Modifier.size(48.dp),
                shape = RoundedCornerShape(12.dp),
                color = Color(0xFFF5F7FA)
            ) {
                Box(contentAlignment = Alignment.Center) {
                    Text(text = icon, fontSize = 24.sp)
                }
            }
            Spacer(modifier = Modifier.width(16.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(text = title, fontSize = 16.sp, fontWeight = FontWeight.Bold)
                Text(text = subtitle, fontSize = 12.sp, color = Color.Gray)
            }
            Text("›", fontSize = 24.sp, color = Color.LightGray)
        }
    }
}
