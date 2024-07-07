// Copyright 2024 GlitchyByte
// SPDX-License-Identifier: MIT-0

plugins {
    id("glitchybyte.java-library-published-conventions")
}

publishing {
    repositories {
        maven {
            // GitHub repository.
            name = "GitHubPackages"
            url = uri("https://maven.pkg.github.com/GlitchyByte/zappy-java")
            credentials {
                username = project.findProperty("gpr.username") as String?
                password = project.findProperty("gpr.token") as String?
            }
            metadataSources {
                gradleMetadata()
            }
        }
    }
    publications {
        create<MavenPublication>("Zappy") {
            from(components["java"])
            artifactId = "zappy"
            pom {
                name = "Zappy"
                description = "Lightweight library for compressing and encoding web-related text (json, URLs, UUIDs, etc.) into a URL-safe format for efficient transport."
                url = "https://github.com/glitchybyte/zappy-java"
                licenses {
                    license {
                        name = "Apache License, Version 2.0"
                        url = "https://opensource.org/licenses/Apache-2.0"
                    }
                }
            }
        }
    }
}

signing {
    sign(publishing.publications["Zappy"])
}

// Setup build info.
group = "com.glitchybyte"
version = File("../version").readLines().first().trim()
