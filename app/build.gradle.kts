plugins { id("com.android.application"); id("org.jetbrains.kotlin.android") }

android {
    namespace = "org.notamusic.app"
    compileSdk = 35
    defaultConfig {
        applicationId = "org.notamusic.app"
        minSdk = 24
        targetSdk = 35
        versionCode = 2
        versionName = "0.2.0"
    }

    signingConfigs {
        create("release") {
            val keystoreFile = System.getenv("RELEASE_KEYSTORE_FILE")
            val storePassword = System.getenv("RELEASE_STORE_PASSWORD")
            val keyAlias = System.getenv("RELEASE_KEY_ALIAS")
            val keyPassword = System.getenv("RELEASE_KEY_PASSWORD")
            if (!keystoreFile.isNullOrBlank() && !storePassword.isNullOrBlank() && !keyAlias.isNullOrBlank() && !keyPassword.isNullOrBlank()) {
                storeFile = file(keystoreFile)
                this.storePassword = storePassword
                this.keyAlias = keyAlias
                this.keyPassword = keyPassword
            }
        }
    }

    buildTypes {
        release {
            isMinifyEnabled = false
            proguardFiles(getDefaultProguardFile("proguard-android-optimize.txt"), "proguard-rules.pro")
            val releaseKeystore = System.getenv("RELEASE_KEYSTORE_FILE")
            if (!releaseKeystore.isNullOrBlank()) {
                signingConfig = signingConfigs.getByName("release")
            }
        }
    }

    compileOptions { sourceCompatibility = JavaVersion.VERSION_17; targetCompatibility = JavaVersion.VERSION_17 }
    kotlinOptions { jvmTarget = "17" }
}

dependencies {
    implementation("androidx.core:core-ktx:1.15.0")
    implementation("androidx.appcompat:appcompat:1.7.0")
    implementation("androidx.activity:activity-ktx:1.10.1")
    implementation("androidx.lifecycle:lifecycle-runtime-ktx:2.8.7")
    implementation("com.google.android.material:material:1.12.0")
    testImplementation("junit:junit:4.13.2")
}
