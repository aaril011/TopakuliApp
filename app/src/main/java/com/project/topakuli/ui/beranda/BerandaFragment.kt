package com.project.topakuli.ui.beranda

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.fragment.app.Fragment
import com.project.topakuli.R

class BerandaFragment : Fragment() {

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        return inflater.inflate(R.layout.fragment_beranda, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // Tambahkan padding top sebesar tinggi status bar ke header
        val headerLayout = view.findViewById<View>(R.id.header_layout)
        ViewCompat.setOnApplyWindowInsetsListener(headerLayout) { v, insets ->
            val statusBarHeight = insets.getInsets(WindowInsetsCompat.Type.statusBars()).top
            v.setPadding(
                v.paddingLeft,
                statusBarHeight + resources.getDimensionPixelSize(R.dimen.header_padding_top),
                v.paddingRight,
                v.paddingBottom
            )
            insets
        }
    }
}