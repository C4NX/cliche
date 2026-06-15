package com.cliche.app.tests

import io.github.jan.supabase.SupabaseClient
import io.github.jan.supabase.createSupabaseClient
import io.github.jan.supabase.postgrest.Postgrest
import io.github.jan.supabase.functions.Functions
import io.github.jan.supabase.storage.Storage
import io.github.jan.supabase.realtime.Realtime
import io.github.jan.supabase.auth.Auth
import io.github.jan.supabase.auth.MemoryCodeVerifierCache
import io.github.jan.supabase.auth.MemorySessionManager

/**
 * Fournit des clients Supabase **partagés** pour les tests.
 * Gère deux instances :
 * - **Client anonyme** : pour les opérations côté utilisateur.
 * - **Client service-role** : pour les opérations administratives (accès complet).
 *
 * @throws IllegalStateException Si les clients ne sont pas initialisés avant utilisation.
 */
object SupabaseTestClientProvider {
    private var anonClient: SupabaseClient? = null
    private var serviceClient: SupabaseClient? = null

    /**
     * Crée un client Supabase configuré pour les tests.
     * Désactive la persistance des sessions et utilise des caches en mémoire.
     *
     * @param url URL de l'instance Supabase (ex: `https://xxx.supabase.co`).
     * @param key Clé d'API (anonyme ou service-role).
     * @param scheme Schéma d'URI pour l'authentification (ex: `com.cliche.app`).
     * @return [SupabaseClient] prêt à l'emploi.
     * @throws IllegalStateException Si la création échoue.
     */
    private fun createClient(url: String, key: String, scheme: String): SupabaseClient {
        return try {
            createSupabaseClient(supabaseUrl = url, supabaseKey = key) {
                install(Auth) {
                    autoSaveToStorage = false  // Désactive la persistance (utiles pour les tests)
                    autoLoadFromStorage = false
                    sessionManager = MemorySessionManager()
                    codeVerifierCache = MemoryCodeVerifierCache()
                    host = url
                    this.scheme = scheme
                }
                install(Postgrest)
                install(Functions)
                install(Storage)
                install(Realtime)
            }
        } catch (e: Exception) {
            throw IllegalStateException("Échec de la création du client Supabase : ${e.message}", e)
        }
    }

    /**
     * Initialise les clients avec des valeurs explicites.
     *
     * @param url URL de Supabase.
     * @param anonKey Clé **anonyme** (pour le client public).
     * @param serviceKey Clé **service-role** (pour le client admin).
     * @param scheme Schéma d'URI (par défaut : `com.cliche.app`).
     * @throws IllegalArgumentException Si un paramètre est vide.
     */
    fun init(
        url: String,
        anonKey: String,
        serviceKey: String,
        scheme: String = "com.cliche.app"
    ) {
        require(url.isNotBlank()) { "L'URL ne peut pas être vide." }
        require(anonKey.isNotBlank()) { "La clé anonyme ne peut pas être vide." }
        require(serviceKey.isNotBlank()) { "La clé service-role ne peut pas être vide." }

        anonClient = createClient(url, anonKey, scheme)
        serviceClient = createClient(url, serviceKey, scheme)
    }

    /**
     * Initialise les clients depuis les constantes **BuildConfig**.
     * Requiert les champs suivants dans `com.cliche.app.BuildConfig` :
     * - `SUPABASE_TEST_URL`
     * - `SUPABASE_TEST_ANON_KEY`
     * - `SUPABASE_TEST_SERVICE_ROLE_KEY`
     *
     * @param scheme Schéma d'URI (par défaut : `com.cliche.app`).
     * @throws IllegalStateException Si une constante est manquante.
     */
    fun initFromBuildConfig(scheme: String = "com.cliche.app") {
        try {
            val buildConfigClass = Class.forName("com.cliche.app.BuildConfig")

            val url = buildConfigClass
                .getDeclaredField("SUPABASE_TEST_URL")
                .get(null) as String

            val anonKey = buildConfigClass
                .getDeclaredField("SUPABASE_TEST_ANON_KEY")
                .get(null) as String

            val serviceKey = buildConfigClass
                .getDeclaredField("SUPABASE_TEST_SERVICE_ROLE_KEY")
                .get(null) as String

            init(url, anonKey, serviceKey, scheme)
        } catch (e: Exception) {
            throw IllegalStateException(
                "Impossible de lire les constantes BuildConfig : ${e.message}",
                e
            )
        }
    }

    /**
     * @return Client **anonyme** (pour les tests utilisateur).
     * @throws IllegalStateException Si non initialisé.
     */
    fun asAnon(): SupabaseClient =
        anonClient ?: error("Client anonyme non initialisé. Appelez `init()` ou `initFromBuildConfig()` d'abord.")

    /**
     * @return Client **service-role** (pour les tests admin).
     * @throws IllegalStateException Si non initialisé.
     */
    fun asService(): SupabaseClient =
        serviceClient
            ?: error("Client service-role non initialisé. Appelez `init()` ou `initFromBuildConfig()` d'abord.")


    /**
     * Réinitialise les clients.
     */
    fun reset() {
        anonClient = null
        serviceClient = null
    }
}