package com.cliche.app.services.api

import android.util.Log
import com.cliche.app.models.Like
import com.cliche.app.models.Post
import com.cliche.app.models.TimelinePost
import com.cliche.app.modules.supabaseClient
import com.cliche.app.services.auth.AuthManager
import com.cliche.app.utils.LatLng
import com.cliche.app.utils.requireUserId
import io.github.jan.supabase.exceptions.UnauthorizedRestException
import io.github.jan.supabase.functions.functions
import io.github.jan.supabase.postgrest.postgrest
import io.github.jan.supabase.postgrest.query.Order
import io.github.jan.supabase.storage.storage
import io.ktor.client.request.forms.MultiPartFormDataContent
import io.ktor.client.request.forms.append
import io.ktor.client.request.forms.formData
import io.ktor.client.request.setBody
import io.ktor.client.statement.bodyAsText
import io.ktor.http.ContentType
import io.ktor.http.isSuccess
import io.ktor.utils.io.core.writePacket
import io.ktor.utils.io.streams.asInput
import java.io.File

/**
 * API for managing posts.
 */
object PostApi {
    const val TAG = "PostApi"

    /**
     * Fetch a single post by its id from the timeline view.
     */
    suspend fun fetchPostById(id: Long): TimelinePost? {
        Log.d(TAG, "Fetching post by id $id")
        val results = supabaseClient.postgrest.from("timeline")
            .select {
                filter { eq("id", id) }
                limit(1)
            }
            .decodeList<TimelinePost>()
        return results.firstOrNull()
    }

    /**
     * Fetches a list of posts within the specified range.
     *
     * @param start The starting index for pagination.
     * @param end The ending index for pagination.
     * @return A list of TimelinePost objects.
     */
    suspend fun fetchTimeline(start: Long, end: Long): List<TimelinePost> {
        Log.d(TAG, "Fetching timeline posts from $start to $end")

        return supabaseClient.postgrest.from("timeline")
            .select {
                order("created_at", Order.DESCENDING)
                range(start, end)
            }
            .decodeList<TimelinePost>()
    }

    /**
     * Fetch posts where the owner is the given user id.
     *
     * @param ownerId The ID of the owner whose posts to fetch.
     * @param start The starting index for pagination.
     * @param end The ending index for pagination.
     * @return A list of TimelinePost objects.
     */
    suspend fun fetchPostsByOwner(ownerId: String, start: Long, end: Long): List<TimelinePost> {
        Log.d(TAG, "Fetching posts for owner $ownerId from $start to $end")

        return supabaseClient.postgrest.from("timeline")
            .select {
                order("created_at", Order.DESCENDING)
                range(start, end)
                filter { eq("owner_id", ownerId) }
            }.decodeList<TimelinePost>()
    }

    /**
     * Creates a new post with the given caption and media files.
     */
    suspend fun createPost(caption: String, filePaths: List<String>, location: LatLng? = null) {
        val userId = requireUserId()
        Log.d(TAG, "Creating post by user $userId")

        val response = supabaseClient.functions.invoke(function = "create-post") {
            setBody(
                MultiPartFormDataContent(
                    formData {
                        append("caption", caption)
                        if (location != null) {
                            append("latitude", location.latitude.toString())
                            append("longitude", location.longitude.toString())
                        }

                        filePaths.forEach { path ->
                            // Validate file existence
                            val file = File(path)
                            if (!file.exists()) {
                                throw IllegalArgumentException("File does not exist: $path")
                            }

                            // Determine content type based on file extension
                            val contentType = when {
                                path.endsWith(".png", ignoreCase = true) -> ContentType.Image.PNG
                                path.endsWith(".jpg", ignoreCase = true) || path.endsWith(
                                    ".jpeg",
                                    ignoreCase = true
                                ) -> ContentType.Image.JPEG

                                else -> ContentType.Application.OctetStream
                            }

                            // Upload file as multipart form data
                            val stream = file.inputStream()
                            append(
                                key = "media",
                                filename = file.name,
                                contentType = contentType,
                            ) {
                                writePacket(stream.asInput())
                            }
                        }
                    }
                ))
        }

        // Check for errors in the response
        if (!response.status.isSuccess()) {
            Log.e(TAG, "Failed to create post: ${response.bodyAsText()}")
            throw Exception("Failed to create post: ${response.status}")
        }
        Log.d(TAG, "Post created successfully: ${response.bodyAsText()}")
    }

    /**
     * Likes a post on behalf of the currently authenticated user.
     *
     * @param postId The ID of the post to like.
     */
    suspend fun like(postId: Long) {
        val userId = requireUserId()

        supabaseClient.postgrest
            .from("likes")
            .upsert(
                Like(
                    post_id = postId,
                    user_id = userId
                )
            )
    }

    /**
     * Unlikes a post on behalf of the currently authenticated user.
     *
     * @param postId The ID of the post to unlike.
     */
    suspend fun unlike(postId: Long) {
        val userId = requireUserId()

        supabaseClient.postgrest
            .from("likes")
            .delete {
                filter {
                    eq("post_id", postId)
                    eq("user_id", userId)
                }
            }
    }


    /**
     * Generates public URLs for the media files associated with a post.
     *
     * @param post The TimelinePost object containing media paths.
     * @return A list of public URLs for the media files, or null if there are no media paths.
     */
    fun getPostPublicUrls(post: TimelinePost): List<String>? {
        return post.media_paths.map {
            supabaseClient.storage
                .from("posts")
                .publicUrl(it)
        }
    }
}