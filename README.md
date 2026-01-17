# Fabrica

Factory automation mod for Hytale - from steam age to quantum tech.

## Building

```bash
./gradlew build          # Standard build
./gradlew shadowJar      # Create distributable JAR
```

Output: `build/libs/Fabrica-<version>.jar`

## Development

1. Run `HytaleServer` configuration in IntelliJ
2. First run: authenticate with `auth login device` then `auth persistence Encrypted`
3. Connect Hytale client to `127.0.0.1`
4. Test with `/fabrica` command

## Requirements

- Java 25 SDK
- Hytale launcher installed
