plugins {
    kotlin("jvm")
}

dependencies {
    implementation(project(":mini-common"))

    val coroutines = "1.3.3"
    api("org.jetbrains.kotlinx:kotlinx-coroutines-core:$coroutines")

    testImplementation("junit:junit:4.12")
    testImplementation("org.amshove.kluent:kluent:1.44")
}

tasks.withType(org.jetbrains.kotlin.gradle.tasks.KotlinCompile::class.java) {
    kotlinOptions.freeCompilerArgs += listOf(
            "-Xuse-experimental=kotlin.Experimental",
            "-Xuse-experimental=kotlinx.coroutines.ExperimentalCoroutinesApi",
            "-Xuse-experimental=kotlinx.coroutines.FlowPreview"
    )
}

publishing {
    publications {
        this.register("mavenJava", MavenPublication::class) {
            from(components["java"])
            artifact(tasks.findByName("sourcesJar"))
        }
    }
}