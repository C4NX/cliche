package com.cliche.app.services.api

import android.util.Log
import com.cliche.app.models.Like
import com.cliche.app.models.Post
import com.cliche.app.models.TimelinePost
import com.cliche.app.modules.supabaseClient
import com.cliche.app.services.auth.AuthManager
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
 * Singleton object for interacting with the Posts API.
 */
object PostApi {
    const val TAG = "PostApi"

    /**
     * Fetches a list of posts within the specified range.
     *
     * @param start The starting index for pagination.
     * @param end The ending index for pagination.
     * @return A list of [Post] objects.
     */
    suspend fun fetchTimeline(start: Long, end: Long): List<TimelinePost> {
        Log.d(TAG, "Fetching timeline posts from $start to $end")

        val response = supabaseClient.postgrest.from("timeline")
            .select {
                order("created_at", Order.DESCENDING)
                range(start, end)
            };

        Log.d(TAG, response.data)
        return response.decodeList<TimelinePost>()
    }

    /**
     * Creates a new post with the given caption and media files.
     *
     * @param caption The caption of the post.
     * @param filePaths A list of file paths to be uploaded as media.
     */
    suspend fun createPost(caption: String, filePaths: List<String>) {
        Log.d(TAG, "Creating post by user ${AuthManager.getUserOrNull()?.id}")

        val response = supabaseClient.functions.invoke(function = "create-post") {
            setBody(
                MultiPartFormDataContent(
                    formData {
                        append("caption", caption)

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

    suspend fun addLike(postId: Long) {
        val userId = AuthManager.getUserOrNull()?.id
            ?: throw IllegalStateException("User must be logged in to like a post")

        Log.d(TAG, "Adding like to post $postId by user $userId")
        supabaseClient.postgrest
            .from("likes")
            .insert(Like(post_id = postId, user_id = userId))
        Log.d(TAG, "Like added to post $postId by user $userId")
    }

    suspend fun removeLike(postId: Long) {
        val userId = AuthManager.getUserOrNull()?.id
            ?: throw IllegalStateException("User must be logged in to remove a like from a post")

        Log.d(TAG, "Removing like from post $postId by user $userId")
        supabaseClient.postgrest
            .from("likes")
            .delete {
                filter {
                    eq("post_id", postId)
                    eq("user_id", userId)
                }
            }
        Log.d(TAG, "Like removed from post $postId by user $userId")
    }

    /**
     * Generates public URLs for the media files associated with a post.
     *
     * @param post The [TimelinePost] object containing media paths.
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