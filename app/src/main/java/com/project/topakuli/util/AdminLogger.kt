package com.project.topakuli.util

import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.project.topakuli.data.local.entity.ActivityLog
import com.project.topakuli.data.local.entity.NotificationEntity
import com.project.topakuli.data.repository.TopakuliRepository
import kotlinx.coroutines.tasks.await
import java.util.UUID

object AdminLogger {
    suspend fun log(context: android.content.Context, repository: TopakuliRepository, type: String, target: String, targetName: String, targetId: String? = null) {
        val appContext = context.applicationContext
        val user = FirebaseAuth.getInstance().currentUser ?: return
        val firestore = FirebaseFirestore.getInstance()
        
        try {
            val doc = firestore.collection("admins").document(user.uid).get().await()
            val adminName = doc.getString("name") ?: "Admin"
            
            // 1. Internal Log for Admin Dashboard
            val log = ActivityLog(
                adminId = user.uid,
                adminName = adminName,
                type = type,
                target = target,
                targetName = targetName,
                timestamp = System.currentTimeMillis()
            )
            repository.logActivity(log)

            // 2. Public Notification for Users
            if (type == "Tambah" || type == "Ubah") {
                val notifTitle = when (type) {
                    "Tambah" -> if (target == "Tanaman") "Tanaman obat baru ditambahkan" else "Resep populer baru tersedia"
                    else -> "Info $target diperbarui"
                }
                
                val notifContent = when {
                    type == "Tambah" && target == "Tanaman" -> "$targetName kini tersedia lengkap dengan manfaat, kandungan, dan cara penggunaannya."
                    type == "Tambah" && target == "Resep" -> "$targetName kini masuk daftar resep pilihan minggu ini."
                    else -> "Data manfaat dan dosis $targetName telah diperbarui berdasarkan referensi terbaru."
                }

                val publicNotif = NotificationEntity(
                    id = UUID.randomUUID().toString(),
                    title = notifTitle,
                    content = notifContent,
                    type = if (target == "Resep") "recipe" else if (type == "Tambah") "added" else "update",
                    targetId = targetId,
                    timestamp = System.currentTimeMillis()
                )
                repository.postNotification(appContext, publicNotif)
                android.util.Log.d("AdminLogger", "Posted public notification: $notifTitle")
            }
        } catch (e: Exception) {
            android.util.Log.e("AdminLogger", "Error logging activity", e)
        }
    }
}
