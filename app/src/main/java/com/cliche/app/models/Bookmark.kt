package com.cliche.app.models

import kotlinx.serialization.Serializable

@Serializable
data class Bookmark(
    val id: Long? = null,
    val post_id: Long,
    val profile_id: String? = null
)
