package com.example.campusfix

import android.graphics.BitmapFactory
import android.util.Base64
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.google.firebase.auth.FirebaseAuth
import kotlinx.coroutines.launch

@Composable
fun StudentProfileScreen(onBackClick: () -> Unit, onEditClick: () -> Unit) {
    val scope = rememberCoroutineScope()
    var userData by remember { mutableStateOf<UserData?>(null) }
    var isLoading by remember { mutableStateOf(true) }

    val auth = FirebaseAuth.getInstance()
    val currentUser = auth.currentUser

    LaunchedEffect(currentUser) {
        if (currentUser != null) {
            scope.launch {
                userData = FirestoreService.getUserData(currentUser.uid)
                isLoading = false
            }
        } else {
            isLoading = false
        }
    }

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
                    text = "My Profile",
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color.Black,
                    modifier = Modifier.weight(1f)
                )

                TextButton(
                    onClick = onEditClick,
                    enabled = true
                ) {
                    Text("Edit ✏️", color = Color(0xFF1565C0), fontWeight = FontWeight.Bold, fontSize = 14.sp)
                }
            }
        }

        // Main Content Area
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp)
                .verticalScroll(rememberScrollState()),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Spacer(modifier = Modifier.height(12.dp))

            // Profile Avatar (Clickable to Edit)
            val profilePhotoBase64 = userData?.profilePhoto ?: ""
            val profileBitmap = remember(profilePhotoBase64) {
                if (profilePhotoBase64.startsWith("base64:")) {
                    try {
                        val pureBase64 = profilePhotoBase64.removePrefix("base64:")
                        val decodedString = Base64.decode(pureBase64, Base64.DEFAULT)
                        BitmapFactory.decodeByteArray(decodedString, 0, decodedString.size)
                    } catch (e: Exception) {
                        null
                    }
                } else {
                    null
                }
            }

            Surface(
                modifier = Modifier
                    .size(90.dp)
                    .clip(CircleShape)
                    .clickable { onEditClick() },
                shape = CircleShape,
                color = Color(0xFF1565C0)
            ) {
                Box(contentAlignment = Alignment.Center) {
                    if (profileBitmap != null) {
                        Image(
                            bitmap = profileBitmap.asImageBitmap(),
                            contentDescription = "Profile Photo",
                            modifier = Modifier.fillMaxSize()
                        )
                    } else {
                        val initial = (userData?.name?.trim()?.take(1) ?: "U").uppercase()
                        Text(
                            text = initial,
                            fontSize = 36.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color.White
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            Text(
                text = userData?.name ?: "Student",
                fontSize = 22.sp,
                fontWeight = FontWeight.ExtraBold,
                color = Color(0xFF1F2937)
            )

            Spacer(modifier = Modifier.height(2.dp))

            Text(
                text = userData?.email ?: "",
                fontSize = 13.sp,
                color = Color(0xFF6B7280)
            )

            Spacer(modifier = Modifier.height(8.dp))

            Surface(
                shape = RoundedCornerShape(50),
                color = Color(0xFFE0F2FE)
            ) {
                Text(
                    text = "🎓 STUDENT",
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color(0xFF1565C0),
                    modifier = Modifier.padding(horizontal = 12.dp, vertical = 4.dp)
                )
            }

            Spacer(modifier = Modifier.height(20.dp))

            // Profile Information Card
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
                        text = "Profile Information",
                        fontSize = 15.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFF374151)
                    )

                    Spacer(modifier = Modifier.height(12.dp))

                    if (isLoading) {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(20.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            CircularProgressIndicator(color = Color(0xFF1565C0))
                        }
                    } else if (userData != null) {
                        ProfileRowItem("🎓 College", userData?.collegeName ?: "N/A")
                        ProfileRowItem("🆔 Register No", userData?.regNo ?: "N/A")
                        ProfileRowItem("📅 Year", userData?.year ?: "N/A")
                        ProfileRowItem("📱 Phone", userData?.phone ?: "N/A")
                        ProfileRowItem("🏢 Department", userData?.department ?: "N/A")
                    } else {
                        Text("Unable to load profile information", color = Color.Red, fontSize = 13.sp)
                    }
                }
            }

            Spacer(modifier = Modifier.height(28.dp))

            // Logout Button
            OutlinedButton(
                onClick = {
                    auth.signOut()
                    onBackClick()
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(50.dp),
                shape = RoundedCornerShape(12.dp),
                border = BorderStroke(1.dp, Color(0xFFEF4444)),
                colors = ButtonDefaults.outlinedButtonColors(
                    containerColor = Color.White,
                    contentColor = Color(0xFFDC2626)
                )
            ) {
                Text(
                    text = "Logout from Device",
                    fontSize = 15.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color(0xFFDC2626)
                )
            }

            Spacer(modifier = Modifier.height(40.dp))
        }
    }
}

@Composable
fun ProfileRowItem(label: String, value: String) {
    if (value.isBlank()) return
    Column {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 10.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = label,
                fontSize = 13.sp,
                color = Color(0xFF6B7280)
            )
            Text(
                text = value,
                fontSize = 13.sp,
                fontWeight = FontWeight.SemiBold,
                color = Color(0xFF1F2937),
                textAlign = TextAlign.End,
                modifier = Modifier.widthIn(max = 220.dp)
            )
        }
        HorizontalDivider(color = Color(0xFFF3F4F6), thickness = 1.dp)
    }
}
