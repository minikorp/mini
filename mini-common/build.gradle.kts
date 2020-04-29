plugins {
    kotlin("jvm")
}

dependencies {
    val kotlinVersion = "1.3.72"
    implementation(kotlin("stdlib-jdk8"))
    api(kotlin("reflect"))
    compileOnly("com.google.android:android:4.1.1.4")

    val coroutines = "1.3.0"
    api("org.jetbrains.kotlinx:kotlinx-coroutines-core:$coroutines")


    testImplementation("junit:junit:4.12")
    testImplementation("org.amshove.kluent:kluent:1.44")
}

publishing {
    publications {
        this.register("mavenJava", MavenPublication::class) {
            from(components["java"])
            artifact(tasks.findByName("sourcesJar"))
        }
    }
}