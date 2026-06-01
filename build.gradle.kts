plugins {
    kotlin("multiplatform") version "2.3.20"
    `maven-publish`
}

group = "io.github.mkfl3x"

repositories {
    mavenCentral()
}

kotlin {
    jvmToolchain(21)

    jvm()
    iosArm64()
    iosSimulatorArm64()

    sourceSets {
        commonMain.dependencies {
            api("org.jetbrains.kotlinx:kotlinx-coroutines-core:1.9.0")
        }
        commonTest.dependencies {
            implementation(kotlin("test"))
        }
    }
}

publishing {
    repositories {
        maven {
            name = "GitHubPackages"
            url = uri("https://maven.pkg.github.com/mkfl3x/obd-kit")
            credentials {
                username = providers.environmentVariable("GITHUB_ACTOR").orNull
                password = providers.environmentVariable("GITHUB_TOKEN").orNull
            }
        }
    }
    publications.withType<MavenPublication> {
        pom {
            name.set("obd-kit")
            description.set("Kotlin Multiplatform library for OBD-II / UDS diagnostics via ELM327 adapters")
            url.set("https://github.com/mkfl3x/obd-kit")
            licenses {
                license {
                    name.set("MIT License")
                    url.set("https://opensource.org/licenses/MIT")
                }
            }
        }
    }
}