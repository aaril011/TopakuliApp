package com.project.topakuli.ui.admin.setelan

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.EditText
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import com.google.firebase.firestore.FirebaseFirestore
import com.project.topakuli.R
import com.project.topakuli.databinding.FragmentAdminManagerDetailBinding

class AdminManagerDetailFragment : Fragment() {

    private var _binding: FragmentAdminManagerDetailBinding? = null
    private val binding get() = _binding!!
    private val firestore = FirebaseFirestore.getInstance()
    
    private var managerUid: String? = null
    private var managerName: String = ""
    private var managerEmail: String = ""
    private var managerPhone: String = ""

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        _binding = FragmentAdminManagerDetailBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        
        managerUid = arguments?.getString("manager_uid")
        
        if (managerUid == null) {
            Toast.makeText(requireContext(), "Data tidak ditemukan", Toast.LENGTH_SHORT).show()
            findNavController().navigateUp()
            return
        }

        loadManagerData()
        
        binding.btnBack.setOnClickListener { findNavController().navigateUp() }
        binding.btnDeleteManager.setOnClickListener { showDeleteConfirmation() }
    }

    private fun loadManagerData() {
        managerUid?.let { uid ->
            firestore.collection("admins").document(uid).get()
                .addOnSuccessListener { doc ->
                    if (isAdded && doc.exists()) {
                        managerName = doc.getString("name") ?: ""
                        managerEmail = doc.getString("email") ?: ""
                        managerPhone = doc.getString("phoneNumber") ?: ""
                        
                        binding.tvManagerName.text = managerName
                        binding.tvManagerEmail.text = managerEmail
                        binding.tvManagerPhone.text = managerPhone.ifEmpty { "-" }
                        
                        val initials = managerName.split(" ")
                            .filter { it.isNotBlank() }
                            .take(2)
                            .map { it[0].uppercase() }
                            .joinToString("")
                        binding.tvManagerInitial.text = if (initials.isNotEmpty()) initials else "AD"
                    }
                }
        }
    }

    private fun showDeleteConfirmation() {
        val dialogView = LayoutInflater.from(requireContext()).inflate(R.layout.dialog_confirm_delete_manager, null)
        val dialog = AlertDialog.Builder(requireContext()).setView(dialogView).create()
        dialog.window?.setBackgroundDrawableResource(android.R.color.transparent)

        val tvMessage = dialogView.findViewById<android.widget.TextView>(R.id.tv_delete_message)
        val etEmail = dialogView.findViewById<EditText>(R.id.et_confirm_email)
        val btnConfirm = dialogView.findViewById<View>(R.id.btn_confirm_delete)
        val btnClose = dialogView.findViewById<View>(R.id.btn_close_dialog)

        tvMessage.text = "Apakah yakin data \"$managerName\" dihapus?"
        
        btnClose.setOnClickListener { dialog.dismiss() }
        
        btnConfirm.setOnClickListener {
            val inputEmail = etEmail.text.toString().trim()
            if (inputEmail == managerEmail) {
                performDelete()
                dialog.dismiss()
            } else {
                Toast.makeText(requireContext(), "Email tidak sesuai", Toast.LENGTH_SHORT).show()
            }
        }

        dialog.show()
    }

    private fun performDelete() {
        managerUid?.let { uid ->
            val currentUser = com.google.firebase.auth.FirebaseAuth.getInstance().currentUser
            if (uid == currentUser?.uid) {
                Toast.makeText(requireContext(), "Anda tidak dapat menghapus akun sendiri", Toast.LENGTH_SHORT).show()
                return
            }

            if (managerEmail == "topakulipro@gmail.com") {
                Toast.makeText(requireContext(), "Akun administrator utama tidak dapat dihapus", Toast.LENGTH_SHORT).show()
                return
            }

            firestore.collection("admins").document(uid).delete()
                .addOnSuccessListener {
                    Toast.makeText(requireContext(), "Pengelola berhasil dihapus", Toast.LENGTH_SHORT).show()
                    findNavController().navigateUp()
                }
                .addOnFailureListener {
                    Toast.makeText(requireContext(), "Gagal menghapus data", Toast.LENGTH_SHORT).show()
                }
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
