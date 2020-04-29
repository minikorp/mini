plugins {
    id("com.android.library")
    kotlin("android")
}

android {
    compileSdkVersion(29)
    buildToolsVersion("29.0.2")

    defaultConfig {
        minSdkVersion(14)
        targetSdkVersion(29)
        versionCode = 1
        versionName = "1.0"
        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
    }
    buildTypes {
        getByName("release") {
            isMinifyEnabled = false
            proguardFiles(getDefaultProguardFile("proguard-android-optimize.txt"), "proguard-rules.pro")
        }
    }
}

dependencies {
    api(project(":mini-common"))

    api("org.jetbrains.kotlinx:kotlinx-coroutines-android:1.3.3")
    api("androidx.appcompat:appcompat:1.1.0")
    api("androidx.lifecycle:lifecycle-runtime-ktx:2.2.0-rc03")

    testImplementation("junit:junit:4.12")
    androidTestImplementation("androidx.test:runner:1.2.0")
    androidTestImplementation("androidx.test.espresso:espresso-core:3.2.0")
}

val androidSourcesJar by tasks.registering(Jar::class) {
    @Suppress("UnstableApiUsage")
    archiveClassifier.set("sources")
    from(android.sourceSets["main"].java.sourceFiles)
}

afterEvaluate {
    publishing {
        publications {
            create<MavenPublication>("aar") {
                artifact(tasks.findByName("androidSourcesJar"))
                from(components["release"])
            }
        }
    }
}
