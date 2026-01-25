package com.cliche.app.ui.home

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.cliche.app.R
import com.cliche.app.databinding.FragmentHomeBinding
import com.cliche.app.models.TimelinePost
import com.cliche.app.services.api.PostApi
import com.cliche.app.ui.profile.ProfileFragment
import kotlinx.coroutines.launch

class HomeFragment : Fragment() {

    val TAG = "HomeFragment"

    private var _binding: FragmentHomeBinding? = null
    private val binding get() = _binding!!
    private lateinit var postsAdapter: PostsAdapter
    private var isLoading = false
    private var isLastPage = false
    private val pageSize = 10

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentHomeBinding.inflate(inflater, container, false)
        val root: View = binding.root

        setupRecyclerView()
        fetchPosts()

        binding.swipeRefreshLayout.setOnRefreshListener {
            clearItems()
            fetchPosts()
            binding.swipeRefreshLayout.isRefreshing = false
        }

        return root
    }

    /**
     * Clear all items from the adapter and reset pagination state.
     */
    private fun clearItems() {
        postsAdapter.clear()
        isLastPage = false
    }

    /**
     * Called when the fragment is no longer interacting with the user.
     */
    override fun onPause() {
        super.onPause()
        clearItems()
    }

    /**
     * Setup the RecyclerView with adapter and scroll listener for pagination.
     */
    private fun setupRecyclerView() {
        postsAdapter = PostsAdapter(
            mutableListOf(),
            TimelinePostActionListener(this)
        )

        val recycler = binding.recyclerView
        val layoutManager = LinearLayoutManager(requireContext())
        recycler.layoutManager = layoutManager
        recycler.adapter = postsAdapter

        recycler.addOnScrollListener(object : RecyclerView.OnScrollListener() {
            override fun onScrolled(rv: RecyclerView, dx: Int, dy: Int) {
                super.onScrolled(rv, dx, dy)
                val totalItemCount = layoutManager.itemCount
                val lastVisibleItem = layoutManager.findLastVisibleItemPosition()

                // Trigger load when we reach the last 3 items
                if (!isLoading && !isLastPage && lastVisibleItem + 3 >= totalItemCount) {
                    fetchPosts()
                }
            }
        })
    }

    /**
     * Called when the view hierarchy associated with the fragment is being removed.
     */
    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    /**
     * Fetch posts from the API with pagination.
     */
    private fun fetchPosts() {
        if (isLoading || isLastPage) return
        isLoading = true

        lifecycleScope.launch {
            binding.statusText.visibility = View.GONE

            try {
                val alreadyLoaded = postsAdapter.itemCount
                val start = alreadyLoaded
                val end = alreadyLoaded + pageSize - 1

                Log.d(TAG, "Fetching posts from $start to $end")

                val posts = PostApi.fetchTimeline(start.toLong(), end.toLong())

                if (posts.isNotEmpty()) {
                    postsAdapter.addAll(posts)
                } else if (alreadyLoaded == 0) {
                    binding.statusText.visibility = View.VISIBLE
                    binding.statusText.text = getString(R.string.activity_main_posts_no_available)
                }

                if (posts.size < pageSize) {
                    isLastPage = true
                }

            } catch (e: Exception) {
                Log.e(TAG, "Error fetching posts: ${e.message}", e)
                Toast.makeText(
                    requireContext(),
                    "Error fetching posts: ${e.message}",
                    Toast.LENGTH_LONG
                ).show()
                binding.statusText.visibility = View.VISIBLE
                binding.statusText.text = getString(R.string.activity_main_posts_err_loading)
            } finally {
                isLoading = false
            }
        }
    }
}