package com.cliche.app.ui.posts

import android.net.Uri
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import coil.load
import com.cliche.app.R
import com.cliche.app.databinding.FragmentBottomNewPostMenuBinding
import com.cliche.app.services.api.PostApi
import com.cliche.app.utils.copyUriToGalleryTempFile
import com.cliche.app.utils.CameraUtils
import kotlinx.coroutines.launch
import java.io.File

class NewPostFragment : Fragment() {
    private val TAG = "NewPostFragment"

    private var _binding: FragmentBottomNewPostMenuBinding? = null
    private val binding get() = _binding!!

    private var selectedImageUri: Uri? = null
    private var photoFile: File? = null

    // Permission launcher
    private val requestPermissionLauncher = registerForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { isGranted ->
        if (isGranted) {
            proceedToOpenCamera()
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
        binding.addPostSend.setOnClickListener { submitPost() }
        binding.addPostImagePreview.setOnClickListener {
            showImageSourceDialog()
        }
    }

    private fun showImageSourceDialog() {
        CameraUtils.showImageSourceDialog(
            fragment = this,
            onPickGallery = { pickImageFromGallery() },
            onCapture = { openCameraWithPermissionCheck() }
        )
    }

    private fun pickImageFromGallery() {
        pickImageLauncher.launch("image/*")
    }

    private fun openCameraWithPermissionCheck() {
        CameraUtils.openCameraWithPermissionCheck(
            fragment = this,
            requestPermissionLauncher = requestPermissionLauncher,
            onGranted = { proceedToOpenCamera() }
        )
    }

    private fun proceedToOpenCamera() {
        try {
            val (uri, file) = CameraUtils.prepareTempImageUri(requireContext())
            photoFile = file
            selectedImageUri = uri
            takePictureLauncher.launch(uri)
        } catch (e: Exception) {
            Log.e(TAG, "Error creating image file", e)
            Toast.makeText(requireContext(), "Unable to open camera", Toast.LENGTH_SHORT).show()
        }
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
                        val tempFile = copyUriToGalleryTempFile(requireContext(),selectedImageUri!!)
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

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}