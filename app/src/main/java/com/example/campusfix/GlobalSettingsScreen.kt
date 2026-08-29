package com.example.campusfix

import android.content.Context
import android.widget.Toast
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
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import java.io.File

@Composable
fun GlobalSettingsScreen(onBackClick: () -> Unit) {
    val context = LocalContext.current
    var notificationsEnabled by remember { mutableStateOf(true) }
    var highPriorityOnly by remember { mutableStateOf(false) }
    var autoAssignStaff by remember { mutableStateOf(true) }
    
    var showTermsDialog by remember { mutableStateOf(false) }
    var showHelpDialog by remember { mutableStateOf(false) }

    // Logic to clear app cache
    fun clearAppCache(context: Context) {
        try {
            val cacheDir = context.cacheDir
            val externalCacheDir = context.externalCacheDir
            
            fun deleteDir(dir: File?): Boolean {
                return dir?.deleteRecursively() ?: false
            }
            
            deleteDir(cacheDir)
            deleteDir(externalCacheDir)
            
            Toast.makeText(context, "Cache cleared successfully! ✅", Toast.LENGTH_SHORT).show()
        } catch (e: Exception) {
            Toast.makeText(context, "Error clearing cache", Toast.LENGTH_SHORT).show()
        }
    }

    if (showTermsDialog) {
        AlertDialog(
            onDismissRequest = { showTermsDialog = false },
            confirmButton = {
                TextButton(onClick = { showTermsDialog = false }) { Text("I Accept") }
            },
            title = { Text("Terms of Service", fontWeight = FontWeight.Bold) },
            text = {
                Text(
                    "By using CampusFix, you agree to report genuine campus issues. " +
                    "Abuse of the SOS or Anonymous features will lead to account suspension. " +
                    "Your data is used strictly for maintenance management.",
                    fontSize = 14.sp
                )
            }
        )
    }

    if (showHelpDialog) {
        AlertDialog(
            onDismissRequest = { showHelpDialog = false },
            confirmButton = {
                TextButton(onClick = { showHelpDialog = false }) { Text("Close") }
            },
            title = { Text("CampusFix Support", fontWeight = FontWeight.Bold) },
            text = {
                Column {
                    Text("Need help? Contact our tech team:", fontSize = 14.sp)
                    Spacer(modifier = Modifier.height(8.dp))
                    Text("📧 Email: support@campusfix.edu", fontWeight = FontWeight.Bold)
                    Text("📞 Helpline: +91 98765 43210", fontWeight = FontWeight.Bold)
                    Spacer(modifier = Modifier.height(12.dp))
                    Text("Visit the server room for manual technical support.", fontSize = 12.sp, color = Color.Gray)
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
                Text("Global Settings", fontSize = 20.sp, fontWeight = FontWeight.Bold)
            }
        }

        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(20.dp)
                .verticalScroll(rememberScrollState())
        ) {
            Text(
                "App Configuration",
                fontWeight = FontWeight.Bold,
                color = Color.DarkGray,
                modifier = Modifier.padding(bottom = 16.dp)
            )

            SettingsToggleCard("Push Notifications", "Receive alerts for new reports", notificationsEnabled) {
                notificationsEnabled = it
                // In a real app, this would update SharedPreferences or Firebase Messaging
            }

            SettingsToggleCard("Critical Only", "Only notify for High priority tasks", highPriorityOnly) {
                highPriorityOnly = it
            }

            SettingsToggleCard("Auto-Assignment", "Enable smart staff matching", autoAssignStaff) {
                autoAssignStaff = it
            }

            Spacer(modifier = Modifier.height(24.dp))

            Text(
                "Support & Legal",
                fontWeight = FontWeight.Bold,
                color = Color.DarkGray,
                modifier = Modifier.padding(bottom = 16.dp)
            )

            SettingsActionCard("Clear App Cache", "Free up storage space", "🧹") {
                clearAppCache(context)
            }
            
            SettingsActionCard("Terms of Service", "Read our user agreement", "📋") {
                showTermsDialog = true
            }
            
            SettingsActionCard("Help Center", "Get support and guidance", "❓") {
                showHelpDialog = true
            }
            
            Spacer(modifier = Modifier.height(40.dp))
        }
    }
}

@Composable
fun SettingsToggleCard(title: String, subtitle: String, checked: Boolean, onCheckedChange: (Boolean) -> Unit) {
    Card(
        modifier = Modifier.fillMaxWidth().padding(bottom = 12.dp),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
    ) {
        Row(
            modifier = Modifier.padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(title, fontWeight = FontWeight.Bold, fontSize = 15.sp)
                Text(subtitle, fontSize = 12.sp, color = Color.Gray)
            }
            Switch(
                checked = checked,
                onCheckedChange = onCheckedChange,
                colors = SwitchDefaults.colors(checkedThumbColor = Color(0xFF1565C0))
            )
        }
    }
}

@Composable
fun SettingsActionCard(title: String, subtitle: String, icon: String, onClick: () -> Unit) {
    Card(
        modifier = Modifier.fillMaxWidth().padding(bottom = 12.dp).clickable { onClick() },
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
    ) {
        Row(
            modifier = Modifier.padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(icon, fontSize = 20.sp)
            Spacer(modifier = Modifier.width(16.dp))
            Column {
                Text(title, fontWeight = FontWeight.Bold, fontSize = 15.sp)
                Text(subtitle, fontSize = 12.sp, color = Color.Gray)
            }
        }
    }
}
