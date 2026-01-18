# Hyflux
(Based on fabrica)

An API for developers to have one centralized way of transmitting liquids, power, items, and gas, ensuring machines from other mods work together


 
> **Note**: Hytale is in Early Access. APIs are subject to breaking changes.

## Features

### Transfer API and Types


We base our power on how [Fabrica](https://github.com/Nfemz/fabrica),
meaning we have real-world units: **Watts** (W) for power and **Joules** (J) for energy.
However, we also provide APIs for the four most used transfer types: liquids, power, items, and gas,
meaning you could easily build a mod that adds cables and machines and easily handle other machines without them needing to handle things.



## Installation

### Players
1. Download the latest release from [Releases](https://github.com/Nfemz/fabrica/releases)
2. Place `Fabrica-<version>.jar` in your Hytale `UserData/Mods/` folder
3. Launch Hytale

### Server Admins
1. Download the JAR from releases
2. Place in your server's mods directory
3. Restart the server

## Building from Source

**Requirements**: Java 25 SDK, Hytale installed

```bash
./gradlew build
```

Output: `build/libs/Fabrica-<version>.jar`

## Development

### Setup
1. Clone the repository
2. Set `hytale_home` in `gradle.properties` to your Hytale installation path
3. Open in IntelliJ IDEA (recommended) or VS Code

### Running the Dev Server

**IntelliJ**: Run the auto-generated `HytaleServer` configuration

**Command Line**:
```bash
cd run
java -cp "<hytale_home>/install/release/package/game/latest/Server/HytaleServer.jar" \
  com.hypixel.hytale.Main \
  --allow-op --disable-sentry \
  --assets="<hytale_home>/install/release/package/game/latest/Assets.zip" \
  --mods="../src/main/resources"
```

### First Run
Authenticate in the server console:
```
auth login device
auth persistence Encrypted
```

## License

GPL-3.0 - See [LICENSE](LICENSE) for details.
