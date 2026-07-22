package com.project.topakuli.data.repository

import androidx.paging.Pager
import androidx.paging.PagingConfig
import androidx.paging.PagingData
import com.project.topakuli.data.local.dao.TopakuliDao
import com.project.topakuli.data.local.entity.ActivityLog
import com.project.topakuli.data.local.entity.HistoryEntity
import com.project.topakuli.data.local.entity.NotificationEntity
import com.project.topakuli.data.local.entity.ResepEntity
import com.project.topakuli.data.local.entity.TanamanImageEntity
import com.project.topakuli.data.local.entity.TanamanObatEntity
import com.project.topakuli.data.remote.FirebaseService
import com.project.topakuli.data.remote.SupabaseStorageService
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first

class TopakuliRepository(
    private val dao: TopakuliDao,
    private val firebaseService: FirebaseService,
    private val supabaseStorageService: SupabaseStorageService
) {

    // Notifications (Fetch directly from Firebase)
    suspend fun getRemoteNotifications(): List<NotificationEntity> {
        return firebaseService.getPublicNotifications()
    }

    suspend fun postNotification(context: android.content.Context, notif: NotificationEntity) {
        firebaseService.postPublicNotification(notif)
        // Also trigger push notification for background users
        firebaseService.sendPushNotification(context, notif.title, notif.content)
    }

    // Activity Logs
    suspend fun getRecentActivities(limit: Long = 50): List<ActivityLog> {
        return firebaseService.getRecentActivities(limit)
    }

    suspend fun logActivity(log: ActivityLog) {
        firebaseService.logActivity(log)
    }

    suspend fun deleteActivityLog(id: String) {
        firebaseService.deleteActivityLog(id)
    }

    // History Identifikasi
    val allHistory: Flow<List<HistoryEntity>> = dao.getAllHistory()
    val historyCount: Flow<Int> = dao.getHistoryCount()

    suspend fun insertHistory(history: HistoryEntity) {
        dao.insertHistory(history)
    }

    suspend fun deleteHistory(id: Int) {
        dao.deleteHistoryById(id)
    }

    suspend fun clearAllHistory() {
        dao.deleteAllHistory()
    }

    suspend fun clearLocalData() {
        dao.deleteAllHistory()
        dao.clearResepHistory()
    }

    // Ambil dari lokal (Room) - UI akan mengamati ini
    val allTanaman: Flow<List<TanamanObatEntity>> = dao.getAllTanaman()
    val allResep: Flow<List<ResepEntity>> = dao.getAllResep()
    val tanamanCount: Flow<Int> = dao.getTanamanCount()
    val resepCount: Flow<Int> = dao.getResepCount()

    suspend fun getTanamanById(id: String): TanamanObatEntity? {
        return dao.getTanamanById(id)
    }

    fun getTanamanPaging(query: String, onlyFavorites: Boolean): Flow<PagingData<TanamanObatEntity>> {
        return Pager(
            config = PagingConfig(pageSize = 10, enablePlaceholders = false),
            pagingSourceFactory = { dao.getTanamanPaging("%$query%", onlyFavorites) }
        ).flow
    }

    fun getResepPaging(query: String, onlySaved: Boolean): Flow<PagingData<ResepEntity>> {
        return Pager(
            config = PagingConfig(pageSize = 10, enablePlaceholders = false),
            pagingSourceFactory = { dao.getResepPaging("%$query%", onlySaved) }
        ).flow
    }

    fun getResepHistoryPaging(query: String): Flow<PagingData<ResepEntity>> {
        return Pager(
            config = PagingConfig(pageSize = 10, enablePlaceholders = false),
            pagingSourceFactory = { dao.getResepHistoryPaging("%$query%") }
        ).flow
    }

    suspend fun markResepAsRead(resepId: String) {
        dao.getResepById(resepId)?.let { resep ->
            val updated = resep.copy(
                lastReadAt = System.currentTimeMillis(),
                viewCount = resep.viewCount + 1
            )
            dao.updateResep(updated)
            // Juga update ke Firebase agar statistik sinkron
            firebaseService.saveResep(updated)
        }
    }

    val popularResep: Flow<List<ResepEntity>> = dao.getPopularResep()

    suspend fun removeFromHistory(resepId: String) {
        dao.getResepById(resepId)?.let { resep ->
            dao.updateResep(resep.copy(lastReadAt = null))
        }
    }

    // Sinkronisasi Tanaman Obat dari Firebase ke Room
    suspend fun syncTanaman() {
        val remoteList = firebaseService.getTanamanList()
        if (remoteList.isNotEmpty()) {
            val remoteIds = remoteList.map { it.id }
            val localFavorites = dao.getFavoriteTanamanIds().toSet()
            
            // 1. Hapus data lokal yang sudah tidak ada di Cloud
            dao.deleteOldTanaman(remoteIds)
            
            // 2. Update/Insert data terbaru
            val updatedList = remoteList.map { tanaman ->
                tanaman.copy(isFavorit = localFavorites.contains(tanaman.id))
            }
            dao.insertTanaman(updatedList)
        }
    }

    // Sinkronisasi Resep dari Firebase ke Room
    suspend fun syncResep() {
        val remoteList = firebaseService.getResepList()
        if (remoteList.isNotEmpty()) {
            val remoteIds = remoteList.map { it.id }
            val localSaved = dao.getSavedResepIds().toSet()
            // Ambil history lokal agar tidak hilang saat sync
            val localHistory = dao.getAllResepList().associate { it.id to it.lastReadAt }
            
            // 1. Hapus data lokal yang sudah tidak ada di Cloud
            dao.deleteOldResep(remoteIds)
            
            // 2. Update/Insert data terbaru
            val updatedList = remoteList.map { resep ->
                resep.copy(
                    isSaved = localSaved.contains(resep.id),
                    lastReadAt = localHistory[resep.id]
                )
            }
            dao.insertResep(updatedList)
        }
    }

    suspend fun toggleFavorit(tanaman: TanamanObatEntity) {
        val updated = tanaman.copy(isFavorit = !tanaman.isFavorit)
        dao.updateTanaman(updated)
        firebaseService.saveTanaman(updated)
    }

    suspend fun toggleSaveResep(resep: ResepEntity) {
        val updated = resep.copy(isSaved = !resep.isSaved)
        dao.updateResep(updated)
        firebaseService.saveResep(updated)
    }

    suspend fun insertTanaman(tanaman: TanamanObatEntity) {
        dao.insertSingleTanaman(tanaman)
        firebaseService.saveTanaman(tanaman)
    }

    suspend fun deleteTanaman(id: String) {
        // 1. Get plant data to retrieve image URLs
        val tanaman = dao.getTanamanById(id)
        
        // 2. Delete main image from Supabase
        tanaman?.imageUrl?.let { url ->
            if (url.contains("supabase")) {
                deleteImage(url, "tanaman")
            }
        }

        // 3. Delete gallery images from Supabase
        val galleryImages = dao.getImagesByTanamanId(id).first()
        for (img in galleryImages) {
            if (img.imageUrl.contains("supabase")) {
                deleteImage(img.imageUrl, "tanaman")
            }
        }

        // 4. Delete from Database and Firebase
        dao.deleteTanamanById(id)
        dao.deleteImagesByTanamanId(id)
        firebaseService.deleteTanaman(id)
    }

    // Tanaman Images
    fun getTanamanImages(tanamanId: String): Flow<List<TanamanImageEntity>> {
        return dao.getImagesByTanamanId(tanamanId)
    }

    suspend fun syncTanamanImages(tanamanId: String) {
        val remoteImages = firebaseService.getTanamanImages(tanamanId)
        if (remoteImages.isNotEmpty()) {
            dao.insertTanamanImages(remoteImages)
        }
    }

    suspend fun insertTanamanImage(image: TanamanImageEntity) {
        dao.insertTanamanImages(listOf(image))
        firebaseService.saveTanamanImage(image)
    }

    suspend fun deleteTanamanImageById(tanamanId: String, imageId: String) {
        dao.deleteImageById(imageId)
        firebaseService.deleteTanamanImage(tanamanId, imageId)
    }

    suspend fun getResepById(id: String): ResepEntity? {
        return dao.getResepById(id)
    }

    suspend fun getResepByTanamanId(tanamanId: String): ResepEntity? {
        return dao.getResepByTanamanId(tanamanId)
    }

    suspend fun getAllResepList(): List<ResepEntity> {
        return dao.getAllResepList()
    }

    suspend fun insertResep(resep: ResepEntity) {
        dao.insertSingleResep(resep)
        firebaseService.saveResep(resep)
    }

    suspend fun deleteResep(id: String) {
        // 1. Get recipe data to retrieve image URL
        val resep = dao.getResepById(id)
        
        // 2. Delete image from Supabase
        resep?.imageUrl?.let { url ->
            if (url.contains("supabase")) {
                deleteImage(url, "resep")
            }
        }

        // 3. Delete from Database and Firebase
        dao.deleteResepById(id)
        firebaseService.deleteResep(id)
    }

    // Hybrid Storage Methods
    suspend fun uploadImage(imageUri: android.net.Uri, bucket: String): String? {
        return supabaseStorageService.uploadImage(imageUri, bucket)
    }

    suspend fun deleteImage(imageUrl: String, bucket: String) {
        supabaseStorageService.deleteImage(imageUrl, bucket)
    }

    suspend fun getAdminStats(): Pair<Int, Int> {
        return firebaseService.getAdminStats()
    }

    suspend fun updateAdminOnlineStatus(uid: String) {
        firebaseService.updateAdminOnlineStatus(uid)
    }

    suspend fun incrementVisitorStats(origin: String) {
        firebaseService.incrementVisitorStats(origin)
    }

    suspend fun getVisitorStats(): Pair<Int, Int> {
        return firebaseService.getVisitorStats()
    }

    suspend fun incrementIdentificationStats(plantName: String) {
        firebaseService.incrementIdentificationStats(plantName)
    }

    suspend fun getIdentificationStats(): List<Pair<String, Int>> {
        return firebaseService.getIdentificationStats()
    }

    suspend fun getAllContributions(): List<com.project.topakuli.data.remote.model.ContributionForm> {
        return firebaseService.getAllContributions()
    }

    suspend fun runDataMaintenance() {
        firebaseService.cleanupOldData()
    }
}
