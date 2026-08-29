package com.example.campusfix

import android.widget.Toast
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
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseAuthUserCollisionException
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.tasks.await
import kotlinx.coroutines.launch

@Composable
fun RegisterScreen(
    title: String,
    emailHint: String,
    collegeName: String,
    onRegisterClick: () -> Unit,
    onBackClick: () -> Unit
) {
    var name by remember { mutableStateOf("") }
    var email by remember { mutableStateOf("") }
    var phone by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var passwordVisible by remember { mutableStateOf(false) }
    var department by remember { mutableStateOf("") }
    var regNo by remember { mutableStateOf("") }
    var year by remember { mutableStateOf("") }
    
    var message by remember { mutableStateOf("") }
    var isSuccess by remember { mutableStateOf(false) }
    var isLoading by remember { mutableStateOf(false) }
    val scope = rememberCoroutineScope()

    val context = LocalContext.current
    val auth = FirebaseAuth.getInstance()
    val db = FirebaseFirestore.getInstance()

    val rolePrefix = when {
        title.contains("Student") -> "student"
        title.contains("Staff") -> "staff"
        title.contains("Admin") -> "admin"
        else -> ""
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFF1565C0))
            .padding(24.dp)
            .verticalScroll(rememberScrollState()),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            IconButton(onClick = onBackClick) {
                Text(text = "←", color = Color.White, fontSize = 28.sp)
            }
        }

        Spacer(modifier = Modifier.height(20.dp))

        Text(collegeName, color = Color.White.copy(alpha = 0.9f), fontSize = 18.sp)
        Text(title, color = Color.White, fontSize = 32.sp, fontWeight = FontWeight.Bold)

        Spacer(modifier = Modifier.height(30.dp))

        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(24.dp),
            colors = CardDefaults.cardColors(containerColor = Color.White),
            elevation = CardDefaults.cardElevation(defaultElevation = 8.dp)
        ) {
            Column(
                modifier = Modifier.padding(24.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                OutlinedTextField(
                    value = name,
                    onValueChange = { name = it },
                    label = { Text("Full Name") },
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp),
                    singleLine = true,
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedTextColor = Color.Black,
                        unfocusedTextColor = Color.Black,
                        focusedBorderColor = Color(0xFF1565C0),
                        focusedLabelColor = Color(0xFF1565C0)
                    )
                )

                Spacer(modifier = Modifier.height(16.dp))

                OutlinedTextField(
                    value = email,
                    onValueChange = { email = it },
                    label = { Text(emailHint) },
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp),
                    singleLine = true,
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedTextColor = Color.Black,
                        unfocusedTextColor = Color.Black,
                        focusedBorderColor = Color(0xFF1565C0),
                        focusedLabelColor = Color(0xFF1565C0)
                    )
                )

                Spacer(modifier = Modifier.height(16.dp))

                OutlinedTextField(
                    value = phone,
                    onValueChange = { phone = it },
                    label = { Text("Mobile Number") },
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp),
                    singleLine = true,
                    keyboardOptions = androidx.compose.foundation.text.KeyboardOptions(
                        keyboardType = androidx.compose.ui.text.input.KeyboardType.Phone
                    ),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedTextColor = Color.Black,
                        unfocusedTextColor = Color.Black,
                        focusedBorderColor = Color(0xFF1565C0),
                        focusedLabelColor = Color(0xFF1565C0)
                    )
                )

                if (rolePrefix == "student") {
                    Spacer(modifier = Modifier.height(16.dp))
                    OutlinedTextField(
                        value = regNo,
                        onValueChange = { regNo = it },
                        label = { Text("Registration Number") },
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(12.dp),
                        singleLine = true,
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedTextColor = Color.Black,
                            unfocusedTextColor = Color.Black,
                            focusedBorderColor = Color(0xFF1565C0),
                            focusedLabelColor = Color(0xFF1565C0)
                        )
                    )
                    Spacer(modifier = Modifier.height(16.dp))
                    OutlinedTextField(
                        value = department,
                        onValueChange = { department = it },
                        label = { Text("Department") },
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(12.dp),
                        singleLine = true,
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedTextColor = Color.Black,
                            unfocusedTextColor = Color.Black,
                            focusedBorderColor = Color(0xFF1565C0),
                            focusedLabelColor = Color(0xFF1565C0)
                        )
                    )
                    Spacer(modifier = Modifier.height(16.dp))
                    OutlinedTextField(
                        value = year,
                        onValueChange = { year = it },
                        label = { Text("Year (e.g. 3rd Year)") },
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(12.dp),
                        singleLine = true,
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedTextColor = Color.Black,
                            unfocusedTextColor = Color.Black,
                            focusedBorderColor = Color(0xFF1565C0),
                            focusedLabelColor = Color(0xFF1565C0)
                        )
                    )
                }

                Spacer(modifier = Modifier.height(16.dp))

                OutlinedTextField(
                    value = password,
                    onValueChange = { password = it },
                    label = { Text("Password") },
                    visualTransformation = if (passwordVisible) VisualTransformation.None else PasswordVisualTransformation(),
                    trailingIcon = {
                        IconButton(onClick = { passwordVisible = !passwordVisible }) {
                            Text(text = if (passwordVisible) "👁️" else "🙈", fontSize = 20.sp)
                        }
                    },
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp),
                    singleLine = true,
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedTextColor = Color.Black,
                        unfocusedTextColor = Color.Black,
                        focusedBorderColor = Color(0xFF1565C0),
                        focusedLabelColor = Color(0xFF1565C0)
                    )
                )

                Spacer(modifier = Modifier.height(32.dp))

                if (isLoading) {
                    CircularProgressIndicator(color = Color(0xFF1565C0))
                } else {
                    Button(
                        onClick = {
                            if (name.isBlank() || email.isBlank() || phone.isBlank() || password.isBlank() || (rolePrefix == "student" && (department.isBlank() || regNo.isBlank()))) {
                                message = "Please fill all fields"
                                isSuccess = false
                                return@Button
                            }
                            
                            val trimmedEmail = email.trim().lowercase()
                            val trimmedPassword = password.trim()
                            val trimmedPhone = phone.trim()

                            if (!trimmedEmail.startsWith(rolePrefix)) {
                                message = "Use your college $rolePrefix email"
                                isSuccess = false
                                return@Button
                            }

                            // Robust Multi-College Validation Logic
                            val collegeWords = collegeName.lowercase()
                                .split(" ", "-", ".")
                                .filter { it.length > 2 && it !in listOf("school", "college", "engineering", "university", "institute", "technology", "dept", "department", "and", "the", "for", "of") }
                            
                            var isCorrectCollege = collegeWords.any { trimmedEmail.contains(it) } || collegeWords.isEmpty()

                            if (!isCorrectCollege && collegeName.isNotBlank()) {
                                message = "Use only $collegeName mail"
                                isSuccess = false
                                return@Button
                            }

                            if (trimmedPassword.length < 6) {
                                message = "Password must be at least 6 characters"
                                isSuccess = false
                                return@Button
                            }

                            isLoading = true
                            auth.createUserWithEmailAndPassword(trimmedEmail, trimmedPassword)
                                .addOnCompleteListener { task ->
                                    if (task.isSuccessful) {
                                        saveUserData(auth.currentUser?.uid ?: "", name, trimmedEmail, trimmedPhone, rolePrefix, department, regNo, collegeName, year, scope, db)
                                    } else if (task.exception is com.google.firebase.auth.FirebaseAuthUserCollisionException) {
                                        // Email exists - update phone number in all possible collections
                                        scope.launch {
                                            try {
                                                val collections = listOf("users", "approvedStaff", "pendingStaffRequests")
                                                var updated = false
                                                for (col in collections) {
                                                    val query = db.collection(col).whereEqualTo("email", trimmedEmail).get().await()
                                                    if (!query.isEmpty) {
                                                        db.collection(col).document(query.documents[0].id).update("phone", trimmedPhone).await()
                                                        updated = true
                                                    }
                                                }
                                                
                                                if (updated) {
                                                    message = "Profile updated with mobile number! ✅"
                                                    isSuccess = true
                                                } else {
                                                    message = "Account exists. Please use Forgot Password."
                                                }
                                                isLoading = false
                                            } catch (e: Exception) {
                                                isLoading = false
                                                message = "Update failed: ${e.message}"
                                            }
                                        }
                                    } else {
                                        isLoading = false
                                        val rawMsg = task.exception?.message ?: "Registration failed"
                                        message = rawMsg
                                        isSuccess = false
                                    }
                                }
                        },
                        modifier = Modifier.fillMaxWidth().height(56.dp),
                        shape = RoundedCornerShape(12.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF1565C0))
                    ) {
                        Text("Create Account", fontSize = 18.sp, fontWeight = FontWeight.Bold)
                    }
                }
            }
        }

        if (message.isNotEmpty()) {
            Spacer(modifier = Modifier.height(16.dp))
            Card(
                colors = CardDefaults.cardColors(
                    containerColor = if (isSuccess) Color(0xFFE8F5E9) else Color(0xFFFFEBEE)
                ),
                shape = RoundedCornerShape(8.dp)
            ) {
                Column(
                    modifier = Modifier.padding(12.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(
                        text = message, 
                        color = if (isSuccess) Color(0xFF2E7D32) else Color.Red,
                        fontSize = 14.sp,
                        textAlign = androidx.compose.ui.text.style.TextAlign.Center
                    )
                    if (isSuccess) {
                        Spacer(modifier = Modifier.height(8.dp))
                        TextButton(onClick = onRegisterClick) {
                            Text("Go to Login", color = Color(0xFF1565C0), fontWeight = FontWeight.Bold)
                        }
                    }
                }
            }
        }
        
        Spacer(modifier = Modifier.height(50.dp))
    }
}

// Helper to keep code clean
private fun saveUserData(userId: String, name: String, email: String, phone: String, role: String, dept: String, reg: String, college: String, year: String, scope: kotlinx.coroutines.CoroutineScope, db: FirebaseFirestore) {
    val user = UserData(uid = userId, name = name, email = email, phone = phone, role = role, department = dept, regNo = reg, collegeName = college.trim(), year = year, isApproved = (role != "staff"))
    scope.launch {
        try {
            if (role == "staff") {
                FirestoreService.createPendingStaffRequest(user)
            } else {
                val data = mapOf("uid" to userId, "name" to name, "email" to email, "phone" to phone, "role" to role, "department" to dept, "regNo" to reg, "collegeName" to college.trim(), "college" to college.trim(), "year" to year, "approved" to true)
                db.collection("users").document(userId).set(data).await()
            }
        } catch (e: Exception) {}
    }
}
