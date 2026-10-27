package com.example.campusfix

import android.widget.Toast
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
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
fun ComplaintHistoryScreen(
    title: String = "Report History",
    complaintList: List<ComplaintData>,
    onComplaintClick: (ComplaintData) -> Unit,
    onBackClick: () -> Unit
) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    var deletingTicketId by remember { mutableStateOf<String?>(null) }

    if (deletingTicketId != null) {
        val targetTicket = deletingTicketId
        AlertDialog(
            onDismissRequest = { deletingTicketId = null },
            title = { Text("Delete Complaint", fontWeight = FontWeight.Bold) },
            text = { Text("Are you sure you want to delete complaint ticket $targetTicket?") },
            confirmButton = {
                TextButton(
                    onClick = {
                        val ticket = targetTicket ?: ""
                        deletingTicketId = null
                        scope.launch {
                            val result = FirestoreService.deleteComplaint(ticket)
                            if (result.isSuccess) {
                                Toast.makeText(context, "Complaint deleted successfully! 🗑️", Toast.LENGTH_SHORT).show()
                            } else {
                                Toast.makeText(context, "Failed to delete complaint", Toast.LENGTH_SHORT).show()
                            }
                        }
                    }
                ) {
                    Text("Delete", color = Color.Red, fontWeight = FontWeight.Bold)
                }
            },
            dismissButton = {
                TextButton(onClick = { deletingTicketId = null }) {
                    Text("Cancel")
                }
            }
        )
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
                Column {
                    Text(title, fontSize = 20.sp, fontWeight = FontWeight.Bold)
                    Text("${complaintList.size} complaints found", fontSize = 12.sp, color = Color.Gray)
                }
            }
        }

        if (complaintList.isEmpty()) {
            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                Text("No complaints found", color = Color.Gray)
            }
        } else {
            LazyColumn(
                contentPadding = PaddingValues(20.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                items(complaintList) { item ->
                    HistoryCardM3(
                        item = item,
                        onClick = { onComplaintClick(item) },
                        onDeleteClick = { deletingTicketId = item.ticketId }
                    )
                }
            }
        }
    }
}

@Composable
fun HistoryCardM3(
    item: ComplaintData,
    onClick: () -> Unit,
    onDeleteClick: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onClick() },
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp),
        border = BorderStroke(1.dp, Color(0xFFE5E7EB))
    ) {
        Column(modifier = Modifier.padding(20.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Surface(
                    color = Color(0xFFF5F7FA),
                    shape = RoundedCornerShape(8.dp)
                ) {
                    Text(
                        text = item.category,
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color.Gray
                    )
                }

                Text(
                    text = item.status,
                    fontSize = 13.sp,
                    fontWeight = FontWeight.Bold,
                    color = if (item.status.contains("Resolved")) Color(0xFF2E7D32) else Color(0xFF1565C0)
                )
            }

            Spacer(modifier = Modifier.height(12.dp))

            Text(
                text = item.title,
                fontSize = 18.sp,
                fontWeight = FontWeight.Bold,
                color = Color.Black
            )

            Spacer(modifier = Modifier.height(12.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Ticket: ${item.ticketId}",
                    fontSize = 12.sp,
                    color = Color.Gray
                )

                Surface(
                    color = Color(0xFFFFEBEE),
                    shape = RoundedCornerShape(8.dp),
                    modifier = Modifier.clickable { onDeleteClick() }
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 5.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text("🗑️ ", fontSize = 12.sp)
                        Text(
                            text = "Delete",
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color(0xFFDC2626)
                        )
                    }
                }
            }
        }
    }
}
