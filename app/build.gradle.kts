import java.net.URI
import java.util.Properties

plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.kotlin.android)
    alias(libs.plugins.kotlin.compose)
    alias(libs.plugins.kotlin.serialization)
    alias(libs.plugins.paparazzi)
}

val localProperties = Properties().apply {
    val localPropertiesFile = rootProject.file("local.properties")
    if (localPropertiesFile.exists()) {
        localPropertiesFile.inputStream().use(::load)
    }
}

fun requiredConcept2Config(environmentName: String, localPropertyName: String): String {
    val environmentValue = providers.environmentVariable(environmentName).orNull?.takeIf { it.isNotBlank() }
    val localPropertyValue = localProperties.getProperty(localPropertyName)?.takeIf { it.isNotBlank() }
    return environmentValue ?: localPropertyValue
    ?: throw GradleException(
        "Missing required Concept2 configuration. Set $environmentName or $localPropertyName in local.properties."
    )
}

fun String.toBuildConfigString(): String = "\"" + replace("\\", "\\\\").replace("\"", "\\\"") + "\""

val concept2ClientId = requiredConcept2Config("CONCEPT2_CLIENT_ID", "concept2.clientId")
val concept2ClientSecret = requiredConcept2Config("CONCEPT2_CLIENT_SECRET", "concept2.clientSecret")
val concept2RedirectUri = requiredConcept2Config("CONCEPT2_REDIRECT_URI", "concept2.redirectUri")

val parsedRedirectUri = URI(concept2RedirectUri)
val redirectScheme = parsedRedirectUri.scheme
    ?: throw GradleException("CONCEPT2_REDIRECT_URI must include a URI scheme.")

android {
    namespace = "com.schroepf.row"
    compileSdk = 35

    defaultConfig {
        applicationId = "com.schroepf.row"
        minSdk = 24
        targetSdk = 35
        versionCode = 1
        versionName = "1.0"

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
        buildConfigField("String", "CONCEPT2_CLIENT_ID", concept2ClientId.toBuildConfigString())
        buildConfigField("String", "CONCEPT2_CLIENT_SECRET", concept2ClientSecret.toBuildConfigString())
        buildConfigField("String", "CONCEPT2_REDIRECT_URI", concept2RedirectUri.toBuildConfigString())
        manifestPlaceholders["appAuthRedirectScheme"] = redirectScheme
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
        compose = true
        buildConfig = true
    }
    testOptions {
        unitTests.isIncludeAndroidResources = true
    }
}

dependencies {
    implementation(libs.androidx.core.ktx)
    implementation(libs.androidx.lifecycle.runtime.ktx)
    implementation(libs.androidx.lifecycle.runtime.compose)
    implementation(libs.androidx.lifecycle.viewmodel.compose)
    implementation(libs.androidx.activity.compose)
    implementation(platform(libs.androidx.compose.bom))
    implementation(libs.androidx.compose.ui)
    implementation(libs.androidx.compose.ui.tooling.preview)
    implementation(libs.androidx.compose.material3)
    implementation(libs.google.material)
    implementation(libs.appauth)
    implementation(libs.kotlinx.coroutines.android)

    implementation(libs.ktor.client.core)
    implementation(libs.ktor.client.cio)
    implementation(libs.ktor.client.content.negotiation)
    implementation(libs.ktor.serialization.kotlinx.json)
    implementation(libs.kotlinx.serialization.json)

    testImplementation(libs.junit4)
    testImplementation(libs.kotlinx.coroutines.test)
    testImplementation(libs.ktor.client.mock)
    testImplementation(platform(libs.androidx.compose.bom))
    testImplementation(libs.androidx.compose.ui)
    testImplementation(libs.paparazzi)

    androidTestImplementation(platform(libs.androidx.compose.bom))
    androidTestImplementation(libs.androidx.compose.ui.test.junit4)
    androidTestImplementation(libs.androidx.test.ext.junit)
    androidTestImplementation(libs.androidx.test.espresso.core)
    debugImplementation(libs.androidx.compose.ui.tooling)
    debugImplementation(libs.androidx.compose.ui.test.manifest)
}
