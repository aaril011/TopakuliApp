package com.project.topakuli.ui.Setelan

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import com.project.topakuli.R
import com.project.topakuli.TopakuliApplication

class TentangFragment : Fragment() {

    private val viewModel: SetelanViewModel by viewModels {
        SetelanViewModel.Factory((requireActivity().application as TopakuliApplication).repository)
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_tentang, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val tvTanamanCount = view.findViewById<TextView>(R.id.tv_tanaman_count)
        val tvResepCount = view.findViewById<TextView>(R.id.tv_resep_count)
        val tvAdminCount = view.findViewById<TextView>(R.id.tv_admin_count)
        val tvHistoryCount = view.findViewById<TextView>(R.id.tv_history_count)

        viewModel.tanamanCount.observe(viewLifecycleOwner) { count ->
            tvTanamanCount.text = count.toString()
        }

        viewModel.resepCount.observe(viewLifecycleOwner) { count ->
            tvResepCount.text = count.toString()
        }

        viewModel.adminCount.observe(viewLifecycleOwner) { count ->
            tvAdminCount.text = count.toString()
        }

        viewModel.historyCount.observe(viewLifecycleOwner) { count ->
            tvHistoryCount.text = count.toString()
        }

        view.findViewById<ImageView>(R.id.btn_back).setOnClickListener {
            findNavController().navigateUp()
        }
    }
}
