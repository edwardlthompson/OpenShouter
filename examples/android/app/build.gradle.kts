import java.util.Properties

plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.kotlin.compose)
    alias(libs.plugins.ksp)
    alias(libs.plugins.hilt)
}

kotlin {
    compilerOptions {
        jvmTarget.set(org.jetbrains.kotlin.gradle.dsl.JvmTarget.JVM_17)
    }
}

val keystoreProperties = Properties().apply {
    val file = rootProject.file("keystore.properties")
    if (file.isFile) {
        load(file.inputStream())
    }
}

fun signingProp(envKeys: List<String>, propKey: String): String? {
    for (envKey in envKeys) {
        System.getenv(envKey)?.takeIf { it.isNotBlank() }?.let { return it }
    }
    return keystoreProperties.getProperty(propKey)?.takeIf { it.isNotBlank() }
}

android {
    namespace = "dev.foss.goldenpath"
    compileSdk = 37

    defaultConfig {
        applicationId = "org.openshouter"
        minSdk = 26
        targetSdk = 35
        versionCode = 33
        versionName = "1.0.0" // x-release-please-version
        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
    }

    signingConfigs {
        create("release") {
            val storePath = signingProp(
                listOf("GOLDENPATH_UPLOAD_STORE_FILE", "RELEASE_STORE_FILE"),
                "storeFile",
            ) ?: return@create
            storeFile = rootProject.file(storePath)
            storePassword = signingProp(
                listOf("GOLDENPATH_UPLOAD_STORE_PASSWORD", "RELEASE_STORE_PASSWORD"),
                "storePassword",
            )
            keyAlias = signingProp(
                listOf("GOLDENPATH_UPLOAD_KEY_ALIAS", "RELEASE_KEY_ALIAS"),
                "keyAlias",
            )
            keyPassword = signingProp(
                listOf("GOLDENPATH_UPLOAD_KEY_PASSWORD", "RELEASE_KEY_PASSWORD"),
                "keyPassword",
            )
        }
    }

    buildTypes {
        release {
            isMinifyEnabled = true
            isShrinkResources = true
            proguardFiles(
                getDefaultProguardFile("proguard-android-optimize.txt"),
                "proguard-rules.pro",
            )
            val releaseSigning = signingConfigs.getByName("release")
            if (releaseSigning.storeFile?.isFile == true) {
                signingConfig = releaseSigning
            }
        }
    }

    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_17
        targetCompatibility = JavaVersion.VERSION_17
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
    val composeBom = platform(libs.compose.bom)
    implementation(composeBom)
    androidTestImplementation(composeBom)

    implementation(libs.core.ktx)
    implementation(libs.core.splashscreen)
    implementation(libs.activity.compose)
    implementation(libs.compose.material3)
    implementation(libs.compose.ui)
    implementation(libs.compose.ui.tooling.preview)
    implementation(libs.compose.icons)
    implementation(libs.datastore)
    implementation(libs.lifecycle.runtime.compose)
    implementation(libs.lifecycle.service)
    implementation(libs.coroutines.android)
    implementation(libs.navigation.compose)
    implementation(libs.hilt.android)
    implementation(libs.hilt.navigation.compose)
    ksp(libs.hilt.compiler)
    implementation(libs.room.runtime)
    implementation(libs.room.ktx)
    ksp(libs.room.compiler)

    testImplementation("androidx.test:core:1.7.0")
    testImplementation("junit:junit:4.13.2")
    testImplementation("org.robolectric:robolectric:4.16.1")
    testImplementation(libs.coroutines.test)
    testImplementation("com.google.dagger:hilt-android-testing:2.60.1")
    kspTest(libs.hilt.compiler)

    androidTestImplementation("androidx.test.ext:junit:1.3.0")
    androidTestImplementation("androidx.test:runner:1.7.0")
    androidTestImplementation("androidx.test:rules:1.7.0")
    androidTestImplementation("androidx.test.espresso:espresso-core:3.7.0")
    androidTestImplementation(libs.compose.ui.test.junit4)

    debugImplementation(libs.compose.ui.tooling)
    debugImplementation(libs.compose.ui.test.manifest)
    // FOSS ONLY: No proprietary Play Services or closed telemetry SDKs
}
