package com.cliche.app.ui.posts

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import com.cliche.app.R
import com.cliche.app.databinding.FragmentPostBinding
import com.cliche.app.models.TimelinePost
import com.cliche.app.services.api.AppApi
import com.cliche.app.services.api.PostApi
import com.cliche.app.ui.home.PostsAdapter
import com.cliche.app.ui.home.TimelinePostActionListener
import kotlinx.coroutines.launch

/**
 * Fragment that displays a single post.
 */
class PostFragment : Fragment() {
    private val TAG = "PostFragment"

    companion object {
        const val ARG_POST_ID = "post_id"
    }

    private var _binding: FragmentPostBinding? = null
    private val binding get() = _binding!!

    private var post: TimelinePost? = null
    private val actionListener by lazy { TimelinePostActionListener(this) }
    private val postsAdapter: PostsAdapter by lazy { PostsAdapter(mutableListOf(), actionListener) }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentPostBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        binding.recyclerPost.layoutManager = LinearLayoutManager(requireContext())
        binding.recyclerPost.adapter = postsAdapter
        loadPost()
    }

    private fun loadPost() {
        val postId = arguments?.getLong(ARG_POST_ID)
        if (postId == null) {
            showError(getString(R.string.error_generic, "No post ID provided"))
            return
        }

        showLoading(true)
        lifecycleScope.launch {
            try {
                post = AppApi.postApi.fetchPostById(postId)
                if (post == null) {
                    showError(getString(R.string.error_generic, "Post not found"))
                    return@launch
                }
                postsAdapter.clear()
                postsAdapter.addAll(listOf(post!!))
                showLoading(false)
            } catch (e: Exception) {
                Log.e(TAG, "Error loading post", e)
                showError(getString(R.string.error_generic, e.message ?: "Unknown error"))
            }
        }
    }
    private fun showLoading(loading: Boolean) {
        binding.progress.visibility = if (loading) View.VISIBLE else View.GONE
        binding.recyclerPost.visibility = if (loading) View.GONE else View.VISIBLE
        binding.statusText.visibility = View.GONE
    }

    private fun showError(message: String) {
        binding.progress.visibility = View.GONE
        binding.recyclerPost.visibility = View.GONE
        binding.statusText.visibility = View.VISIBLE
        binding.statusText.text = message
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}