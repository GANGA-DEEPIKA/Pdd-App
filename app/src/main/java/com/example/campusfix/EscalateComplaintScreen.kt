package com.example.campusfix

import androidx.compose.foundation.background
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
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun EscalateComplaintScreen(
    complaintList: List<ComplaintData>,
    initialTicketId: String = "",
    onBackClick: () -> Unit
) {
    var selectedTicketId by remember { mutableStateOf(initialTicketId) }
    var expanded by remember { mutableStateOf(false) }
    var reason by remember { mutableStateOf("") }
    var message by remember { mutableStateOf("") }

    val selectedComplaint = complaintList.find { it.ticketId.trim() == selectedTicketId.trim() }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFFF5F7FA))
    ) {
        // Header
        Surface(
            modifier = Modifier.fillMaxWidth(),
            color = Color.White,
            shadowElevation = 2.dp
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
                Text("Escalate Complaint", fontSize = 18.sp, fontWeight = FontWeight.Bold)
            }
        }

        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp)
                .verticalScroll(rememberScrollState())
        ) {
            // Escalation Banner
            Surface(
                modifier = Modifier.fillMaxWidth(),
                color = Color(0xFFFFF3E0),
                shape = RoundedCornerShape(8.dp)
            ) {
                Row(
                    modifier = Modifier.padding(12.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text("⬆️", fontSize = 20.sp)
                    Spacer(modifier = Modifier.width(12.dp))
                    Column {
                        Text("Escalate to Admin", fontSize = 14.sp, fontWeight = FontWeight.Bold, color = Color(0xFFE65100))
                        Text("Use this when a complaint needs admin attention or has been unresolved for too long.", fontSize = 11.sp, color = Color(0xFFEF6C00))
                    }
                }
            }

            Spacer(modifier = Modifier.height(24.dp))

            // Select Complaint Section
            Text(
                "Select Complaint",
                fontSize = 12.sp,
                color = Color.Gray,
                modifier = Modifier.padding(bottom = 8.dp)
            )

            ExposedDropdownMenuBox(
                expanded = expanded,
                onExpandedChange = { expanded = !expanded },
                modifier = Modifier.fillMaxWidth()
            ) {
                OutlinedTextField(
                    value = selectedTicketId,
                    onValueChange = { 
                        selectedTicketId = it
                        expanded = true 
                    },
                    label = { Text("Complaint Ticket ID") },
                    placeholder = { Text("CMPXXXXXX") },
                    trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = expanded) },
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedTextColor = Color.Black,
                        unfocusedTextColor = Color.Black,
                        focusedBorderColor = Color(0xFFE65100),
                        unfocusedBorderColor = Color.LightGray,
                        focusedContainerColor = Color.White,
                        unfocusedContainerColor = Color.White
                    ),
                    modifier = Modifier.menuAnchor(ExposedDropdownMenuAnchorType.PrimaryEditable).fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp)
                )

                val filteredList = complaintList.filter { 
                    it.ticketId.contains(selectedTicketId, ignoreCase = true) || 
                    it.title.contains(selectedTicketId, ignoreCase = true) 
                }

                if (filteredList.isNotEmpty()) {
                    ExposedDropdownMenu(
                        expanded = expanded,
                        onDismissRequest = { expanded = false },
                        modifier = Modifier.background(Color.White)
                    ) {
                        filteredList.forEach { complaint ->
                            DropdownMenuItem(
                                text = { Text("${complaint.ticketId} — ${complaint.title}") },
                                onClick = {
                                    selectedTicketId = complaint.ticketId
                                    expanded = false
                                }
                            )
                        }
                    }
                }
            }

            // Complaint Details Card
            if (selectedComplaint != null) {
                Spacer(modifier = Modifier.height(16.dp))
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp),
                    colors = CardDefaults.cardColors(containerColor = Color.White),
                    elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Text(text = selectedComplaint.title, fontSize = 16.sp, fontWeight = FontWeight.Bold, color = Color.Black)
                        Text(text = "${selectedComplaint.category} • ${selectedComplaint.status}", fontSize = 12.sp, color = Color.Gray)
                    }
                }
            }

            Spacer(modifier = Modifier.height(32.dp))

            // Reason Section
            Text(
                "Reason for Escalation *",
                fontSize = 12.sp,
                color = Color.Gray,
                modifier = Modifier.padding(bottom = 8.dp)
            )

            OutlinedTextField(
                value = reason,
                onValueChange = { reason = it },
                placeholder = { Text("Why is this being escalated? (e.g. Parts not available, requires admin authorization...)") },
                modifier = Modifier.fillMaxWidth().height(150.dp),
                shape = RoundedCornerShape(12.dp),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedTextColor = Color.Black,
                    unfocusedTextColor = Color.Black,
                    focusedContainerColor = Color.White,
                    unfocusedContainerColor = Color.White
                )
            )

            Spacer(modifier = Modifier.height(32.dp))

            // Submit Button
            Button(
                onClick = {
                    if (selectedTicketId.isNotEmpty() && reason.isNotBlank()) {
                        message = "Issue escalated to Administration ✅"
                        android.os.Handler(android.os.Looper.getMainLooper()).postDelayed({
                            onBackClick()
                        }, 2000)
                    } else {
                        message = "Please select a ticket and enter reason"
                    }
                },
                modifier = Modifier.fillMaxWidth().height(56.dp),
                shape = RoundedCornerShape(12.dp),
                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFE65100)),
                enabled = selectedTicketId.isNotEmpty() && reason.isNotBlank()
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text("⬆️", fontSize = 16.sp)
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Escalate to Admin", fontSize = 16.sp, fontWeight = FontWeight.Bold)
                }
            }

            if (message.isNotEmpty()) {
                Text(
                    text = message,
                    modifier = Modifier.padding(top = 16.dp).fillMaxWidth(),
                    textAlign = androidx.compose.ui.text.style.TextAlign.Center,
                    color = if (message.contains("escalated")) Color(0xFFE65100) else Color.Red,
                    fontWeight = FontWeight.Bold
                )
            }
        }
    }
}
