package com.example.campusfix

import android.graphics.BitmapFactory
import android.util.Base64
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
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
                Text("My Profile", fontSize = 20.sp, fontWeight = FontWeight.Bold, modifier = Modifier.weight(1f))
                
                if (!isLoading && userData != null) {
                    TextButton(
                        onClick = {
                            onEditClick()
                        },
                        modifier = Modifier.padding(start = 8.dp)
                    ) {
                        Text("Edit", color = Color(0xFF1565C0), fontWeight = FontWeight.Bold, fontSize = 16.sp)
                    }
                }
            }
        }

        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(20.dp)
                .verticalScroll(rememberScrollState()),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Spacer(modifier = Modifier.height(24.dp))
            
            Surface(
                modifier = Modifier
                    .size(100.dp)
                    .clip(CircleShape),
                shape = CircleShape,
                color = Color(0xFFE3F2FD)
            ) {
                Box(contentAlignment = Alignment.Center) {
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

                    if (profileBitmap != null) {
                        Image(
                            bitmap = profileBitmap.asImageBitmap(),
                            contentDescription = "Profile Photo",
                            modifier = Modifier.fillMaxSize()
                        )
                    } else {
                        Text(text = "👤", fontSize = 50.sp)
                    }
                }
            }
            
            Spacer(modifier = Modifier.height(16.dp))
            
            Text(
                text = userData?.name ?: "User Name",
                fontSize = 24.sp,
                fontWeight = FontWeight.Bold,
                color = Color(0xFF1565C0)
            )
            Text(
                text = "Student ID: ${userData?.regNo ?: "N/A"}",
                fontSize = 14.sp,
                color = Color.Gray
            )

            Spacer(modifier = Modifier.height(32.dp))

            if (isLoading) {
                CircularProgressIndicator(color = Color(0xFF1565C0))
            } else if (userData != null) {
                ProfileCardM3("Full Name", userData?.name ?: "N/A", "👤")
                ProfileCardM3("Email Address", userData?.email ?: "N/A", "✉️")
                ProfileCardM3("Mobile Number", userData?.phone ?: "N/A", "📞")
                ProfileCardM3("College", userData?.collegeName ?: "N/A", "🏫")
                ProfileCardM3("Department", userData?.department ?: "N/A", "📚")
                ProfileCardM3("Academic Year", userData?.year ?: "N/A", "🗓️")
            } else {
                Text("Unable to load profile data", color = Color.Red)
            }
            
            Spacer(modifier = Modifier.height(40.dp))
            
            OutlinedButton(
                onClick = { auth.signOut(); onBackClick() },
                modifier = Modifier.fillMaxWidth().height(50.dp),
                shape = RoundedCornerShape(12.dp),
                colors = ButtonDefaults.outlinedButtonColors(contentColor = Color.Red)
            ) {
                Text("Logout from Device", fontWeight = FontWeight.Bold)
            }
            
            Spacer(modifier = Modifier.height(50.dp))
        }
    }
}
