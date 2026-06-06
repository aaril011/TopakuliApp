package com.project.topakuli.ui.Setelan

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.LinearLayout
import androidx.navigation.fragment.findNavController
import com.google.android.material.bottomsheet.BottomSheetDialog
import com.project.topakuli.R

class SetelanFragment : Fragment() {

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_setelan, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        view.findViewById<LinearLayout>(R.id.btn_tips_aman).setOnClickListener {
            findNavController().navigate(R.id.action_setelan_to_tipsAman)
        }

        view.findViewById<LinearLayout>(R.id.btn_tentang).setOnClickListener {
            findNavController().navigate(R.id.action_setelan_to_tentang)
        }

        view.findViewById<LinearLayout>(R.id.btn_hubungi_kami).setOnClickListener {
            showContactBottomSheet()
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
