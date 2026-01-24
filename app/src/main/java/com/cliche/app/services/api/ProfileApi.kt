package com.cliche.app.services.api

import android.util.Log
import com.cliche.app.models.Profile
import com.cliche.app.modules.supabaseClient
import io.github.jan.supabase.postgrest.postgrest
import java.io.File
import io.github.jan.supabase.storage.storage

/**
 * API for managing user profiles.
 */
object ProfileApi {
    private const val TAG = "ProfileApi"

    /**
     * Generates a public URL for the given avatar stored in Supabase Storage.
     */
    fun getPublicAvatarUrl(avatarUrl: String?): String? {
        if (avatarUrl == null || avatarUrl.isEmpty()) {
            return null
        }

        return supabaseClient.storage
            .from("avatars")
            .publicUrl(avatarUrl)
    }

    /**
     * Récupère le profil de l'utilisateur par son ID.
     *
     * @param userId L'ID de l'utilisateur.
     * @return Le profil de l'utilisateur ou null s'il n'existe pas.
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
     * Met à jour le profil de l'utilisateur.
     *
     * @param userId L'ID de l'utilisateur.
     * @param username Le nouveau nom d'utilisateur (nullable).
     * @param bio La nouvelle biographie (nullable).
     * @param avatarPath Le chemin de l'avatar dans le stockage (nullable).
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