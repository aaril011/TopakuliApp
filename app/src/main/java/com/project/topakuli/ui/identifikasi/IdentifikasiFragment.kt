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
import androidx.activity.OnBackPressedCallback
import androidx.activity.result.contract.ActivityResultContracts
import androidx.camera.core.*
import androidx.camera.lifecycle.ProcessCameraProvider
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import com.project.topakuli.R
import com.project.topakuli.TopakuliApplication
import com.project.topakuli.data.api.ApiConfig
import com.project.topakuli.data.local.entity.HistoryEntity
import com.project.topakuli.data.remote.model.TogaResponse
import com.project.topakuli.data.repository.TopakuliRepository
import com.project.topakuli.databinding.FragmentIdentifikasiBinding
import com.project.topakuli.utils.reduceFileImage
import com.project.topakuli.utils.uriToFile
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.MultipartBody
import okhttp3.RequestBody.Companion.asRequestBody
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
    private var isUiVisible = true

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
            findNavController().popBackStack(R.id.navigation_beranda, false)
        }

        requireActivity().onBackPressedDispatcher.addCallback(viewLifecycleOwner, object : OnBackPressedCallback(true) {
            override fun handleOnBackPressed() {
                findNavController().popBackStack(R.id.navigation_beranda, false)
            }
        })

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

        binding.ivPreview.setOnClickListener {
            if (binding.layoutFailure.visibility == View.VISIBLE) {
                toggleFailureUiVisibility()
            }
        }

        binding.layoutFailure.setOnClickListener {
            toggleFailureUiVisibility()
        }

        // Prevent click-through from the card to the background layout
        binding.cvFailureCard.setOnClickListener { 
            // Do nothing, just consume the click
        }

        binding.btnRetry.setOnClickListener {
            hideFailure()
            hidePreview()
        }

        binding.btnFailureInstruction.setOnClickListener {
            findNavController().navigate(R.id.action_identifikasi_to_petunjuk)
        }
    }

    private fun toggleFailureUiVisibility() {
        isUiVisible = !isUiVisible
        val duration = 400L
        val interpolator = android.view.animation.AccelerateDecelerateInterpolator()

        if (isUiVisible) {
            // Show UI
            binding.btnBack.animate().translationY(0f).alpha(1f).setDuration(duration).setInterpolator(interpolator).start()
            binding.btnFlipCamera.animate().translationY(0f).alpha(1f).setDuration(duration).setInterpolator(interpolator).start()
            binding.btnFlash.animate().translationY(0f).alpha(1f).setDuration(duration).setInterpolator(interpolator).start()
            
            binding.cvFailureCard.animate().translationY(0f).alpha(1f).setDuration(duration).setInterpolator(interpolator).start()
            binding.layoutFailure.animate().alpha(1f).setDuration(duration).start()
        } else {
            // Hide UI
            binding.btnBack.animate().translationY(-200f).alpha(0f).setDuration(duration).setInterpolator(interpolator).start()
            binding.btnFlipCamera.animate().translationY(-200f).alpha(0f).setDuration(duration).setInterpolator(interpolator).start()
            binding.btnFlash.animate().translationY(-200f).alpha(0f).setDuration(duration).setInterpolator(interpolator).start()

            binding.cvFailureCard.animate().translationY(binding.cvFailureCard.height.toFloat() + 100f).alpha(0f).setDuration(duration).setInterpolator(interpolator).start()
            binding.layoutFailure.animate().alpha(0f).setDuration(duration).start()
        }
    }

    private fun showFailure() {
        binding.layoutFailure.visibility = View.VISIBLE
        binding.layoutFailure.alpha = 1f
        binding.bottomBar.visibility = View.GONE
        binding.btnClosePreview.visibility = View.GONE
        isUiVisible = true
        
        // Reset positions if they were animated out
        binding.btnBack.translationY = 0f
        binding.btnBack.alpha = 1f
        binding.btnFlipCamera.translationY = 0f
        binding.btnFlipCamera.alpha = 1f
        binding.btnFlash.translationY = 0f
        binding.btnFlash.alpha = 1f
        binding.cvFailureCard.translationY = 0f
        binding.cvFailureCard.alpha = 1f
    }

    private fun hideFailure() {
        binding.layoutFailure.visibility = View.GONE
        if (currentImageUri != null) {
            binding.btnProcess.visibility = View.VISIBLE
            binding.btnClosePreview.visibility = View.VISIBLE
        } else {
            binding.bottomBar.visibility = View.VISIBLE
        }
    }

    private fun mapLabelAndNavigate(label: String, latin: String, daerah: String, score: Float) {
        val accuracy = score * 100.0
        
        lifecycleScope.launch {
            val allTanamanList = withContext(Dispatchers.IO) { repository.allTanaman.first() }
            val match = allTanamanList.find { it.nama.equals(label, ignoreCase = true) }
            
            saveAndNavigate(label, latin, daerah, accuracy, match?.id ?: "")
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
        hideFailure()
        binding.bottomBar.visibility = View.VISIBLE
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
        val uri = currentImageUri ?: return
        binding.layoutLoading.visibility = View.VISIBLE
        binding.btnClosePreview.visibility = View.GONE

        lifecycleScope.launch {
            try {
                val imageFile = withContext(Dispatchers.IO) {
                    uriToFile(uri, requireContext()).reduceFileImage()
                }

                val requestImageFile = imageFile.asRequestBody("image/jpeg".toMediaType())
                val multipartBody = MultipartBody.Part.createFormData(
                    "file",
                    imageFile.name,
                    requestImageFile
                )

                val apiService = ApiConfig.getApiService()
                val successResponse = apiService.predict(multipartBody)

                withContext(Dispatchers.Main) {
                    binding.layoutLoading.visibility = View.GONE
                    if (successResponse.status == "success") {
                        if (successResponse.confidence >= 0.7) {
                            mapLabelAndNavigate(
                                successResponse.namaTanaman,
                                successResponse.namaLatin,
                                successResponse.namaDaerah,
                                successResponse.confidence.toFloat()
                            )
                        } else {
                            showFailure()
                        }
                    } else {
                        Toast.makeText(requireContext(), "Gagal mendapatkan hasil prediksi", Toast.LENGTH_SHORT).show()
                    }
                }
            } catch (e: Exception) {
                withContext(Dispatchers.Main) {
                    binding.layoutLoading.visibility = View.GONE
                    Log.e("IdentifikasiFragment", "Error processing image: ${e.message}")
                    Toast.makeText(requireContext(), "Gagal memproses gambar: ${e.message}", Toast.LENGTH_SHORT).show()
                }
            }
        }
    }

    private fun saveAndNavigate(name: String, latin: String, daerah: String, accuracy: Double, tanamanId: String) {
        val imagePath = currentImageUri?.toString() ?: ""
        Log.d("IdentifikasiFragment", "Navigating to detail with: $name, $accuracy, $tanamanId")

        val history = HistoryEntity(
            tanamanId = tanamanId,
            namaTanaman = name,
            namaLatin = latin,
            namaDaerah = daerah,
            akurasi = accuracy,
            imagePath = imagePath
        )

        lifecycleScope.launch {
            try {
                repository.insertHistory(history)
                Log.d("IdentifikasiFragment", "History saved successfully")

                val bundle = Bundle().apply {
                    putString("nama", name)
                    putString("namaLatin", latin)
                    putString("namaDaerah", daerah)
                    putFloat("akurasi", accuracy.toFloat())
                    putLong("timestamp", history.timestamp)
                    putString("imagePath", imagePath)
                    putString("tanamanId", tanamanId)
                }
                
                // Hide loading before navigation
                binding.layoutLoading.visibility = View.GONE
                
                findNavController().navigate(R.id.historyDetailFragment, bundle)
            } catch (e: Exception) {
                Log.e("IdentifikasiFragment", "Error saving history or navigating", e)
                binding.layoutLoading.visibility = View.GONE
                Toast.makeText(requireContext(), "Terjadi kesalahan: ${e.message}", Toast.LENGTH_SHORT).show()
            }
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}