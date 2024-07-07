# Zappy-Java

![Version](https://img.shields.io/badge/Version-1.0.0-blue)
![Spec](https://img.shields.io/badge/Spec-1.1.0-cyan)
![Java](https://img.shields.io/badge/Java-21-orange)

Lightweight library for compressing and encoding web-related text
(json, URLs, UUIDs, etc.) into a URL-safe format for
efficient transport.
[Read the javadoc!](https://glitchybyte.github.io/zappy-java/)

[Read the spec here!](https://github.com/GlitchyByte/zappy/blob/main/SPEC.md)

#### Goals

* Capable of encoding any valid utf-8 string.
* Transportable as URL-safe plain text.
* Produce smaller encoded string than vanilla base64 on ASCII payloads.
* Fast encoding and decoding.

#### Non-Goals

* Encryption. This ain't it. It's obfuscation at best.

#### Notes

To fully take advantage of Zappy, you as a dev should provide
`contraction tables` specialized to your payloads.

Zappy strings should not be stored. They are designed for transport
where one side encodes before transmitting and the other side decodes
after receiving. If the `contraction tables` change between encoding
and decoding, it's very possible the output will not be the same or
even invalid. Keep this in mind when decoding and handle these cases
accordingly. That is, always sanitize your (decoded) output and handle
decoding error conditions.

# API

```java
// Constructor.
Zappy(final Map<Integer, String[]> source>)

// Base64 string encode/decode.
String base64StringEncode(final String str)
String base64StringDecode(final String str) throws ZappyParseException

// Zappy encode/decode.
String encode(final String str)
String decode(final String str) throws ZappyParseException
```

# How to use

### Add to your project

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

### Define contraction tables

There are 17 `contraction tables` available for use.

Table 0 is called the fast lookup table, it gets the best compression
gains, but only 16 entries are permitted. Entries in table 0 can have
a minimum of 2 characters (or 2 bytes when converted to UTF-8) and
still have a gain.

Tables 1-17 allow 256 entries each. Entries can have a minimum of 3
characters (or 3 bytes when converted to UTF-8).

Developer defined `contraction tables` are overlaid onto
[default tables](https://github.com/GlitchyByte/zappy-java/blob/main/code/lib/src/main/java/com/glitchybyte/zappy/ZappyDefaultContractions.java). There is a default table 0 specialized in json, and a default
table 16 with common strings.

Define your `contraction tables` like so:

```java
final Map<Integer, String[]> contractions = Map.of(
    1, new String[] {
        "glitchybyte",
        "defenestration",
        "internationalization"
    }
);
```

### Encode and decode

```java
final Zappy zappy = new Zappy(contractions);
final String json = "{" +
  "\"codeUrl\":\"https://github.com/glitchybyte/zappy\"," +
  "\"msg\":\"When I deal with internationalization I think of defenestration.\"" +
  "}";
final String encoded = zappy.encode(json);
System.out.printf(Locale.US, "[%d] %s%n", encoded.length(), encoded);
//  [90] 6mNvZGVVcmzm4GdpdGh1Yv8EL_ACL3phcHB5521zZ-ZXaGVuIEkgZGVhbCB3
//       aXRoIPAAIEkgdGhpbmsgb2Yg8AEu6w

// While vanilla base64 is:
final String base64Encoded = zappy.base64StringEncode(json);
System.out.printf(Locale.US, "[%d] %s%n", base64Encoded.length(), base64Encoded);
// [164] eyJjb2RlVXJsIjoiaHR0cHM6Ly9naXRodWIuY29tL2dsaXRjaHlieXRlL3ph
//       cHB5IiwibXNnIjoiV2hlbiBJIGRlYWwgd2l0aCBpbnRlcm5hdGlvbmFsaXph
//       dGlvbiBJIHRoaW5rIG9mIGRlZmVuZXN0cmF0aW9uLiJ9

final String decoded = zappy.decode(encoded);
System.out.printf(Locale.US, "[%d] %s%n", decoded.length(), decoded);
// [123] {"codeUrl":"https://github.com/glitchybyte/zappy","msg":"Whe
//       n I deal with internationalization I think of defenestration
//       ."}
```
