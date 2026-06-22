package com.project.topakuli.ui.admin.auth

import android.content.Intent
import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.google.firebase.auth.FirebaseAuth
import com.project.topakuli.databinding.ActivityLoginAdminBinding
import com.project.topakuli.ui.admin.AdminMainActivity

class LoginAdminActivity : AppCompatActivity() {

    private lateinit var binding: ActivityLoginAdminBinding
    private lateinit var auth: FirebaseAuth

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityLoginAdminBinding.inflate(layoutInflater)
        setContentView(binding.root)

        auth = FirebaseAuth.getInstance()

        setupActions()
    }

    private fun setupActions() {
        binding.btnBackLogin.setOnClickListener {
            finish()
        }

        binding.btnLogin.setOnClickListener {
            val email = binding.etEmail.text.toString().trim()
            val password = binding.etPassword.text.toString().trim()

            if (email.isEmpty() || password.isEmpty()) {
                Toast.makeText(this, "Email dan Password harus diisi", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            if (!android.util.Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
                Toast.makeText(this, "Format email tidak valid", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            performLogin(email, password)
        }
    }

    private fun performLogin(email: String, password: String) {
        binding.btnLogin.isEnabled = false
        binding.btnBackLogin.isEnabled = false

        auth.signInWithEmailAndPassword(email, password)
            .addOnSuccessListener {
                Toast.makeText(this, "Login Berhasil", Toast.LENGTH_SHORT).show()
                startActivity(Intent(this, AdminMainActivity::class.java))
                finish()
            }
            .addOnFailureListener { exception ->
                showError("Login Gagal: ${exception.message}")
            }
    }

    private fun showError(message: String) {
        binding.btnLogin.isEnabled = true
        binding.btnBackLogin.isEnabled = true
        Toast.makeText(this, message, Toast.LENGTH_LONG).show()
    }
}
