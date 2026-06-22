package com.project.topakuli.ui.admin.common

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import com.project.topakuli.R
import com.project.topakuli.TopakuliApplication
import com.project.topakuli.databinding.FragmentAdminMasukanListBinding
import com.project.topakuli.ui.admin.beranda.AdminDashboardViewModel
import kotlinx.coroutines.launch

import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
import com.project.topakuli.data.local.entity.UserContribution

class AdminMasukanListFragment : Fragment() {

    private var _binding: FragmentAdminMasukanListBinding? = null
    private val binding get() = _binding!!
    private var listenerRegistration: com.google.firebase.firestore.ListenerRegistration? = null

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        _binding = FragmentAdminMasukanListBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setupUI()
        setupRealtimeListener()
    }

    private fun setupUI() {
        val adapter = AdminMasukanAdapter { contribution ->
            val bundle = Bundle().apply {
                putString("CONTRIBUTION_ID", contribution.id)
                putString("USER_NAME", contribution.userName)
            }
            findNavController().navigate(R.id.action_adminMasukanListFragment_to_adminMasukanChatFragment, bundle)
        }

        binding.rvMasukan.apply {
            layoutManager = LinearLayoutManager(requireContext())
            this.adapter = adapter
        }
    }

    private fun setupRealtimeListener() {
        val firestore = FirebaseFirestore.getInstance()
        listenerRegistration = firestore.collection("contributions")
            .addSnapshotListener { snapshot, e ->
                if (e != null) {
                    android.util.Log.e("AdminMasukanList", "Firestore error: ${e.message}", e)
                    if (isAdded) {
                        Toast.makeText(requireContext(), "Error: ${e.localizedMessage}", Toast.LENGTH_LONG).show()
                    }
                    return@addSnapshotListener
                }
                
                if (snapshot == null) return@addSnapshotListener
                
                android.util.Log.d("AdminMasukanList", "Fetched ${snapshot.size()} contribution documents")
                
                val contributions = snapshot.documents.mapNotNull { doc ->
                    try {
                        UserContribution(
                            id = doc.id,
                            userId = doc.getString("userId") ?: "",
                            userName = doc.getString("userName") ?: "Pengguna Anonim",
                            userEmail = doc.getString("userEmail") ?: "",
                            userPhone = doc.getString("userPhone") ?: "",
                            lastMessage = doc.getString("lastMessage") ?: "Pesan baru",
                            timestamp = doc.getLong("timestamp") ?: 0L,
                            isReadByAdmin = doc.getBoolean("isReadByAdmin") ?: false
                        )
                    } catch (ex: Exception) {
                        android.util.Log.e("AdminMasukanList", "Mapping error for doc ${doc.id}", ex)
                        null
                    }
                }.sortedByDescending { it.timestamp }
                
                (binding.rvMasukan.adapter as AdminMasukanAdapter).submitList(contributions)
                binding.tvEmpty.visibility = if (contributions.isEmpty()) View.VISIBLE else View.GONE
            }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        listenerRegistration?.remove()
        _binding = null
    }
}
