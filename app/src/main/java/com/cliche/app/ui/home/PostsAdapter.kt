package com.cliche.app.ui.home

import android.annotation.SuppressLint
import android.content.Intent
import android.graphics.Color
import android.net.Uri
import android.util.Log
import android.util.TypedValue
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import com.google.android.material.button.MaterialButton
import android.widget.TextView
import android.widget.Toast
import androidx.recyclerview.widget.RecyclerView
import androidx.viewpager2.widget.ViewPager2
import coil.load
import com.cliche.app.R
import com.cliche.app.models.TimelinePost
import com.cliche.app.services.api.PostApi
import com.cliche.app.services.api.ProfileApi

/**
 * Adapter pour l'affichage des posts dans un RecyclerView.
 */
class PostsAdapter(
    private val items: MutableList<TimelinePost>,
    private val listener: OnPostActionListener? = null
) : RecyclerView.Adapter<PostsAdapter.ViewHolder>() {
    val TAG = "PostsAdapter"

    /**
     * Interface pour gérer les actions sur les posts.
     *
     * @see PostsAdapter
     */
    interface OnPostActionListener {
        fun onLike(post: TimelinePost)
        fun onComment(post: TimelinePost)
        fun onShare(post: TimelinePost)
        fun onBookmark(post: TimelinePost)
        fun onItemClick(post: TimelinePost)
        fun onProfileClick(userId: String)
    }

    class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val ivAvatar: ImageView = itemView.findViewById(R.id.ivAvatar)
        val tvUsername: TextView = itemView.findViewById(R.id.tvUsername)
        val vpPostImages: ViewPager2 = itemView.findViewById(R.id.vpPostImages)
        val tvLikes: TextView = itemView.findViewById(R.id.tvLikes)
        val tvCaption: TextView = itemView.findViewById(R.id.tvCaption)
        val ivLike: ImageView = itemView.findViewById(R.id.iv_like)
        val ivComment: ImageView = itemView.findViewById(R.id.iv_comment)
        val ivShare: ImageView = itemView.findViewById(R.id.iv_share)
        val btnLocation: MaterialButton = itemView.findViewById(R.id.btn_location)
        val ivBookmark: ImageView = itemView.findViewById(R.id.iv_bookmark)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val v = LayoutInflater.from(parent.context).inflate(R.layout.item_post, parent, false)
        return ViewHolder(v)
    }

    @SuppressLint("SetTextI18n")
    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val post = items[position]

        holder.tvUsername.text = post.owner_username
        val avatarUrl = ProfileApi.getPublicAvatarUrl(post.owner_avatar_url)
        holder.ivAvatar.load(avatarUrl) {
            crossfade(true)
            placeholder(R.drawable.ic_avatar_placeholder)
            error(R.drawable.ic_avatar_placeholder)
        }

        // Get all public image URLs & setup ViewPager
        val publicPostUrls = PostApi.getPostPublicUrls(post) ?: emptyList()

        if (publicPostUrls.isEmpty()) {
            holder.vpPostImages.visibility = View.GONE
        } else {
            holder.vpPostImages.visibility = View.VISIBLE
            val adapter = ImagePostAdapter(publicPostUrls)
            holder.vpPostImages.adapter = adapter
        }

        holder.tvLikes.text = "${post.likes_count} likes"
        holder.tvCaption.text = post.caption

        updateLikeState(holder, post.liked_by_user)

        // Listeners
        holder.ivLike.setOnClickListener { v ->
            try {
                val alreadyLiked = post.liked_by_user

                listener?.onLike(post)

                if (alreadyLiked) {
                    post.liked_by_user = false
                    post.likes_count -= 1
                } else {
                    post.liked_by_user = true
                    post.likes_count += 1
                }
            } catch (e: Exception) {
                Log.e(TAG, "Error in onLike callback: ${e.message}")
                Toast.makeText(v.context, "Error in onLike callback: ${e.message}", Toast.LENGTH_LONG).show()
            } finally {
                doLikeClickOnHolder(holder, post)
            }
        }

        holder.ivComment.setOnClickListener {
            listener?.onComment(post)
        }

        holder.ivShare.setOnClickListener {
            listener?.onShare(post)
        }

        // Location button: visible only if both latitude and longitude are present
        val hasLocation = (post.latitude != null && post.longitude != null)
        holder.btnLocation.visibility = if (hasLocation) View.VISIBLE else View.GONE
        if (hasLocation) {
            holder.btnLocation.setOnClickListener { v ->
                try {
                    val lat = post.latitude!!
                    val lon = post.longitude!!
                    // Use geo URI to open in map apps
                    val uri = Uri.parse("geo:$lat,$lon?q=$lat,$lon")
                    val intent = Intent(Intent.ACTION_VIEW, uri)
                    v.context.startActivity(intent)
                } catch (e: Exception) {
                    Log.e(TAG, "Error opening location: ${e.message}")
                    Toast.makeText(v.context, "Unable to open location", Toast.LENGTH_LONG).show()
                }
            }
        }

        holder.ivBookmark.setOnClickListener {
            listener?.onBookmark(post)
        }

        holder.itemView.setOnClickListener {
            listener?.onItemClick(post)
        }

        holder.ivAvatar.setOnClickListener {
            listener?.onProfileClick(post.owner_id)
        }
    }

    /**
     * Gère le clic sur le bouton "like" d'un post.
     *
     * @param holder Le ViewHolder du post.
     * @param updatedPost Le post mis à jour (après le like).
     */
    @SuppressLint("SetTextI18n")
    private fun doLikeClickOnHolder(holder: ViewHolder, updatedPost: TimelinePost) {
        // Scale up puis revenir
        holder.ivLike.animate().scaleX(1.3f).scaleY(1.3f).setDuration(120).withEndAction {
            holder.ivLike.animate().scaleX(1f).scaleY(1f).setDuration(120).start()
        }.start()

        // Met à jour l'UI (couleur et nombre de likes)
        updateLikeState(holder, updatedPost.liked_by_user)
        holder.tvLikes.text = updatedPost.likes_count.toString() + " likes"
    }

    override fun getItemCount(): Int = items.size

    fun addAll(newItems: List<TimelinePost>) {
        val start = items.size
        items.addAll(newItems)
        notifyItemRangeInserted(start, newItems.size)
    }

    @SuppressLint("NotifyDataSetChanged")
    fun clear() {
        items.clear()
        notifyDataSetChanged()
    }

    /**
     * Helper pour mettre à jour l'état du bouton "like" d'un post.
     */
    private fun updateLikeState(holder: ViewHolder, isLiked: Boolean) {
        holder.ivLike.setImageResource(
            if (isLiked)
                R.drawable.ic_favorite_filled_24dp
            else
                R.drawable.ic_favorite_border_24dp
        )

        val typedValue = TypedValue()
        val theme = holder.itemView.context.theme

        theme.resolveAttribute(
            if (isLiked)
                com.google.android.material.R.attr.colorPrimary
            else
                com.google.android.material.R.attr.colorOnSurface
            , typedValue, true
        )
        holder.ivLike.setColorFilter(typedValue.data)
    }
}
