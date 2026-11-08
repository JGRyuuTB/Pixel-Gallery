plugins {
    id("com.android.application")
    id("org.jetbrains.kotlin.android")
    id("org.jetbrains.kotlin.plugin.compose")
}

android {
    namespace = "com.GalleryYou.Pixel"
    compileSdk = 35

    defaultConfig {
        applicationId = "com.GalleryYou.Pixel"
        minSdk = 26
        targetSdk = 35
        versionCode = 12
        versionName = "1.3.0"
        buildConfigField("boolean", "NIGHTLY", "false")
        // Nombre visible de la app; la variante nightly lo cambia para poder distinguirla.
        manifestPlaceholders["appLabel"] = "@string/app_name"
    }

    // Clave fija incluida en el repo: así cada versión nueva se instala encima de la anterior
    // sin tener que desinstalar (los ajustes se conservan).
    signingConfigs {
        create("galeria") {
            storeFile = file("galeria.keystore")
            storePassword = "galeria123"
            keyAlias = "galeria"
            keyPassword = "galeria123"
        }
    }

    buildTypes {
        release {
            isMinifyEnabled = false
            signingConfig = signingConfigs.getByName("galeria")
        }
        // Variante Nightly: se instala junto a la estable (otro applicationId), con otro nombre,
        // y se compila cada noche desde la rama "nightly".
        create("nightly") {
            initWith(getByName("release"))
            applicationIdSuffix = ".nightly"
            versionNameSuffix = "-nightly.${project.findProperty("nightlyBuild") ?: "0"}"
            signingConfig = signingConfigs.getByName("galeria")
            matchingFallbacks += listOf("release")
            buildConfigField("boolean", "NIGHTLY", "true")
            manifestPlaceholders["appLabel"] = "Pixel Gallery Nightly"
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
    lint {
        abortOnError = false
        checkReleaseBuilds = false
    }
}

// Cada compilación nightly sube el versionCode (1_000_000 + número de ejecución del workflow),
// así se instala encima de la anterior sin desinstalar.
androidComponents {
    onVariants(selector().withBuildType("nightly")) { variant ->
        val build = (project.findProperty("nightlyBuild") as String?)?.toIntOrNull() ?: 0
        variant.outputs.forEach { it.versionCode.set(1_000_000 + build) }
    }
}

dependencies {
    val composeBom = platform("androidx.compose:compose-bom:2024.10.01")
    implementation(composeBom)
    implementation("androidx.core:core-ktx:1.13.1")
    implementation("androidx.activity:activity-compose:1.9.3")
    implementation("androidx.compose.ui:ui")
    implementation("androidx.compose.foundation:foundation")
    implementation("androidx.compose.animation:animation")
    implementation("androidx.compose.material3:material3")

    // Reproductor de video
    implementation("androidx.media3:media3-exoplayer:1.4.1")
    implementation("androidx.media3:media3-ui:1.4.1")

    // Miniaturas de fotos y videos
    implementation("io.coil-kt:coil-compose:2.7.0")
    implementation("io.coil-kt:coil-video:2.7.0")
}
