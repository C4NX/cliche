package com.cliche.app.models

import kotlinx.serialization.Serializable

@Serializable
data class Profile (
    val id: String,
    val username: String,
    val bio: String? = null,
    val avatar_url: String? = null
)
