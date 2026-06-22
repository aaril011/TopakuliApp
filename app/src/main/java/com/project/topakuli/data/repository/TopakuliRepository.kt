package com.project.topakuli.data.repository

import androidx.paging.Pager
import androidx.paging.PagingConfig
import androidx.paging.PagingData
import com.project.topakuli.data.local.dao.TopakuliDao
import com.project.topakuli.data.local.entity.*
import com.project.topakuli.data.remote.FirebaseService
import kotlinx.coroutines.flow.Flow

class TopakuliRepository(
    private val dao: TopakuliDao,
    private val firebaseService: FirebaseService
) {

    // Notifications
    val allNotifications: Flow<List<NotificationEntity>> = dao.getAllNotifications()

    suspend fun syncNotifications() {
        val remoteList = firebaseService.getPublicNotifications()
        if (remoteList.isNotEmpty()) {
            val localIds = dao.getAllNotificationIds().toSet()
            val newList = remoteList.map { it.copy(isRead = localIds.contains(it.id)) }
            dao.insertNotifications(newList)
        }
    }

    suspend fun markAllNotificationsAsRead() {
        dao.markAllNotificationsAsRead()
    }

    suspend fun postNotification(notif: NotificationEntity) {
        firebaseService.postPublicNotification(notif)
    }

    // Chats
    val allChats: Flow<List<ChatEntity>> = dao.getAllChats()
    
    suspend fun insertChat(chat: ChatEntity) {
        dao.insertChat(chat)
    }
    
    suspend fun clearAllChats() {
        dao.clearAllChats()
    }

    suspend fun deleteChatByType(type: String) {
        dao.deleteChatByType(type)
    }

    // Activity Logs
    suspend fun getRecentActivities(): List<ActivityLog> {
        return firebaseService.getRecentActivities()
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
            val localFavorites = dao.getFavoriteTanamanIds().toSet()
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
            val localSaved = dao.getSavedResepIds().toSet()
            // Ambil history lokal agar tidak hilang saat sync
            val localHistory = dao.getAllResepList().associate { it.id to it.lastReadAt }
            
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
        dao.deleteTanamanById(id)
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
        dao.deleteResepById(id)
        firebaseService.deleteResep(id)
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

    // Masukan & Chat
    suspend fun getUserContributions(): List<UserContribution> {
        return firebaseService.getUserContributions()
    }

    suspend fun getChatMessages(contributionId: String): List<ChatMessage> {
        return firebaseService.getChatMessages(contributionId)
    }

    suspend fun sendChatMessage(contributionId: String, message: ChatMessage) {
        firebaseService.sendChatMessage(contributionId, message)
    }
}
