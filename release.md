# Zappy-Java

Lightweight library for compressing and encoding web-related text
(json, URLs, UUIDs, etc.) into a URL-safe format for
efficient transport.
[Read the javadoc!](https://glitchybyte.github.io/zappy-java/)

[Read the spec here!](https://github.com/GlitchyByte/zappy/blob/main/SPEC.md)

To use in your own projects, make sure you have the appropriate credentials in your `gradle.properties`, and add the repository and dependency like this (Gradle Kotlin):

```kotlin
repositories {
    maven {
        // GitHub repository.
        name = "GitHubPackages"
        url = uri("https://maven.pkg.github.com/GlitchyByte/*")
        credentials {
            username = project.findProperty("gpr.username") as String?
            password = project.findProperty("gpr.token") as String?
        }
        metadataSources {
            gradleMetadata()
        }
    }
}

dependencies {
    implementation("com.glitchybyte.zappy:zappy-java:1.0.0")
}
```

Notable changes:

* Translated from original ts implementation, including tests.
