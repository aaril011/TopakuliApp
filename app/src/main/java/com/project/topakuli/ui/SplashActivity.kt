package com.project.topakuli.ui

import android.annotation.SuppressLint
import android.content.Intent
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import androidx.appcompat.app.AppCompatActivity
import com.project.topakuli.R

import com.project.topakuli.ui.intro.IntroActivity

@SuppressLint("CustomSplashScreen")
class SplashActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_splash)

        // Delay for 2 seconds before checking navigation
        Handler(Looper.getMainLooper()).postDelayed({
            checkFirstInstall()
        }, 2000)
    }

    private fun checkFirstInstall() {
        val sharedPref = getSharedPreferences("topakuli_pref", MODE_PRIVATE)
        val isFirstInstall = sharedPref.getBoolean("is_first_install", true)

        if (isFirstInstall) {
            startActivity(Intent(this, IntroActivity::class.java))
        } else {
            startActivity(Intent(this, MainActivity::class.java))
        }
        finish()
    }
}