# CLAUDE.md

This file provides guidance to Claude Code (claude.ai/code) when working with code in this repository.

## Project Overview

**Fabrica** is a factory automation mod for Hytale - from steam age to quantum tech. It uses Gradle with automatic Hytale server JAR detection and IDE run configuration generation.

**Early Access Warning**: Hytale is in Early Access. APIs are subject to breaking changes.

---

## Quick Start: Development → Test → Play Loop

This is the primary workflow for making changes and testing them in-game.

### Prerequisites

1. **Java 25 SDK** installed and configured
2. **Hytale launcher** installed with the game downloaded
3. `hytale_home` path set correctly in `gradle.properties` (currently: `/mnt/c/Users/femia/AppData/Roaming/Hytale`)

### Step 1: Make Code Changes

Edit files in `src/main/java/io/fabrica/`:
- `FabricaPlugin.java` - Plugin entry point, lifecycle hooks
- `FabricaCommand.java` - The `/fabrica` command implementation

### Step 2: Build the Plugin

```bash
./gradlew build
```

This compiles the plugin and outputs to `build/libs/Fabrica-<version>.jar`.

The build automatically:
- Syncs `manifest.json` with version from `gradle.properties`
- Compiles against the local Hytale server JAR

### Step 3: Run the Development Server

**Option A: IntelliJ IDEA (Recommended)**
1. Open project in IntelliJ
2. Gradle auto-generates `HytaleServer` run configuration
3. Click Run (green play button) on the `HytaleServer` configuration

**Option B: VS Code**
```bash
./gradlew generateVSCodeLaunch
```
Then use the generated `.vscode/launch.json` to run.

**Option C: Command Line**
```bash
cd run
java -cp "/mnt/c/Users/femia/AppData/Roaming/Hytale/install/release/package/game/latest/Server/HytaleServer.jar" \
  com.hypixel.hytale.Main \
  --allow-op --disable-sentry \
  --assets="/mnt/c/Users/femia/AppData/Roaming/Hytale/install/release/package/game/latest/Assets.zip" \
  --mods="../src/main/resources"
```

### Step 4: First-Time Server Authentication

On first run, you must authenticate in the server console:
```
auth login device
```
Follow the device code flow, then persist credentials:
```
auth persistence Encrypted
```

### Step 5: Connect and Test In-Game

1. Launch the Hytale client
2. Connect to server address: `127.0.0.1`
3. Test the plugin with: `/fabrica`

### Step 6: Iterate

1. Stop the server (Ctrl+C or stop in IDE)
2. Make code changes
3. Run `./gradlew build`
4. Restart the server
5. Reconnect and test

---

## LLM Development Guidelines

When working on this codebase as an AI assistant:

### Before Making Changes
1. Read the relevant source files first
2. Check `manifest.json` Main class matches actual plugin class (`io.fabrica.FabricaPlugin`)
3. Understand the plugin lifecycle: constructor → preLoad → setup → start → shutdown

### After Making Changes
1. Run `./gradlew build` to verify compilation
2. Check for errors in build output
3. If user wants to test: remind them to restart the server and reconnect

### Common Tasks

| Task | Command/Action |
|------|----------------|
| Add a new command | Create class extending `CommandBase`, register in `FabricaPlugin.setup()` |
| Add an event listener | Use `getEventRegistry().register()` in `setup()` |
| Add a block/item | Create JSON in `src/main/resources/Server/Item/Items/` |
| Run tests | `./gradlew test` |
| Full rebuild | `./gradlew clean build` |
| Check for compile errors | `./gradlew compileJava` |

### File Locations

| Purpose | Path |
|---------|------|
| Plugin code | `src/main/java/io/fabrica/` |
| Plugin manifest | `src/main/resources/manifest.json` |
| Asset definitions | `src/main/resources/Server/` |
| Build output | `build/libs/Fabrica-<version>.jar` |
| Server working dir | `run/` |
| Version config | `gradle.properties` |

---

## Deployment

### Local Testing (Single Player)

The development server described above is the primary testing method.

### Distributing to Others

1. Build the JAR:
   ```bash
   ./gradlew build
   ```
2. Output: `build/libs/Fabrica-<version>.jar`
3. Recipients install by placing the JAR in their `UserData/Mods/` folder within their Hytale installation

### Multiplayer Server Deployment

1. Build with `./gradlew build`
2. Copy `build/libs/Fabrica-<version>.jar` to the server's plugins directory
3. Restart the server

> **Note**: If you add external dependencies later, you'll need to configure the Shadow plugin for fat JARs. Currently all dependencies come from the Hytale server itself.

---

## Build Commands

```bash
./gradlew build                 # Standard build (compiles + packages JAR)
./gradlew compileJava           # Compile only (fast check for errors)
./gradlew clean build           # Full clean rebuild
./gradlew test                  # Run tests
./gradlew updatePluginManifest  # Sync manifest.json with gradle.properties
./gradlew generateVSCodeLaunch  # Generate .vscode/launch.json
```

Output JAR: `build/libs/<project-name>-<version>.jar`

## Configuration Files

- **gradle.properties**: Version, maven group, Java version, patchline (release/pre-release), hytale_home path
- **settings.gradle**: Project name (rootProject.name)
- **src/main/resources/manifest.json**: Plugin metadata (Name, Version, Main class, etc.)

**Critical**: The `Main` property in manifest.json must exactly match the plugin entry-point class (e.g., `org.example.plugin.ExamplePlugin`).

## Development Workflow

1. Run `HytaleServer` configuration in IntelliJ (auto-generated by Gradle)
2. First run: authenticate with `auth login device` then `auth persistence Encrypted` in server console
3. Connect Hytale client to `127.0.0.1`
4. Test with `/test` command

## Key Dependencies

- Java 25 SDK required
- Hytale server JAR auto-resolved from local Hytale installation
- Hytale home auto-detected or set via `hytale_home` property in gradle.properties

---

## Hytale Plugin API Reference

### Plugin Lifecycle

Plugins extend `JavaPlugin` (`com.hypixel.hytale.server.core.plugin.JavaPlugin`):

```java
public class MyPlugin extends JavaPlugin {
    private static final HytaleLogger LOGGER = HytaleLogger.forEnclosingClass();

    public MyPlugin(@Nonnull JavaPluginInit init) {
        super(init);
    }

    @Override
    protected void preLoad() {
        // Async config loading before initialization
    }

    @Override
    protected void setup() {
        // Register commands, events, assets
    }

    @Override
    protected void start() {
        // Post-setup logic, all plugins initialized
    }

    @Override
    protected void shutdown() {
        // Cleanup before registry finalization
    }
}
```

### Available Registries

| Registry | Access Method | Purpose |
|----------|---------------|---------|
| CommandRegistry | `getCommandRegistry()` | Player/console commands |
| EventRegistry | `getEventRegistry()` | Game event listeners |
| TaskRegistry | `getTaskRegistry()` | Async/scheduled tasks |
| EntityRegistry | `getEntityRegistry()` | Custom entity types |
| BlockStateRegistry | `getBlockStateRegistry()` | Custom block states |
| AssetRegistry | `getAssetRegistry()` | Textures, models, sounds |
| EntityStoreRegistry | `getEntityStoreRegistry()` | Entity ECS components |
| ChunkStoreRegistry | `getChunkStoreRegistry()` | Chunk ECS components |
| ClientFeatureRegistry | `getClientFeatureRegistry()` | Client behavior features |

### Utility Methods

- `getLogger()` → `HytaleLogger` for logging
- `getDataDirectory()` → `Path` for plugin config/data folder
- `getManifest()` → `PluginManifest` for metadata access
- `withConfig()` → Configuration file registration

---

## Commands

### Basic Command (CommandBase)

```java
public class MyCommand extends CommandBase {
    public MyCommand() {
        super("commandname", "Description");
        this.setPermissionGroup(GameMode.Adventure); // Allows all players
    }

    @Override
    protected void executeSync(@Nonnull CommandContext ctx) {
        ctx.sendMessage(Message.raw("Response text"));
    }
}
```

### Player Command with ECS Access (AbstractPlayerCommand)

Use when accessing player components (thread-safe ECS access):

```java
public class WhereAmICommand extends AbstractPlayerCommand {
    public WhereAmICommand() {
        super("whereami", "Shows your location");
        this.setPermissionGroup(GameMode.Adventure);
    }

    @Override
    protected void execute(@Nonnull CommandContext ctx,
            @Nonnull Store<EntityStore> store, @Nonnull Ref<EntityStore> ref,
            @Nonnull PlayerRef playerRef, @Nonnull World world) {
        Player player = store.getComponent(ref, Player.getComponentType());
        TransformComponent transform = store.getComponent(ref, TransformComponent.getComponentType());
        player.sendMessage(Message.raw("Position: " + transform.getPosition()));
    }
}
```

### Command Arguments

```java
// In constructor:
RequiredArg<String> messageArg = this.withRequiredArg("message", "The message", ArgTypes.STRING);
RequiredArg<Integer> countArg = this.withRequiredArg("count", "Number", ArgTypes.INTEGER);

// In execute:
String message = messageArg.get(ctx);
int count = countArg.get(ctx);
```

**Argument Types**: `ArgTypes.STRING`, `INTEGER`, `BOOLEAN`, `FLOAT`, `DOUBLE`, `UUID`

### Registration

```java
@Override
protected void setup() {
    this.getCommandRegistry().registerCommand(new MyCommand());
}
```

---

## Events

### Standard Event Registration

```java
// Basic registration
getEventRegistry().register(PlayerJoinEvent.class, this::onPlayerJoin);

// With priority (FIRST, EARLY, NORMAL, LATE, LAST)
getEventRegistry().register(EventPriority.EARLY, SomeEvent.class, this::handleEarly);

// Global registration (all instances)
getEventRegistry().registerGlobal(PlayerReadyEvent.class, this::onPlayerReady);

// Keyed events (specific context)
getEventRegistry().register(WorldEvent.class, "world_name", this::onWorldEvent);
```

### Event Handler Example

```java
public void onPlayerReady(PlayerReadyEvent event) {
    Player player = event.getPlayer();
    player.sendMessage(Message.raw("Welcome " + player.getDisplayName()));
}
```

### Common Events

- **Server**: `BootEvent`, `ShutdownEvent`, `PluginSetupEvent`
- **World**: `AddWorldEvent`, `RemoveWorldEvent`, `AllWorldsLoadedEvent`
- **Player**: `PlayerConnectEvent`, `PlayerDisconnectEvent`, `PlayerChatEvent`, `PlayerReadyEvent`
- **Block**: `PlaceBlockEvent`, `BreakBlockEvent`, `DamageBlockEvent` (all implement `ICancellable`)
- **Entity**: `EntityRemoveEvent`, `EntitySpawnEvent`

### Cancellable Events

```java
public void onBlockBreak(BreakBlockEvent event) {
    if (someCondition) {
        event.setCancelled(true);
    }
}
```

### ECS Entity Events (EntityEventSystem)

For events in the Entity Component System:

```java
class CancelCraftSystem extends EntityEventSystem<EntityStore, CraftRecipeEvent.Pre> {
    public CancelCraftSystem() {
        super(CraftRecipeEvent.Pre.class);
    }

    @Override
    public void handle(int index, @Nonnull ArchetypeChunk<EntityStore> chunk,
            @Nonnull Store<EntityStore> store, @Nonnull CommandBuffer<EntityStore> buffer,
            @Nonnull CraftRecipeEvent.Pre event) {
        if (shouldCancel(event.getCraftedRecipe())) {
            event.setCancelled(true);
        }
    }

    @Override
    public Query<EntityStore> getQuery() {
        return Archetype.empty();
    }
}

// Registration in setup():
this.getEntityStoreRegistry().registerSystem(new CancelCraftSystem());
```

---

## Task Scheduling & Async

### TaskRegistry

```java
TaskRegistry tasks = getTaskRegistry();

// Register CompletableFuture (auto-cancelled on plugin disable)
CompletableFuture<Void> future = CompletableFuture.runAsync(() -> {
    // Background work
});
tasks.registerTask(future);
```

### Scheduled Tasks

```java
// Using HytaleServer.SCHEDULED_EXECUTOR (single-threaded)
ScheduledFuture<?> scheduled = HytaleServer.SCHEDULED_EXECUTOR.scheduleAtFixedRate(
    () -> saveData(),
    5, 5, TimeUnit.MINUTES
);
getTaskRegistry().registerTask((ScheduledFuture<Void>) scheduled);
```

### World Thread Execution

World state must only be modified on the world thread:

```java
world.execute(() -> {
    // Safe to modify blocks, entities here
});

// Async load then apply on world thread
CompletableFuture.supplyAsync(() -> loadData())
    .thenAcceptAsync(data -> applyData(data), world);
```

**Never** block the world thread with `.join()` or `Thread.sleep()`.

---

## Entity Spawning

Requires understanding of the Entity Component System (ECS).

```java
world.execute(() -> {
    Store<EntityStore> store = world.getEntityStore().getStore();
    Holder<EntityStore> holder = EntityStore.REGISTRY.newHolder();

    // Model
    ModelAsset modelAsset = ModelAsset.getAssetMap().getAsset("Minecart");
    Model model = Model.createScaledModel(modelAsset, 1.0f);

    // Required components
    holder.ensureComponent(UUIDComponent.getComponentType());
    // Add TransformComponent, ModelComponent, BoundingBox, NetworkId, etc.

    store.addEntity(holder, AddReason.SPAWN);
});
```

---

## Permissions

### Permission Checking

```java
PermissionsModule permissions = PermissionsModule.get();
boolean canTeleport = permissions.hasPermission(playerUuid, "hytale.command.teleport");
```

### Built-in Permission Groups

- **OP**: Has `*` wildcard (all permissions)
- **Default**: Assigned to all players

### GameMode-Based Permissions

```java
this.setPermissionGroup(GameMode.Adventure); // All players
this.setPermissionGroup(GameMode.Creative);  // Creative mode only
this.requirePermission(HytalePermissions.fromCommand("mycommand.use"));
```

---

## Asset Bundling (Packs)

### Directory Structure

```
src/main/resources/
├── Common/
│   ├── BlockTextures/     # 16x16 or 32x32 PNG textures
│   ├── Icons/ItemsGenerated/
│   └── Models/
└── Server/
    ├── Block/
    ├── Item/
    │   ├── Items/         # Item JSON definitions
    │   ├── Category/
    │   └── Recipes/       # Crafting recipes
    └── Languages/
        └── en-US/
            └── server.lang
```

Set `includes_pack=true` in gradle.properties to enable.

### Block Definition Example

`Server/Item/Items/Example_Block.json`:
```json
{
  "TranslationProperties": { "Name": "server.Example_Block.name" },
  "MaxStack": 100,
  "Icon": "Icons/ItemsGenerated/Example_Block.png",
  "Categories": ["Blocks.Rocks"],
  "BlockType": {
    "Material": "Solid",
    "DrawType": "Cube",
    "Group": "Stone",
    "Textures": [{"All": "BlockTextures/Example_Block.png"}],
    "BlockSoundSetId": "Stone",
    "ParticleColor": "#aeae8c"
  }
}
```

### Per-Side Textures

```json
"Textures": [{
  "Top": "BlockTextures/Block_Top.png",
  "Bottom": "BlockTextures/Block_Bottom.png",
  "Sides": "BlockTextures/Block_Sides.png"
}]
```

### Translation File

`Server/Languages/en-US/server.lang`:
```
Example_Block.name = Example Block
```

---

## Custom UI (.ui Files)

Hytale uses `.ui` files for custom user interfaces. These files use a **CSS/HTML-like syntax, NOT JSON**.

### File Location

All `.ui` files must be in `src/main/resources/Common/UI/Custom/`. The manifest must have `"IncludesAssetPack": true`.

### Correct .ui Syntax

```
// Comments use double slashes
@TextureVar = PatchStyle(TexturePath: "Common/UI/Custom/MyTexture.png");

Group {
    LayoutMode: Center;

    Group #panelId {
        Background: @TextureVar;
        Anchor: (Width: 176, Height: 166);
        LayoutMode: Top;

        Label #labelId {
            Style: (FontSize: 14, Alignment: Left);
            Text: "Display Text";
            Anchor: (Top: 6, Left: 8);
        }

        Group #slotArea {
            Anchor: (Top: 35, Left: 56, Width: 18, Height: 18);
            Background: @SlotTexture;
        }
    }
}
```

### Key Rules

- **NOT JSON**: Do not use `{}` with colons and commas like JSON. Use `Property: value;` syntax
- **File extension**: Must be `.ui` only. Files like `.ui.json` will cause "Failed to load CustomUI documents" errors
- **Elements**: `Group` (container/div), `Label` (text), `TextField` (input)
- **Properties**: `Anchor` for positioning, `Background` for textures, `Style` for fonts, `LayoutMode` for layout
- **Variables**: Define textures with `@VarName = PatchStyle(TexturePath: "path.png");`
- **IDs**: Use `#elementId` after the element type

### Opening Custom UI from Java

```java
public class MachineUI extends BasicCustomUIPage {
    public MachineUI(PlayerRef playerRef) {
        super(playerRef, CustomPageLifetime.CanDismiss);
    }

    @Override
    public void build(UICommandBuilder commands) {
        commands.append("Common/UI/Custom/MachineUI.ui");
        commands.set("#powerLabel", "Power: 100 W");  // Update dynamic elements
    }
}

// To open:
Player player = store.getComponent(ref, Player.getComponentType());
player.getPageManager().openCustomPage(ref, store, new MachineUI(playerRef));
```

### Troubleshooting

| Error | Cause | Solution |
|-------|-------|----------|
| "Failed to load CustomUI documents" | Invalid .ui syntax or wrong file extension | Ensure files use CSS-like syntax, not JSON. Use only `.ui` extension |
| UI not appearing | File path wrong or not registered | Check path in `commands.append()`, ensure `IncludesAssetPack: true` |

---

## Logging

```java
private static final HytaleLogger LOGGER = HytaleLogger.forEnclosingClass();

LOGGER.atInfo().log("Information message");
LOGGER.atWarning().log("Warning message");
LOGGER.atSevere().withCause(exception).log("Error occurred");
```

---

## Troubleshooting

### Build Fails

| Error | Solution |
|-------|----------|
| "Could not find HytaleServer.jar" | Check `hytale_home` in `gradle.properties` points to correct Hytale installation |
| "Java 25 required" | Install Java 25 SDK, ensure `JAVA_HOME` is set |
| Compilation errors | Check import statements match Hytale API packages |

### Server Won't Start

| Issue | Solution |
|-------|----------|
| "Port already in use" | Stop other servers, or change port in server config |
| "Assets not found" | Verify `--assets` path in run configuration |
| Authentication errors | Run `auth login device` in server console |

### Plugin Not Loading

| Issue | Solution |
|-------|----------|
| Plugin not detected | Check `manifest.json` is in JAR at root level |
| "Main class not found" | Verify `Main` in `manifest.json` matches fully qualified class name |
| Version mismatch | Ensure `ServerVersion` in manifest is `*` or matches server version |

### In-Game Issues

| Issue | Solution |
|-------|----------|
| Command not found | Check command registered in `setup()`, try `/help` |
| Permission denied | Set `setPermissionGroup(GameMode.Adventure)` for all players |
| Changes not appearing | Rebuild with `./gradlew build`, restart server, reconnect |

### WSL-Specific (Your Setup)

Since Hytale is installed on Windows but development is in WSL:
- The `hytale_home` path uses `/mnt/c/...` to access Windows files
- Server runs in WSL, client connects from Windows
- File changes in WSL are immediately visible to the Windows filesystem

---

## External Documentation

- [Hytale Modding Documentation (Britakee Studios)](https://britakee-studios.gitbook.io/hytale-modding-documentation)
- [HytaleDocs Community Wiki](https://hytale-docs.com/docs/modding/plugins/overview)
- [Hytale Server Docs (Unofficial)](https://hytale-docs.pages.dev/modding/plugins/plugin-system/)
- [HytaleModding.dev Guides](https://hytalemodding.dev/en/docs/guides/plugin/creating-events)
