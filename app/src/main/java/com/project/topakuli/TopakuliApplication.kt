package com.project.topakuli

import android.app.Application
import com.project.topakuli.data.local.TopakuliDatabase
import com.project.topakuli.data.remote.FirebaseService
import com.project.topakuli.data.repository.TopakuliRepository

class TopakuliApplication : Application() {
    // Database and Repository instance for manual DI (Dependency Injection)
    val database by lazy { TopakuliDatabase.getDatabase(this) }
    val firebaseService by lazy { FirebaseService() }
    val repository by lazy { TopakuliRepository(database.topakuliDao(), firebaseService) }
}
