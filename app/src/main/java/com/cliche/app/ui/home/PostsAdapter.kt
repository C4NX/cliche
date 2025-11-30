package com.cliche.app.ui.home

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import coil.load
import com.cliche.app.R
import com.cliche.app.models.Post

class PostsAdapter(
    private val items: MutableList<Post>
) : RecyclerView.Adapter<PostsAdapter.ViewHolder>() {

    class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val ivAvatar: ImageView = itemView.findViewById(R.id.ivAvatar)
        val tvUsername: TextView = itemView.findViewById(R.id.tvUsername)
        val ivPostImage: ImageView = itemView.findViewById(R.id.ivPostImage)
        val tvLikes: TextView = itemView.findViewById(R.id.tvLikes)
        val tvCaption: TextView = itemView.findViewById(R.id.tvCaption)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val v = LayoutInflater.from(parent.context).inflate(R.layout.item_instagram_post, parent, false)
        return ViewHolder(v)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val post = items[position]

        holder.tvUsername.text = post.owner?.username ?: "Unknown"
        val avatarUrl = post.owner?.avatar_url
        if (!avatarUrl.isNullOrEmpty()) {
            holder.ivAvatar.load(avatarUrl) {
                crossfade(true)
                placeholder(R.drawable.ic_avatar_placeholder)
                error(R.drawable.ic_avatar_placeholder)
            }
        } else {
            holder.ivAvatar.setImageResource(R.drawable.ic_avatar_placeholder)
        }

        val firstImageUrl = post.content?.firstOrNull()
        if (!firstImageUrl.isNullOrEmpty()) {
            holder.ivPostImage.load(firstImageUrl) {
                crossfade(true)
                placeholder(R.drawable.ic_avatar_placeholder)
                error(R.drawable.ic_avatar_placeholder)
            }
        } else {
            holder.ivPostImage.setImageResource(R.drawable.ic_avatar_placeholder)
        }

        holder.tvLikes.text = post.created_at
        holder.tvCaption.text = post.description
    }

    override fun getItemCount(): Int = items.size

    fun addAll(newItems: List<Post>) {
        val start = items.size
        items.addAll(newItems)
        notifyItemRangeInserted(start, newItems.size)
    }
}
