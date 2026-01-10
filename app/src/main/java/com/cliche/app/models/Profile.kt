package com.cliche.app.models

import kotlinx.serialization.Serializable

@Serializable
data class Profile (
    val username: String,
    val avatar_url: String,
)
