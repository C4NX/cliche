package com.cliche.app.models

import kotlinx.serialization.Serializable

@Serializable
data class Post (
    val id: Int,
    var created_at: String,
    var caption: String?,
    var owner: Profile? = null,
    var media_paths: List<String>? = null
)
