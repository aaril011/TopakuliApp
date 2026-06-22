package com.project.topakuli.ui.admin.common

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import com.google.firebase.firestore.FirebaseFirestore
import com.project.topakuli.data.local.entity.ChatMessage
import com.project.topakuli.databinding.FragmentAdminMasukanChatBinding

class AdminMasukanChatFragment : Fragment() {

    private var _binding: FragmentAdminMasukanChatBinding? = null
    private val binding get() = _binding!!
    private val firestore = FirebaseFirestore.getInstance()
    private var listenerRegistration: com.google.firebase.firestore.ListenerRegistration? = null
    
    private var contributionId: String? = null
    private var userName: String? = null
    private var userEmail: String? = null
    private var userPhone: String? = null
    private lateinit var adapter: AdminChatAdapter

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        _binding = FragmentAdminMasukanChatBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        
        contributionId = arguments?.getString("CONTRIBUTION_ID")
        userName = arguments?.getString("USER_NAME")
        
        binding.tvUserNameChat.text = userName ?: "Obrolan"
        binding.btnBack.setOnClickListener { findNavController().navigateUp() }
        
        setupRecyclerView()
        setupRealtimeListener()
        fetchUserDetails()
    }

    private fun fetchUserDetails() {
        val id = contributionId ?: return
        firestore.collection("contributions").document(id).get()
            .addOnSuccessListener { doc ->
                if (isAdded && doc.exists()) {
                    userEmail = doc.getString("userEmail")
                    userPhone = doc.getString("userPhone")
                    
                    binding.btnEmailAction.setOnClickListener {
                        if (!userEmail.isNullOrEmpty()) openEmail(userEmail!!)
                        else Toast.makeText(requireContext(), "Email pengguna tidak tersedia", Toast.LENGTH_SHORT).show()
                    }
                    
                    binding.btnWhatsappAction.setOnClickListener {
                        if (!userPhone.isNullOrEmpty()) openWhatsApp(userPhone!!)
                        else Toast.makeText(requireContext(), "Nomor WA pengguna tidak tersedia", Toast.LENGTH_SHORT).show()
                    }
                }
            }
    }

    private fun openEmail(email: String) {
        val intent = android.content.Intent(android.content.Intent.ACTION_SENDTO).apply {
            data = android.net.Uri.parse("mailto:$email")
            putExtra(android.content.Intent.EXTRA_SUBJECT, "Tanggapan Kontribusi Topakuli")
        }
        try {
            startActivity(intent)
        } catch (e: Exception) {
            Toast.makeText(requireContext(), "Aplikasi email tidak ditemukan", Toast.LENGTH_SHORT).show()
        }
    }

    private fun openWhatsApp(phone: String) {
        val formattedPhone = if (phone.startsWith("0")) "62" + phone.substring(1) else phone
        val url = "https://api.whatsapp.com/send?phone=$formattedPhone"
        val intent = android.content.Intent(android.content.Intent.ACTION_VIEW).apply {
            data = android.net.Uri.parse(url)
        }
        try {
            startActivity(intent)
        } catch (e: Exception) {
            Toast.makeText(requireContext(), "WhatsApp tidak terpasang", Toast.LENGTH_SHORT).show()
        }
    }

    private fun setupRecyclerView() {
        adapter = AdminChatAdapter()
        binding.rvChatMasukan.apply {
            layoutManager = LinearLayoutManager(requireContext())
            adapter = this@AdminMasukanChatFragment.adapter
        }
    }

    private fun setupRealtimeListener() {
        val id = contributionId ?: return
        val firestore = FirebaseFirestore.getInstance()
        
        listenerRegistration = firestore.collection("contributions").document(id)
            .collection("messages")
            .addSnapshotListener { snapshot, e ->
                if (e != null) {
                    android.util.Log.e("AdminMasukanChat", "Listen failed.", e)
                    return@addSnapshotListener
                }
                
                val messages = snapshot?.documents?.mapNotNull { doc ->
                    ChatMessage(
                        id = doc.id,
                        senderId = doc.getString("senderId") ?: "",
                        senderName = doc.getString("senderName") ?: "",
                        message = doc.getString("message") ?: "",
                        timestamp = doc.getLong("timestamp") ?: 0L,
                        senderType = doc.getString("senderType") ?: "user"
                    )
                }?.sortedBy { it.timestamp } ?: emptyList()
                
                adapter.submitList(messages)
                if (messages.isNotEmpty()) {
                    binding.rvChatMasukan.scrollToPosition(messages.size - 1)
                }
                
                // Mark as read when admin opens the chat
                if (messages.any { it.senderType == "user" }) {
                    firestore.collection("contributions").document(id).update("isReadByAdmin", true)
                }
            }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        listenerRegistration?.remove()
        _binding = null
    }
}
