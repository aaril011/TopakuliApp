package com.project.topakuli.data.repository

import com.google.firebase.firestore.FirebaseFirestore
import com.project.topakuli.data.local.dao.TopakuliDao
import com.project.topakuli.data.local.entity.ResepEntity
import com.project.topakuli.data.local.entity.TanamanObatEntity
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.tasks.await

class TopakuliRepository(private val dao: TopakuliDao) {

    private val firestore = FirebaseFirestore.getInstance()

    // Ambil dari lokal (Room)
    val allTanaman: Flow<List<TanamanObatEntity>> = dao.getAllTanaman()
    val allResep: Flow<List<ResepEntity>> = dao.getAllResep()

    // Sinkronisasi Tanaman Obat dari Firebase ke Room
    suspend fun syncTanaman() {
        try {
            val snapshot = firestore.collection("tanaman_obat").get().await()
            val list = snapshot.documents.map { doc ->
                TanamanObatEntity(
                    id = doc.id,
                    nama = doc.getString("nama") ?: "",
                    namaDaerah = doc.getString("namaDaerah") ?: "",
                    namaLatin = doc.getString("namaLatin") ?: "",
                    deskripsi = doc.getString("deskripsi") ?: "",
                    khasiat = doc.getString("khasiat") ?: "",
                    imageUrl = doc.getString("imageUrl"),
                    isFavorit = doc.getBoolean("isFavorit") ?: false
                )
            }
            dao.insertTanaman(list)
        } catch (e: Exception) {
            // Handle error (e.g. offline or permission)
        }
    }

    // Sinkronisasi Resep dari Firebase ke Room
    suspend fun syncResep() {
        try {
            val snapshot = firestore.collection("resep").get().await()
            val list = snapshot.documents.map { doc ->
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
                    tanamanId = doc.getString("tanamanId"),
                    imageUrl = doc.getString("imageUrl"),
                    catatan = doc.getString("catatan"),
                    isSaved = doc.getBoolean("isSaved") ?: false
                )
            }
            dao.insertResep(list)
        } catch (e: Exception) {
            // Handle error
        }
    }

    suspend fun populateDummyData() {
        dao.insertTanaman(com.project.topakuli.data.DataDummy.generateDummyTanaman())
        dao.insertResep(com.project.topakuli.data.DataDummy.generateDummyResep())
    }
}
