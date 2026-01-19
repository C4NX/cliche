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
import com.cliche.app.databinding.FragmentBottomNewPostMenuBinding
import kotlinx.coroutines.launch
import java.io.File
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class NewPostFragment : Fragment() {

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

        binding.addPostPickImage.setOnClickListener { openCameraWithPermissionCheck() }
        binding.addPostBack.setOnClickListener { findNavController().navigateUp() }
        binding.addPostSend.setOnClickListener { submitPost() }
        binding.addPostPickContainer.setOnClickListener { openCameraWithPermissionCheck() }
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
                // Optional: Show explanation dialog before requesting
                Toast.makeText(
                    requireContext(),
                    "Camera access is needed to take photos for your post.",
                    Toast.LENGTH_LONG
                ).show()
                requestPermissionLauncher.launch(Manifest.permission.CAMERA)
            }

            else -> {
                // Request permission directly
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
            Log.e("NewPostFragment", "Error creating image file", e)
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
            Log.e("NewPostFragment", "Error loading image preview", e)
            Toast.makeText(requireContext(), "Unable to load image", Toast.LENGTH_SHORT).show()
        }
    }

    private fun submitPost() {
        val caption = binding.addPostCaption.text.toString().trim()

        if (caption.isEmpty() && selectedImageUri == null) {
            Toast.makeText(
                requireContext(),
                getString(com.cliche.app.R.string.add_post_no_content_error),
                Toast.LENGTH_SHORT
            ).show()
            return
        }

        lifecycleScope.launch {
            try {
                // TODO: Upload image from photoFile and caption
                Toast.makeText(requireContext(), "Post shared", Toast.LENGTH_SHORT).show()
                findNavController().navigateUp()
            } catch (e: Exception) {
                Log.e("NewPostFragment", "Error sharing post", e)
                Toast.makeText(
                    requireContext(),
                    "Error sharing post: ${e.message}",
                    Toast.LENGTH_LONG
                ).show()
            }
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}