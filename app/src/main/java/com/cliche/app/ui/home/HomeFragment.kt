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
import kotlinx.coroutines.launch

class HomeFragment : Fragment() {

    private var _binding: FragmentHomeBinding? = null

    // This property is only valid between onCreateView and
    // onDestroyView.
    private val binding get() = _binding!!

    // Pagination / adapter state
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

        binding.fabAddPost.setOnClickListener {

            Log.d("HomeFragment", "Navigating to AddPostFragment")

            // naviguer vers la destination ajoutée dans le nav graph
            findNavController().navigate(R.id.navigation_add_post)
        }

        binding.swipeRefreshLayout.setOnRefreshListener {
            clearItems()
            fetchPosts()
            binding.swipeRefreshLayout.isRefreshing = false
        }

        return root
    }

    private fun clearItems() {
        postsAdapter.clear()
        isLastPage = false
    }

    override fun onPause() {
        super.onPause()

        // Effacer les données avant de recharger les données (quand lose focus)
        clearItems()
    }

    private fun setupRecyclerView() {
        postsAdapter = PostsAdapter(mutableListOf(), object : PostsAdapter.OnPostActionListener {
            override fun onLike(post: TimelinePost) {
                Toast.makeText(requireContext(), "Liked post ${post.id}", Toast.LENGTH_SHORT).show()
                Log.d("HomeFragment", "onLike: ${post.id}")

                lifecycleScope.launch {
                    if(post.liked_by_user) {
                        PostApi.removeLike(post.id)
                        post.liked_by_user = false
                        post.likes_count -= 1
                    } else {
                        PostApi.addLike(post.id)
                        post.liked_by_user = true
                        post.likes_count += 1
                    }
                }
            }

            override fun onComment(post: TimelinePost) {
                Toast.makeText(requireContext(), "Comment on post ${post.id}", Toast.LENGTH_SHORT).show()
                Log.d("HomeFragment", "onComment: ${post.id}")
            }

            override fun onShare(post: TimelinePost) {
                Toast.makeText(requireContext(), "Share post ${post.id}", Toast.LENGTH_SHORT).show()
                Log.d("HomeFragment", "onShare: ${post.id}")
            }

            override fun onBookmark(post: TimelinePost) {
                Toast.makeText(requireContext(), "Bookmarked post ${post.id}", Toast.LENGTH_SHORT).show()
                Log.d("HomeFragment", "onBookmark: ${post.id}")
            }

            override fun onItemClick(post: TimelinePost) {
                Toast.makeText(requireContext(), "Open post ${post.id}", Toast.LENGTH_SHORT).show()
                Log.d("HomeFragment", "onItemClick: ${post.id}")
            }
        })

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

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    private fun fetchPosts() {
        if (isLoading || isLastPage) return
        isLoading = true

        lifecycleScope.launch {
            binding.statusText.visibility = View.GONE

            try {
                val alreadyLoaded = postsAdapter.itemCount
                val start = alreadyLoaded
                val end = alreadyLoaded + pageSize - 1

                Log.d("HomeFragment", "Fetching posts from $start to $end")

                val posts = PostApi.fetchTimeline(start.toLong(), end.toLong())

                if (posts.isNotEmpty()) {
                    postsAdapter.addAll(posts)
                } else if(alreadyLoaded == 0) {
                    binding.statusText.visibility = View.VISIBLE
                    binding.statusText.text = getString(R.string.activity_main_posts_no_available)
                }

                if (posts.size < pageSize) {
                    isLastPage = true
                }

            } catch (e: Exception) {
                Log.e("HomeFragment", "Error fetching posts: ${e.message}", e)
                Toast.makeText(requireContext(), "Error fetching posts: ${e.message}", Toast.LENGTH_LONG).show()
                binding.statusText.visibility = View.VISIBLE
                binding.statusText.text = getString(R.string.activity_main_posts_err_loading)
            } finally {
                isLoading = false
            }
        }
    }
}