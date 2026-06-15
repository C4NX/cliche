import java.util.Properties

val localProperties = Properties().apply {
    val file = rootProject.file("local.properties")
    if (file.exists()) file.inputStream().use { load(it) }
}

val supabaseUrlProp = localProperties.getProperty("SUPABASE_URL") ?: ""
val supabaseAnonKeyProp = localProperties.getProperty("SUPABASE_ANON_KEY") ?: ""

val testSupabaseUrlProp = localProperties.getProperty("SUPABASE_TEST_URL")
val testSupabaseAnonKeyProp = localProperties.getProperty("SUPABASE_TEST_ANON_KEY")
val testSupabaseServiceRoleKeyProp = localProperties.getProperty("SUPABASE_TEST_SERVICE_ROLE_KEY")

val tagName = localProperties.getProperty("TAG_NAME") ?: "@dev"

plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.kotlin.android)
    alias(libs.plugins.kotlin.serialization)
}

android {
    namespace = "com.cliche.app"
    compileSdk = 36

    defaultConfig {
        applicationId = "com.cliche.app"
        minSdk = 27
        targetSdk = 36
        versionCode = 1
        versionName = "1.0"

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"

        buildConfigField("String", "SUPABASE_URL", "\"$supabaseUrlProp\"")
        buildConfigField("String", "SUPABASE_ANON_KEY", "\"$supabaseAnonKeyProp\"")
        buildConfigField("String", "TAG_NAME", "\"$tagName\"")

        buildConfigField("String", "SUPABASE_TEST_URL", "\"${testSupabaseUrlProp ?: ""}\"")
        buildConfigField("String", "SUPABASE_TEST_ANON_KEY", "\"${testSupabaseAnonKeyProp ?: ""}\"")
        buildConfigField("String", "SUPABASE_TEST_SERVICE_ROLE_KEY", "\"${testSupabaseServiceRoleKeyProp ?: ""}\"")
    }

    testOptions {
        unitTests {
            isIncludeAndroidResources = true
            all {
                it.systemProperty("robolectric.defaultSdk", "34")
            }
        }
    }

    java {
        toolchain {
            languageVersion.set(JavaLanguageVersion.of(17))
        }
    }

    buildTypes {
        release {
            isMinifyEnabled = false
            proguardFiles(
                getDefaultProguardFile("proguard-android-optimize.txt"),
                "proguard-rules.pro"
            )
        }
    }
    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_17
        targetCompatibility = JavaVersion.VERSION_17
    }
    kotlinOptions {
        jvmTarget = "17"
    }
    buildFeatures {
        buildConfig = true
        viewBinding = true
    }
}

tasks.withType<Test>().configureEach {
    javaLauncher.set(javaToolchains.launcherFor {
        languageVersion.set(JavaLanguageVersion.of(17))
    })
    
    testLogging {
        showStandardStreams = true
        events("passed", "failed", "skipped")
    }
}

dependencies {
    implementation(libs.androidx.core.ktx)
    implementation(libs.androidx.appcompat)
    implementation(libs.androidx.swiperefreshlayout)
    implementation(libs.material)
    implementation(libs.androidx.constraintlayout)
    implementation(libs.androidx.lifecycle.livedata.ktx)
    implementation(libs.androidx.lifecycle.viewmodel.ktx)
    implementation(libs.androidx.activity.ktx)
    implementation(libs.androidx.navigation.fragment.ktx)
    implementation(libs.androidx.navigation.ui.ktx)
    implementation(libs.androidx.ui.text)
    implementation(libs.androidx.junit.ktx)
    testImplementation(libs.junit)
    testImplementation(libs.robolectric)
    testImplementation(libs.kotlinx.coroutines.core)
    testImplementation(libs.androidx.test.core)
    testImplementation(libs.kotlinx.coroutines.test)
    testImplementation(libs.androidx.test.ext.junit)
    testImplementation(libs.mockito.core)
    testImplementation(libs.mockito.inline)
    testImplementation(libs.multiplatform.settings.test)
    androidTestImplementation(libs.androidx.junit)
    androidTestImplementation(libs.androidx.espresso.core)
    implementation(platform(libs.supabase.bom))
    implementation(libs.postgrest.kt)
    implementation(libs.auth.kt)
    implementation(libs.realtime.kt)
    implementation(libs.functions.kt)
    implementation(libs.storage.kt)
    implementation(libs.ktor.client.okhttp)
    implementation(libs.coil)
    implementation(libs.androidx.preference.ktx)
    implementation(libs.play.services.location)
}