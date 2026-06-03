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
 * Fournit des clients Supabase partagés pour les tests.
 *
 * Cette fabrique crée et conserve deux instances distinctes :
 * - un client anonyme pour les opérations côté utilisateur
 * - un client service-role pour les opérations de service
 */
object SupabaseTestClientProvider {
    private var anonClient: SupabaseClient? = null
    private var serviceClient: SupabaseClient? = null

    /**
     * Crée un client Supabase configuré pour les besoins des tests.
     *
     * @param url URL de l'instance Supabase.
     * @param key Clé d'accès utilisée pour l'authentification du client.
     * @param scheme Schéma d'URI personnalisé utilisé par l'authentification.
     * @return Un [SupabaseClient] prêt à l'emploi.
     * @throws IllegalStateException Si le client ne peut pas être créé.
     */
    private fun createClient(url: String, key: String, scheme: String): SupabaseClient {
        try {
            return createSupabaseClient(url, key) {
                install(Auth) {
                    autoSaveToStorage = false
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
            println("Error creating SupabaseClient: ${e.message}")
            throw IllegalStateException("Failed to create SupabaseClient: ${e.message}", e)
        }
    }

    /**
     * Initialise les clients de test avec les valeurs fournies.
     *
     * @param url URL de Supabase.
     * @param anonKey Clé anonyme utilisée pour le client public.
     * @param serviceKey Clé service-role utilisée pour le client privilégié.
     * @param scheme Schéma d'URI personnalisé utilisé par l'authentification.
     * @throws IllegalArgumentException Si l'un des paramètres obligatoires est vide.
     */
    fun init(
        url: String,
        anonKey: String,
        serviceKey: String,
        scheme: String = "com.cliche.app"
    ) {
        require(url.isNotEmpty()) { "URL cannot be empty" }
        require(anonKey.isNotEmpty()) { "Anon key cannot be empty" }
        require(serviceKey.isNotEmpty()) { "Service key cannot be empty" }

        anonClient = createClient(url, anonKey, scheme)
        println("Supabase Anon client created with URL: $url")

        this.serviceClient = createClient(url, serviceKey, scheme)
        println("Supabase Service client created with URL: $url")
    }

    /**
     * Initialise les clients de test à partir des constantes `BuildConfig`.
     *
     * Cette méthode lit `SUPABASE_TEST_URL`, `SUPABASE_TEST_ANON_KEY` et
     * `SUPABASE_TEST_SERVICE_ROLE_KEY` depuis `com.cliche.app.BuildConfig`.
     *
     * @param scheme Schéma d'URI personnalisé utilisé par l'authentification.
     * @throws IllegalStateException Si une des constantes attendues est absente.
     */
    fun initFromBuildConfig(scheme: String = "com.cliche.app") {
        val buildConfigClass = Class.forName("com.cliche.app.BuildConfig")

        val url = try {
            buildConfigClass
                .getDeclaredField("SUPABASE_TEST_URL")
                .get(null) as String
        } catch (_: Exception) {
            throw IllegalStateException("Failed to retrieve SUPABASE_TEST_URL from BuildConfig")
        }

        val anonKey = try {
            buildConfigClass
                .getDeclaredField("SUPABASE_TEST_ANON_KEY")
                .get(null) as String
        } catch (_: Exception) {
            throw IllegalStateException("Failed to retrieve SUPABASE_TEST_ANON_KEY from BuildConfig")
        }

        val serviceKey = try {
            buildConfigClass
                .getDeclaredField("SUPABASE_TEST_SERVICE_ROLE_KEY")
                .get(null) as String
        } catch (_: Exception) {
            throw IllegalStateException("Failed to retrieve SUPABASE_TEST_SERVICE_ROLE_KEY from BuildConfig")
        }

        init(url, anonKey, serviceKey, scheme)
    }

    fun asAnon(): SupabaseClient =
        anonClient ?: error("Anon SupabaseClient not initialized. Call init() first.")

    fun asService(): SupabaseClient =
        serviceClient ?: error("Service SupabaseClient not initialized. Call init() first.")
}