package com.example.campusfix

import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.net.Uri
import android.util.Base64
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
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
    var isLoading by remember { mutableStateOf(true) }
    var isSaving by remember { mutableStateOf(false) }

    // Editable field states
    var name by remember { mutableStateOf(initialUserData?.name ?: "") }
    var phone by remember { mutableStateOf(initialUserData?.phone ?: "") }
    var year by remember { mutableStateOf(if (initialUserData?.year.isNullOrBlank()) "4th Year" else initialUserData!!.year) }
    var regNo by remember { mutableStateOf(initialUserData?.regNo ?: "") }
    var profilePhotoBase64 by remember { mutableStateOf(initialUserData?.profilePhoto ?: "") }

    var expandedYear by remember { mutableStateOf(false) }
    val yearOptions = listOf("1st Year", "2nd Year", "3rd Year", "4th Year", "PG")

    val auth = FirebaseAuth.getInstance()
    val db = FirebaseFirestore.getInstance()
    val currentUser = auth.currentUser

    // Image Picker Launcher
    val launcher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri: Uri? ->
        uri?.let {
            try {
                val inputStream = context.contentResolver.openInputStream(it)
                val originalBitmap = BitmapFactory.decodeStream(inputStream)

                if (originalBitmap != null) {
                    val scale = if (originalBitmap.width > originalBitmap.height) 400f / originalBitmap.width else 400f / originalBitmap.height
                    val scaledBitmap = if (scale < 1f) {
                        Bitmap.createScaledBitmap(originalBitmap, (originalBitmap.width * scale).toInt(), (originalBitmap.height * scale).toInt(), true)
                    } else originalBitmap

                    val outputStream = ByteArrayOutputStream()
                    scaledBitmap.compress(Bitmap.CompressFormat.JPEG, 70, outputStream)
                    val byteArray = outputStream.toByteArray()
                    profilePhotoBase64 = "base64:" + Base64.encodeToString(byteArray, Base64.DEFAULT)
                }
            } catch (e: Exception) {
                Toast.makeText(context, "Failed to load image", Toast.LENGTH_SHORT).show()
            }
        }
    }

    // Load fresh profile data on screen launch
    LaunchedEffect(Unit) {
        val uid = currentUser?.uid ?: initialUserData?.uid
        if (uid != null) {
            val fetched = FirestoreService.getUserData(uid)
            if (fetched != null) {
                userData = fetched
                if (fetched.name.isNotBlank()) name = fetched.name
                if (fetched.phone.isNotBlank()) phone = fetched.phone
                if (fetched.year.isNotBlank()) year = fetched.year
                if (fetched.regNo.isNotBlank()) regNo = fetched.regNo
                if (fetched.profilePhoto.isNotBlank()) profilePhotoBase64 = fetched.profilePhoto
            }
        }
        isLoading = false
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFFF8F9FA))
    ) {
        // Top App Bar
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
                    text = "Edit Profile",
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color.Black
                )
            }
        }

        if (isLoading) {
            Box(
                modifier = Modifier.fillMaxSize(),
                contentAlignment = Alignment.Center
            ) {
                CircularProgressIndicator(color = Color(0xFF1565C0))
            }
        } else {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(16.dp)
                    .verticalScroll(rememberScrollState()),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Spacer(modifier = Modifier.height(16.dp))

                // Profile Avatar with Camera Badge
                Box(
                    modifier = Modifier
                        .size(90.dp)
                        .clickable { launcher.launch("image/*") },
                    contentAlignment = Alignment.BottomEnd
                ) {
                    Surface(
                        modifier = Modifier
                            .fillMaxSize()
                            .clip(CircleShape),
                        shape = CircleShape,
                        color = Color(0xFF1565C0)
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
                                val initial = (name.trim().take(1).ifEmpty { "U" }).uppercase()
                                Text(
                                    text = initial,
                                    fontSize = 36.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = Color.White
                                )
                            }
                        }
                    }

                    Surface(
                        shape = CircleShape,
                        color = Color(0xFF1565C0),
                        border = BorderStroke(2.dp, Color.White),
                        modifier = Modifier.size(28.dp)
                    ) {
                        Box(contentAlignment = Alignment.Center) {
                            Text("📷", fontSize = 13.sp)
                        }
                    }
                }

                Spacer(modifier = Modifier.height(20.dp))

                // Edit Information Card
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(20.dp),
                    colors = CardDefaults.cardColors(containerColor = Color.White),
                    border = BorderStroke(1.dp, Color(0xFFE5E7EB)),
                    elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
                ) {
                    Column(
                        modifier = Modifier.padding(20.dp)
                    ) {
                        Text(
                            text = "Edit Information",
                            fontSize = 15.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color(0xFF374151)
                        )

                        Spacer(modifier = Modifier.height(16.dp))

                        // Full Name
                        Text(
                            text = "Full Name",
                            fontSize = 13.sp,
                            fontWeight = FontWeight.Medium,
                            color = Color(0xFF374151)
                        )
                        Spacer(modifier = Modifier.height(6.dp))
                        OutlinedTextField(
                            value = name,
                            onValueChange = { name = it },
                            singleLine = true,
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(10.dp),
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedBorderColor = Color(0xFF2563EB),
                                unfocusedBorderColor = Color(0xFFD1D5DB),
                                focusedContainerColor = Color.White,
                                unfocusedContainerColor = Color.White,
                                focusedTextColor = Color.Black,
                                unfocusedTextColor = Color.Black
                            )
                        )

                        Spacer(modifier = Modifier.height(16.dp))

                        // Phone
                        Text(
                            text = "Phone",
                            fontSize = 13.sp,
                            fontWeight = FontWeight.Medium,
                            color = Color(0xFF374151)
                        )
                        Spacer(modifier = Modifier.height(6.dp))
                        OutlinedTextField(
                            value = phone,
                            onValueChange = { phone = it },
                            singleLine = true,
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(10.dp),
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedBorderColor = Color(0xFF2563EB),
                                unfocusedBorderColor = Color(0xFFD1D5DB),
                                focusedContainerColor = Color.White,
                                unfocusedContainerColor = Color.White,
                                focusedTextColor = Color.Black,
                                unfocusedTextColor = Color.Black
                            )
                        )

                        Spacer(modifier = Modifier.height(16.dp))

                        // Year Dropdown
                        Text(
                            text = "Year",
                            fontSize = 13.sp,
                            fontWeight = FontWeight.Medium,
                            color = Color(0xFF374151)
                        )
                        Spacer(modifier = Modifier.height(6.dp))
                        Box(modifier = Modifier.fillMaxWidth()) {
                            OutlinedTextField(
                                value = year,
                                onValueChange = {},
                                readOnly = true,
                                modifier = Modifier.fillMaxWidth(),
                                shape = RoundedCornerShape(10.dp),
                                trailingIcon = {
                                    Text(
                                        text = if (expandedYear) "▲" else "▼",
                                        fontSize = 11.sp,
                                        color = Color.Gray,
                                        modifier = Modifier.clickable { expandedYear = !expandedYear }
                                    )
                                },
                                colors = OutlinedTextFieldDefaults.colors(
                                    focusedBorderColor = Color(0xFFD1D5DB),
                                    unfocusedBorderColor = Color(0xFFD1D5DB),
                                    focusedContainerColor = Color.White,
                                    unfocusedContainerColor = Color.White,
                                    focusedTextColor = Color.Black,
                                    unfocusedTextColor = Color.Black
                                )
                            )
                            Box(
                                modifier = Modifier
                                    .matchParentSize()
                                    .clickable { expandedYear = true }
                            )
                            DropdownMenu(
                                expanded = expandedYear,
                                onDismissRequest = { expandedYear = false },
                                modifier = Modifier
                                    .fillMaxWidth(0.85f)
                                    .background(Color.White)
                            ) {
                                yearOptions.forEach { option ->
                                    DropdownMenuItem(
                                        text = { Text(option, color = Color.Black) },
                                        onClick = {
                                            year = option
                                            expandedYear = false
                                        }
                                    )
                                }
                            }
                        }

                        Spacer(modifier = Modifier.height(16.dp))

                        // Register No
                        Text(
                            text = "Register No",
                            fontSize = 13.sp,
                            fontWeight = FontWeight.Medium,
                            color = Color(0xFF374151)
                        )
                        Spacer(modifier = Modifier.height(6.dp))
                        OutlinedTextField(
                            value = regNo,
                            onValueChange = { regNo = it },
                            singleLine = true,
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(10.dp),
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedBorderColor = Color(0xFF2563EB),
                                unfocusedBorderColor = Color(0xFFD1D5DB),
                                focusedContainerColor = Color.White,
                                unfocusedContainerColor = Color.White,
                                focusedTextColor = Color.Black,
                                unfocusedTextColor = Color.Black
                            )
                        )

                        Spacer(modifier = Modifier.height(24.dp))

                        // Save Changes Button
                        Button(
                            onClick = {
                                isSaving = true
                                scope.launch {
                                    try {
                                        val updates = mutableMapOf<String, Any>(
                                            "name" to name.trim(),
                                            "phone" to phone.trim(),
                                            "year" to year,
                                            "regNo" to regNo.trim()
                                        )
                                        if (profilePhotoBase64.isNotBlank()) {
                                            updates["profilePhoto"] = profilePhotoBase64
                                        }
                                        val uid = currentUser?.uid ?: initialUserData?.uid
                                        if (uid != null) {
                                            val collections = listOf("users", "approvedStaff", "pendingStaffRequests")
                                            for (col in collections) {
                                                try {
                                                    db.collection(col).document(uid).update(updates).await()
                                                } catch (e: Exception) {}
                                            }
                                            Toast.makeText(context, "Profile updated successfully! ✅", Toast.LENGTH_SHORT).show()
                                            onBackClick()
                                        }
                                    } catch (e: Exception) {
                                        Toast.makeText(context, "Update failed: ${e.message}", Toast.LENGTH_SHORT).show()
                                    } finally {
                                        isSaving = false
                                    }
                                }
                            },
                            enabled = !isSaving,
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(52.dp),
                            shape = RoundedCornerShape(12.dp),
                            colors = ButtonDefaults.buttonColors(
                                containerColor = Color(0xFF1565C0)
                            )
                        ) {
                            if (isSaving) {
                                CircularProgressIndicator(color = Color.White, modifier = Modifier.size(24.dp))
                            } else {
                                Text(
                                    text = "Save Changes",
                                    fontSize = 16.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = Color.White
                                )
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(40.dp))
            }
        }
    }
}
