plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.ksp)
    alias(libs.plugins.google.services)
}

android {
    namespace = "com.project.topakuli"
    compileSdk = 36

    configurations.all {
        resolutionStrategy {
            force("io.grpc:grpc-android:1.62.2")
            force("io.grpc:grpc-okhttp:1.62.2")
            force("io.grpc:grpc-stub:1.62.2")
            force("io.grpc:grpc-api:1.62.2")
            force("io.grpc:grpc-core:1.62.2")
            force("io.grpc:grpc-protobuf-lite:1.62.2")
        }
    }

    defaultConfig {
        applicationId = "com.project.topakuli"
        minSdk = 26
        targetSdk = 35
        versionCode = 1
        versionName = "1.0"

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
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
        sourceCompatibility = JavaVersion.VERSION_11
        targetCompatibility = JavaVersion.VERSION_11
    }

    buildFeatures {
        viewBinding = true
    }

    packaging {
        resources {
            excludes += "**/MANIFEST.MF"
            excludes += "META-INF/INDEX.LIST"
            excludes += "META-INF/DEPENDENCIES"
        }
    }
}

ksp {
    arg("room.generateKotlin", "true")
}

    dependencies {
    implementation(libs.androidx.activity.ktx)
    implementation(libs.androidx.appcompat)
    implementation(libs.androidx.constraintlayout)
    implementation(libs.androidx.core.ktx)
    implementation(libs.androidx.fragment)
    implementation(libs.androidx.navigation.fragment.ktx)
    implementation(libs.androidx.navigation.ui.ktx)
    implementation(libs.material)

    implementation(libs.androidx.camera.core)
    implementation(libs.androidx.camera.camera2)
    implementation(libs.androidx.camera.lifecycle)
    implementation(libs.androidx.camera.view)

    // Firebase
    implementation(platform(libs.firebase.bom))
    implementation(libs.firebase.auth)
    implementation(libs.firebase.firestore)
    implementation(libs.firebase.storage)
    implementation(libs.firebase.analytics)
    implementation(libs.firebase.messaging)
    implementation(libs.kotlinx.coroutines.play.services)

    // Force gRPC dependencies for Firestore compatibility
    implementation(libs.grpc.android)
    implementation(libs.grpc.okhttp)
    implementation(libs.grpc.stub)

    // Supabase Storage
    implementation(platform(libs.supabase.bom))
    implementation(libs.supabase.storage)

    implementation(libs.supabase.postgrest)
    implementation(libs.supabase.auth)
    implementation(libs.supabase.realtime)

    // Ktor (Required by Supabase)
    implementation(libs.ktor.client.android)
    implementation(libs.ktor.client.core)
    implementation(libs.ktor.client.content.negotiation)
    implementation(libs.ktor.serialization.kotlinx.json)

    // Room
    implementation(libs.androidx.room.runtime)
    implementation(libs.androidx.room.ktx)
    implementation(libs.androidx.room.paging)
    ksp(libs.androidx.room.compiler)

    implementation(libs.androidx.paging.runtime)

    implementation(libs.glide)
    ksp(libs.glide.compiler)
    implementation(libs.flexbox)
    implementation(libs.shimmer)

    // Retrofit
    implementation(libs.retrofit)
    implementation(libs.retrofit.converter.gson)
    implementation(libs.okhttp.logging)

    // Google Auth for FCM v1
    implementation(libs.google.auth.library.oauth2.http) {
        exclude(group = "io.grpc", module = "grpc-okhttp")
        exclude(group = "io.opencensus", module = "opencensus-api")
    }
    implementation(libs.google.http.client.gson)
    implementation(libs.google.api.client.gson)

    testImplementation(libs.junit)
    androidTestImplementation(libs.androidx.espresso.core)
    androidTestImplementation(libs.androidx.junit)
}