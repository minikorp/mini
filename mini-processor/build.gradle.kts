plugins {
    kotlin("jvm")
}

dependencies {
    implementation(project(":mini-common"))
    implementation("com.google.auto:auto-common:0.10")
    implementation("com.squareup:kotlinpoet:1.4.0")
    implementation("net.ltgt.gradle.incap:incap-processor:0.2")
    implementation("net.ltgt.gradle.incap:incap:0.2")
    testImplementation("junit:junit:4.12")
    testImplementation("com.google.testing.compile:compile-testing:0.15")
}