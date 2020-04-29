plugins {
    kotlin("jvm")
}

dependencies {
    implementation(project(":mini-common"))

    val rx = "2.2.6"
    api("io.reactivex.rxjava2:rxjava:$rx")

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