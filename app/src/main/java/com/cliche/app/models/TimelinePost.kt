package com.cliche.app.models

import kotlinx.serialization.Serializable

@Serializable
data class TimelinePost(
    val id: Long,
    val created_at: String,
    val caption: String?,
    val owner_id: String,
    val owner_username: String,
    val owner_avatar_url: String?,
    val media_paths: List<String>,
    var likes_count: Long,
    val comments_count: Long,
    var liked_by_user: Boolean,
);
