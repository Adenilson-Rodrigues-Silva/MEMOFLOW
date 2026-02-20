plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.kotlin.android)
    alias(libs.plugins.kotlin.compose)
}

android {
    namespace = "com.example.memoflow"
    compileSdk = 35 // SDK Estável para evitar erros de "Unresolved reference"

    defaultConfig {
        applicationId = "com.example.memoflow"
        minSdk = 26
        targetSdk = 35 // Alinhado com o compileSdk
        versionCode = 1
        versionName = "1.0"

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
        vectorDrawables {
            useSupportLibrary = true
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
        compose = true
    }
}

dependencies {
    // 1. O BOM deve ser sempre o primeiro para gerenciar as versões da UI
    implementation(platform(libs.androidx.compose.bom))

    // 2. UI e Material3 (Pegando versões do BOM automaticamente)
    implementation(libs.androidx.ui)
    implementation(libs.androidx.ui.graphics)
    implementation(libs.androidx.ui.tooling.preview)
    implementation(libs.androidx.material3)

    // 3. Core e Ciclo de Vida
    implementation(libs.androidx.core.ktx)
    implementation(libs.androidx.lifecycle.runtime.ktx)
    implementation(libs.androidx.activity.compose)
    implementation(libs.androidx.lifecycle.viewmodel.compose)

    // 4. Navegação (Apenas uma linha, sem duplicatas!)
    implementation(libs.androidx.navigation.compose)

    // 5. Imagem (Coil) e Animação (Lottie)
    implementation(libs.coil.compose)
    implementation(libs.lottie.compose)

    // 6. Outros
    // Se estas bibliotecas não estiverem no seu TOML, mantenha a string manual:
    // implementation(libs.androidx.remote.creation.core)
    // implementation(libs.places)

    // 7. Testes e Debug
    testImplementation(libs.junit)
    androidTestImplementation(libs.androidx.junit)
    androidTestImplementation(libs.androidx.espresso.core)
    androidTestImplementation(platform(libs.androidx.compose.bom))
    debugImplementation(libs.androidx.ui.tooling)
    debugImplementation(libs.androidx.ui.test.manifest)
}