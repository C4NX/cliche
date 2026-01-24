package com.cliche.app.ui.home

import android.os.Bundle
import android.util.Log
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import com.cliche.app.R
import com.cliche.app.models.TimelinePost
import com.cliche.app.services.api.PostApi
import com.cliche.app.ui.profile.ProfileFragment
import kotlinx.coroutines.launch

/**
 * Implémentation de l'interface OnPostActionListener pour gérer les actions sur les posts.
 *
 * @param fragment Le fragment dans lequel les actions sont gérées.
 */
class TimelinePostActionListener(
    private val fragment: Fragment
) : PostsAdapter.OnPostActionListener {
    var TAG = "TimelinePostActionListener"

    override fun onLike(post: TimelinePost) {
        Log.d(TAG, "onLike: ${post.id}")

        fragment.viewLifecycleOwner.lifecycleScope.launch {
            if (post.liked_by_user) {
                PostApi.unlike(post.id)
                post.liked_by_user = false
            } else {
                PostApi.like(post.id)
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
        Toast.makeText(fragment.requireContext(), "Bookmarked post ${post.id}", Toast.LENGTH_SHORT)
            .show()
        Log.d(TAG, "onBookmark: ${post.id}")
    }

    override fun onItemClick(post: TimelinePost) {
        Toast.makeText(fragment.requireContext(), "Open post ${post.id}", Toast.LENGTH_SHORT).show()
        Log.d(TAG, "onItemClick: ${post.id}")
    }

    override fun onProfileClick(userId: String) {
        fragment.findNavController()
            .navigate(R.id.navigation_profile, Bundle().apply {
                putString(ProfileFragment.ARG_USER_ID, userId)
            })
    }
}