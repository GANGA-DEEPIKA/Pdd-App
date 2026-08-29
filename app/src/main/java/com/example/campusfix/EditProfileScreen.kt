package com.example.campusfix

import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.net.Uri
import android.util.Base64
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
import java.io.ByteArrayOutputStream

@Composable
fun EditProfileScreen(
    initialUserData: UserData?,
    onBackClick: () -> Unit
) {
    val scope = rememberCoroutineScope()
    val context = LocalContext.current
    var userData by remember { mutableStateOf<UserData?>(initialUserData) }
    var isLoading by remember { mutableStateOf(initialUserData == null) }
    var isSaving by remember { mutableStateOf(false) }

    // Editable states initialized from passed data
    var name by remember { mutableStateOf(initialUserData?.name ?: "") }
    var phone by remember { mutableStateOf(initialUserData?.phone ?: "") }
    var department by remember { mutableStateOf(initialUserData?.department ?: "") }
    var year by remember { mutableStateOf(initialUserData?.year ?: "") }
    var profilePhotoBase64 by remember { mutableStateOf(initialUserData?.profilePhoto ?: "") }

    val auth = FirebaseAuth.getInstance()
    val db = FirebaseFirestore.getInstance()
    val currentUser = auth.currentUser

    // Image Picker
    val launcher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri: Uri? ->
        uri?.let {
            try {
                val inputStream = context.contentResolver.openInputStream(it)
                val originalBitmap = BitmapFactory.decodeStream(inputStream)
                
                // Scale to fit Firestore limits
                val scale = if (originalBitmap.width > originalBitmap.height) 400f / originalBitmap.width else 400f / originalBitmap.height
                val scaledBitmap = if (scale < 1f) {
                    Bitmap.createScaledBitmap(originalBitmap, (originalBitmap.width * scale).toInt(), (originalBitmap.height * scale).toInt(), true)
                } else originalBitmap

                val outputStream = ByteArrayOutputStream()
                scaledBitmap.compress(Bitmap.CompressFormat.JPEG, 70, outputStream)
                val byteArray = outputStream.toByteArray()
                profilePhotoBase64 = "base64:" + Base64.encodeToString(byteArray, Base64.DEFAULT)
            } catch (e: Exception) {
                Toast.makeText(context, "Failed to load image", Toast.LENGTH_SHORT).show()
            }
        }
    }

    LaunchedEffect(currentUser) {
        if (currentUser != null && initialUserData == null) {
            scope.launch {
                userData = FirestoreService.getUserData(currentUser.uid)
                userData?.let {
                    name = it.name
                    phone = it.phone
                    department = it.department
                    year = it.year
                    profilePhotoBase64 = it.profilePhoto
                }
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
                Text("Edit Profile", fontSize = 20.sp, fontWeight = FontWeight.Bold, modifier = Modifier.weight(1f))
                
                if (!isLoading && userData != null) {
                    if (isSaving) {
                        CircularProgressIndicator(modifier = Modifier.size(24.dp), strokeWidth = 2.dp)
                    } else {
                        TextButton(onClick = {
                            isSaving = true
                            scope.launch {
                                try {
                                    val updates = mapOf(
                                        "name" to name,
                                        "phone" to phone,
                                        "department" to department,
                                        "year" to year,
                                        "profilePhoto" to profilePhotoBase64
                                    )
                                    val collections = listOf("users", "approvedStaff", "pendingStaffRequests")
                                    for (col in collections) {
                                        try {
                                            db.collection(col).document(currentUser!!.uid).update(updates).await()
                                        } catch (e: Exception) {}
                                    }
                                    Toast.makeText(context, "Profile Updated! ✅", Toast.LENGTH_SHORT).show()
                                    onBackClick()
                                } catch (e: Exception) {
                                    Toast.makeText(context, "Update failed: ${e.message}", Toast.LENGTH_SHORT).show()
                                } finally {
                                    isSaving = false
                                }
                            }
                        }) {
                            Text("Save", color = Color(0xFF1565C0), fontWeight = FontWeight.Bold)
                        }
                    }
                }
            }
        }

        if (isLoading) {
            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                CircularProgressIndicator(color = Color(0xFF1565C0))
            }
        } else {
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
                        .clip(CircleShape)
                        .clickable {
                            launcher.launch("image/*")
                        },
                    shape = CircleShape,
                    color = Color(0xFFE3F2FD)
                ) {
                    Box(contentAlignment = Alignment.Center) {
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
                        
                        Box(
                            modifier = Modifier
                                .fillMaxSize()
                                .background(Color.Black.copy(alpha = 0.3f)),
                            contentAlignment = Alignment.Center
                        ) {
                            Text("Change", color = Color.White, fontSize = 12.sp, fontWeight = FontWeight.Bold)
                        }
                    }
                }
                
                Spacer(modifier = Modifier.height(32.dp))

                EditProfileField("Full Name", name, "👤") { name = it }
                ProfileCardM3("Email Address", userData?.email ?: "N/A", "✉️") 
                EditProfileField("Mobile Number", phone, "📞") { phone = it }
                ProfileCardM3("College", userData?.collegeName ?: "N/A", "🏫")
                EditProfileField("Department", department, "📚") { department = it }
                EditProfileField("Academic Year", year, "🗓️") { year = it }
                
                Spacer(modifier = Modifier.height(50.dp))
            }
        }
    }
}
