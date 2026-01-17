package com.cliche.app.modules

import com.cliche.app.BuildConfig
import io.github.jan.supabase.auth.Auth
import io.github.jan.supabase.createSupabaseClient
import io.github.jan.supabase.postgrest.Postgrest

/**
 * Supabase client instance for interacting with the Supabase backend.
 */
val supabaseClient by lazy {
    createSupabaseClient(
        supabaseUrl = BuildConfig.SUPABASE_URL,
        supabaseKey = BuildConfig.SUPABASE_ANON_KEY
    ) {
        install(Auth) {
            autoSaveToStorage = true
            autoLoadFromStorage = true
            host = BuildConfig.SUPABASE_URL
            scheme = "com.cliche.app"
        }
        install(Postgrest)
    }
}