package com.example.campusfix

import android.content.Context
import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.onFocusChanged
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.google.firebase.auth.FirebaseAuth
import kotlinx.coroutines.launch

@Composable
fun LoginScreen(
    title: String,
    emailHint: String,
    collegeName: String,
    onLogin: () -> Unit,
    onForgotPassword: () -> Unit,
    onBackClick: () -> Unit,
    onRegisterClick: () -> Unit
) {
    var email by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var passwordVisible by remember { mutableStateOf(false) }
    var savePassword by remember { mutableStateOf(true) }
    var showSavedBox by remember { mutableStateOf(false) }
    var errorMessage by remember { mutableStateOf("") }
    var isSuccess by remember { mutableStateOf(false) }
    var isLoading by remember { mutableStateOf(false) }

    val context = LocalContext.current
    val auth = FirebaseAuth.getInstance()
    val scope = rememberCoroutineScope()
    val sharedPref = context.getSharedPreferences("login_data", Context.MODE_PRIVATE)
    var lastFocusState by remember { mutableStateOf(false) }

    val savedAccountsSet = sharedPref.getStringSet("saved_accounts", emptySet()) ?: emptySet()
    
    val rolePrefix = when {
        title.contains("Student") -> "student"
        title.contains("Staff") -> "staff"
        title.contains("Admin") -> "admin"
        else -> ""
    }

    // Filter accounts that match the current role (student/staff/admin)
    val relevantAccounts = savedAccountsSet.filter { it.startsWith("$rolePrefix|") }

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

        Text(
            text = collegeName,
            color = Color.White.copy(alpha = 0.9f),
            fontSize = 18.sp,
            fontWeight = FontWeight.Medium,
            textAlign = androidx.compose.ui.text.style.TextAlign.Center
        )
        
        Text(
            text = title,
            color = Color.White,
            fontSize = 32.sp,
            fontWeight = FontWeight.Bold
        )

        Spacer(modifier = Modifier.height(40.dp))

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
                    value = email,
                    onValueChange = { email = it },
                    label = { Text(emailHint) },
                    modifier = Modifier
                        .fillMaxWidth()
                        .onFocusChanged { focusState ->
                            if (focusState.isFocused && relevantAccounts.isNotEmpty()) {
                                showSavedBox = true
                            }
                        },
                    shape = RoundedCornerShape(12.dp),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedTextColor = Color.Black,
                        unfocusedTextColor = Color.Black,
                        focusedBorderColor = Color(0xFF1565C0),
                        focusedLabelColor = Color(0xFF1565C0)
                    )
                )

                if (showSavedBox && relevantAccounts.isNotEmpty()) {
                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .heightIn(max = 200.dp)
                            .padding(top = 8.dp),
                        shape = RoundedCornerShape(12.dp),
                        elevation = CardDefaults.cardElevation(4.dp),
                        colors = CardDefaults.cardColors(containerColor = Color(0xFFF5F7FA))
                    ) {
                        LazyColumn(modifier = Modifier.padding(8.dp)) {
                            item {
                                Text("Saved Accounts", fontWeight = FontWeight.Bold, modifier = Modifier.padding(8.dp), fontSize = 14.sp)
                            }
                            items(relevantAccounts) { account ->
                                val parts = account.split("|")
                                if (parts.size >= 3) {
                                    ListItem(
                                        headlineContent = { Text(parts[1], fontSize = 14.sp) }, // parts[1] is email
                                        modifier = Modifier.clickable {
                                            email = parts[1]
                                            password = parts[2]
                                            showSavedBox = false
                                        },
                                        colors = ListItemDefaults.colors(containerColor = Color.Transparent)
                                    )
                                }
                            }
                            item {
                                TextButton(onClick = { showSavedBox = false }, modifier = Modifier.align(Alignment.End)) {
                                    Text("Dismiss", color = Color(0xFF1565C0))
                                }
                            }
                        }
                    }
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
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedTextColor = Color.Black,
                        unfocusedTextColor = Color.Black,
                        focusedBorderColor = Color(0xFF1565C0),
                        focusedLabelColor = Color(0xFF1565C0)
                    )
                )

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Checkbox(
                        checked = savePassword,
                        onCheckedChange = { savePassword = it },
                        colors = CheckboxDefaults.colors(checkedColor = Color(0xFF1565C0))
                    )
                    Text("Remember me", color = Color.DarkGray, fontSize = 14.sp)
                }

                Spacer(modifier = Modifier.height(24.dp))

                if (isLoading) {
                    CircularProgressIndicator(color = Color(0xFF1565C0))
                } else {
                    Button(
                        onClick = {
                            if (email.isBlank() || password.isBlank()) {
                                errorMessage = "Required fields missing"
                                return@Button
                            }
                            
                            val trimmedEmail = email.trim().lowercase()
                            val trimmedPassword = password.trim()

                            // Intelligent College Email Validation
                            val collegeLower = collegeName.lowercase()
                            val domainKeywords = listOf("saveetha", "srm", "vit", "annauniv", "iit", "nit")
                            
                            var isCorrectCollege = domainKeywords.any { 
                                collegeLower.contains(it) && trimmedEmail.contains(it) 
                            }
                            
                            if (!isCorrectCollege) {
                                val noiseWords = listOf("school", "engineering", "college", "university", "institute", "technology", "clg", "univ", "of", "and", "the", "for")
                                val collegeKeywords = collegeLower
                                    .split(" ", ",", ".", "-")
                                    .filter { it.length > 2 && it !in noiseWords }
                                
                                isCorrectCollege = collegeKeywords.any { trimmedEmail.contains(it) } || collegeKeywords.isEmpty()
                            }

                            if (!isCorrectCollege && collegeName.isNotBlank()) {
                                errorMessage = "Use only $collegeName mail"
                                return@Button
                            }

                            isLoading = true
                            auth.signInWithEmailAndPassword(trimmedEmail, trimmedPassword)
                                .addOnCompleteListener { task ->
                                    isLoading = false
                                    if (task.isSuccessful) {
                                        val userId = auth.currentUser?.uid ?: ""
                                        scope.launch {
                                            val user = FirestoreService.getUserData(userId)
                                            if (user != null) {
                                                if (user.role == "staff" && !user.isApproved) {
                                                    auth.signOut()
                                                    errorMessage = "Access Denied. Waiting for Admin Approval."
                                                    isSuccess = false
                                                } else if (rolePrefix.isNotEmpty() && user.role.isNotEmpty() && !user.role.equals(rolePrefix, ignoreCase = true)) {
                                                    auth.signOut()
                                                    errorMessage = "Role Mismatch: This account is registered as ${user.role.replaceFirstChar { it.uppercase() }}."
                                                    isSuccess = false
                                                } else {
                                                    if (savePassword) {
                                                        val currentSaved = sharedPref.getStringSet("saved_accounts", emptySet()) ?: emptySet()
                                                        val updated = currentSaved.toMutableSet()
                                                        // Format: rolePrefix|email|password
                                                        updated.add("$rolePrefix|${trimmedEmail}|${trimmedPassword}")
                                                        sharedPref.edit().putStringSet("saved_accounts", updated).apply()
                                                    }
                                                    isSuccess = true
                                                    errorMessage = "Login is successful"
                                                    Toast.makeText(context, "Login is successful", Toast.LENGTH_SHORT).show()
                                                    onLogin()
                                                }
                                            } else {
                                                auth.signOut()
                                                errorMessage = "User profile not found. Please register."
                                            }
                                        }
                                    } else {
                                        isLoading = false
                                        val exception = task.exception
                                        val rawMsg = exception?.message ?: "Login failed"
                                        
                                        // Precise Network Error Handling
                                        if (exception is com.google.firebase.FirebaseNetworkException) {
                                            errorMessage = "Network Error: Please check your WiFi connection or DNS settings."
                                        } else if (rawMsg.contains("re-captcha", true)) {
                                            errorMessage = "Security check failed. Please try again."
                                        } else {
                                            errorMessage = rawMsg
                                        }
                                        
                                        isSuccess = false
                                        android.util.Log.e("LoginError", "Error: $rawMsg", exception)
                                    }
                                }
                        },
                        modifier = Modifier.fillMaxWidth().height(56.dp),
                        shape = RoundedCornerShape(12.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF1565C0))
                    ) {
                        Text("Login", fontSize = 18.sp, fontWeight = FontWeight.Bold)
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(24.dp))

        TextButton(onClick = onForgotPassword) {
            Text("Forgot Password?", color = Color.White, fontSize = 16.sp)
        }

        Row(verticalAlignment = Alignment.CenterVertically) {
            Text("Don't have an account?", color = Color.White.copy(alpha = 0.8f))
            TextButton(onClick = onRegisterClick) {
                Text("Register Now", color = Color.White, fontWeight = FontWeight.Bold)
            }
        }

        if (errorMessage.isNotEmpty()) {
            Spacer(modifier = Modifier.height(16.dp))
            Card(
                colors = CardDefaults.cardColors(
                    containerColor = if (isSuccess) Color(0xFFE8F5E9) else Color(0xFFFFEBEE)
                ),
                shape = RoundedCornerShape(8.dp)
            ) {
                Text(
                    text = errorMessage,
                    color = if (isSuccess) Color(0xFF2E7D32) else Color.Red,
                    modifier = Modifier.padding(12.dp),
                    fontSize = 14.sp,
                    textAlign = androidx.compose.ui.text.style.TextAlign.Center
                )
            }
        }
        
        Spacer(modifier = Modifier.height(50.dp))
    }
}
