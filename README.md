# Fabrica

Factory automation mod for Hytale - from steam age to quantum tech.

> **Note**: Hytale is in Early Access. APIs are subject to breaking changes.

## Features

### Power System

Fabrica uses real-world units: **Watts** (W) for power and **Joules** (J) for energy.

| Machine | Power | Capacity/Cost |
|---------|-------|---------------|
| Generator | 400 W output | 1,000 J buffer |
| Battery | 640 W charge/discharge | 100,000 J (100 kJ) |
| Macerator | 160 W | 1,600 J per operation |
| Electric Furnace | 160 W | 1,280 J per operation |

- **Energy Networks**: Machines connected via cables form automatic power networks
- **Generators**: Burn solid fuel (coal, wood) to produce 400 W of power
- **Batteries**: Store up to 100 kJ for later use
- **Cables**: Transfer power between machines in a network

### Machines
- **Macerator**: Grind ores into dust for 2x output (10 sec @ 160 W)
- **Electric Furnace**: Smelt items using electricity (8 sec @ 160 W)

### Transport
- **Conveyor Belts**: Move items automatically between machines

### Materials
- **Ores**: Copper, Tin
- **Dusts**: Iron, Copper, Tin, Bronze
- **Ingots**: Copper, Tin, Bronze
- **Components**: Cables, Batteries

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

### Testing
1. Connect Hytale client to `127.0.0.1`
2. Use `/fabrica` to verify the plugin is loaded
3. Use `/fabrica give <item>` to spawn items
4. Use `/fabrica power` to debug power networks

## Project Structure

```
src/main/java/io/fabrica/
├── FabricaPlugin.java      # Plugin entry point
├── api/                    # Public API for mod integration
│   └── power/              # Power system interfaces
├── command/                # Chat commands
├── machine/                # Machine implementations
├── power/                  # Power network internals
├── transport/              # Conveyor system
└── ui/                     # Machine GUIs

src/main/resources/
├── manifest.json           # Plugin manifest
├── Common/                 # Textures and UI assets
│   ├── BlockTextures/
│   ├── Icons/
│   └── UI/
└── Server/                 # Game data
    ├── Item/Items/         # Item/block definitions
    ├── Item/Recipes/       # Crafting recipes
    └── Languages/          # Translations
```

## Commands

| Command | Description |
|---------|-------------|
| `/fabrica` | Show plugin info |
| `/fabrica power` | Debug power network stats |
| `/fabrica give <item>` | Give yourself a Fabrica item |

## License

GPL-3.0 - See [LICENSE](LICENSE) for details.
