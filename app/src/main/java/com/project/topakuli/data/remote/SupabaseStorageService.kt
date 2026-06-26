package com.project.topakuli.data.remote

import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.net.Uri
import com.project.topakuli.TopakuliApplication
import io.github.jan.supabase.storage.storage
import io.ktor.http.ContentType
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.ByteArrayOutputStream
import java.util.UUID

class SupabaseStorageService(private val app: TopakuliApplication) {
    
    private val storage by lazy { app.supabaseClient.storage }

    /**
     * Upload image with compression
     */
    suspend fun uploadImage(imageUri: Uri, bucket: String): String? = withContext(Dispatchers.IO) {
        try {
            android.util.Log.d("SupabaseStorage", "Starting upload to bucket: $bucket")
            val fileName = "${UUID.randomUUID()}.jpg"
            val bucketRef = app.supabaseClient.storage.from(bucket)
            
            // 1. Ambil bytes asli
            val inputStream = app.contentResolver.openInputStream(imageUri)
            val originalBytes = inputStream?.readBytes() ?: return@withContext null
            inputStream.close()

            // 2. Kompresi Gambar agar di bawah 2MB (dan lebih cepat)
            val compressedBytes = compressImage(originalBytes)
            
            android.util.Log.d("SupabaseStorage", "Original size: ${originalBytes.size} bytes, Compressed: ${compressedBytes.size} bytes")
            
            // 3. Upload ke Supabase
            try {
                bucketRef.upload(fileName, compressedBytes) {
                    upsert = true
                    contentType = ContentType.Image.JPEG
                }
                android.util.Log.d("SupabaseStorage", "Upload successful: $fileName")
            } catch (uploadError: Exception) {
                if (uploadError is CancellationException) throw uploadError
                android.util.Log.e("SupabaseStorage", "Upload failed: ${uploadError.message}")
                return@withContext null
            }

            bucketRef.publicUrl(fileName)
        } catch (e: Exception) {
            if (e is CancellationException) throw e
            android.util.Log.e("SupabaseStorage", "Error in uploadImage: ${e.message}")
            null
        }
    }

    private fun compressImage(originalBytes: ByteArray): ByteArray {
        val bitmap = BitmapFactory.decodeByteArray(originalBytes, 0, originalBytes.size)
        val out = ByteArrayOutputStream()
        
        // Kompresi kualitas ke 70% (biasanya memotong ukuran file hingga 80% tanpa merusak visual)
        bitmap.compress(Bitmap.CompressFormat.JPEG, 70, out)
        
        return out.toByteArray()
    }

    /**
     * Delete image from Supabase Storage
     */
    suspend fun deleteImage(imageUrl: String, bucket: String) = withContext(Dispatchers.IO) {
        try {
            // Extract filename safely, removing query parameters if any
            val path = imageUrl.substringBefore("?")
            val fileName = path.substringAfterLast("/")
            
            if (fileName.isEmpty() || fileName == imageUrl) {
                android.util.Log.e("SupabaseStorage", "Failed to extract filename from URL: $imageUrl")
                return@withContext
            }

            android.util.Log.d("SupabaseStorage", "Attempting to delete image: $fileName from bucket: $bucket")
            app.supabaseClient.storage.from(bucket).delete(fileName)
            android.util.Log.d("SupabaseStorage", "Delete call successful for: $fileName")
        } catch (e: Exception) {
            if (e is CancellationException) throw e
            android.util.Log.e("SupabaseStorage", "Error deleting image from Supabase: ${e.message}", e)
        }
    }
}
