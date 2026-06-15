package com.cliche.app.services.api

import com.cliche.app.modules.supabaseClient
import com.cliche.app.services.auth.AuthManager

/**
 * Central singleton for accessing all API clients using supabaseClient singleton instance.
 */
object AppApi {
    val authManager: AuthManager by lazy { AuthManager(supabaseClient) }
    val postApi: PostApi by lazy { PostApi(supabaseClient, authManager) }
    val profileApi: ProfileApi by lazy { ProfileApi(supabaseClient) }
}