plugins {
    id("com.android.application")
    kotlin("android")
    kotlin("android.extensions")
    kotlin("kapt")
}

kapt {
    javacOptions {
        option("-source", "8")
        option("-target", "8")
    }
}

//Android
android {
    compileSdkVersion = "android-28"
    buildToolsVersion = "28.0.3"

    defaultConfig {
        applicationId = "com.minikorp.mini"
        minSdkVersion(21)
        targetSdkVersion("android-28")
        versionCode = 1
        multiDexEnabled = true
        versionName = "1.0"
        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
    }

    buildTypes {
        getByName("release") {
            isMinifyEnabled = false
            proguardFiles(getDefaultProguardFile("proguard-android.txt"), "proguard-rules.pro")
        }
    }

    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_1_8
        targetCompatibility = JavaVersion.VERSION_1_8
    }

    lintOptions {
        isAbortOnError = false
    }

    packagingOptions {
        exclude("META-INF/rxjava.properties")
    }
}

dependencies {

    val kotlinx_coroutines_version = "0.13"
    val spek_version = "1.1.0"
    val dagger_version = "2.15"
    val rx_version = "2.0.3"
    val rx_android_version = "2.0.2"
    val leak_canary_version = "1.5"
    val butterkinfe_version = "8.4.0"
    val support_version = "27.0.0"

    implementation(project(":mini-android"))
    kapt(project(":mini-processor"))

    //Misc
    implementation("com.github.minikorp:grove:1.0.3")

    //Reactive
    val coroutines = "1.3.0-RC"
    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-core:$coroutines")
    val rx = "2.2.6"
    implementation("io.reactivex.rxjava2:rxjava:$rx")

    //Support
    implementation("androidx.core:core:1.1.0")
    implementation("androidx.constraintlayout:constraintlayout:1.1.3")

    testImplementation("junit:junit:4.12")
    testImplementation("org.amshove.kluent:kluent:1.44")
    androidTestImplementation("androidx.test:runner:1.1.0-alpha4")
    androidTestImplementation("androidx.test.espresso:espresso-core:3.1.0-alpha4")
}


