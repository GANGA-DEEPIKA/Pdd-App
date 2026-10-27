package com.example.campusfix

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.net.Uri
import android.util.Base64
import android.util.Log
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.DocumentSnapshot
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.FirebaseFirestoreSettings
import com.google.firebase.firestore.ktx.toObject
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.tasks.await
import java.io.ByteArrayOutputStream
import java.io.File

object FirestoreService {

    private val db by lazy {
        FirebaseFirestore.getInstance().apply {
            val settings = FirebaseFirestoreSettings.Builder()
                .setPersistenceEnabled(true)
                .setCacheSizeBytes(FirebaseFirestoreSettings.CACHE_SIZE_UNLIMITED)
                .build()
            firestoreSettings = settings
        }
    }

    private const val TAG = "FirestoreService"

    private fun getCollegeId(college: String): String {
        return college.trim().lowercase()
            .replace(Regex("sch+o+l"), "school")
            .replace("school of engineering", "")
            .replace("college", "")
            .replace("university", "")
            .replace("institute", "")
            .replace("technology", "")
            .replace("of", "")
            .replace("ssaveetha", "saveetha")
            .replace(" ", "")
            .replace("_", "")
            .filter { it.isLetterOrDigit() }
            .ifEmpty { "general" }
    }

    // 100% FREE Base64 storage - No Card or Setup required
            suspend fun submitComplaint(context: Context, complaint: ComplaintData): Result<ComplaintData> {
        return try {
            val currentUser = FirebaseAuth.getInstance().currentUser
            val currentUid = currentUser?.uid ?: ""
            val studentEmail = currentUser?.email ?: ""
            
            val userData = getUserData(currentUid) ?: throw Exception("User profile not found.")
            val rawCollegeName = userData.collegeName.trim()
            val collegeId = getCollegeId(rawCollegeName)

            var photoUrl = complaint.photoUrl
            var voiceUrl = complaint.voiceUrl
            
            // Convert Photo to Base64 Text for FREE storage
            if (complaint.photoUri.isNotEmpty() && !complaint.photoUri.startsWith("base64:")) {
                photoUrl = "base64:" + uriToBase64(context, Uri.parse(complaint.photoUri))
            }
            
            // Convert Voice to Base64 Text for FREE storage
            if (complaint.voicePath.isNotEmpty() && !complaint.voicePath.startsWith("base64:")) {
                try {
                    val audioFile = File(complaint.voicePath)
                    if (audioFile.exists()) {
                        val bytes = audioFile.readBytes()
                        voiceUrl = "base64:" + Base64.encodeToString(bytes, Base64.DEFAULT)
                    }
                } catch (e: Exception) {
                    Log.e(TAG, "Audio conversion error", e)
                }
            }

            val assignedStaff = findBestStaffForCategory(rawCollegeName, complaint.category)
            val finalStatus = if (assignedStaff != null) "Assigned" else "Waiting for Staff Assignment"
            val ticketId = "CMP" + System.currentTimeMillis().toString().takeLast(6) + (10..99).random()

            // Respect anonymity: Don't overwrite student info if it's already set to "anonymous"
            val finalStudentUid = if (complaint.studentUid == "anonymous") "anonymous" else currentUid
            val finalStudentEmail = if (complaint.studentUid == "anonymous") "anonymous@campus.edu" else studentEmail
            val now = java.util.Date()

            val finalComplaint = complaint.copy(
                ticketId = ticketId, 
                photoUrl = photoUrl, 
                voiceUrl = voiceUrl,
                studentUid = finalStudentUid, 
                studentEmail = finalStudentEmail,
                collegeName = rawCollegeName, 
                collegeId = collegeId,
                status = finalStatus, 
                assignedStaffId = assignedStaff?.uid ?: "",
                assignedStaffEmail = assignedStaff?.email ?: "",
                createdAt = now
            )

            db.collection("all_complaints").document(ticketId).set(finalComplaint).await()
            
            if (assignedStaff != null) {
                try {
                    db.collection("approvedStaff").document(assignedStaff.uid)
                        .update("activeComplaintCount", FieldValue.increment(1)).await()
                } catch (e: Exception) {
                    Log.e(TAG, "Workload update failed", e)
                }
            }

            Result.success(finalComplaint)
        } catch (e: Exception) {
            Log.e(TAG, "Submission error", e)
            Result.failure(e)
        }
    }

    // Advanced conversion: Scales and Compresses image to fit in FREE Database
    private fun uriToBase64(context: Context, uri: Uri): String {
        return try {
            val inputStream = context.contentResolver.openInputStream(uri)
            val originalBitmap = BitmapFactory.decodeStream(inputStream) ?: return ""
            
            // Calculate scale to keep image under 800px (Fits perfectly in 1MB Firestore limit)
            val scale = if (originalBitmap.width > originalBitmap.height) 800f / originalBitmap.width else 800f / originalBitmap.height
            val scaledBitmap = if (scale < 1f) {
                Bitmap.createScaledBitmap(originalBitmap, (originalBitmap.width * scale).toInt(), (originalBitmap.height * scale).toInt(), true)
            } else originalBitmap

            val outputStream = ByteArrayOutputStream()
            scaledBitmap.compress(Bitmap.CompressFormat.JPEG, 70, outputStream)
            val byteArray = outputStream.toByteArray()
            Base64.encodeToString(byteArray, Base64.DEFAULT)
        } catch (e: Exception) {
            Log.e(TAG, "Base64 Error", e)
            ""
        }
    }

    private suspend fun findBestStaffForCategory(college: String, category: String): UserData? {
        return try {
            val collegeId = getCollegeId(college)
            val staffQuery = db.collection("approvedStaff").get().await()
            val staffList = staffQuery.documents.mapNotNull { mapDocToUserData(it) }
            staffList.filter { getCollegeId(it.collegeName) == collegeId &&
                it.assignedCategories.any { cat -> cat.trim().equals(category.trim(), ignoreCase = true) }
            }.minByOrNull { it.activeComplaintCount }
        } catch (e: Exception) { null }
    }

    fun getComplaintsRealTime(userData: UserData? = null): Flow<List<ComplaintData>> = callbackFlow {
        if (userData == null || userData.collegeName.isEmpty()) {
            trySend(emptyList())
            awaitClose { }
        } else {
            val collegeId = getCollegeId(userData.collegeName)
            val subscription = db.collection("all_complaints")
                .addSnapshotListener { snapshot, error ->
                    if (error != null) {
                        close(error)
                        return@addSnapshotListener
                    }
                    
                    val allComplaints = snapshot?.documents?.mapNotNull { doc -> 
                        doc.toObject<ComplaintData>() 
                    } ?: emptyList()

                    val filtered = allComplaints.filter { complaint ->
                        val instMatch = getCollegeId(complaint.collegeName) == collegeId || 
                                       complaint.collegeId == collegeId

                        instMatch && when (userData.role) {
                            "student" -> complaint.studentUid == userData.uid
                            "staff" -> userData.assignedCategories.any { staffCat -> 
                                staffCat.trim().equals(complaint.category.trim(), ignoreCase = true) 
                            }
                            "admin" -> true
                            else -> false
                        }
                    }
                    trySend(filtered.sortedByDescending { it.ticketId })
                }
            awaitClose { subscription.remove() }
        }
    }

    suspend fun updateComplaintStatus(ticketId: String, college: String, newStatus: String): Result<Unit> {
        return try {
            val docRef = db.collection("all_complaints").document(ticketId)
            db.runTransaction { transaction ->
                val snapshot = transaction.get(docRef)
                val staffId = snapshot.getString("assignedStaffId") ?: ""
                transaction.update(docRef, "status", newStatus)
                if (newStatus == "Resolved" && snapshot.getString("status") != "Resolved" && staffId.isNotEmpty()) {
                    transaction.update(db.collection("approvedStaff").document(staffId), "activeComplaintCount", FieldValue.increment(-1))
                }
            }.await()
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun uploadStaffProof(context: Context, ticketId: String, photoUri: Uri?, videoUri: Uri?): Result<Unit> {
        return try {
            var proofPhotoUrl = ""
            if (photoUri != null) {
                proofPhotoUrl = "base64:" + uriToBase64(context, photoUri)
            }

            db.collection("all_complaints").document(ticketId).update(mapOf(
                "status" to "Resolved",
                "proofPhotoUrl" to proofPhotoUrl
            )).await()

            // Handle workload decrement
            val doc = db.collection("all_complaints").document(ticketId).get().await()
            val staffId = doc.getString("assignedStaffId") ?: ""
            if (staffId.isNotEmpty()) {
                db.collection("approvedStaff").document(staffId)
                    .update("activeComplaintCount", FieldValue.increment(-1)).await()
            }

            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun getUserData(uid: String): UserData? {
        val collections = listOf("approvedStaff", "users", "pendingStaffRequests")
        for (col in collections) {
            val doc = try { db.collection(col).document(uid).get().await() } catch (e: Exception) { null }
            if (doc != null && doc.exists()) return mapDocToUserData(doc)
        }
        return null
    }

    private fun mapDocToUserData(doc: DocumentSnapshot): UserData? {
        return try {
            val user = doc.toObject<UserData>() ?: return null
            if (user.collegeName.isEmpty()) {
                user.collegeName = doc.getString("collegeName") ?: doc.getString("college") ?: ""
            }
            val approved = doc.get("approved") as? Boolean ?: doc.get("isApproved") as? Boolean ?: user.isApproved
            user.isApproved = approved
            user
        } catch (e: Exception) {
            null
        }
    }

    suspend fun createPendingStaffRequest(user: UserData): Result<Unit> {
        val data = mapOf(
            "uid" to user.uid, "name" to user.name, "email" to user.email, "phone" to user.phone,
            "collegeName" to user.collegeName.trim(), "role" to "staff", "approved" to false,
            "assignedCategories" to emptyList<String>(), "activeComplaintCount" to 0
        )
        return try {
            db.collection("pendingStaffRequests").document(user.uid).set(data).await()
            db.collection("users").document(user.uid).set(data).await()
            Result.success(Unit)
        } catch (e: Exception) { Result.failure(e) }
    }

    fun getPendingStaffRequests(college: String): Flow<List<UserData>> = callbackFlow {
        val subscription = db.collection("pendingStaffRequests")
            .addSnapshotListener { snapshot, error ->
                if (error != null) { close(error); return@addSnapshotListener }
                val allRequests = snapshot?.documents?.mapNotNull { mapDocToUserData(it) } ?: emptyList()
                val filtered = allRequests.filter { req -> getCollegeId(req.collegeName) == getCollegeId(college) }
                trySend(filtered)
            }
        awaitClose { subscription.remove() }
    }

    suspend fun approveStaffRequest(user: UserData, categories: List<String>): Result<Unit> {
        val data = mapOf(
            "uid" to user.uid, "name" to user.name, "email" to user.email, "phone" to user.phone,
            "collegeName" to user.collegeName.trim(), "role" to "staff", "approved" to true,
            "assignedCategories" to categories, "activeComplaintCount" to 0
        )
        return try {
            db.collection("approvedStaff").document(user.uid).set(data).await()
            db.collection("pendingStaffRequests").document(user.uid).delete().await()
            db.collection("users").document(user.uid).set(data).await()
            Result.success(Unit)
        } catch (e: Exception) { Result.failure(e) }
    }

    suspend fun rejectStaffRequest(uid: String): Result<Unit> {
        return try {
            db.collection("pendingStaffRequests").document(uid).delete().await()
            db.collection("users").document(uid).delete().await()
            Result.success(Unit)
        } catch (e: Exception) { Result.failure(e) }
    }
    
    suspend fun checkStaffStatusByEmail(email: String): String? {
        return try {
            val pending = db.collection("pendingStaffRequests").whereEqualTo("email", email).get().await()
            if (!pending.isEmpty) return "PENDING"
            val approved = db.collection("approvedStaff").whereEqualTo("email", email).get().await()
            if (!approved.isEmpty) return "APPROVED"
            null
        } catch (e: Exception) { null }
    }

    suspend fun submitFeedback(rating: Int, comments: String, collegeName: String): Result<Unit> {
        return try {
            val currentUser = FirebaseAuth.getInstance().currentUser
            val data = mapOf(
                "rating" to rating,
                "comments" to comments,
                "collegeName" to collegeName,
                "userEmail" to (currentUser?.email ?: "Anonymous"),
                "timestamp" to FieldValue.serverTimestamp()
            )
            db.collection("feedbacks").add(data).await()
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    fun getStudentsByCollege(college: String): Flow<List<UserData>> = callbackFlow {
        val collegeId = getCollegeId(college)
        val subscription = db.collection("users")
            .addSnapshotListener { snapshot, error ->
                if (error != null) { close(error); return@addSnapshotListener }
                val allUsers = snapshot?.documents?.mapNotNull { mapDocToUserData(it) } ?: emptyList()
                val students = allUsers.filter { user ->
                    val isStudent = user.role.isBlank() || 
                                   user.role.equals("student", ignoreCase = true) ||
                                   (user.role != "staff" && user.role != "admin")
                    val instMatch = college.isBlank() || 
                                   collegeId == "general" ||
                                   user.collegeName.isBlank() ||
                                   getCollegeId(user.collegeName) == collegeId ||
                                   getCollegeId(user.collegeName).contains(collegeId) ||
                                   collegeId.contains(getCollegeId(user.collegeName))
                    isStudent && instMatch
                }
                trySend(students)
            }
        awaitClose { subscription.remove() }
    }

    suspend fun removeUser(uid: String): Result<Unit> {
        return try {
            db.collection("users").document(uid).delete().await()
            Result.success(Unit)
        } catch (e: Exception) { Result.failure(e) }
    }

    suspend fun deleteComplaint(ticketId: String): Result<Unit> {
        return try {
            if (ticketId.isNotBlank()) {
                db.collection("all_complaints").document(ticketId).delete().await()
            }
            Result.success(Unit)
        } catch (e: Exception) { Result.failure(e) }
    }
}
