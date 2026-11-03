package com.example.campusfix

import android.os.Bundle
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.BackHandler
import androidx.activity.compose.setContent
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Surface
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import com.example.campusfix.ui.theme.CampusFixTheme
import com.google.firebase.auth.FirebaseAuth
import kotlinx.coroutines.launch

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            CampusFixTheme {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = Color.White
                ) {
                    CampusFixApp()
                }
            }
        }
    }
}

@Composable
fun CampusFixApp() {
    val context = androidx.compose.ui.platform.LocalContext.current
    val scope = rememberCoroutineScope()
    
    var screen by remember { mutableStateOf("college") }
    val navigationStack = remember { mutableStateListOf<String>() }

    var collegeName by remember { mutableStateOf("") }
    var complaintData by remember { mutableStateOf(ComplaintData()) }
    var complaintList by remember { mutableStateOf(listOf<ComplaintData>()) }
    var currentUserData by remember { mutableStateOf<UserData?>(null) }
    var filterType by remember { mutableStateOf("all") } 

    // Auth State Listener
    DisposableEffect(Unit) {
        val auth = FirebaseAuth.getInstance()
        val listener = FirebaseAuth.AuthStateListener { firebaseAuth ->
            val user = firebaseAuth.currentUser
            if (user != null) {
                scope.launch {
                    try {
                        currentUserData = FirestoreService.getUserData(user.uid)
                    } catch (_: Exception) {
                    }
                }
            } else {
                currentUserData = null
            }
        }
        auth.addAuthStateListener(listener)
        onDispose {
            auth.removeAuthStateListener(listener)
        }
    }

    // Real-time backend connection
    LaunchedEffect(currentUserData) {
        if (currentUserData != null) {
            try {
                FirestoreService.getComplaintsRealTime(currentUserData).collect { list ->
                    complaintList = list
                }
            } catch (e: Exception) {
                // Handle collection errors
            }
        } else {
            complaintList = emptyList()
        }
    }

    fun navigateTo(newScreen: String) {
        if (screen != newScreen) {
            navigationStack.add(screen)
            screen = newScreen
        }
    }
    
    fun goBack() {
        if (navigationStack.isNotEmpty()) {
            screen = navigationStack.removeAt(navigationStack.size - 1)
        } else {
            if (screen != "college") screen = "college"
        }
    }

    BackHandler(enabled = screen != "college") {
        goBack()
    }

    // Safety net: White background box to ensure something always renders
    Box(modifier = Modifier.fillMaxSize().background(Color.White)) {
        when (screen) {
            "college" -> CollegeSelectionScreen { name ->
                collegeName = name
                navigateTo("role")
            }

        "role" -> RoleSelectionScreen(
            onStudentClick = { navigateTo("login") },
            onStaffClick = { navigateTo("staffLogin") },
            onAdminClick = { navigateTo("adminLogin") }
        )

        "login" -> LoginScreen(
            title = "Student Sign In",
            emailHint = "Student College Email",
            collegeName = collegeName,
            onLogin = { 
                navigateTo("studentDashboard") 
            },
            onForgotPassword = { navigateTo("forgot") },
            onBackClick = { goBack() },
            onRegisterClick = { navigateTo("studentRegister") }
        )

        "staffLogin" -> LoginScreen(
            title = "Staff Sign In",
            emailHint = "Staff College Email",
            collegeName = collegeName,
            onLogin = { 
                navigateTo("staffDashboard") 
            },
            onForgotPassword = { navigateTo("forgot") },
            onBackClick = { goBack() },
            onRegisterClick = { navigateTo("staffRegister") }
        )

        "adminLogin" -> LoginScreen(
            title = "Admin Sign In",
            emailHint = "Admin College Email",
            collegeName = collegeName,
            onLogin = { 
                navigateTo("adminDashboard")
            },
            onForgotPassword = { navigateTo("forgot") },
            onBackClick = { goBack() },
            onRegisterClick = { navigateTo("adminRegister") }
        )

        "studentRegister" -> RegisterScreen(
            title = "Student Register",
            emailHint = "Student College Email",
            collegeName = collegeName,
            onRegisterClick = { goBack() }, 
            onBackClick = { goBack() }
        )

        "staffRegister" -> RegisterScreen(
            title = "Staff Register",
            emailHint = "Staff College Email",
            collegeName = collegeName,
            onRegisterClick = { goBack() },
            onBackClick = { goBack() }
        )

        "adminRegister" -> RegisterScreen(
            title = "Admin Register",
            emailHint = "Admin College Email",
            collegeName = collegeName,
            onRegisterClick = { goBack() },
            onBackClick = { goBack() }
        )

        "forgot" -> ForgotPasswordScreen(collegeName = collegeName) { goBack() }

        "studentDashboard" -> StudentDashboardScreen(
            complaintList = complaintList,
            onFilterClick = { filter ->
                filterType = filter
                navigateTo("complaintHistory")
            },
            onNewComplaintClick = { navigateTo("newComplaint") },
            onTrackClick = { navigateTo("complaintStatus") },
            onHistoryClick = { 
                filterType = "all"
                navigateTo("complaintHistory")
            },
            onEmergencyClick = { navigateTo("emergencyComplaint") },
            onAnonymousClick = { navigateTo("anonymousComplaint") },
            onNotificationClick = { navigateTo("notification") },
            onFeedbackClick = { navigateTo("feedback") },
            onProfileClick = { navigateTo("studentProfile") },
            onBackClick = { goBack() }
        )

        "staffDashboard" -> StaffDashboardScreen(
            complaintList = complaintList,
            onFilterClick = { filter ->
                filterType = filter
                navigateTo("assignedComplaints")
            },
            staffInfo = StaffData(
                name = currentUserData?.name ?: "Staff",
                department = currentUserData?.department ?: "Maintenance",
                email = currentUserData?.email ?: "",
                collegeName = currentUserData?.collegeName ?: "",
                assignedCategories = currentUserData?.assignedCategories ?: emptyList()
            ),
            onBackClick = {
                goBack()
            },
            onAssignedClick = {
                filterType = "all"
                navigateTo("assignedComplaints")
            },
            onUpdateStatusClick = { navigateTo("updateStatus") },
            onResolutionNotesClick = { navigateTo("resolutionNotes") },
            onUploadProofClick = { navigateTo("uploadProof") },
            onEscalateClick = { navigateTo("escalateComplaint") },
            onPerformanceClick = { navigateTo("performanceSummary") },
            onDuplicateGroupsClick = { navigateTo("duplicateGroups") }
        )

        "adminDashboard" -> AdminDashboardScreen(
            collegeName = currentUserData?.collegeName ?: collegeName,
            complaintList = complaintList,
            onFilterClick = { filter ->
                filterType = filter
                navigateTo("assignedComplaints")
            },
            onBackClick = { 
                goBack()
            },
            onManageStudentsClick = { navigateTo("manageStudents") },
            onStaffApprovalClick = { navigateTo("staffApproval") },
            onAnalyticsClick = { navigateTo("performanceSummary") },
            onSettingsClick = { navigateTo("globalSettings") }
        )

        "newComplaint" -> NewComplaintScreen(
            userCollege = currentUserData?.collegeName ?: collegeName,
            onBackClick = { goBack() },
            onReviewClick = { complaint ->
                complaintData = complaint
                navigateTo("reviewComplaint")
            }
        )

        "reviewComplaint" -> ReviewComplaintScreen(
            complaint = complaintData,
            onBackClick = { goBack() },
            onViewExistingClick = { existing ->
                complaintData = existing
                screen = "complaintStatus"
            },
            onSubmitClick = {
                try {
                    val result = FirestoreService.submitComplaint(context, complaintData)
                    if (result.isSuccess) {
                        val submitted = result.getOrNull() ?: complaintData.copy(createdAt = java.util.Date())
                        complaintData = submitted.copy(
                            createdAt = submitted.createdAt ?: java.util.Date()
                        )
                        Toast.makeText(context, "Complaint submitted successfully! ✅", Toast.LENGTH_LONG).show()
                        if (navigationStack.isNotEmpty()) {
                            navigationStack.removeAt(navigationStack.size - 1)
                        }
                        screen = "complaintStatus"
                    } else {
                        val error = result.exceptionOrNull()
                        val errorMsg = error?.message ?: "Submission failed"
                        
                        if (errorMsg.contains("Sync delay", true) || 
                            errorMsg.contains("indexing", true) || 
                            errorMsg.contains("processing", true) ||
                            errorMsg.contains("timeout", true)) {
                            Toast.makeText(context, "Server delay. Please wait 5 seconds and tap 'Confirm' again. ⏳", Toast.LENGTH_LONG).show()
                        } else {
                            Toast.makeText(context, "Error: $errorMsg ❌", Toast.LENGTH_LONG).show()
                        }
                    }
                } catch (e: Exception) {
                    Toast.makeText(context, "Unexpected Error: ${e.message}", Toast.LENGTH_LONG).show()
                }
            }
        )

        "complaintStatus" -> ComplaintStatusScreen(
            complaint = complaintData,
            onBackClick = { 
                complaintData = ComplaintData()
                goBack() 
            }
        )

        "complaintHistory" -> ComplaintHistoryScreen(
            title = when(filterType) {
                "Resolved" -> "Resolved Complaints"
                "active" -> "Active Complaints"
                else -> "All Complaints"
            },
            complaintList = when(filterType) {
                "Resolved" -> complaintList.filter { it.status == "Resolved" }
                "active" -> complaintList.filter { it.status != "Resolved" }
                else -> complaintList
            },
            onComplaintClick = { complaint ->
                complaintData = complaint
                navigateTo("complaintStatus")
            },
            onBackClick = { goBack() }
        )

        "emergencyComplaint" -> EmergencyComplaintScreen(onBackClick = { goBack() })
        "anonymousComplaint" -> AnonymousComplaintScreen(onBackClick = { goBack() })
        "notification" -> NotificationScreen(complaintList = complaintList, onBackClick = { goBack() })
        "feedback" -> FeedbackScreen(collegeName = currentUserData?.collegeName ?: collegeName) { goBack() }
        "studentProfile" -> StudentProfileScreen(
            onBackClick = { goBack() },
            onEditClick = { 
                Toast.makeText(context, "Opening Edit Page...", Toast.LENGTH_SHORT).show()
                navigateTo("editProfile") 
            }
        )
        "editProfile" -> EditProfileScreen(
            initialUserData = currentUserData,
            onBackClick = { goBack() }
        )

        "assignedComplaints" -> AssignedComplaintsScreen(
            title = when(filterType) {
                "Resolved" -> "Resolved Tasks"
                "Pending" -> "Pending Tasks"
                "In Progress" -> "In Progress"
                else -> "All Tasks"
            },
            complaintList = when(filterType) {
                "Resolved" -> complaintList.filter { it.status == "Resolved" }
                "Pending" -> complaintList.filter { it.status == "Pending" || it.status == "Assigned" || it.status.startsWith("Waiting") }
                "In Progress" -> complaintList.filter { it.status == "In Progress" }
                else -> complaintList
            },
            onComplaintClick = { complaint ->
                complaintData = complaint
                navigateTo("updateStatus")
            },
            onBackClick = { goBack() }
        )

        "duplicateGroups" -> DuplicateGroupsScreen(
            complaintList = complaintList,
            onBackClick = { goBack() }
        )

        "updateStatus" -> UpdateStatusScreen(
            complaintList = complaintList,
            initialTicketId = complaintData.ticketId,
            onBackClick = { goBack() }
        )

        "resolutionNotes" -> ResolutionNotesScreen(
            complaintList = complaintList,
            initialTicketId = complaintData.ticketId,
            onBackClick = { goBack() }
        )

        "uploadProof" -> UploadProofScreen(
            complaintList = complaintList,
            initialTicketId = complaintData.ticketId,
            onBackClick = { goBack() }
        )

        "escalateComplaint" -> EscalateComplaintScreen(
            complaintList = complaintList,
            initialTicketId = complaintData.ticketId,
            onBackClick = { goBack() }
        )
        "performanceSummary" -> PerformanceSummaryScreen(complaintList = complaintList, onBackClick = { goBack() })
        "globalSettings" -> GlobalSettingsScreen(complaintList = complaintList, currentUserData = currentUserData, collegeName = collegeName, onBackClick = { goBack() })
        "staffApproval" -> StaffApprovalScreen(
            college = currentUserData?.collegeName ?: collegeName,
            onBackClick = { goBack() }
        )
        "manageStudents" -> ManageStudentsScreen(
            college = currentUserData?.collegeName ?: collegeName,
            onBackClick = { goBack() }
        )
        else -> {
             CollegeSelectionScreen { name ->
                collegeName = name
                navigateTo("role")
             }
        }
    }
} // Closed Box
} // Closed CampusFixApp