package com.project.topakuli.data.remote

import android.net.Uri
import com.project.topakuli.data.api.FcmMessage
import com.project.topakuli.data.api.FcmNotification
import com.project.topakuli.data.api.FcmRequest
import com.project.topakuli.data.api.FcmService
import com.google.auth.oauth2.GoogleCredentials
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.storage.FirebaseStorage
import com.project.topakuli.data.local.entity.ActivityLog
import com.project.topakuli.data.local.entity.NotificationEntity
import com.project.topakuli.data.local.entity.ResepEntity
import com.project.topakuli.data.local.entity.TanamanImageEntity
import com.project.topakuli.data.local.entity.TanamanObatEntity
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.tasks.await
import kotlinx.coroutines.withContext
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.RequestBody.Companion.toRequestBody
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory

class FirebaseService {
    private val firestore = FirebaseFirestore.getInstance()
    private val storage = FirebaseStorage.getInstance()

    private val fcmService: FcmService by lazy {
        Retrofit.Builder()
            .baseUrl("https://fcm.googleapis.com/")
            .addConverterFactory(GsonConverterFactory.create())
            .build()
            .create(FcmService::class.java)
    }

    private suspend fun getAccessToken(context: android.content.Context): String? = withContext(Dispatchers.IO) {
        try {
            val inputStream = context.assets.open("service-account.json")
            val googleCredentials = GoogleCredentials
                .fromStream(inputStream)
                .createScoped(listOf("https://www.googleapis.com/auth/firebase.messaging"))
            googleCredentials.refreshIfExpired()
            "Bearer ${googleCredentials.accessToken.tokenValue}"
        } catch (e: Throwable) {
            e.printStackTrace()
            null
        }
    }

    suspend fun getPublicNotifications(): List<NotificationEntity> {
        return try {
            val snapshot = firestore.collection("public_notifications")
                .orderBy("timestamp", com.google.firebase.firestore.Query.Direction.DESCENDING)
                .limit(20)
                .get()
                .await()
            snapshot.documents.mapNotNull { doc ->
                NotificationEntity(
                    id = doc.id,
                    title = doc.getString("title") ?: "",
                    content = doc.getString("content") ?: "",
                    type = doc.getString("type") ?: "update",
                    targetId = doc.getString("targetId"),
                    timestamp = doc.getLong("timestamp") ?: 0L,
                    isRead = false // Local status
                )
            }
        } catch (e: Exception) {
            if (e is CancellationException) throw e
            emptyList()
        }
    }

    suspend fun postPublicNotification(notif: NotificationEntity) {
        try {
            firestore.collection("public_notifications").document(notif.id).set(notif).await()
        } catch (e: Exception) {
            if (e is CancellationException) throw e
            e.printStackTrace()
        }
    }

    suspend fun getRecentActivities(limit: Long = 50): List<ActivityLog> {
        return try {
            val query = firestore.collection("activity_logs")
                .orderBy("timestamp", com.google.firebase.firestore.Query.Direction.DESCENDING)
            
            val snapshot = if (limit > 0) query.limit(limit).get().await() else query.get().await()

            snapshot.documents.mapNotNull { doc ->
                ActivityLog(
                    id = doc.id,
                    adminId = doc.getString("adminId") ?: "",
                    adminName = doc.getString("adminName") ?: "",
                    type = doc.getString("type") ?: "",
                    target = doc.getString("target") ?: "",
                    targetName = doc.getString("targetName") ?: "",
                    timestamp = doc.getLong("timestamp") ?: 0L
                )
            }
        } catch (e: Exception) {
            if (e is CancellationException) throw e
            emptyList()
        }
    }

    suspend fun logActivity(log: ActivityLog) {
        try {
            firestore.collection("activity_logs").add(log).await()
        } catch (e: Exception) {
            if (e is CancellationException) throw e
            e.printStackTrace()
        }
    }

    suspend fun deleteActivityLog(id: String) {
        try {
            firestore.collection("activity_logs").document(id).delete().await()
        } catch (e: Exception) {
            if (e is CancellationException) throw e
            e.printStackTrace()
        }
    }

    suspend fun getTanamanList(): List<TanamanObatEntity> {
        return try {
            android.util.Log.d("FirebaseService", "Mengambil data tanaman dari Firestore...")
            val snapshot = firestore.collection("tanaman_obat").get().await()
            android.util.Log.d("FirebaseService", "Berhasil mengambil ${snapshot.size()} dokumen tanaman")
            
            val list = snapshot.documents.mapNotNull { doc ->
                try {
                    TanamanObatEntity(
                        id = doc.id,
                        nama = doc.getString("nama") ?: "",
                        namaDaerah = doc.getString("namaDaerah") ?: "",
                        namaLatin = doc.getString("namaLatin") ?: "",
                        deskripsi = doc.getString("deskripsi") ?: "",
                        khasiat = doc.getString("khasiat") ?: "",
                        imageUrl = doc.getString("imageUrl"),
                        isFavorit = doc.getBoolean("isFavorit") ?: false,
                        createdAt = doc.getLong("createdAt") ?: System.currentTimeMillis()
                    )
                } catch (e: Exception) {
                    android.util.Log.e("FirebaseService", "Gagal map tanaman ID ${doc.id}: ${e.message}")
                    null
                }
            }
            android.util.Log.d("FirebaseService", "Total tanaman berhasil di-map: ${list.size}")
            list
        } catch (e: Exception) {
            if (e is CancellationException) throw e
            android.util.Log.e("FirebaseService", "Error getTanamanList: ${e.message}")
            emptyList()
        }
    }

    suspend fun getTanamanImages(tanamanId: String): List<TanamanImageEntity> {
        return try {
            val snapshot = firestore.collection("tanaman_obat").document(tanamanId)
                .collection("images").get().await()
            snapshot.documents.mapNotNull { doc ->
                TanamanImageEntity(
                    id = doc.id,
                    tanamanId = tanamanId,
                    imageUrl = doc.getString("imageUrl") ?: "",
                    createdAt = doc.getLong("createdAt") ?: System.currentTimeMillis()
                )
            }
        } catch (e: Exception) {
            if (e is CancellationException) throw e
            emptyList()
        }
    }

    suspend fun getResepList(): List<ResepEntity> {
        return try {
            android.util.Log.d("FirebaseService", "Mengambil data resep dari Firestore...")
            val snapshot = firestore.collection("resep").get().await()
            android.util.Log.d("FirebaseService", "Berhasil mengambil ${snapshot.size()} dokumen resep")
            
            val list = snapshot.documents.mapNotNull { doc ->
                try {
                    ResepEntity(
                        id = doc.id,
                        judul = doc.getString("judul") ?: "",
                        deskripsi = doc.getString("deskripsi") ?: "",
                        bahan = doc.getString("bahan") ?: "",
                        caraPembuatan = doc.getString("caraPembuatan") ?: "",
                        kategori = doc.getString("kategori") ?: "",
                        manfaat = doc.getString("manfaat") ?: "",
                        waktuPersiapan = doc.getString("waktuPersiapan") ?: "",
                        caraPakai = doc.getString("caraPakai") ?: "",
                        frekuensi = doc.getString("frekuensi") ?: "",
                        masaSimpan = doc.getString("masaSimpan") ?: "",
                        tingkatKesulitan = doc.getString("tingkatKesulitan") ?: "",
                        tanamanIds = doc.getString("tanamanIds"),
                        imageUrl = doc.getString("imageUrl"),
                        catatan = doc.getString("catatan"),
                        isSaved = doc.getBoolean("isSaved") ?: false,
                        lastReadAt = doc.getLong("lastReadAt"),
                        viewCount = doc.getLong("viewCount")?.toInt() ?: 0,
                        createdAt = doc.getLong("createdAt") ?: System.currentTimeMillis()
                    )
                } catch (e: Exception) {
                    android.util.Log.e("FirebaseService", "Gagal map resep ID ${doc.id}: ${e.message}")
                    null
                }
            }
            android.util.Log.d("FirebaseService", "Total resep berhasil di-map: ${list.size}")
            list
        } catch (e: Exception) {
            if (e is CancellationException) throw e
            android.util.Log.e("FirebaseService", "Error getResepList: ${e.message}")
            emptyList()
        }
    }

    suspend fun saveTanaman(tanaman: TanamanObatEntity) {
        firestore.collection("tanaman_obat").document(tanaman.id).set(tanaman).await()
    }

    suspend fun saveTanamanImage(image: TanamanImageEntity) {
        firestore.collection("tanaman_obat").document(image.tanamanId)
            .collection("images").document(image.id).set(image).await()
    }

    suspend fun deleteTanamanImage(tanamanId: String, imageId: String) {
        try {
            firestore.collection("tanaman_obat").document(tanamanId)
                .collection("images").document(imageId).delete().await()
        } catch (e: Exception) {
            if (e is CancellationException) throw e
            e.printStackTrace()
        }
    }

    suspend fun deleteTanaman(id: String) {
        firestore.collection("tanaman_obat").document(id).delete().await()
    }

    suspend fun saveResep(resep: ResepEntity) {
        firestore.collection("resep").document(resep.id).set(resep).await()
    }

    suspend fun deleteResep(id: String) {
        firestore.collection("resep").document(id).delete().await()
    }

    suspend fun uploadImage(imageUri: Uri, folder: String): String? {
        val fileName = "${System.currentTimeMillis()}.jpg"
        val ref = storage.reference.child("$folder/$fileName")
        return try {
            ref.putFile(imageUri).await()
            ref.downloadUrl.await().toString()
        } catch (e: Exception) {
            if (e is CancellationException) throw e
            null
        }
    }

    suspend fun updateAdminOnlineStatus(uid: String) {
        try {
            val data = mapOf(
                "lastActive" to System.currentTimeMillis(),
                "isOnline" to true
            )
            firestore.collection("admins").document(uid).set(data, com.google.firebase.firestore.SetOptions.merge()).await()
        } catch (e: Exception) {
            if (e is CancellationException) throw e
            e.printStackTrace()
        }
    }

    suspend fun getAdminStats(): Pair<Int, Int> {
        return try {
            val allAdmins = firestore.collection("admins").get().await()
            val total = allAdmins.size()
            
            // Online if active in last 5 minutes
            val fiveMinutesAgo = System.currentTimeMillis() - (5 * 60 * 1000)
            val online = allAdmins.documents.count { doc ->
                val lastActive = doc.getLong("lastActive") ?: 0L
                lastActive > fiveMinutesAgo
            }
            
            Pair(total, online)
        } catch (e: Exception) {
            if (e is CancellationException) throw e
            Pair(0, 0)
        }
    }

    suspend fun incrementVisitorStats(origin: String) {
        // origin: "Desa Pakuli" or "Luar Desa Pakuli"
        try {
            val field = if (origin == "Desa Pakuli") "pakuli" else "outside"
            firestore.collection("stats").document("visitors")
                .update(field, com.google.firebase.firestore.FieldValue.increment(1))
                .await()
        } catch (e: Exception) {
            if (e is CancellationException) throw e
            // Document might not exist, create it
            val data = if (origin == "Desa Pakuli") mapOf("pakuli" to 1, "outside" to 0) 
                       else mapOf("pakuli" to 0, "outside" to 1)
            firestore.collection("stats").document("visitors").set(data).await()
        }
    }

    suspend fun getVisitorStats(): Pair<Int, Int> {
        return try {
            val doc = firestore.collection("stats").document("visitors").get().await()
            val pakuli = doc.getLong("pakuli")?.toInt() ?: 0
            val outside = doc.getLong("outside")?.toInt() ?: 0
            Pair(pakuli, outside)
        } catch (e: Exception) {
            if (e is CancellationException) throw e
            Pair(0, 0)
        }
    }

    suspend fun getAllContributions(): List<com.project.topakuli.data.remote.model.ContributionForm> {
        return try {
            val snapshot = firestore.collection("user_contributions").get().await()
            snapshot.documents.mapNotNull { it.toObject(com.project.topakuli.data.remote.model.ContributionForm::class.java) }
        } catch (e: Exception) {
            if (e is CancellationException) throw e
            emptyList()
        }
    }

    suspend fun sendPushNotification(context: android.content.Context, title: String, content: String) {
        try {
            val accessToken = getAccessToken(context) ?: return
            val projectId = "topakuli-1998e"

            val dataPayload = mapOf(
                "title" to title,
                "content" to content
            )

            val fcmRequest = FcmRequest(
                message = FcmMessage(
                    topic = "all_users",
                    notification = FcmNotification(
                        title = title,
                        body = content
                    ),
                    data = dataPayload
                )
            )

            val response = fcmService.sendNotification(projectId, accessToken, fcmRequest)
            if (response.isSuccessful) {
                android.util.Log.d("FCM", "Notification sent successfully via v1 API")
            } else {
                android.util.Log.e("FCM", "Failed to send notification: ${response.errorBody()?.string()}")
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }
}
