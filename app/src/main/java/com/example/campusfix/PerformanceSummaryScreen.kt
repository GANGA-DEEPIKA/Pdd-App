package com.example.campusfix

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.tasks.await

@Composable
fun PerformanceSummaryScreen(onBackClick: () -> Unit) {
    var feedbacks by remember { mutableStateOf<List<Map<String, Any>>>(emptyList()) }

    LaunchedEffect(Unit) {
        try {
            val snapshot = FirebaseFirestore.getInstance().collection("feedbacks")
                .orderBy("timestamp", com.google.firebase.firestore.Query.Direction.DESCENDING)
                .limit(10)
                .get()
                .await()
            feedbacks = snapshot.documents.mapNotNull { it.data }
        } catch (e: Exception) {
            // Error handling
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
                Text("Performance & Feedback", fontSize = 20.sp, fontWeight = FontWeight.Bold)
            }
        }

        LazyColumn(
            modifier = Modifier.fillMaxSize().padding(20.dp)
        ) {
            item {
                Text(
                    text = "Metric Overview",
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color.DarkGray,
                    modifier = Modifier.padding(bottom = 20.dp)
                )

                PerformanceCard("Resolved Tasks", "24", "Total work completed", Color(0xFF4CAF50))
                PerformanceCard("Avg. Response", "2 Hours", "Quick turnaround", Color(0xFF1565C0))
                
                Spacer(modifier = Modifier.height(24.dp))
                
                Text(
                    text = "Recent Student Feedback",
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color.DarkGray,
                    modifier = Modifier.padding(bottom = 16.dp)
                )
            }

            if (feedbacks.isEmpty()) {
                item {
                    Text("No feedback received yet", color = Color.Gray, fontSize = 14.sp)
                }
            } else {
                items(feedbacks) { data ->
                    FeedbackItemCard(data)
                }
            }
            
            item { Spacer(modifier = Modifier.height(40.dp)) }
        }
    }
}

@Composable
fun FeedbackItemCard(data: Map<String, Any>) {
    val rating = (data["rating"] as? Long)?.toInt() ?: 0
    val comments = data["comments"] as? String ?: ""
    val user = data["userEmail"] as? String ?: "Anonymous"

    Card(
        modifier = Modifier.fillMaxWidth().padding(bottom = 12.dp),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(1.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text("⭐".repeat(rating), fontSize = 14.sp)
                Spacer(modifier = Modifier.weight(1f))
                Text(user, fontSize = 10.sp, color = Color.Gray)
            }
            Spacer(modifier = Modifier.height(8.dp))
            Text(comments, fontSize = 14.sp, color = Color.DarkGray)
        }
    }
}

@Composable
fun PerformanceCard(title: String, value: String, subtitle: String, color: Color) {
    Card(
        modifier = Modifier.fillMaxWidth().padding(bottom = 16.dp),
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
    ) {
        Row(
            modifier = Modifier.padding(20.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(text = title, fontSize = 14.sp, fontWeight = FontWeight.Bold, color = Color.Gray)
                Text(text = subtitle, fontSize = 11.sp, color = Color.LightGray)
                Spacer(modifier = Modifier.height(4.dp))
                Text(text = value, fontSize = 22.sp, fontWeight = FontWeight.ExtraBold, color = color)
            }
            Surface(
                modifier = Modifier.size(40.dp),
                shape = RoundedCornerShape(10.dp),
                color = color.copy(alpha = 0.1f)
            ) {
                Box(contentAlignment = Alignment.Center) {
                    Text("📈", fontSize = 20.sp)
                }
            }
        }
    }
}
