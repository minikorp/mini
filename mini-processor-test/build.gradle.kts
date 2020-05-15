plugins {
    kotlin("jvm")
    kotlin("kapt")
}


repositories {
    mavenCentral()
}

dependencies {
    implementation(project(":mini-common"))
    kapt(project(":mini-processor"))

    testImplementation("junit:junit:4.12")
    testImplementation("org.amshove.kluent:kluent:1.44")
}

tasks {
    compileKotlin {
        kotlinOptions.jvmTarget = "1.8"
    }
    compileTestKotlin {
        kotlinOptions.jvmTarget = "1.8"
    }
}