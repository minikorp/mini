plugins {
    kotlin("jvm")
    `maven-publish`
}

repositories {
    maven {
        setUrl("https://jitpack.io")
    }
}

dependencies {
    implementation(project(":mini-common"))
    implementation(kotlin("stdlib-jdk8"))
    implementation("com.google.devtools.ksp:symbol-processing-api:1.4.30-1.0.0-alpha04")
    val kotlinPoetVersion = "1.7.2"
    implementation("com.squareup:kotlinpoet:$kotlinPoetVersion")
    testImplementation("junit:junit:4.12")
    testImplementation("com.google.testing.compile:compile-testing:0.15")
}