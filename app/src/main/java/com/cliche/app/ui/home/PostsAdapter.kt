package com.cliche.app.ui.home

import android.annotation.SuppressLint
import android.graphics.Color
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.recyclerview.widget.RecyclerView
import coil.load
import com.cliche.app.R
import com.cliche.app.models.Post
import com.cliche.app.models.TimelinePost

/**
 * Adapter pour l'affichage des posts dans un RecyclerView.
 */
class PostsAdapter(
    private val items: MutableList<TimelinePost>,
    private val listener: OnPostActionListener? = null
) : RecyclerView.Adapter<PostsAdapter.ViewHolder>() {

    /**
     * Interface pour gérer les actions sur les posts.
     */
    interface OnPostActionListener {
        fun onLike(post: TimelinePost)
        fun onComment(post: TimelinePost)
        fun onShare(post: TimelinePost)
        fun onBookmark(post: TimelinePost)
        fun onItemClick(post: TimelinePost)
    }

    /**
     * Set des IDs des posts likés localement.
     */
    private val likedIds = mutableSetOf<Long>()

    class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val ivAvatar: ImageView = itemView.findViewById(R.id.ivAvatar)
        val tvUsername: TextView = itemView.findViewById(R.id.tvUsername)
        val ivPostImage: ImageView = itemView.findViewById(R.id.ivPostImage)
        val tvLikes: TextView = itemView.findViewById(R.id.tvLikes)
        val tvCaption: TextView = itemView.findViewById(R.id.tvCaption)
        val ivLike: ImageView = itemView.findViewById(R.id.iv_like)
        val ivComment: ImageView = itemView.findViewById(R.id.iv_comment)
        val ivShare: ImageView = itemView.findViewById(R.id.iv_share)
        val ivBookmark: ImageView = itemView.findViewById(R.id.iv_bookmark)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val v = LayoutInflater.from(parent.context).inflate(R.layout.item_instagram_post, parent, false)
        return ViewHolder(v)
    }

    @SuppressLint("SetTextI18n")
    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val post = items[position]

        holder.tvUsername.text = post.username
        val avatarUrl = post.avatar_url
        if (!avatarUrl.isNullOrEmpty()) {
            holder.ivAvatar.load(avatarUrl) {
                crossfade(true)
                placeholder(R.drawable.ic_avatar_placeholder)
                error(R.drawable.ic_avatar_placeholder)
            }
        } else {
            holder.ivAvatar.setImageResource(R.drawable.ic_avatar_placeholder)
        }

        val firstImageUrl = post.content.firstOrNull()
        if (!firstImageUrl.isNullOrEmpty()) {
            holder.ivPostImage.load(firstImageUrl) {
                crossfade(true)
                placeholder(R.drawable.ic_avatar_placeholder)
                error(R.drawable.ic_avatar_placeholder)
            }
        } else {
            holder.ivPostImage.setImageResource(R.drawable.ic_avatar_placeholder)
        }


        // tvLikes actuellement affiche created_at dans le code initial, on conserve si pas de champ likes
        holder.tvLikes.text = post.likes_count.toString() + " likes"
        holder.tvCaption.text = post.description

        // Gestion état de like
        val isLiked = post.user_has_liked
        if(isLiked) {
            likedIds.add(post.id)
        }

        holder.ivLike.alpha = if (isLiked) 1.0f else 0.8f
        holder.ivLike.setColorFilter(if (isLiked) Color.RED else Color.DKGRAY)

        // Listeners
        holder.ivLike.setOnClickListener { v ->
            doLikeClickOnHolder(holder, post)

            try {
                listener?.onLike(post)
            } catch (e: Exception) {
                Log.e("PostsAdapter", "Error in onLike callback: ${e.message}")
                doLikeClickOnHolder(holder, post) // Revert UI change
                Toast.makeText(v.context, "Error in onLike callback: ${e.message}", Toast.LENGTH_LONG).show()
            }
        }

        holder.ivComment.setOnClickListener {
            listener?.onComment(post)
        }

        holder.ivShare.setOnClickListener {
            listener?.onShare(post)
        }

        holder.ivBookmark.setOnClickListener {
            listener?.onBookmark(post)
        }

        holder.itemView.setOnClickListener {
            listener?.onItemClick(post)
        }
    }

    /**
     * Gère le clic sur le bouton like d'un post (animation + changement UI).
     */
    @SuppressLint("SetTextI18n")
    private fun doLikeClickOnHolder(holder: ViewHolder, post: TimelinePost) {
        val nowLiked = if (likedIds.contains(post.id)) {
            likedIds.remove(post.id)
            false
        } else {
            likedIds.add(post.id)
            true
        }

        // Scale up puis revenir
        holder.ivLike.animate().scaleX(1.3f).scaleY(1.3f).setDuration(120).withEndAction {
            holder.ivLike.animate().scaleX(1f).scaleY(1f).setDuration(120).start()
        }.start()

        // Update UI
        holder.ivLike.setColorFilter(if (nowLiked) Color.RED else Color.DKGRAY)
        holder.tvLikes.text = if (nowLiked) {
            post.likes_count + 1
        } else {
            post.likes_count - 1
        }.toString() + " likes"
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
}
