package com.cliche.app.ui.profile

import android.net.Uri
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import android.view.Gravity
import android.widget.LinearLayout
import androidx.activity.result.contract.ActivityResultContracts
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import coil.load
import com.cliche.app.R
import com.cliche.app.databinding.FragmentProfileBinding
import com.cliche.app.services.api.PostApi
import com.cliche.app.services.api.ProfileApi
import com.cliche.app.models.Profile
import com.cliche.app.ui.home.PostsAdapter
import com.cliche.app.ui.home.TimelinePostActionListener
import com.cliche.app.utils.CameraUtils
import com.cliche.app.utils.copyUriToGalleryTempFile
import com.cliche.app.utils.requireUserId
import kotlinx.coroutines.launch
import java.io.File

/**
 * Fragment pour afficher et éditer le profil utilisateur.
 */
class ProfileFragment : Fragment() {
    private val TAG = "ProfileFragment"

    private var _binding: FragmentProfileBinding? = null
    private val binding get() = _binding!!

    private var selectedImageUri: Uri? = null
    private var photoFile: File? = null

    private val profilePostsAdapter: PostsAdapter by lazy {
        PostsAdapter(mutableListOf(), TimelinePostActionListener(this) )
    }

    private var isEditMode: Boolean = false
    private var profile: Profile? = null

    companion object {
        const val ARG_USER_ID = "arg_user_id"
    }

    private val requestPermissionLauncher = registerForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { isGranted ->
        if (isGranted) proceedToOpenCamera()
        else Toast.makeText(requireContext(), "Camera permission is required.", Toast.LENGTH_SHORT).show()
    }

    private val takePictureLauncher = registerForActivityResult(
        ActivityResultContracts.TakePicture()
    ) { success ->
        if (success && selectedImageUri != null) showImagePreview(selectedImageUri!!)
        else Toast.makeText(requireContext(), "Failed to capture image", Toast.LENGTH_SHORT).show()
    }

    private val pickImageLauncher = registerForActivityResult(
        ActivityResultContracts.GetContent()
    ) { uri: Uri? ->
        if (uri != null) {
            selectedImageUri = uri
            photoFile = null
            showImagePreview(uri)
        }
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        _binding = FragmentProfileBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        binding.btnEditAvatar.setOnClickListener {
            if (!isCurrentUser()) return@setOnClickListener
            CameraUtils.showImageSourceDialog(
                fragment = this,
                onPickGallery = { pickImageLauncher.launch("image/*") },
                onCapture = {
                    openCameraWithPermissionCheck()
                }
            )
        }

        binding.recyclerMyPosts.layoutManager = LinearLayoutManager(requireContext())
        binding.recyclerMyPosts.adapter = profilePostsAdapter

        binding.btnToggleEdit.setOnClickListener {
            if (!isCurrentUser()) return@setOnClickListener

            if (isEditMode) {
                val usernameInput = binding.etUsername.text.toString().trim()
                val bioInput = binding.etBio.text.toString().trim()
                val usernameChanged = profile?.username != usernameInput
                val bioChanged = (profile?.bio ?: "").trim() != bioInput
                val avatarChanged = selectedImageUri != null

                if (usernameChanged || bioChanged || avatarChanged) {
                    saveProfile()
                } else {
                    isEditMode = false
                    applyEditModeUi()
                }
            } else {
                isEditMode = true
                applyEditModeUi()
            }
        }

        // Initial UI state
        binding.etUsername.visibility = View.GONE
        binding.etBio.visibility = View.GONE
        binding.tvBio.visibility = View.GONE
        binding.tvMyPosts.text = ""

        loadProfileAndPosts()
    }

    private fun applyEditModeUi() {
        val isOwner = isCurrentUser()
        val bioEmpty = binding.tvBio.text.isNullOrBlank()
        if (isOwner) {
            binding.etUsername.visibility = if (isEditMode) View.VISIBLE else View.GONE
            binding.etBio.visibility = if (isEditMode) View.VISIBLE else View.GONE
            binding.tvBio.visibility = if (isEditMode || bioEmpty) View.GONE else View.VISIBLE
            binding.btnEditAvatar.visibility = if (isEditMode) View.VISIBLE else View.GONE
            binding.btnToggleEdit.visibility = View.VISIBLE
            binding.btnToggleEdit.setImageResource(
                if (isEditMode) R.drawable.mdi__content_save else R.drawable.mdi__edit
            )
        } else {
            binding.btnToggleEdit.visibility = View.GONE
            binding.etUsername.visibility = View.GONE
            binding.etBio.visibility = View.GONE
            binding.tvBio.visibility = if (bioEmpty) View.GONE else View.VISIBLE
            binding.btnEditAvatar.visibility = View.GONE
        }
        applyHeaderAlignment()
    }

    private fun openCameraWithPermissionCheck() {
        if (!isCurrentUser()) {
            Toast.makeText(requireContext(), getString(R.string.error_not_authorized), Toast.LENGTH_SHORT).show()
            return
        }
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
        binding.ivAvatar.load(uri) { crossfade(true) }
    }

    /**
     * Charge le profil et les posts de l'utilisateur (y compris pour un autre utilisateur).
     */
    private fun loadProfileAndPosts() {
        lifecycleScope.launch {
            setLoadingState(true)
            try {
                val profileUserId = getProfileUserId()
                val isCurrentUser = isCurrentUser()

                Log.d(TAG, "Loading profile and posts for user $profileUserId (current user: ${requireUserId()})")

                profile = ProfileApi.fetchProfile(profileUserId)
                if (profile != null) {
                    binding.tvUsername.text = profile!!.username
                    binding.etUsername.setText(profile!!.username)
                    binding.etBio.setText(profile!!.bio ?: "")
                    binding.tvBio.text = profile!!.bio ?: ""

                    val avatarUrl = ProfileApi.getPublicAvatarUrl(profile!!.avatar_url)
                    binding.ivAvatar.load(avatarUrl) {
                        placeholder(R.drawable.ic_avatar_placeholder)
                        error(R.drawable.ic_avatar_placeholder)
                    }
                }

                val posts = PostApi.fetchPostsByOwner(profileUserId, 0, 50)
                profilePostsAdapter.clear()
                profilePostsAdapter.addAll(posts)

                applyEditModeUi()

                // Section header text
                binding.tvMyPosts.text = if (isCurrentUser) getString(R.string.profile_my_posts) else getString(R.string.profile_user_posts, profile?.username ?: "")

            } catch (e: Exception) {
                Log.e(TAG, "Error loading profile or posts", e)
                Toast.makeText(requireContext(), "Error loading profile or posts: ${e.message}", Toast.LENGTH_LONG).show()
            } finally {
                setLoadingState(false)
            }
        }
    }

    private fun applyHeaderAlignment() {
        val bioEmpty = binding.tvBio.text.isNullOrBlank()
        val row = binding.llUsernameRow
        val tv = binding.tvUsername

        val params = tv.layoutParams as LinearLayout.LayoutParams
        if (bioEmpty && !isEditMode) {
            params.width = LinearLayout.LayoutParams.WRAP_CONTENT
            params.weight = 0f
            row.gravity = Gravity.START or Gravity.CENTER_VERTICAL
        } else {
            params.width = 0
            params.weight = 1f
            row.gravity = Gravity.START or Gravity.CENTER_VERTICAL
        }
        tv.layoutParams = params
    }

    private fun saveProfile() {
        if (!isCurrentUser()) {
            Toast.makeText(requireContext(), getString(R.string.error_not_authorized), Toast.LENGTH_SHORT).show()
            return
        }

        val username = binding.etUsername.text.toString().trim()
        val bio = binding.etBio.text.toString().trim()

        lifecycleScope.launch {
            setLoadingState(true)
            try {
                val userId = requireUserId()
                val usernameChange = if (profile?.username != username) username else null
                val bioChange = if ((profile?.bio ?: "").trim() != bio) bio else null
                val wantsAvatarChange = selectedImageUri != null

                if (usernameChange == null && bioChange == null && !wantsAvatarChange) {
                    Toast.makeText(requireContext(), "No changes to update", Toast.LENGTH_SHORT).show()
                    return@launch
                }

                var avatarChange: String? = null
                if (wantsAvatarChange) {
                    val localPath = if (photoFile != null) photoFile!!.absolutePath else copyUriToGalleryTempFile(requireContext(), selectedImageUri!!).absolutePath
                    val path = "${userId}/avatar_${System.currentTimeMillis()}.jpg"
                    ProfileApi.uploadAvatar(localPath, path)
                    avatarChange = path
                }

                ProfileApi.updateProfile(userId, usernameChange, bioChange, avatarChange)
                Toast.makeText(requireContext(), "Profile updated", Toast.LENGTH_SHORT).show()
                isEditMode = false
                selectedImageUri = null
                photoFile = null
                loadProfileAndPosts()
            } catch (e: Exception) {
                Log.e(TAG, "Error saving profile", e)
                Toast.makeText(requireContext(), "Error saving profile: ${e.message}", Toast.LENGTH_LONG).show()
            } finally {
                setLoadingState(false)
            }
        }
    }

    /**
     * Récupère l'ID de l'utilisateur dont le profil doit être affiché.
     *
     * @return L'ID de l'utilisateur du profil.
     */
    private fun getProfileUserId(): String {
        return arguments?.getString(ARG_USER_ID) ?: requireUserId()
    }


    /**
     * Vérifie si le profil affiché appartient à l'utilisateur actuellement connecté.
     *
     * @return true si c'est le profil de l'utilisateur actuel, false sinon.
     */
    private fun isCurrentUser(): Boolean {
        return requireUserId() == getProfileUserId()
    }

    /**
     * Toggle UI into a loading state: show progress bar and disable inputs.
     */
    private fun setLoadingState(isLoading: Boolean) {
        binding.progressBar.visibility = if (isLoading) View.VISIBLE else View.GONE
        binding.contentContainer.visibility = if (isLoading) View.GONE else View.VISIBLE
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
