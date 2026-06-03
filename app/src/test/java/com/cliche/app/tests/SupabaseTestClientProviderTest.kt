package com.cliche.app.tests

import org.junit.Before
import org.junit.Test
import org.junit.Assert.assertNotNull
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [34])
class SupabaseTestClientProviderTest {
    @Before
    fun setup() {
        SupabaseTestClientProvider.initFromBuildConfig()
    }

    @Test
    fun testAnonClientIsInitialized() {        
        assertNotNull("Anon client should be initialized", SupabaseTestClientProvider.asAnon())
    }

    @Test
    fun testServiceClientIsInitialized() {
        assertNotNull(
            "Service client should be initialized",
            SupabaseTestClientProvider.asService()
        )
    }
}