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
import androidx.recyclerview.widget.LinearLayoutManager
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
import com.project.topakuli.R
import com.project.topakuli.databinding.FragmentAdminManagerListBinding

class AdminManagerListFragment : Fragment() {

    private var _binding: FragmentAdminManagerListBinding? = null
    private val binding get() = _binding!!
    private val firestore = FirebaseFirestore.getInstance()
    private lateinit var adapter: AdminManagerAdapter

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        _binding = FragmentAdminManagerListBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        
        setupRecyclerView()
        loadManagers()
        
        binding.btnBack.setOnClickListener { findNavController().navigateUp() }
        binding.btnAddManager.setOnClickListener { showAddManagerDialog() }
    }

    private fun setupRecyclerView() {
        adapter = AdminManagerAdapter { manager ->
            val bundle = Bundle().apply {
                putString("manager_uid", manager.uid)
            }
            findNavController().navigate(R.id.action_admin_manager_list_to_detail, bundle)
        }
        binding.rvManagers.apply {
            layoutManager = LinearLayoutManager(requireContext())
            adapter = this@AdminManagerListFragment.adapter
        }
    }

    private fun loadManagers() {
        binding.progressBar.visibility = View.VISIBLE
        firestore.collection("admins")
            .whereEqualTo("role", "Pengelola")
            .addSnapshotListener { snapshot, e ->
                if (isAdded) {
                    binding.progressBar.visibility = View.GONE
                    if (e != null) {
                        android.util.Log.e("AdminManagerList", "Error fetching managers", e)
                        Toast.makeText(requireContext(), "Gagal memuat data: ${e.localizedMessage}", Toast.LENGTH_LONG).show()
                        return@addSnapshotListener
                    }
                    
                    val managers = snapshot?.documents?.mapNotNull { doc ->
                        ManagerModel(
                            uid = doc.id,
                            name = doc.getString("name") ?: "",
                            email = doc.getString("email") ?: "",
                            phoneNumber = doc.getString("phoneNumber") ?: ""
                        )
                    }?.sortedByDescending { manager -> 
                        // Sorting locally using the snapshot to get the timestamp
                        snapshot.documents.find { it.id == manager.uid }?.getLong("createdAt") ?: 0L
                    } ?: emptyList()
                    
                    adapter.submitList(managers)
                    binding.tvEmpty.visibility = if (managers.isEmpty()) View.VISIBLE else View.GONE
                }
            }
    }

    private fun showAddManagerDialog() {
        val dialogView = LayoutInflater.from(requireContext()).inflate(R.layout.dialog_admin_add_manager, null)
        val dialog = AlertDialog.Builder(requireContext()).setView(dialogView).create()
        dialog.window?.setBackgroundDrawableResource(android.R.color.transparent)

        val etName = dialogView.findViewById<EditText>(R.id.et_new_admin_name)
        val etEmail = dialogView.findViewById<EditText>(R.id.et_new_admin_email)
        val etPhone = dialogView.findViewById<EditText>(R.id.et_new_admin_phone)
        val etPass = dialogView.findViewById<EditText>(R.id.et_new_admin_password)
        val btnSubmit = dialogView.findViewById<View>(R.id.btn_submit_new_admin)
        val btnClose = dialogView.findViewById<View>(R.id.btn_close_dialog)

        btnClose.setOnClickListener { dialog.dismiss() }

        btnSubmit.setOnClickListener {
            val name = etName.text.toString().trim()
            val email = etEmail.text.toString().trim()
            val phone = etPhone.text.toString().trim()
            val pass = etPass.text.toString().trim()

            if (name.isEmpty() || email.isEmpty() || phone.isEmpty() || pass.isEmpty()) {
                Toast.makeText(requireContext(), "Harap isi semua kolom", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            if (pass.length < 6) {
                Toast.makeText(requireContext(), "Password minimal 6 karakter", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            btnSubmit.isEnabled = false
            registerNewManager(name, email, phone, pass, dialog, btnSubmit)
        }

        dialog.show()
    }

    private fun registerNewManager(name: String, email: String, phone: String, pass: String, dialog: AlertDialog, btnSubmit: View) {
        val currentApp = com.google.firebase.FirebaseApp.getInstance()
        val options = currentApp.options
        
        try {
            val secondaryApp = try {
                com.google.firebase.FirebaseApp.initializeApp(requireContext(), options, "secondary")
            } catch (e: Exception) {
                com.google.firebase.FirebaseApp.getInstance("secondary")
            }
            
            val secondaryAuth = FirebaseAuth.getInstance(secondaryApp)
            
            secondaryAuth.createUserWithEmailAndPassword(email, pass)
                .addOnSuccessListener { result ->
                    val uid = result.user?.uid ?: ""
                    val adminData = mapOf(
                        "name" to name,
                        "email" to email,
                        "phoneNumber" to phone,
                        "role" to "Pengelola",
                        "createdAt" to System.currentTimeMillis()
                    )
                    
                    firestore.collection("admins").document(uid).set(adminData)
                        .addOnSuccessListener {
                            Toast.makeText(requireContext(), "Pengelola berhasil didaftarkan", Toast.LENGTH_SHORT).show()
                            secondaryAuth.signOut()
                            dialog.dismiss()
                        }
                }
                .addOnFailureListener { e ->
                    btnSubmit.isEnabled = true
                    Toast.makeText(requireContext(), "Gagal: ${e.message}", Toast.LENGTH_SHORT).show()
                }
        } catch (e: Exception) {
            btnSubmit.isEnabled = true
            Toast.makeText(requireContext(), "Error: ${e.message}", Toast.LENGTH_SHORT).show()
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
