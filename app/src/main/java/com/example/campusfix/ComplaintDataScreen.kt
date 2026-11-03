package com.example.campusfix

import com.google.firebase.firestore.PropertyName
import com.google.firebase.firestore.ServerTimestamp
import java.util.Date

data class ComplaintData(
    var ticketId: String = "",
    var title: String = "",
    var details: String = "",
    var location: String = "",
    var category: String = "",
    var department: String = "",
    var priority: String = "Medium",
    var status: String = "Pending",
    var studentEmail: String = "",
    var studentUid: String = "",
    var collegeName: String = "",
    var collegeId: String = "",
    @ServerTimestamp val createdAt: Date? = null,
    var photoUrl: String = "",
    var videoUrl: String = "",
    var voiceUrl: String = "",
    var assignedStaffId: String = "",
    var assignedStaffEmail: String = "",
    var proofPhotoUrl: String = "",
    var proofVideoUrl: String = "",
    
    // UI Helpers (Transient)
    val photoUri: String = "",
    val videoUri: String = "",
    val voicePath: String = ""
)

data class StaffData(
    val name: String = "",
    val staffId: String = "",
    val department: String = "",
    val email: String = "",
    val phone: String = "",
    val assignedTaskCount: Int = 0,
    val completedTaskCount: Int = 0,
    @get:PropertyName("approved") @set:PropertyName("approved") var isApproved: Boolean = false,
    var collegeName: String = "",
    val assignedCategories: List<String> = emptyList()
)

data class UserData(
    val uid: String = "",
    val name: String = "",
    val email: String = "",
    val phone: String = "",
    val role: String = "",
    val department: String = "",
    val regNo: String = "",
    var collegeName: String = "",
    val year: String = "",
    @get:PropertyName("approved") @set:PropertyName("approved") var isApproved: Boolean = false,
    val assignedCategories: List<String> = emptyList(),
    val activeComplaintCount: Int = 0,
    var profilePhoto: String = "",
    @ServerTimestamp val createdAt: Date? = null
)
