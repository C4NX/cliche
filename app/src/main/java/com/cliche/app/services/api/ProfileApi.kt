package com.cliche.app.services.api

import android.util.Log
import com.cliche.app.models.Profile
import io.github.jan.supabase.SupabaseClient
import io.github.jan.supabase.postgrest.postgrest
import io.github.jan.supabase.storage.storage
import java.io.File

/**
 * API for managing user profiles.
 */
class ProfileApi(
    private val supabaseClient: SupabaseClient
) {
    private val TAG = "ProfileApi"

    /**
     * Generates a public URL for the given avatar stored in Supabase Storage.
     */
    fun getPublicAvatarUrl(avatarUrl: String?): String? {
        if (avatarUrl.isNullOrEmpty()) {
            return null
        }
        return supabaseClient.storage
            .from("avatars")
            .publicUrl(avatarUrl)
    }

    /**
     * Fetches the user profile by its ID.
     *
     * @param userId The user ID.
     * @return The user profile or null if it doesn't exist.
     */
    suspend fun fetchProfile(userId: String): Profile? {
        Log.d(TAG, "Fetching profile for $userId")
        val resp = supabaseClient.postgrest.from("profiles").select {
            filter { eq("id", userId) }
            limit(1)
        }

        return try {
            resp.decodeList<Profile>().firstOrNull()
        } catch (e: Exception) {
            Log.e(TAG, "Error decoding profile: ", e)
            null
        }
    }

    /**
     * Updates the user profile.
     *
     * @param userId The user ID.
     * @param username The new username (nullable).
     * @param bio The new bio (nullable).
     * @param avatarPath The avatar path in storage (nullable).
     */
    suspend fun updateProfile(
        userId: String,
        username: String?,
        bio: String?,
        avatarPath: String?
    ) {
        supabaseClient.postgrest
            .from("profiles")
            .update({
                if (bio != null) Profile::bio setTo bio
                if (username != null) Profile::username setTo username
                if (avatarPath != null) Profile::avatar_url setTo avatarPath
            }) {
                filter {
                    eq("id", userId)
                }
            }
    }

    /**
     * Uploads an avatar image to Supabase Storage.
     *
     * @param localFilePath The local file path of the image to upload.
     * @param storagePath The destination path in Supabase Storage.
     * @return The storage path where the avatar was uploaded.
     */
    suspend fun uploadAvatar(localFilePath: String, storagePath: String): String {
        val file = File(localFilePath)
        if (!file.exists()) throw IllegalArgumentException("File does not exist: $localFilePath")

        val bytes = file.readBytes()
        supabaseClient.storage.from("avatars").upload(storagePath, bytes)

        return storagePath
    }
}