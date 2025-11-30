package com.cliche.app.ui.home

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
import com.cliche.app.databinding.FragmentHomeBinding
import com.cliche.app.models.Post
import com.cliche.app.modules.supabaseClient
import io.github.jan.supabase.postgrest.from
import io.github.jan.supabase.postgrest.query.Columns
import io.github.jan.supabase.postgrest.query.Order
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
        // load first page
        fetchPosts()

        return root
    }

    private fun setupRecyclerView() {
        postsAdapter = PostsAdapter(mutableListOf())
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
            try {
                val alreadyLoaded = postsAdapter.itemCount
                val start = alreadyLoaded
                val end = alreadyLoaded + pageSize - 1

                Log.d("HomeFragment", "Fetching posts from $start to $end")

                val columns = Columns.raw("*, owner:profiles(username, avatar_url)")

                val postsResponse = supabaseClient
                    .from("posts")
                    .select(
                        columns = columns,
                        request = {
                            order("created_at", Order.DESCENDING)
                            range(start.toLong(), end.toLong())
                        })

                Log.d("HomeFragment", "Posts response: ${postsResponse.data}")

                val posts: List<Post> = postsResponse.decodeList<Post>()

                if (posts.isNotEmpty()) {
                    postsAdapter.addAll(posts)
                }

                if (posts.size < pageSize) {
                    isLastPage = true
                }

            } catch (e: Exception) {
                Log.e("HomeFragment", "Error fetching posts: ${e.message}", e)
                Toast.makeText(requireContext(), "Error fetching posts: ${e.message}", Toast.LENGTH_LONG).show()
            } finally {
                isLoading = false
            }
        }
    }
}