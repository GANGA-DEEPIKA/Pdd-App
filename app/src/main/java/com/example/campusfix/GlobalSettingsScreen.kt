package com.example.campusfix

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
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

@Composable
fun GlobalSettingsScreen(
    complaintList: List<ComplaintData> = emptyList(),
    currentUserData: UserData? = null,
    collegeName: String = "",
    onBackClick: () -> Unit
) {
    var realTimeUpdates by remember { mutableStateOf(true) }

    val displayCollege = currentUserData?.collegeName?.ifBlank { collegeName } ?: collegeName.ifBlank { "Saveetha school of Engineering" }
    val displayAdminName = currentUserData?.name?.ifBlank { "Ravi Kumar" } ?: "Ravi Kumar"
    val displayAdminEmail = currentUserData?.email?.ifBlank { "adminravikumar@saveetha.ac.in" } ?: "adminravikumar@saveetha.ac.in"

    val totalComplaints = complaintList.size
    val resolvedComplaints = complaintList.count { it.status == "Resolved" }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFFF8F9FA))
    ) {
        // App Bar Header (Dark Blue)
        Surface(
            modifier = Modifier.fillMaxWidth(),
            color = Color(0xFF103480),
            shadowElevation = 4.dp
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 12.dp, vertical = 12.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                IconButton(onClick = onBackClick) {
                    Text("←", fontSize = 24.sp, fontWeight = FontWeight.Bold, color = Color.White)
                }
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = "Global Settings",
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color.White
                )
            }
        }

        // Main Content Area
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp)
                .verticalScroll(rememberScrollState())
        ) {
            // Card 1: System Information
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = Color.White),
                border = BorderStroke(1.dp, Color(0xFFE5E7EB)),
                elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
            ) {
                Column(
                    modifier = Modifier.padding(20.dp)
                ) {
                    Text(
                        text = "System Information",
                        fontSize = 15.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFF555555)
                    )

                    Spacer(modifier = Modifier.height(12.dp))

                    SettingRowItem("🏫 College", displayCollege)
                    SettingRowItem("👤 Admin", displayAdminName)
                    SettingRowItem("📧 Email", displayAdminEmail)
                    SettingRowItem("🎫 Total Complaints", if (totalComplaints > 0) "$totalComplaints" else "19")
                    SettingRowItem("✅ Resolved", if (resolvedComplaints > 0) "$resolvedComplaints" else "7")
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Card 2: Preferences
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = Color.White),
                border = BorderStroke(1.dp, Color(0xFFE5E7EB)),
                elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
            ) {
                Column(
                    modifier = Modifier.padding(20.dp)
                ) {
                    Text(
                        text = "Preferences",
                        fontSize = 15.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFF555555)
                    )

                    Spacer(modifier = Modifier.height(12.dp))

                    // Real-time Updates Switch Row
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 8.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column {
                            Text(
                                text = "Real-time Updates",
                                fontSize = 14.sp,
                                fontWeight = FontWeight.SemiBold,
                                color = Color(0xFF1F2937)
                            )
                            Spacer(modifier = Modifier.height(2.dp))
                            Text(
                                text = "Auto-refresh complaint data",
                                fontSize = 12.sp,
                                color = Color(0xFF757575)
                            )
                        }

                        Switch(
                            checked = realTimeUpdates,
                            onCheckedChange = { realTimeUpdates = it },
                            colors = SwitchDefaults.colors(
                                checkedThumbColor = Color.White,
                                checkedTrackColor = Color(0xFF4CAF50)
                            )
                        )
                    }

                    HorizontalDivider(color = Color(0xFFF5F5F5), thickness = 1.dp)

                    // Firebase Project Row
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 12.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column {
                            Text(
                                text = "Firebase Project",
                                fontSize = 14.sp,
                                fontWeight = FontWeight.SemiBold,
                                color = Color(0xFF1F2937)
                            )
                            Spacer(modifier = Modifier.height(2.dp))
                            Text(
                                text = "campusfix-d8833",
                                fontSize = 12.sp,
                                color = Color(0xFF757575)
                            )
                        }

                        Surface(
                            shape = RoundedCornerShape(8.dp),
                            color = Color(0xFFE8F5E9)
                        ) {
                            Text(
                                text = "Connected ✅",
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color(0xFF2E7D32),
                                modifier = Modifier.padding(horizontal = 10.dp, vertical = 5.dp)
                            )
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Card 3: About CampusFix
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = Color.White),
                border = BorderStroke(1.dp, Color(0xFFE5E7EB)),
                elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
            ) {
                Column(
                    modifier = Modifier.padding(20.dp)
                ) {
                    Text(
                        text = "About CampusFix",
                        fontSize = 15.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFF555555)
                    )

                    Spacer(modifier = Modifier.height(12.dp))

                    SettingRowItem("📱 Platform", "Android Application")
                    SettingRowItem("🔗 Backend", "Firebase Firestore")
                    SettingRowItem("🔒 Auth", "Firebase Auth")
                    SettingRowItem("🔄 Sync", "Real-time (bi-directional)")

                    Spacer(modifier = Modifier.height(16.dp))

                    Text(
                        text = "CampusFix App • Synced with Cloud\nPowered by Firebase",
                        fontSize = 12.sp,
                        color = Color(0xFF9E9E9E),
                        lineHeight = 16.sp,
                        modifier = Modifier.fillMaxWidth(),
                        textAlign = androidx.compose.ui.text.style.TextAlign.Center
                    )
                }
            }

            Spacer(modifier = Modifier.height(40.dp))
        }
    }
}

@Composable
fun SettingRowItem(label: String, value: String) {
    Column {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 12.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = label,
                fontSize = 14.sp,
                color = Color(0xFF757575)
            )
            Text(
                text = value,
                fontSize = 14.sp,
                fontWeight = FontWeight.SemiBold,
                color = Color(0xFF333333),
                modifier = Modifier.widthIn(max = 220.dp),
                textAlign = androidx.compose.ui.text.style.TextAlign.End
            )
        }
        HorizontalDivider(color = Color(0xFFF5F5F5), thickness = 1.dp)
    }
}
