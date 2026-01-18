---
name: deploy-plugin
description: Builds the Fabrica plugin and deploys it to the Hytale mods folder. Use when deploying, building for release, or updating the mod installation.
---

Build the Fabrica plugin and deploy it to the Hytale mods folder.

## Steps

1. Run `./gradlew build` to build the plugin
2. Find the base JAR file (not -javadoc or -sources) in `build/libs/`
3. Copy it to `/mnt/c/Users/femia/AppData/Roaming/Hytale/UserData/Mods/`
4. Report success with the filename and confirm deployment location
