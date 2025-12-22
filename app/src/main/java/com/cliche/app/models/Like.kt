package com.cliche.app.models

import kotlinx.serialization.Serializable

@Serializable
data class Like (
    val id: Long? = null,
    val post_id: Long,
    val user_id: String? = null
)
