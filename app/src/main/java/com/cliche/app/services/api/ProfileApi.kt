package com.cliche.app.services.api

import com.cliche.app.modules.supabaseClient
import io.github.jan.supabase.storage.storage

object ProfileApi {
    /**
     * Generates a public URL for the given avatar stored in Supabase Storage.
     *
     * @param avatarUrl The path of the avatar in the storage bucket.
     * @return The public URL of the avatar, or null if the input is null or empty.
     */
    fun getPublicAvatarUrl(avatarUrl: String?): String? {
        if (avatarUrl == null || avatarUrl.isEmpty()) {
            return null
        }

        return supabaseClient.storage
            .from("avatars")
            .publicUrl(avatarUrl)
    }
}