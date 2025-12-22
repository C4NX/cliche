package com.cliche.app.models

import kotlinx.serialization.Serializable
import java.util.UUID

@Serializable
data class TimelinePost(
        val id: Long,
        val created_at: String,
        val description: String,
        val owner_id: String,
        var user_has_liked: Boolean,
        val content: List<String>,
        val username: String,
        val avatar_url: String?,
        var likes_count: Int,
        val comments_count: Int,
        val score: Double
);
