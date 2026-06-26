package com.project.topakuli

import android.app.Application
import com.project.topakuli.data.local.TopakuliDatabase
import com.project.topakuli.data.remote.FirebaseService
import com.project.topakuli.data.remote.SupabaseStorageService
import com.project.topakuli.data.repository.TopakuliRepository
import io.github.jan.supabase.SupabaseClient
import io.github.jan.supabase.createSupabaseClient
import io.github.jan.supabase.storage.Storage

class TopakuliApplication : Application() {

    // Supabase Client for Storage
    val supabaseClient: SupabaseClient by lazy {
        createSupabaseClient(
            supabaseUrl = "https://rnlnqyywwvfdrguabvjd.supabase.co",
            supabaseKey = "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9.eyJpc3MiOiJzdXBhYmFzZSIsInJlZiI6InJubG5xeXl3d3ZmZHJndWFidmpkIiwicm9sZSI6ImFub24iLCJpYXQiOjE3ODIyNjY4NTgsImV4cCI6MjA5Nzg0Mjg1OH0.GSNqkKksTht9bYihgJZx0iPwj9Q1wY5T54I4WDc-SH8"
        ) {
            install(Storage)
        }
    }

    // Database and Repository instance for manual DI (Dependency Injection)
    val database by lazy { TopakuliDatabase.getDatabase(this) }
    val firebaseService by lazy { FirebaseService() }
    val supabaseStorageService by lazy { SupabaseStorageService(this) }
    
    val repository by lazy { 
        TopakuliRepository(database.topakuliDao(), firebaseService, supabaseStorageService) 
    }
}
