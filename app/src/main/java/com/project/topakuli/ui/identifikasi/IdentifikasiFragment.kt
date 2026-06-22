package com.project.topakuli.ui.identifikasi

import android.Manifest
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.graphics.ImageDecoder
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.provider.MediaStore
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.camera.core.*
import androidx.camera.lifecycle.ProcessCameraProvider
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import com.project.topakuli.R
import com.project.topakuli.TopakuliApplication
import com.project.topakuli.data.local.entity.HistoryEntity
import com.project.topakuli.data.repository.TopakuliRepository
import com.project.topakuli.databinding.FragmentIdentifikasiBinding
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.io.File
import java.text.SimpleDateFormat
import java.util.*

class IdentifikasiFragment : Fragment() {

    private var _binding: FragmentIdentifikasiBinding? = null
    private val binding get() = _binding!!

    private var imageCapture: ImageCapture? = null
    private var camera: Camera? = null
    private var lensFacing = CameraSelector.LENS_FACING_BACK
    private var isFlashOn = false
    private var currentImageUri: Uri? = null
    private lateinit var repository: TopakuliRepository

    private val requestPermissionLauncher =
        registerForActivityResult(ActivityResultContracts.RequestPermission()) { isGranted ->
            if (isGranted) startCamera() else Toast.makeText(requireContext(), "Izin kamera diperlukan!", Toast.LENGTH_SHORT).show()
        }

    private val pickImageLauncher =
        registerForActivityResult(ActivityResultContracts.GetContent()) { uri: Uri? ->
            if (uri != null) {
                currentImageUri = uri
                showPreview(uri)
            }
        }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        _binding = FragmentIdentifikasiBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        repository = (requireActivity().application as TopakuliApplication).repository

        if (ContextCompat.checkSelfPermission(requireContext(), Manifest.permission.CAMERA) == PackageManager.PERMISSION_GRANTED) {
            startCamera()
        } else {
            requestPermissionLauncher.launch(Manifest.permission.CAMERA)
        }

        binding.btnBack.setOnClickListener {
            requireActivity().onBackPressedDispatcher.onBackPressed()
        }

        binding.btnCapture.setOnClickListener {
            takePhoto()
        }

        binding.btnFlipCamera.setOnClickListener {
            lensFacing = if (lensFacing == CameraSelector.LENS_FACING_BACK) CameraSelector.LENS_FACING_FRONT else CameraSelector.LENS_FACING_BACK
            startCamera()
        }

        binding.btnFlash.setOnClickListener {
            isFlashOn = !isFlashOn
            camera?.cameraControl?.enableTorch(isFlashOn)

            if (isFlashOn) {
                binding.btnFlash.setColorFilter(ContextCompat.getColor(requireContext(), android.R.color.holo_orange_light))
            } else {
                binding.btnFlash.setColorFilter(ContextCompat.getColor(requireContext(), android.R.color.white))
            }
        }

        binding.btnGallery.setOnClickListener {
            openGallery()
        }

        binding.btnPetunjuk.setOnClickListener {
            findNavController().navigate(R.id.action_identifikasi_to_petunjuk)
        }

        binding.btnClosePreview.setOnClickListener {
            hidePreview()
        }

        binding.btnProcess.setOnClickListener {
            processImage()
        }
    }

    private fun openGallery() {
        pickImageLauncher.launch("image/*")
    }

    private fun startCamera() {
        val cameraProviderFuture = ProcessCameraProvider.getInstance(requireContext())
        cameraProviderFuture.addListener({
            val cameraProvider = cameraProviderFuture.get()

            val preview = Preview.Builder().build().also {
                it.setSurfaceProvider(binding.viewFinder.surfaceProvider)
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
        val imageCapture = imageCapture ?: return

        val photoFile = File(
            requireContext().getExternalFilesDir(null),
            SimpleDateFormat("yyyyMMdd_HHmmss", Locale.US).format(System.currentTimeMillis()) + ".jpg"
        )

        val outputOptions = ImageCapture.OutputFileOptions.Builder(photoFile).build()

        imageCapture.takePicture(
            outputOptions, ContextCompat.getMainExecutor(requireContext()),
            object : ImageCapture.OnImageSavedCallback {
                override fun onError(exc: ImageCaptureException) {
                    Log.e("CameraX", "Gagal mengambil gambar: ${exc.message}", exc)
                }

                override fun onImageSaved(output: ImageCapture.OutputFileResults) {
                    val savedUri = Uri.fromFile(photoFile)
                    currentImageUri = savedUri
                    showPreview(savedUri)
                }
            }
        )
    }

    private fun showPreview(uri: Uri) {
        binding.ivPreview.visibility = View.VISIBLE
        binding.ivPreview.setImageURI(uri)
        binding.btnCapture.visibility = View.GONE
        binding.btnProcess.visibility = View.VISIBLE
        binding.btnClosePreview.visibility = View.VISIBLE
        binding.btnBack.visibility = View.GONE
        binding.viewFinder.visibility = View.GONE
        binding.btnFlipCamera.visibility = View.GONE
        binding.btnFlash.visibility = View.GONE
        binding.btnGallery.visibility = View.GONE
        binding.btnPetunjuk.visibility = View.GONE
    }

    private fun hidePreview() {
        binding.ivPreview.visibility = View.GONE
        binding.btnCapture.visibility = View.VISIBLE
        binding.btnProcess.visibility = View.GONE
        binding.btnClosePreview.visibility = View.GONE
        binding.btnBack.visibility = View.VISIBLE
        binding.viewFinder.visibility = View.VISIBLE
        binding.btnFlipCamera.visibility = View.VISIBLE
        binding.btnFlash.visibility = View.VISIBLE
        binding.btnGallery.visibility = View.VISIBLE
        binding.btnPetunjuk.visibility = View.VISIBLE
        currentImageUri = null
    }

    private fun processImage() {
        if (currentImageUri == null) return
        binding.layoutLoading.visibility = View.VISIBLE

        lifecycleScope.launch {
            // Dummy processing delay
            delay(2000)
            
            binding.layoutLoading.visibility = View.GONE
            
            // Dummy result: Sirih
            saveAndNavigate("Sirih", 98.5, "id_25")
        }
    }

    private fun saveAndNavigate(name: String, accuracy: Double, tanamanId: String) {
        val imagePath = currentImageUri?.toString() ?: ""

        val history = HistoryEntity(
            tanamanId = tanamanId,
            namaTanaman = name,
            akurasi = accuracy,
            imagePath = imagePath
        )

        lifecycleScope.launch {
            repository.insertHistory(history)

            val bundle = Bundle().apply {
                putString("nama", name)
                putFloat("akurasi", accuracy.toFloat())
                putLong("timestamp", history.timestamp)
                putString("imagePath", imagePath)
                putString("tanamanId", tanamanId)
            }
            findNavController().navigate(R.id.historyDetailFragment, bundle)
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}