package com.project.topakuli.ui.admin.common

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.navigation.fragment.findNavController
import com.project.topakuli.R
import com.google.android.material.tabs.TabLayout
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.ListenerRegistration
import com.project.topakuli.data.remote.model.ContributionForm
import com.project.topakuli.databinding.FragmentAdminMasukanListBinding

class AdminMasukanListFragment : Fragment() {

    private var _binding: FragmentAdminMasukanListBinding? = null
    private val binding get() = _binding!!
    
    private var firestoreListener: ListenerRegistration? = null
    private lateinit var contributionAdapter: AdminContributionAdapter

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        _binding = FragmentAdminMasukanListBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setupUI()
        setupListeners()
    }

    private fun setupUI() {
        contributionAdapter = AdminContributionAdapter { contribution ->
            val bundle = Bundle().apply {
                putString("CONTRIBUTION_ID", contribution.id)
                putString("CONTRIBUTION_TYPE", contribution.type)
            }
            findNavController().navigate(R.id.action_adminMasukanListFragment_to_adminContributionDetailFragment, bundle)
        }

        binding.rvMasukan.apply {
            layoutManager = LinearLayoutManager(requireContext())
            adapter = contributionAdapter
        }

        binding.tabLayout.addOnTabSelectedListener(object : TabLayout.OnTabSelectedListener {
            override fun onTabSelected(tab: TabLayout.Tab?) {
                setupListeners() // Reload data for the selected tab
            }
            override fun onTabUnselected(tab: TabLayout.Tab?) {}
            override fun onTabReselected(tab: TabLayout.Tab?) {}
        })
    }

    private fun setupListeners() {
        // Remove existing listener if any
        firestoreListener?.remove()

        val firestore = FirebaseFirestore.getInstance()
        val filterType = when (binding.tabLayout.selectedTabPosition) {
            0 -> "resep"
            1 -> "tanaman"
            2 -> "koreksi"
            else -> "resep"
        }

        firestoreListener = firestore.collection("user_contributions")
            .whereEqualTo("type", filterType)
            .addSnapshotListener { snapshot, e ->
                if (e != null) {
                    if (isAdded) Toast.makeText(requireContext(), "Error: ${e.message}", Toast.LENGTH_SHORT).show()
                    return@addSnapshotListener
                }

                val contributions = snapshot?.documents?.mapNotNull { doc ->
                    try {
                        doc.toObject(ContributionForm::class.java)?.copy(id = doc.id)
                    } catch (ex: Exception) { null }
                }?.sortedByDescending { it.timestamp } ?: emptyList()

                contributionAdapter.submitList(contributions)
                binding.tvEmpty.visibility = if (contributions.isEmpty()) View.VISIBLE else View.GONE
            }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        firestoreListener?.remove()
        _binding = null
    }
}
