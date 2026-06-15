package com.cliche.app.ui.bookmarks

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.cliche.app.R
import com.cliche.app.databinding.FragmentBookmarksBinding
import com.cliche.app.services.api.AppApi
import com.cliche.app.services.api.PostApi
import com.cliche.app.ui.home.PostsAdapter
import com.cliche.app.ui.home.TimelinePostActionListener
import kotlinx.coroutines.launch

/**
 * A fragment representing a list of bookmarked posts.
 */
class BookmarksFragment : Fragment() {
    private val TAG = "BookmarksFragment"

    private var _binding: FragmentBookmarksBinding? = null
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
        _binding = FragmentBookmarksBinding.inflate(inflater, container, false)
        val root = binding.root

        setupRecyclerView()
        fetchBookmarks()

        binding.swipeRefreshLayout.setOnRefreshListener {
            clearItems()
            fetchBookmarks()
            binding.swipeRefreshLayout.isRefreshing = false
        }

        return root
    }

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

                if (!isLoading && !isLastPage && lastVisibleItem + 3 >= totalItemCount) {
                    fetchBookmarks()
                }
            }
        })
    }

    private fun clearItems() {
        postsAdapter.clear()
        isLastPage = false
    }

    override fun onPause() {
        super.onPause()
        clearItems()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    private fun fetchBookmarks() {
        if (isLoading || isLastPage) return
        isLoading = true

        lifecycleScope.launch {
            binding.statusText.visibility = View.GONE

            try {
                val alreadyLoaded = postsAdapter.itemCount
                val start = alreadyLoaded
                val end = alreadyLoaded + pageSize - 1

                Log.d(TAG, "Fetching bookmarked posts from $start to $end")

                val posts = AppApi.postApi.fetchBookmarked(start.toLong(), end.toLong())

                if (posts.isNotEmpty()) {
                    postsAdapter.addAll(posts)
                } else if (alreadyLoaded == 0) {
                    binding.statusText.visibility = View.VISIBLE
                    binding.statusText.text = getString(R.string.activity_main_bookmarks_empty)
                }

                if (posts.size < pageSize) {
                    isLastPage = true
                }

            } catch (e: Exception) {
                Log.e(TAG, "Error fetching bookmarks: ${e.message}", e)
                Toast.makeText(
                    requireContext(),
                    "Error fetching bookmarks: ${e.message}",
                    Toast.LENGTH_LONG
                ).show()
                binding.statusText.visibility = View.VISIBLE
                binding.statusText.text = getString(R.string.error_generic, e.message)
            } finally {
                isLoading = false
            }
        }
    }

    /**
     * Removes a post from the list and updates empty state when needed.
     *
     * @param id The ID of the post to remove.
     */
    fun removePostById(id: Long) {
        val removed = postsAdapter.removeById(id)
        if (removed && postsAdapter.itemCount == 0) {
            binding.statusText.visibility = View.VISIBLE
            binding.statusText.text = getString(R.string.activity_main_bookmarks_empty)
        }
    }
}
