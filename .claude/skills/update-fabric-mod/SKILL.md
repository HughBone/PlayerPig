---
name: update-fabric-mod
description: Update Fabric Minecraft mod to new version
---

# Update

Update Fabric mod to versions in reference template at `/fabric-mod-template`

## 0. Template and Target locations

- **Template (source of truth):** `/fabric-mod-template/gradle.properties`
  If path missing, stop and tell user template mount is not available (it mounted into dev container by `run.sh`)
- **Target mod:** mod being updated located at `/workspace`

## 1. Record starting state

1a. Before changing anything, read target mod `gradle.properties`, `build.gradle`, and `src/**/fabric.mod.json`. Note old Minecraft version, the before-after delta helps diagnose build failures (mappings/intermediary mismatches surface clearly when you know what moved)

1b. Backup and extract minecraft source code `*sources.jar` from both target and template. If you can't find the .jar file, you may need to run `./gradlew genSources --no-daemon` first. Extract the jar file to `/tmp/template-mc-code` and `/tmp/target-mc-code` respectively. Note this is minecraft/mojang source code and not the mod jar file.

## 2. Update `gradle.properties`

- In target mod, set keys to the template values (preserve existing comments/formatting, only change values)
- Leave mod-specific keys untouched (e.g. `mod_version`, `maven_group`, mod id)
- Bump the mc part of `mod_version`. For example, if updating minecraft from 26.1 to 26.2 it should look like e.x. `mod_version=XXX_mc26.1` -> `mod_version=XXX_mc26.2`. This is purely visual for jar file name.

## 3. Update `build.gradle`

Align target with template where versions/toolchain are involved:

- **Java version:**
  Match template `options.release`, `sourceCompatibility`, and `targetCompatibility`. A Minecraft version bump may raise Java version.

Never blindly overwrite whole file. Target may have mod-specific dependencies, mixins, etc.

## 4. Bump the `fabric.mod.json` `depends` constraints

In target `src/**/fabric.mod.json`, update `depends` block to match these:

- `minecraft` → `*`
- `fabricloader` → `>=<template loader ver>`
- `java` → `>=<template java ver>`
- `fabric-api` → `*`

## 5. Build to verify

Run the build from the target mod directory. I have a bash alias, so can just do "build" and it will build.

- A clean `BUILD SUCCESSFUL` means the update is complete and the jar in
  `build/libs/` is valid for the new version.
- On `BUILD FAILED`, read the error. Common cases:
  - **Toolchain / Java release errors:** Java version in step 3 doesn't match new requirements. Recheck against template.
  - **Loom / Gradle plugin errors:** `loom_version` mismatch. Confirm it matches the template.
  - **Compilation errors against Minecraft classes:** Mod source uses APIs that changed between Minecraft versions. Find out what changed and fix until build succeeds.
    - Search through `/tmp/template-mc-code` and `/tmp/target-mc-code` that was created in step 1 to help debug.
    - Can also look at https://fabricmc.net/blog for help
    - If above suggestions dont work think of other methods and try them!

If class / method / field names were renamed, run bash command for search and replace renaming.

Re-run the build after any fix until it passes.

## 6. You're done

No need to summarize changes. Just say "Done" when updated and built successfully.
