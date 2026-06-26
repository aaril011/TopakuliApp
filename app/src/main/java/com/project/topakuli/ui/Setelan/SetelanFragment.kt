package com.project.topakuli.ui.Setelan

import android.content.Intent
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import android.widget.Toast
import com.google.android.material.materialswitch.MaterialSwitch
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import com.google.android.material.bottomsheet.BottomSheetDialog
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.project.topakuli.R
import com.project.topakuli.TopakuliApplication
import com.project.topakuli.ui.admin.auth.LoginAdminActivity

class SetelanFragment : Fragment() {

    private val viewModel: SetelanViewModel by viewModels {
        SetelanViewModel.Factory((requireActivity().application as TopakuliApplication).repository)
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_setelan, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val tvSpesiesCount = view.findViewById<TextView>(R.id.tv_spesies_count)
        val tvTeridentifikasiCount = view.findViewById<TextView>(R.id.tv_teridentifikasi_count)
        val tvResepCount = view.findViewById<TextView>(R.id.tv_resep_count)

        viewModel.tanamanCount.observe(viewLifecycleOwner) { count ->
            tvSpesiesCount.text = count.toString()
        }

        viewModel.historyCount.observe(viewLifecycleOwner) { count ->
            tvTeridentifikasiCount.text = count.toString()
        }

        viewModel.resepCount.observe(viewLifecycleOwner) { count ->
            tvResepCount.text = count.toString()
        }

        view.findViewById<LinearLayout>(R.id.btn_tips_aman).setOnClickListener {
            findNavController().navigate(R.id.action_setelan_to_tipsAman)
        }

        view.findViewById<LinearLayout>(R.id.btn_tentang).setOnClickListener {
            findNavController().navigate(R.id.action_setelan_to_tentang)
        }

        view.findViewById<LinearLayout>(R.id.btn_kontribusi).setOnClickListener {
            findNavController().navigate(R.id.action_setelan_to_kontribusi)
        }

        view.findViewById<LinearLayout>(R.id.btn_hubungi_kami).setOnClickListener {
            showContactBottomSheet()
        }

        view.findViewById<LinearLayout>(R.id.btn_pengelola).setOnClickListener {
            val intent = Intent(requireContext(), LoginAdminActivity::class.java)
            startActivity(intent)
        }

        setupNotificationSwitch(view)
    }

    private fun setupNotificationSwitch(view: View) {
        val switchNotif = view.findViewById<MaterialSwitch>(R.id.switch_notification)
        val prefs = requireContext().getSharedPreferences("topakuli_notif_prefs", android.content.Context.MODE_PRIVATE)
        
        // Set initial state
        switchNotif.isChecked = prefs.getBoolean("notifications_enabled", true)

        switchNotif.setOnCheckedChangeListener { _, isChecked ->
            prefs.edit().putBoolean("notifications_enabled", isChecked).apply()
            
            if (isChecked) {
                Toast.makeText(requireContext(), "Notifikasi diaktifkan", Toast.LENGTH_SHORT).show()
                // Restart notification listener in MainActivity if needed, 
                // but usually it requires app restart or a broadcast
            } else {
                Toast.makeText(requireContext(), "Notifikasi dimatikan", Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun showContactBottomSheet() {
        val bottomSheetView = layoutInflater.inflate(R.layout.layout_contact_bottom_sheet, null)
        val dialog = BottomSheetDialog(requireContext(), R.style.BottomSheetDialogTheme)
        dialog.setContentView(bottomSheetView)

        bottomSheetView.findViewById<ImageView>(R.id.btn_close).setOnClickListener {
            dialog.dismiss()
        }

        dialog.show()
    }
}
