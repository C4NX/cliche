package com.cliche.app.models.events

import kotlinx.serialization.Serializable

@Serializable
data class LikeEvent (
    val id: Long? = null,
    val post_id: Long,
    val user_id: String? = null
) {
    companion object {
        val EVENT_NAME: String = "insert"
    }
}
