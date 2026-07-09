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
import com.google.firebase.firestore.FirebaseFirestore
import com.project.topakuli.R
import com.project.topakuli.TopakuliApplication
import com.project.topakuli.ui.admin.auth.LoginAdminActivity

class SetelanFragment : Fragment() {

    private val firestore = FirebaseFirestore.getInstance()
    private var waAdmin: String? = null
    private var phoneAdmin: String? = null
    private var emailAdmin: String? = null

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

        val tvWa = bottomSheetView.findViewById<TextView>(R.id.contact_whatsapp_val)
        val tvPhone = bottomSheetView.findViewById<TextView>(R.id.contact_phone_val)
        val tvEmail = bottomSheetView.findViewById<TextView>(R.id.contact_email_val)

        // Pre-fill with defaults from strings.xml
        tvWa?.text = getString(R.string.contact_whatsapp_val)
        tvPhone?.text = getString(R.string.contact_phone_val)
        tvEmail?.text = getString(R.string.contact_email_val)

        // Fetch from Firestore
        firestore.collection("settings").document("contacts").get()
            .addOnSuccessListener { doc ->
                if (doc.exists()) {
                    waAdmin = doc.getString("wa_admin")
                    phoneAdmin = doc.getString("phone_admin")
                    emailAdmin = doc.getString("email_admin")

                    waAdmin?.let { tvWa?.text = it }
                    phoneAdmin?.let { tvPhone?.text = it }
                    emailAdmin?.let { tvEmail?.text = it }
                }
            }

        bottomSheetView.findViewById<ImageView>(R.id.btn_close).setOnClickListener {
            dialog.dismiss()
        }

        // WhatsApp Action
        bottomSheetView.findViewById<View>(R.id.btn_wa_action).setOnClickListener {
            val rawPhone = waAdmin ?: getString(R.string.contact_whatsapp_val)
            val phoneNumber = rawPhone.replace(" ", "").replace("-", "").replace("+", "")
            val intent = android.content.Intent(android.content.Intent.ACTION_VIEW).apply {
                data = android.net.Uri.parse("https://api.whatsapp.com/send?phone=$phoneNumber")
            }
            try {
                startActivity(intent)
            } catch (e: Exception) {
                Toast.makeText(requireContext(), "Aplikasi WhatsApp tidak ditemukan", Toast.LENGTH_SHORT).show()
            }
        }

        // Phone Action
        bottomSheetView.findViewById<View>(R.id.btn_phone_action).setOnClickListener {
            val rawPhone = phoneAdmin ?: getString(R.string.contact_phone_val)
            val phoneNumber = rawPhone.replace(" ", "").replace("-", "").replace("+", "")
            val intent = android.content.Intent(android.content.Intent.ACTION_DIAL).apply {
                data = android.net.Uri.parse("tel:$phoneNumber")
            }
            startActivity(intent)
        }

        // Email Action
        bottomSheetView.findViewById<View>(R.id.btn_email_action).setOnClickListener {
            val email = emailAdmin ?: getString(R.string.contact_email_val)
            val intent = android.content.Intent(android.content.Intent.ACTION_SENDTO).apply {
                data = android.net.Uri.parse("mailto:$email")
                putExtra(android.content.Intent.EXTRA_SUBJECT, "Pertanyaan Aplikasi Topakuli")
            }
            try {
                startActivity(intent)
            } catch (e: Exception) {
                Toast.makeText(requireContext(), "Aplikasi Email tidak ditemukan", Toast.LENGTH_SHORT).show()
            }
        }

        dialog.show()
    }
}
