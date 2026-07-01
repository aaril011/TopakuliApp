package com.project.topakuli.ui.admin.auth

import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.EditText
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import com.google.firebase.auth.FirebaseAuth
import com.project.topakuli.R
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
        binding.tvForgotPassword.setOnClickListener {
            showForgotPasswordDialog()
        }

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
                // Save admin session state
                getSharedPreferences("topakuli_pref", MODE_PRIVATE)
                    .edit().putBoolean("is_admin_logged_in", true).apply()

                com.project.topakuli.util.DialogHelper.showSuccessDialog(
                    this,
                    "Login Berhasil",
                    "Selamat datang kembali! Anda sekarang masuk sebagai admin.",
                    "Masuk ke Dashboard"
                ) {
                    startActivity(Intent(this, AdminMainActivity::class.java))
                    finish()
                }
            }
            .addOnFailureListener { exception ->
                showError("Login Gagal: ${exception.message}")
            }
    }

    private fun showForgotPasswordDialog() {
        val dialogView = layoutInflater.inflate(R.layout.dialog_forgot_password_email, null)
        val dialog = AlertDialog.Builder(this).setView(dialogView).create()
        dialog.window?.setBackgroundDrawableResource(android.R.color.transparent)

        val etEmail = dialogView.findViewById<EditText>(R.id.et_email_forgot)
        val btnSend = dialogView.findViewById<View>(R.id.btn_send_reset)
        val btnClose = dialogView.findViewById<View>(R.id.btn_close_dialog)

        btnClose.setOnClickListener { dialog.dismiss() }

        btnSend.setOnClickListener {
            val email = etEmail.text.toString().trim()
            if (email.isEmpty()) {
                Toast.makeText(this, "Harap isi email", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            if (!android.util.Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
                Toast.makeText(this, "Format email tidak valid", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            btnSend.isEnabled = false
            auth.sendPasswordResetEmail(email)
                .addOnSuccessListener {
                    Toast.makeText(this, "Link pemulihan telah dikirim ke email anda", Toast.LENGTH_LONG).show()
                    dialog.dismiss()
                }
                .addOnFailureListener { e ->
                    btnSend.isEnabled = true
                    Toast.makeText(this, "Gagal: ${e.message}", Toast.LENGTH_SHORT).show()
                }
        }

        dialog.show()
    }

    private fun showError(message: String) {
        binding.btnLogin.isEnabled = true
        binding.btnBackLogin.isEnabled = true
        Toast.makeText(this, message, Toast.LENGTH_LONG).show()
    }
}
