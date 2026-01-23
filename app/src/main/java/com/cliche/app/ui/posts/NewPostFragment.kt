package com.cliche.app.ui.posts

import android.Manifest
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.content.ContextCompat
import androidx.core.content.FileProvider
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import coil.load
import com.cliche.app.R
import com.cliche.app.databinding.FragmentBottomNewPostMenuBinding
import com.cliche.app.services.api.PostApi
import kotlinx.coroutines.launch
import java.io.File
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class NewPostFragment : Fragment() {
    val TAG = "NewPostFragment"

    private var _binding: FragmentBottomNewPostMenuBinding? = null
    private val binding get() = _binding!!

    private var selectedImageUri: Uri? = null
    private var photoFile: File? = null

    // Permission launcher
    private val requestPermissionLauncher = registerForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { isGranted ->
        if (isGranted) {
            openCameraInternal()
        } else {
            Toast.makeText(
                requireContext(),
                "Camera permission is required to take a photo.",
                Toast.LENGTH_LONG
            ).show()
        }
    }

    // Camera launcher
    private val takePictureLauncher = registerForActivityResult(
        ActivityResultContracts.TakePicture()
    ) { success ->
        if (success && selectedImageUri != null) {
            showImagePreview(selectedImageUri!!)
        } else {
            Toast.makeText(requireContext(), "Failed to capture image", Toast.LENGTH_SHORT).show()
        }
    }

    // Gallery launcher
    private val pickImageLauncher = registerForActivityResult(
        ActivityResultContracts.GetContent()
    ) { uri: Uri? ->
        if (uri != null) {
            selectedImageUri = uri
            photoFile = null
            showImagePreview(uri)
        } else {
            Toast.makeText(requireContext(), "No image selected", Toast.LENGTH_SHORT).show()
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentBottomNewPostMenuBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        binding.addPostPickImage.setOnClickListener { showImageSourceDialog() }
        binding.addPostPickContainer.setOnClickListener { showImageSourceDialog() }
        binding.addPostBack.setOnClickListener { findNavController().navigateUp() }
        binding.addPostSend.setOnClickListener { submitPost() }
        binding.addPostImagePreview.setOnClickListener {
            showImageSourceDialog()
        }
    }

    private fun showImageSourceDialog() {
        val options = arrayOf(
            getString(R.string.add_post_select_from_gallery),
            getString(R.string.add_post_select_capture),
        )
        val builder = android.app.AlertDialog.Builder(requireContext())
        builder.setTitle(getString(R.string.add_post_select_title))
        builder.setItems(options) { _, which ->
            when (which) {
                0 -> openCameraWithPermissionCheck()
                1 -> pickImageFromGallery()
            }
        }
        builder.setNegativeButton(getString(R.string.cancel), null)
        builder.show()
    }

    private fun pickImageFromGallery() {
        pickImageLauncher.launch("image/*")
    }

    private fun openCameraWithPermissionCheck() {
        when {
            ContextCompat.checkSelfPermission(
                requireContext(),
                Manifest.permission.CAMERA
            ) == PackageManager.PERMISSION_GRANTED -> {
                openCameraInternal()
            }

            shouldShowRequestPermissionRationale(Manifest.permission.CAMERA) -> {
                Toast.makeText(
                    requireContext(),
                    "Camera access is needed to take photos for your post.",
                    Toast.LENGTH_LONG
                ).show()
                requestPermissionLauncher.launch(Manifest.permission.CAMERA)
            }

            else -> {
                requestPermissionLauncher.launch(Manifest.permission.CAMERA)
            }
        }
    }

    private fun openCameraInternal() {
        try {
            photoFile = createImageFile()
            selectedImageUri = FileProvider.getUriForFile(
                requireContext(),
                "${requireContext().packageName}.fileprovider",
                photoFile!!
            )
            takePictureLauncher.launch(selectedImageUri)
        } catch (e: Exception) {
            Log.e(TAG, "Error creating image file", e)
            Toast.makeText(requireContext(), "Unable to open camera", Toast.LENGTH_SHORT).show()
        }
    }

    @Throws(Exception::class)
    private fun createImageFile(): File {
        val timeStamp = SimpleDateFormat("yyyyMMdd_HHmmss", Locale.US).format(Date())
        val storageDir = requireContext().getExternalFilesDir(null)
        return File.createTempFile(
            "JPEG_${timeStamp}_",
            ".jpg",
            storageDir
        )
    }

    private fun showImagePreview(uri: Uri) {
        try {
            binding.addPostImagePreview.apply {
                visibility = View.VISIBLE
                alpha = 0f

                load(uri) {
                    crossfade(true)
                }

                animate().alpha(1f).setDuration(200).start()
            }

            binding.addPostPickContainer.visibility = View.GONE

        } catch (e: Exception) {
            Log.e(TAG, "Error loading image preview", e)
            Toast.makeText(requireContext(), "Unable to load image", Toast.LENGTH_SHORT).show()
        }
    }

    private fun submitPost() {
        val caption = binding.addPostCaption.text.toString().trim()

        if (caption.isEmpty() && selectedImageUri == null) {
            Toast.makeText(
                requireContext(),
                getString(R.string.add_post_no_content_error),
                Toast.LENGTH_SHORT
            ).show()
            return
        }

        lifecycleScope.launch {
            try {
                val filePaths = if (selectedImageUri != null) {
                    if (photoFile != null) {
                        listOf(photoFile!!.absolutePath)
                    } else {
                        val tempFile = copyUriToFile(selectedImageUri!!)
                        listOf(tempFile.absolutePath)
                    }
                } else {
                    emptyList()
                }

                PostApi.createPost(caption, filePaths)

                Toast.makeText(
                    requireContext(),
                    getString(R.string.toast_newpost_success),
                    Toast.LENGTH_SHORT
                ).show()
                findNavController().navigateUp()
            } catch (e: Exception) {
                Log.e(TAG, "Error sharing post", e)
                Toast.makeText(
                    requireContext(),
                    "Error sharing post: ${e.message}",
                    Toast.LENGTH_LONG
                ).show()
            }
        }
    }

    @Throws(Exception::class)
    private fun copyUriToFile(uri: Uri): File {
        val inputStream = requireContext().contentResolver.openInputStream(uri)
            ?: throw IllegalArgumentException("Unable to open input stream for URI: $uri")

        val timeStamp = SimpleDateFormat("yyyyMMdd_HHmmss", Locale.US).format(Date())
        val storageDir = requireContext().getExternalFilesDir(null)
        val tempFile = File.createTempFile("GALLERY_${timeStamp}_", ".jpg", storageDir)

        inputStream.use { input ->
            tempFile.outputStream().use { output ->
                input.copyTo(output)
            }
        }

        return tempFile
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}