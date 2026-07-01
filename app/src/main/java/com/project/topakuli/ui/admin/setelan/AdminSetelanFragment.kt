package com.project.topakuli.ui.admin.setelan

import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.EditText
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.project.topakuli.R
import com.project.topakuli.databinding.FragmentAdminSetelanBinding
import com.project.topakuli.ui.SplashActivity

class AdminSetelanFragment : Fragment() {

    private var _binding: FragmentAdminSetelanBinding? = null
    private val binding get() = _binding!!
    private val auth = FirebaseAuth.getInstance()
    private val firestore = FirebaseFirestore.getInstance()
    private var currentPhoneNumber: String = ""

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentAdminSetelanBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        loadProfile()

        binding.btnDaftarPengelola.setOnClickListener {
            findNavController().navigate(R.id.action_admin_setelan_to_manager_list)
        }
        
        binding.ivEditProfile.setOnClickListener { showEditOptionsDialog() }
        binding.btnEditProfil.setOnClickListener { showEditOptionsDialog() }

        binding.btnBantuan.setOnClickListener {
            findNavController().navigate(R.id.admin_navigation_help)
        }

        binding.btnLaporan.setOnClickListener {
            findNavController().navigate(R.id.action_admin_setelan_to_laporan)
        }

        binding.btnTentangApp.setOnClickListener {
            findNavController().navigate(R.id.action_admin_setelan_to_tentang)
        }

        binding.btnLogoutCard.setOnClickListener {
            showLogoutConfirmation()
        }
    }

    private fun showEditOptionsDialog() {
        val dialogView = LayoutInflater.from(requireContext()).inflate(R.layout.dialog_admin_account_options, null)
        val dialog = AlertDialog.Builder(requireContext()).setView(dialogView).create()
        dialog.window?.setBackgroundDrawableResource(android.R.color.transparent)

        dialogView.findViewById<View>(R.id.btn_close_dialog).setOnClickListener { dialog.dismiss() }
        
        dialogView.findViewById<View>(R.id.btn_option_edit_name).setOnClickListener {
            dialog.dismiss()
            showEditNameDialog()
        }

        dialogView.findViewById<View>(R.id.btn_option_edit_phone).setOnClickListener {
            dialog.dismiss()
            showEditPhoneDialog()
        }
        
        dialogView.findViewById<View>(R.id.btn_option_change_pass).setOnClickListener {
            dialog.dismiss()
            showChangePasswordDialog()
        }

        dialog.show()
    }

    private fun showChangePasswordDialog() {
        val dialogView = LayoutInflater.from(requireContext()).inflate(R.layout.dialog_admin_change_password, null)
        val dialog = AlertDialog.Builder(requireContext()).setView(dialogView).create()
        
        dialog.window?.setBackgroundDrawableResource(android.R.color.transparent)
        
        val etOld = dialogView.findViewById<EditText>(R.id.et_old_password)
        val etNew = dialogView.findViewById<EditText>(R.id.et_new_password)
        val btnSubmit = dialogView.findViewById<View>(R.id.btn_submit_password)
        val btnClose = dialogView.findViewById<View>(R.id.btn_close_dialog)

        btnClose.setOnClickListener { dialog.dismiss() }

        btnSubmit.setOnClickListener {
            val oldPass = etOld.text.toString().trim()
            val newPass = etNew.text.toString().trim()

            if (oldPass.isEmpty() || newPass.isEmpty()) {
                Toast.makeText(requireContext(), "Harap isi semua field", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            if (newPass.length < 6) {
                Toast.makeText(requireContext(), "Password baru minimal 6 karakter", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            btnSubmit.isEnabled = false
            performChangePassword(oldPass, newPass, dialog, btnSubmit)
        }

        dialog.show()
    }

    private fun performChangePassword(oldPass: String, newPass: String, dialog: AlertDialog, btnSubmit: View) {
        val user = auth.currentUser ?: return
        val email = user.email ?: return

        val credential = com.google.firebase.auth.EmailAuthProvider.getCredential(email, oldPass)
        
        user.reauthenticate(credential).addOnCompleteListener { task ->
            if (task.isSuccessful) {
                user.updatePassword(newPass).addOnCompleteListener { updateTask ->
                    if (updateTask.isSuccessful) {
                        Toast.makeText(requireContext(), "Password berhasil diperbarui", Toast.LENGTH_SHORT).show()
                        dialog.dismiss()
                    } else {
                        btnSubmit.isEnabled = true
                        Toast.makeText(requireContext(), "Gagal: ${updateTask.exception?.message}", Toast.LENGTH_SHORT).show()
                    }
                }
            } else {
                btnSubmit.isEnabled = true
                Toast.makeText(requireContext(), "Password lama salah", Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun showEditNameDialog() {
        val currentName = binding.tvAdminName.text.toString()
        val displayName = if (currentName == "Memuat...") "" else currentName
        
        val dialogView = LayoutInflater.from(requireContext()).inflate(R.layout.dialog_admin_edit_name, null)
        val dialog = AlertDialog.Builder(requireContext()).setView(dialogView).create()
        dialog.window?.setBackgroundDrawableResource(android.R.color.transparent)

        val etInput = dialogView.findViewById<EditText>(R.id.et_edit_name_input)
        val btnSubmit = dialogView.findViewById<View>(R.id.btn_submit_edit_name)
        val btnClose = dialogView.findViewById<View>(R.id.btn_close_dialog)

        etInput.setText(displayName)
        etInput.setSelection(displayName.length)
        
        btnClose.setOnClickListener { dialog.dismiss() }

        btnSubmit.setOnClickListener {
            val newName = etInput.text.toString().trim()
            if (newName.isNotEmpty()) {
                updateName(newName)
                dialog.dismiss()
            } else {
                Toast.makeText(requireContext(), "Nama tidak boleh kosong", Toast.LENGTH_SHORT).show()
            }
        }

        dialog.show()
    }

    private fun updateName(newName: String) {
        val uid = auth.currentUser?.uid ?: return
        firestore.collection("admins").document(uid)
            .update("name", newName)
            .addOnSuccessListener {
                loadProfile()
                Toast.makeText(requireContext(), "Nama berhasil diperbarui", Toast.LENGTH_SHORT).show()
            }
    }

    private fun showEditPhoneDialog() {
        val dialogView = LayoutInflater.from(requireContext()).inflate(R.layout.dialog_admin_edit_phone, null)
        val dialog = AlertDialog.Builder(requireContext()).setView(dialogView).create()
        dialog.window?.setBackgroundDrawableResource(android.R.color.transparent)

        val etInput = dialogView.findViewById<EditText>(R.id.et_edit_phone_input)
        val btnSubmit = dialogView.findViewById<View>(R.id.btn_submit_edit_phone)
        val btnClose = dialogView.findViewById<View>(R.id.btn_close_dialog)

        etInput.setText(currentPhoneNumber)
        etInput.setSelection(currentPhoneNumber.length)
        
        btnClose.setOnClickListener { dialog.dismiss() }

        btnSubmit.setOnClickListener {
            val newPhone = etInput.text.toString().trim()
            if (newPhone.isNotEmpty()) {
                updatePhone(newPhone)
                dialog.dismiss()
            } else {
                Toast.makeText(requireContext(), "Nomor telepon tidak boleh kosong", Toast.LENGTH_SHORT).show()
            }
        }

        dialog.show()
    }

    private fun updatePhone(newPhone: String) {
        val uid = auth.currentUser?.uid ?: return
        firestore.collection("admins").document(uid)
            .update("phoneNumber", newPhone)
            .addOnSuccessListener {
                loadProfile()
                Toast.makeText(requireContext(), "Nomor telepon berhasil diperbarui", Toast.LENGTH_SHORT).show()
            }
    }

    private fun showLogoutConfirmation() {
        val dialogView = LayoutInflater.from(requireContext()).inflate(R.layout.dialog_confirm_logout, null)
        val dialog = AlertDialog.Builder(requireContext()).setView(dialogView).create()
        dialog.window?.setBackgroundDrawableResource(android.R.color.transparent)

        val btnCancel = dialogView.findViewById<View>(R.id.btn_cancel_logout)
        val btnConfirm = dialogView.findViewById<View>(R.id.btn_confirm_logout_action)

        btnCancel.setOnClickListener { dialog.dismiss() }
        
        btnConfirm.setOnClickListener {
            dialog.dismiss()
            // Clear admin session
            requireContext().getSharedPreferences("topakuli_pref", android.content.Context.MODE_PRIVATE)
                .edit().putBoolean("is_admin_logged_in", false).apply()

            auth.signOut()
            val intent = Intent(requireContext(), SplashActivity::class.java)
            intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
            startActivity(intent)
            requireActivity().finish()
        }

        dialog.show()
    }

    private fun loadProfile() {
        val uid = auth.currentUser?.uid ?: return
        val email = auth.currentUser?.email ?: ""
        
        firestore.collection("admins").document(uid).get()
            .addOnSuccessListener { doc ->
                if (isAdded) {
                    if (doc.exists()) {
                        val name = doc.getString("name") ?: "Admin"
                        currentPhoneNumber = doc.getString("phoneNumber") ?: ""
                        
                        // Enforce strictly: Only topakulipro is Administrator
                        val role = if (email == "topakulipro@gmail.com") "Administrator" else "Pengelola"
                        
                        if (role == "Administrator") {
                            binding.btnDaftarPengelola.visibility = View.VISIBLE
                            binding.tvLabelAkun.visibility = View.VISIBLE
                        } else {
                            binding.btnDaftarPengelola.visibility = View.GONE
                            binding.tvLabelAkun.visibility = View.GONE
                        }
                        binding.tvAdminName.text = name
                        binding.tvAdminEmailRole.text = "${email.substringBefore("@")} - $role"
                        
                        val initials = name.split(" ")
                            .filter { it.isNotBlank() }
                            .take(2)
                            .map { it[0].uppercase() }
                            .joinToString("")
                        binding.tvAdminInitial.text = if (initials.isNotEmpty()) initials else "AD"
                    } else {
                        // Document doesn't exist, use fallback
                        binding.tvAdminName.text = "Admin"
                        binding.tvAdminEmailRole.text = email
                        binding.tvAdminInitial.text = "AD"
                        binding.btnDaftarPengelola.visibility = View.GONE
                    }
                }
            }
            .addOnFailureListener {
                if (isAdded) {
                    binding.tvAdminName.text = "Admin"
                    binding.tvAdminEmailRole.text = email
                    binding.tvAdminInitial.text = "AD"
                }
            }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
