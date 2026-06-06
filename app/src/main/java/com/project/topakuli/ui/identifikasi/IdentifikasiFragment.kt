package com.project.topakuli.ui.identifikasi

import android.Manifest
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.camera.core.*
import androidx.camera.lifecycle.ProcessCameraProvider
import androidx.camera.view.PreviewView
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import com.project.topakuli.R

class IdentifikasiFragment : Fragment() {

    private var imageCapture: ImageCapture? = null
    private var camera: Camera? = null
    private var lensFacing = CameraSelector.LENS_FACING_BACK
    private var isFlashOn = false
    private val requestPermissionLauncher =
        registerForActivityResult(ActivityResultContracts.RequestPermission()) { isGranted ->
            if (isGranted) startCamera() else Toast.makeText(requireContext(), "Izin kamera diperlukan!", Toast.LENGTH_SHORT).show()
        }
    private val pickImageLauncher =
        registerForActivityResult(ActivityResultContracts.GetContent()) { uri: Uri? ->
            if (uri != null) {
                Log.d("TopakuliGallery", "Gambar dipilih: $uri")
                Toast.makeText(requireContext(), "Gambar berhasil dipilih!", Toast.LENGTH_SHORT).show()
            } else {
                Log.d("TopakuliGallery", "Pemilihan gambar dibatalkan")
            }
        }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        return inflater.inflate(R.layout.fragment_identifikasi, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        if (ContextCompat.checkSelfPermission(requireContext(), Manifest.permission.CAMERA) == PackageManager.PERMISSION_GRANTED) {
            startCamera()
        } else {
            requestPermissionLauncher.launch(Manifest.permission.CAMERA)
        }

        view.findViewById<ImageView>(R.id.btn_back).setOnClickListener {
            requireActivity().onBackPressedDispatcher.onBackPressed()
        }

        view.findViewById<ImageView>(R.id.btn_capture).setOnClickListener {
            takePhoto()
        }

        view.findViewById<ImageView>(R.id.btn_flip_camera).setOnClickListener {
            lensFacing = if (lensFacing == CameraSelector.LENS_FACING_BACK) CameraSelector.LENS_FACING_FRONT else CameraSelector.LENS_FACING_BACK
            startCamera()
        }

        val btnFlash = view.findViewById<ImageView>(R.id.btn_flash)
        btnFlash.setOnClickListener {
            isFlashOn = !isFlashOn
            camera?.cameraControl?.enableTorch(isFlashOn)

            if (isFlashOn) {
                btnFlash.setColorFilter(ContextCompat.getColor(requireContext(), android.R.color.holo_orange_light))
            } else {
                btnFlash.setColorFilter(ContextCompat.getColor(requireContext(), android.R.color.white))
            }
        }

        val btnGallery = view.findViewById<LinearLayout>(R.id.btn_gallery)
        btnGallery.setOnClickListener {
            openGallery()
        }

        view.findViewById<LinearLayout>(R.id.btn_petunjuk).setOnClickListener {
            findNavController().navigate(R.id.action_identifikasi_to_petunjuk)
        }
    }

    private fun openGallery() {
        pickImageLauncher.launch("image/*")
    }

    private fun startCamera() {
        val cameraProviderFuture = ProcessCameraProvider.getInstance(requireContext())
        cameraProviderFuture.addListener({
            val cameraProvider = cameraProviderFuture.get()
            val previewView = view?.findViewById<PreviewView>(R.id.viewFinder)

            val preview = Preview.Builder().build().also {
                it.setSurfaceProvider(previewView?.surfaceProvider)
            }

            imageCapture = ImageCapture.Builder().build()
            val cameraSelector = CameraSelector.Builder().requireLensFacing(lensFacing).build()

            try {
                cameraProvider.unbindAll()
                camera = cameraProvider.bindToLifecycle(
                    viewLifecycleOwner, cameraSelector, preview, imageCapture
                )
                camera?.cameraControl?.enableTorch(isFlashOn)
            } catch (exc: Exception) {
                Log.e("CameraX", "Gagal memulai kamera", exc)
            }
        }, ContextCompat.getMainExecutor(requireContext()))
    }

    private fun takePhoto() {
        Toast.makeText(requireContext(), "Mengambil gambar...", Toast.LENGTH_SHORT).show()
    }
}