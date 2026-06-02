# obd-kit

![Build](https://github.com/mkfl3x/obd-kit/actions/workflows/publish.yml/badge.svg)
![Version](https://img.shields.io/badge/version-0.0.6-blue)
![License](https://img.shields.io/badge/license-MIT-green)

Kotlin JVM library for OBD-II / UDS diagnostics via ELM327 adapters.

## Installation

```kotlin
repositories {
    maven {
        url = uri("https://maven.pkg.github.com/mkfl3x/obd-kit")
        credentials {
            username = System.getenv("GITHUB_ACTOR")
            password = System.getenv("GITHUB_TOKEN")
        }
    }
}

dependencies {
    implementation("io.github.mkfl3x:obd-kit:<version>")
}
```

## Usage

```kotlin
val adapter = OBDAdapter(connector)
adapter.connect("AA:BB:CC:DD:EE:FF")

val result = adapter.executeCommand(myCommand) // CommandResult
```