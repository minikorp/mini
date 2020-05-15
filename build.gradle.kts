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


subprojects {

    apply(plugin = "maven-publish")
    version = runCommand("$rootDir/scripts/latest-version.sh")
    group = "com.minikorp"

    afterEvaluate {
        val modules = arrayOf("mini-common", "mini-processor", "mini-android")
        if (this.name !in modules) return@afterEvaluate

        if (tasks.findByName("sourcesJar") == null) {
            tasks.register("sourcesJar", Jar::class) {
                archiveClassifier.set("sources")
                from(sourceSets["main"].allSource)
            }
        }

        this.publishing {
            repositories {
                maven {
                    name = "GitHubPackages"
                    url = uri("https://maven.pkg.github.com/minikorp/mini")
                    credentials {
                        username = "minikorp"
                        password = System.getenv("GITHUB_TOKEN")
                    }
                }
            }

            publications {
                if (publications.findByName("gpr") == null) {
                    register("gpr", MavenPublication::class) {
                        from(components["java"])
                        artifact(tasks.findByName("sourcesJar"))
                    }
                }
            }
        }
    }
}


allprojects {
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

