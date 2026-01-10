package com.cliche.app.services.api

import android.util.Log
import com.cliche.app.models.Like
import com.cliche.app.models.Post
import com.cliche.app.models.TimelinePost
import com.cliche.app.modules.supabaseClient
import com.cliche.app.services.auth.AuthManager
import io.github.jan.supabase.postgrest.postgrest
import kotlinx.serialization.json.JsonPrimitive
import kotlinx.serialization.json.buildJsonObject

/**
 * Singleton object for interacting with the Posts API.
 */
object PostApi {
    /**
     * Fetches a list of posts within the specified range.
     *
     * @param start The starting index for pagination.
     * @param end The ending index for pagination.
     * @return A list of [Post] objects.
     */
    suspend fun getTimeline(start: Long, end: Long): List<TimelinePost> {
        val params = buildJsonObject {
            put("start_idx", JsonPrimitive(start))
            put("end_idx", JsonPrimitive(end))
        }
        Log.d("PostApi", "Fetching timeline posts from $start to $end")
        val rpcResponse = supabaseClient.postgrest.rpc("get_timeline",params)
        Log.d("PostApi", "Fetched timeline posts from $start to $end, response: ${rpcResponse.data}")
        return rpcResponse.decodeList<TimelinePost>()
    }

    suspend fun addLike(postId: Long) {
        val userId = AuthManager.getUserOrNull()?.id

        if(userId == null) {
            Log.e("PostApi", "Cannot add like: user is not logged in")
            return
        }

        Log.d("PostApi", "Adding like to post $postId by user $userId")
        supabaseClient.postgrest
            .from("likes")
            .insert(Like(post_id = postId, user_id = userId))
        Log.d("PostApi", "Like added to post $postId by user $userId")
    }

    suspend fun removeLike(postId: Long) {
        val userId = AuthManager.getUserOrNull()?.id

        if(userId == null) {
            Log.e("PostApi", "Cannot remove like: user is not logged in")
            return
        }

        Log.d("PostApi", "Removing like from post $postId by user $userId")
        supabaseClient.postgrest
            .from("likes")
            .delete {
                filter {
                    eq("post_id", postId)
                    eq("user_id", userId)
                }
            }
        Log.d("PostApi", "Like removed from post $postId by user $userId")
    }
}