package com.cliche.app.ui.home

import android.os.Bundle
import android.util.Log
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import com.cliche.app.R
import com.cliche.app.models.TimelinePost
import com.cliche.app.services.api.AppApi
import com.cliche.app.services.api.PostApi
import com.cliche.app.ui.profile.ProfileFragment
import com.cliche.app.ui.bookmarks.BookmarksFragment
import kotlinx.coroutines.launch

/**
 * Implémentation de l'interface OnPostActionListener pour gérer les actions sur les posts.
 *
 * @param fragment Le fragment dans lequel les actions sont gérées.
 */
class TimelinePostActionListener(
    private val fragment: Fragment
) : PostsAdapter.OnPostActionListener {
    private var TAG = "TimelinePostActionListener"

    override fun onLike(post: TimelinePost) {
        Log.d(TAG, "onLike: ${post.id}")

        fragment.viewLifecycleOwner.lifecycleScope.launch {
            if (post.liked_by_user) {
                AppApi.postApi.unlike(post.id)
                post.liked_by_user = false
            } else {
                AppApi.postApi.like(post.id)
                post.liked_by_user = true
            }
        }
    }

    override fun onComment(post: TimelinePost) {
        Toast.makeText(fragment.requireContext(), "Comment on post ${post.id}", Toast.LENGTH_SHORT)
            .show()
        Log.d(TAG, "onComment: ${post.id}")
    }

    override fun onShare(post: TimelinePost) {
        Toast.makeText(fragment.requireContext(), "Share post ${post.id}", Toast.LENGTH_SHORT)
            .show()
        Log.d(TAG, "onShare: ${post.id}")
    }

    override fun onBookmark(post: TimelinePost) {
        Log.d(TAG, "onBookmark: ${post.id}")

        fragment.viewLifecycleOwner.lifecycleScope.launch {
            try {
                if (post.bookmarked_by_user) {
                    AppApi.postApi.unbookmark(post.id)
                    post.bookmarked_by_user = false
                    if (fragment is BookmarksFragment) {
                        fragment.removePostById(post.id)
                    }
                } else {
                    AppApi.postApi.bookmark(post.id)
                    post.bookmarked_by_user = true
                }
            } catch (e: Exception) {
                Log.e(TAG, "Error toggling bookmark: ${e.message}")
                Toast.makeText(fragment.requireContext(), "Error toggling bookmark", Toast.LENGTH_SHORT).show()
            }
        }
    }

    override fun onItemClick(post: TimelinePost) {
        Log.d(TAG, "onItemClick: ${post.id}")
        fragment.findNavController().navigate(
            R.id.navigation_post,
            Bundle().apply { putLong(com.cliche.app.ui.posts.PostFragment.ARG_POST_ID, post.id) }
        )
    }

    override fun onProfileClick(userId: String) {
        if(fragment is ProfileFragment) {
            val profileFragmentUserId = fragment.arguments?.getString(ProfileFragment.ARG_USER_ID)
            if (profileFragmentUserId == userId) {
                // already on the profile page of the user
                return
            }
        }

        fragment.findNavController()
            .navigate(R.id.navigation_profile, Bundle().apply {
                putString(ProfileFragment.ARG_USER_ID, userId)
            })
    }
}