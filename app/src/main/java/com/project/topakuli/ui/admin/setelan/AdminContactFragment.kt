package com.project.topakuli.ui.admin.setelan

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import com.google.firebase.firestore.FirebaseFirestore
import com.project.topakuli.databinding.FragmentAdminContactBinding

class AdminContactFragment : Fragment() {

    private var _binding: FragmentAdminContactBinding? = null
    private val binding get() = _binding!!
    private val firestore = FirebaseFirestore.getInstance()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentAdminContactBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        loadCurrentContacts()

        binding.btnBack.setOnClickListener {
            findNavController().navigateUp()
        }

        binding.btnSave.setOnClickListener {
            saveContacts()
        }
    }

    private fun loadCurrentContacts() {
        binding.progressBar.visibility = View.VISIBLE
        firestore.collection("settings").document("contacts").get()
            .addOnSuccessListener { doc ->
                binding.progressBar.visibility = View.GONE
                if (doc.exists()) {
                    binding.etWaPuskesmas.setText(doc.getString("wa_puskesmas") ?: "")
                    binding.etWaAdmin.setText(doc.getString("wa_admin") ?: "")
                    binding.etPhoneAdmin.setText(doc.getString("phone_admin") ?: "")
                    binding.etEmailAdmin.setText(doc.getString("email_admin") ?: "")
                }
            }
            .addOnFailureListener {
                binding.progressBar.visibility = View.GONE
                Toast.makeText(requireContext(), "Gagal memuat data: ${it.message}", Toast.LENGTH_SHORT).show()
            }
    }

    private fun saveContacts() {
        val waPuskesmas = binding.etWaPuskesmas.text.toString().trim()
        val waAdmin = binding.etWaAdmin.text.toString().trim()
        val phoneAdmin = binding.etPhoneAdmin.text.toString().trim()
        val emailAdmin = binding.etEmailAdmin.text.toString().trim()

        if (waPuskesmas.isEmpty() || waAdmin.isEmpty() || phoneAdmin.isEmpty() || emailAdmin.isEmpty()) {
            Toast.makeText(requireContext(), "Harap isi semua field", Toast.LENGTH_SHORT).show()
            return
        }

        val contactData = hashMapOf(
            "wa_puskesmas" to waPuskesmas,
            "wa_admin" to waAdmin,
            "phone_admin" to phoneAdmin,
            "email_admin" to emailAdmin
        )

        binding.progressBar.visibility = View.VISIBLE
        binding.btnSave.isEnabled = false

        firestore.collection("settings").document("contacts")
            .set(contactData)
            .addOnSuccessListener {
                binding.progressBar.visibility = View.GONE
                binding.btnSave.isEnabled = true
                Toast.makeText(requireContext(), "Kontak berhasil diperbarui", Toast.LENGTH_SHORT).show()
            }
            .addOnFailureListener {
                binding.progressBar.visibility = View.GONE
                binding.btnSave.isEnabled = true
                Toast.makeText(requireContext(), "Gagal menyimpan: ${it.message}", Toast.LENGTH_SHORT).show()
            }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
