package com.project.topakuli.ui.Setelan

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import com.google.android.material.button.MaterialButton
import com.google.firebase.firestore.FirebaseFirestore
import com.project.topakuli.R

class TipsAmanFragment : Fragment() {

    private val firestore = FirebaseFirestore.getInstance()
    private var waPuskesmas: String = "+6281234567890" // Default fallback

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_tips_aman, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        loadContacts()

        view.findViewById<ImageView>(R.id.btn_back).setOnClickListener {
            findNavController().navigateUp()
        }

        view.findViewById<MaterialButton>(R.id.btn_whatsapp).setOnClickListener {
            val url = "https://api.whatsapp.com/send?phone=${waPuskesmas.replace(" ", "").replace("+", "")}"
            val intent = Intent(Intent.ACTION_VIEW)
            intent.data = Uri.parse(url)
            try {
                startActivity(intent)
            } catch (e: Exception) {
                Toast.makeText(requireContext(), "WhatsApp tidak terpasang", Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun loadContacts() {
        firestore.collection("settings").document("contacts").get()
            .addOnSuccessListener { doc ->
                if (doc.exists()) {
                    waPuskesmas = doc.getString("wa_puskesmas") ?: waPuskesmas
                }
            }
    }
}
