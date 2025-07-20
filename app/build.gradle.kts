plugins {
    id("com.android.application")
}

android {
    namespace = "com.robmapps.keepingscore"
    compileSdk = 35

    defaultConfig {
        applicationId = "com.robmapps.keepingscore"
        minSdk = 27
        targetSdk = 33
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
        sourceCompatibility = JavaVersion.VERSION_21
        targetCompatibility = JavaVersion.VERSION_21
    }
}

dependencies {
    implementation("androidx.fragment:fragment:1.6.2")
    implementation("com.google.code.gson:gson:2.10.1")
    implementation("androidx.appcompat:appcompat:1.7.0")
    implementation("com.google.android.material:material:1.12.0")
    implementation("androidx.constraintlayout:constraintlayout:2.1.4")
    implementation("com.google.firebase:firebase-crashlytics-buildtools:3.0.3")
    implementation("androidx.navigation:navigation-fragment:2.9.0")
    implementation("androidx.navigation:navigation-ui:2.9.0")
    implementation("androidx.core:core-animation:1.0.0")
    implementation("androidx.media3:media3-common:1.2.1")
    testImplementation("junit:junit:4.13.2")
    androidTestImplementation("androidx.test.ext:junit:1.2.1")
    androidTestImplementation("androidx.test.espresso:espresso-core:3.6.1")
// Room dependencies for Java projects
    implementation("androidx.room:room-runtime:2.6.1")
    annotationProcessor("androidx.room:room-compiler:2.6.1") // Use annotationProcessor for Java
}

// Task to check for outdated dependencies
tasks.register("outdatedDependencies") {
    doLast {
        println("\nDependencies Status Report:")
        println("============================")
        println("Note: firebase-crashlytics-buildtools 3.0.5 is not available in repositories")
        println("\nCurrent versions:")
        println("- androidx.fragment:fragment:1.6.2")
        println("- com.google.code.gson:gson:2.10.1")
        println("- androidx.appcompat:appcompat:1.7.0")
        println("- com.google.android.material:material:1.12.0")
        println("- androidx.constraintlayout:constraintlayout:2.1.4")
        println("- com.google.firebase:firebase-crashlytics-buildtools:3.0.3")
        println("- androidx.navigation:navigation-fragment:2.9.0")
        println("- androidx.navigation:navigation-ui:2.9.0")
        println("- androidx.core:core-animation:1.0.0")
        println("- androidx.media3:media3-common:1.2.1")
        println("- androidx.room:room-runtime:2.6.1")
        println("- androidx.room:room-compiler:2.6.1")
    }
}