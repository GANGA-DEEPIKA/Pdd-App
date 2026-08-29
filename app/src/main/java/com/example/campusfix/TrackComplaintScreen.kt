package com.example.campusfix

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
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
fun TrackComplaintScreen(onBackClick: () -> Unit) {
    val activeTickets = listOf(
        TrackItem("CMP1025", "Pending", "WiFi connection issue in Library"),
        TrackItem("CMP1026", "In Progress", "Water leakage in Block C"),
        TrackItem("CMP1027", "Resolved", "Broken chair in Hall 4")
    )

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
                Text("Search Tickets", fontSize = 20.sp, fontWeight = FontWeight.Bold)
            }
        }

        LazyColumn(
            contentPadding = PaddingValues(20.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            item {
                Text(
                    text = "Active Tickets",
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color.DarkGray
                )
                Spacer(modifier = Modifier.height(8.dp))
            }
            
            items(activeTickets) { item ->
                TrackCardM3(item)
            }
        }
    }
}

data class TrackItem(val id: String, val status: String, val desc: String)

@Composable
fun TrackCardM3(item: TrackItem) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
    ) {
        Column(modifier = Modifier.padding(20.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = item.id,
                    fontSize = 18.sp,
                    fontWeight = FontWeight.ExtraBold,
                    color = Color(0xFF1565C0)
                )
                
                val color = when(item.status) {
                    "Resolved" -> Color(0xFF4CAF50)
                    "In Progress" -> Color(0xFFFF9800)
                    else -> Color(0xFF1565C0)
                }
                
                Surface(
                    color = color.copy(alpha = 0.1f),
                    shape = RoundedCornerShape(8.dp)
                ) {
                    Text(
                        text = item.status,
                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp),
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold,
                        color = color
                    )
                }
            }
            
            Spacer(modifier = Modifier.height(12.dp))
            
            Text(
                text = item.desc,
                fontSize = 15.sp,
                color = Color.DarkGray,
                lineHeight = 20.sp
            )
            
            Spacer(modifier = Modifier.height(16.dp))
            
            LinearProgressIndicator(
                progress = {
                    when(item.status) {
                        "Resolved" -> 1.0f
                        "In Progress" -> 0.6f
                        else -> 0.2f
                    }
                },
                modifier = Modifier.fillMaxWidth().height(6.dp).background(Color(0xFFEEEEEE), RoundedCornerShape(3.dp)),
                color = when(item.status) {
                    "Resolved" -> Color(0xFF4CAF50)
                    "In Progress" -> Color(0xFFFF9800)
                    else -> Color(0xFF1565C0)
                }
            )
        }
    }
}
