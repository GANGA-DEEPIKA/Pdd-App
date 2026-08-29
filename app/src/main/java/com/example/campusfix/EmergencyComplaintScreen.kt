package com.example.campusfix

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
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

@Composable
fun EmergencyComplaintScreen(onBackClick: () -> Unit) {
    var message by remember { mutableStateOf("") }
    var isLoading by remember { mutableStateOf(false) }
    val scope = rememberCoroutineScope()
    val context = androidx.compose.ui.platform.LocalContext.current

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
                Text("Emergency SOS", fontSize = 20.sp, fontWeight = FontWeight.Bold)
            }
        }

        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Surface(
                modifier = Modifier.size(140.dp),
                shape = CircleShape,
                color = Color(0xFFFFEBEE)
            ) {
                Box(contentAlignment = Alignment.Center) {
                    Text("🚨", fontSize = 64.sp)
                }
            }
            
            Spacer(modifier = Modifier.height(32.dp))
            
            Text(
                text = "Urgent Intervention Needed?",
                fontSize = 24.sp,
                fontWeight = FontWeight.Bold,
                color = Color.Red,
                textAlign = androidx.compose.ui.text.style.TextAlign.Center
            )
            
            Spacer(modifier = Modifier.height(16.dp))
            
            Text(
                text = "Clicking the button below will alert all campus security and maintenance supervisors immediately.",
                fontSize = 14.sp,
                color = Color.Gray,
                textAlign = androidx.compose.ui.text.style.TextAlign.Center,
                modifier = Modifier.padding(horizontal = 16.dp)
            )

            Spacer(modifier = Modifier.height(48.dp))

            if (isLoading) {
                CircularProgressIndicator(color = Color.Red)
            } else {
                Button(
                    onClick = {
                        isLoading = true
                        scope.launch {
                            val emergency = ComplaintData(
                                title = "EMERGENCY SOS ALERT",
                                details = "User triggered emergency SOS button from app.",
                                priority = "High",
                                category = "Security",
                                status = "Pending"
                            )
                            val result = FirestoreService.submitComplaint(context, emergency)
                            isLoading = false
                            if (result.isSuccess) {
                                message = "EMERGENCY ALERT SENT! Support is on the way. 🚨"
                                android.widget.Toast.makeText(context, "Emergency submitted! ✅", android.widget.Toast.LENGTH_SHORT).show()
                            } else {
                                message = "Failed to send alert. Please call campus security directly."
                                android.widget.Toast.makeText(context, "Submission failed! ❌", android.widget.Toast.LENGTH_SHORT).show()
                            }
                        }
                    },
                    modifier = Modifier.fillMaxWidth().height(70.dp),
                    shape = RoundedCornerShape(20.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = Color.Red)
                ) {
                    Text("SEND EMERGENCY ALERT", fontSize = 18.sp, fontWeight = FontWeight.ExtraBold)
                }
            }

            if (message.isNotEmpty()) {
                Spacer(modifier = Modifier.height(32.dp))
                Card(
                    colors = CardDefaults.cardColors(
                        containerColor = if (message.contains("SENT")) Color(0xFFE8F5E9) else Color(0xFFFFEBEE)
                    ),
                    shape = RoundedCornerShape(16.dp),
                    border = androidx.compose.foundation.BorderStroke(1.dp, if (message.contains("SENT")) Color(0xFF2E7D32) else Color.Red)
                ) {
                    Text(
                        text = message,
                        color = if (message.contains("SENT")) Color(0xFF2E7D32) else Color.Red,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.padding(20.dp),
                        textAlign = androidx.compose.ui.text.style.TextAlign.Center
                    )
                }
            }
        }
    }
}
