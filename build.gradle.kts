buildscript {
    repositories {
        mavenCentral()
        google()
        jcenter()
    }
    dependencies {
        classpath(kotlin("gradle-plugin", version = "1.3.72"))
        classpath("com.android.tools.build:gradle:3.6.3")
    }
}

plugins {
    kotlin("jvm") version "1.3.72"
    `maven-publish`
}

fun runCommand(command: String): String {
    val stream = Runtime.getRuntime().exec(command)
            .apply { waitFor() }.inputStream
    return stream.reader().readText().trim()
}

allprojects {

    version = runCommand("$rootDir/scripts/latest-version.sh")
    group = "com.minikorp"

    apply(plugin = "maven-publish")

    tasks.register("sourcesJar", Jar::class.java) {
        @Suppress("UnstableApiUsage")
        archiveClassifier.set("sources")
        try {
            from(sourceSets["main"].allSource)
        } catch (e: Throwable) {
            //Will throw for android libs
        }
    }

    repositories {
        jcenter()
        mavenCentral()
        maven { url = uri("https://jitpack.io") }
        google()
    }
}

repositories {
    jcenter()
    mavenCentral()
    //maven { url = URI.parse("https://jitpack.io") }
    google()
}

