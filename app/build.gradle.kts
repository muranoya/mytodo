import org.jetbrains.kotlin.gradle.dsl.JvmTarget

val versionFile = rootProject.file("VERSION")
check(versionFile.exists()) { "VERSION file not found at ${versionFile.path}" }
val appVersionName = versionFile.readText().trim()
val semver = Regex("""^(\d+)\.(\d+)\.(\d+)$""").matchEntire(appVersionName)
    ?: error("VERSION must match MAJOR.MINOR.PATCH (got '$appVersionName')")
val (semverMajor, semverMinor, semverPatch) = semver.destructured
listOf(semverMajor, semverMinor, semverPatch).forEach {
    require(it.toInt() in 0..99) {
        "Each semver component must be 0..99 (got '$appVersionName')"
    }
}
val appVersionCode =
    semverMajor.toInt() * 10_000 + semverMinor.toInt() * 100 + semverPatch.toInt()

plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.kotlin.compose)
    alias(libs.plugins.kotlin.serialization)
    alias(libs.plugins.ksp)
    alias(libs.plugins.hilt)
}

android {
    namespace = "net.meshpeak.mytodo"
    compileSdk = 36

    defaultConfig {
        applicationId = "net.meshpeak.mytodo"
        minSdk = 29
        targetSdk = 36
        versionCode = appVersionCode
        versionName = appVersionName

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
        vectorDrawables.useSupportLibrary = true
    }

    signingConfigs {
        val ksPath = System.getenv("RELEASE_KEYSTORE_PATH")
        val ksPassword = System.getenv("RELEASE_KEYSTORE_PASSWORD")
        val alias = System.getenv("RELEASE_KEY_ALIAS")
        val keyPwd = System.getenv("RELEASE_KEY_PASSWORD")
        if (!ksPath.isNullOrBlank() && file(ksPath).exists() &&
            !ksPassword.isNullOrBlank() && !alias.isNullOrBlank() && !keyPwd.isNullOrBlank()
        ) {
            create("release") {
                storeFile = file(ksPath)
                storePassword = ksPassword
                keyAlias = alias
                keyPassword = keyPwd
            }
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
            signingConfigs.findByName("release")?.let { signingConfig = it }
        }
        debug {
            isDebuggable = true
        }
    }

    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_17
        targetCompatibility = JavaVersion.VERSION_17
    }

    buildFeatures {
        compose = true
    }

    packaging {
        resources {
            excludes += "/META-INF/{AL2.0,LGPL2.1}"
        }
    }

    sourceSets.getByName("androidTest") {
        assets.srcDirs("$projectDir/schemas")
    }
}

kotlin {
    compilerOptions {
        jvmTarget.set(JvmTarget.JVM_17)
    }
}

ksp {
    arg("room.schemaLocation", "$projectDir/schemas")
}

dependencyLocking {
    lockAllConfigurations()
}

dependencies {
    implementation(libs.androidx.core.ktx)
    implementation(libs.androidx.activity.compose)
    implementation(libs.androidx.lifecycle.runtime.ktx)
    implementation(libs.androidx.lifecycle.viewmodel.compose)
    implementation(libs.androidx.lifecycle.runtime.compose)

    implementation(platform(libs.androidx.compose.bom))
    implementation(libs.bundles.compose.ui)
    debugImplementation(libs.androidx.compose.ui.tooling)
    debugImplementation(libs.androidx.compose.ui.test.manifest)

    implementation(libs.androidx.navigation.compose)
    implementation(libs.kotlinx.coroutines.android)

    implementation(libs.hilt.android)
    ksp(libs.hilt.compiler)
    implementation(libs.androidx.hilt.navigation.compose)
    implementation(libs.androidx.hilt.work)
    ksp(libs.androidx.hilt.compiler)

    implementation(libs.androidx.room.runtime)
    implementation(libs.androidx.room.ktx)
    ksp(libs.androidx.room.compiler)

    implementation(libs.androidx.work.runtime)
    implementation(libs.google.android.material)

    implementation(libs.sh.calvin.reorderable)

    testImplementation(libs.junit)

    androidTestImplementation(platform(libs.androidx.compose.bom))
    androidTestImplementation(libs.androidx.test.ext.junit)
    androidTestImplementation(libs.androidx.test.espresso.core)
    androidTestImplementation(libs.androidx.compose.ui.test.junit4)
    androidTestImplementation(libs.androidx.room.testing)
    androidTestImplementation(libs.kotlinx.coroutines.test)
}
