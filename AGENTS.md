# ezshow engineering guide

This file is the canonical technical and contributor documentation for ezshow. Keep the two READMEs user-facing; update this file whenever architecture, behavior, supported platforms, build tooling, or release procedure changes.

## Product intent

ezshow is a deliberately small item-sharing mod. Its job is to let a player type `/show` and broadcast the item they are holding as a hoverable chat component.

The product priorities, in order, are:

1. Correct behavior for unmodified clients when ezshow is installed only on a dedicated server.
2. Correct per-viewer localization of ordinary item names.
3. Compatibility with normally registered vanilla and modded `ItemStack` data.
4. A small, predictable command surface with configurable anti-spam and standard permissions.
5. Clear boundaries that can be extracted for future loaders without paying multi-loader complexity in the first implementation.

The interaction is inspired by Quark's Item Sharing. ezshow is not intended to reproduce Quark's broader feature set.

## Supported scope

Current release: `1.1.0`.

| Dimension | Supported |
| --- | --- |
| Minecraft | 1.12.2 only |
| Loader | Forge 14.23.5.2859; later compatible 1.12.2 Forge builds may work |
| Java | Java 8; production bytecode must remain major version 52 |
| Dedicated server | Yes; ezshow may be installed only on the server |
| Integrated server | Yes; install ezshow on the hosting client |
| Client-only remote use | No; a client cannot add this server command to a remote server |

Fabric, modern Forge, and NeoForge are future targets, not current deliverables.

## Business invariants

These rules define `/show` and must not drift accidentally:

- `/show` is the only command form. There are no `main`, `off`, aliases, flags, or subcommands.
- Any argument is invalid and produces the correct `/show` usage.
- A non-empty main hand always wins.
- The off hand is used only when the main hand is empty.
- When both hands are empty, the player sees the correct `/show` usage.
- A cooldown rejection also shows the correct `/show` usage and broadcasts nothing.
- Invalid, empty-hand, and rejected attempts do not start or extend the cooldown.
- The cooldown is per player UUID, is in memory, and resets when the server process or integrated server restarts.
- Players with `ezshow.cooldown.bypass` neither check nor acquire a cooldown.
- A successful message has the shape `<player display name>: [<item name>]`.
- The visible item name and hover data come from a defensive copy of the selected stack.
- Default item names are resolved by each receiving client. Literal custom names remain literal and italic, matching vanilla behavior.
- The full serialized stack drives the vanilla `SHOW_ITEM` hover event.

Do not catch broad runtime failures and disguise them as usage errors. Bad input and expected policy rejection use `WrongUsageException`; unexpected failures should remain visible in the server log.

## Source layout and ownership

```text
dev.julian.ezshow
├── core
│   └── cooldown
│       └── CooldownGate.java
└── platform
    └── forge112
        ├── EzShowForgeMod.java
        ├── command
        │   ├── ShowCommand.java
        │   └── ItemTextComponentFactory.java
        ├── config
        │   └── EzShowConfig.java
        └── permission
            └── EzShowPermissions.java
```

- `core` contains Java 8 rules with no Minecraft or loader imports. It is the only code that is immediately portable.
- `platform/forge112` owns Forge lifecycle, Minecraft command objects, text components, configuration, and permissions.
- `src/main/resources/assets/ezshow/logo.png` is the single 256×256 project logo used by Forge metadata and both READMEs.
- `README.md` is the English user guide. `README-zh.md` is its Simplified Chinese counterpart.
- `CHANGELOG.md` records release-visible changes.

The current single-module layout is intentional. Package boundaries provide a clean extraction seam without introducing speculative Gradle modules.

## Runtime flow

1. Forge discovers `EzShowForgeMod` through `@Mod`.
2. During `FMLInitializationEvent`, `EzShowPermissions.register()` registers the two PermissionAPI nodes.
3. During `FMLServerStartingEvent`, the mod registers one `ShowCommand` and one fresh in-memory `CooldownGate<UUID>`.
4. Vanilla command dispatch checks `ShowCommand.checkPermission()` before execution.
5. `ShowCommand.execute()` rejects arguments, selects main hand then off hand, applies cooldown policy, creates a rich item component, and broadcasts through `PlayerList.sendMessage()`.
6. Every receiving client renders its own default item translation and reconstructs the hover tooltip from the serialized item stack.

No custom packets, capabilities, event-bus listeners, mixins, coremods, persistent data, or client proxies are required.

## Official API choices

Prefer the smallest official Forge or vanilla mechanism that meets the requirement:

- Register commands from `FMLServerStartingEvent`.
- Extend vanilla `CommandBase` and use `WrongUsageException` for expected misuse.
- Use Forge `@Config` annotations and range validation for `cooldownSeconds`.
- Use Forge `PermissionAPI.registerNode()` and `PermissionAPI.hasPermission()`; never install or replace the global permission handler.
- Use `ItemStack.writeToNBT()`, `HoverEvent.Action.SHOW_ITEM`, and Forge rarity color for item hover behavior.
- Broadcast with the server's `PlayerList`, not a custom network channel.
- Set `acceptableRemoteVersions = "*"` so a remote client without ezshow is accepted.
- Declare Forge `14.23.5.2859` as the minimum local dependency used and tested by this build.
- Set `acceptedMinecraftVersions = "[1.12.2]"` to reject accidental use on other Minecraft versions.

### Why `ItemStack#getTextComponent()` is not called directly

Minecraft 1.12.2's implementation eagerly calls `ItemStack#getDisplayName()` and stores the result in a `TextComponentString`. On a dedicated server that resolves ordinary names in the server locale, usually English. The receiving Chinese client can localize the hover tooltip, but it cannot translate the already flattened visible label.

`ItemTextComponentFactory` is therefore a narrow compatibility adapter around the vanilla implementation:

- NBT `display.Name` becomes a literal italic `TextComponentString`.
- NBT `display.LocName` remains a `TextComponentTranslation`.
- An ordinary stack uses `item.getUnlocalizedName(stack) + ".name"` as a `TextComponentTranslation`.
- Hover payload and rarity color use the same public vanilla/Forge APIs as `ItemStack#getTextComponent()`.

This is intentionally not a general text framework. Keep it package-private and covered by focused tests.

### Why there is no ezshow language catalogue

A dedicated-server-only installation cannot assume clients have ezshow translation resources. The previous implementation reflected into `EntityPlayerMP` to read its language and parsed `.lang` files on the server. That duplicated platform behavior and was fragile under mappings.

All expected failures now throw `WrongUsageException("/show")`. Vanilla wraps it in the built-in `commands.generic.usage` component, so every unmodified client localizes the prefix and receives the literal, universally valid `/show` usage. Permission denial likewise uses vanilla command feedback. The packaged `en_us.lang` is only a standard Forge resource marker that avoids FML's missing-English-resource warning; it contains no custom command messages and is not parsed by ezshow.

Do not add locale reflection, client-only `I18n`, or a custom server translation parser unless a future user-visible requirement cannot be expressed through vanilla components.

## Cooldown policy

`CooldownGate<K>` stores monotonic deadlines and returns a boolean from `tryAcquire`.

- Callers provide `System.nanoTime()`, never wall-clock time.
- Deadline comparison uses subtraction so `nanoTime()` wraparound remains safe for realistic durations.
- Access is synchronized, even though Forge normally executes commands on the server thread; this keeps the core type correct if a future adapter calls it elsewhere.
- A duration of zero removes any existing deadline.
- The configured maximum is 86,400 seconds, far below the wraparound safety bound.

Do not persist cooldowns unless persistence becomes an explicit product requirement.

## Configuration and permissions

Forge owns `config/ezshow.cfg` through `EzShowConfig`.

| Setting | Default | Range / values | Restart |
| --- | --- | --- | --- |
| `cooldownSeconds` | `5` | `0..86400` | No for a directly reloaded config; normal file edits are safest with restart |
| `permissions.showCommand` | `ALL` | `ALL`, `OP`, `NONE` | Yes |
| `permissions.bypassCooldown` | `OP` | `ALL`, `OP`, `NONE` | Yes |

Permission nodes:

- `ezshow.command.show`
- `ezshow.cooldown.bypass`

Names follow Forge's recommended `modid.subgroup.permission_id` pattern. A third-party permission mod may replace Forge's handler; ezshow only registers and queries nodes.

## Dedicated-server compatibility

Production source must not reference `net.minecraft.client.*` or client-only Forge APIs. Review imports after every platform change.

The server may run ezshow without clients having the JAR because:

- no custom network channel or ezshow-specific packet is sent;
- all chat component types and translation keys used on the wire are vanilla;
- `acceptableRemoteVersions = "*"` accepts an absent remote mod;
- the item-owning content mod, when applicable, remains a separate client/server requirement.

Never set `serverSideOnly = true`: that would prevent the same JAR from loading on a client for single-player and LAN-host use.

## Build system

The repository uses:

- ForgeGradle `5.1.40`;
- Forge `1.12.2-14.23.5.2859`;
- snapshot mappings `20171003-1.12`;
- Gradle wrapper `7.4.2` with a pinned SHA-256;
- JUnit `4.13.2` for tests.

Use the project-local Gradle home to avoid polluting or depending on a machine-global cache:

```powershell
$env:JAVA_HOME='C:\Program Files\Eclipse Adoptium\jdk-8.0.492.9-hotspot'
$env:GRADLE_USER_HOME="$PWD\.gradle-user-home"
.\gradlew.bat clean build --offline --no-daemon
```

Remove `--offline` only when dependencies genuinely need to be downloaded. The distributable output is `build/libs/ezshow-forge-1.12.2-<version>.jar`.

The Gradle archive task disables source timestamps and uses reproducible entry order. ForgeGradle 5.1.40's in-place `reobfJar` step rewrites ZIP entry timestamps to the build time, so final JARs from identical sources have identical entry content but are not byte-for-byte reproducible. Do not add further time-dependent content such as manifest timestamps, and do not add a custom ZIP normalizer unless reproducible release artifacts become an explicit requirement.

### Legacy development-run workaround

ForgeGradle's legacy 1.12.2 merge path can put obsolete or module metadata classes on the development runtime classpath. `build.gradle` therefore:

- excludes MergeTool from `runtimeClasspath`;
- extracts only Forge's universal `Side.class` into `build/forgeUserdevFix`;
- prepends that directory only to `runClient` and `runServer`.

This workaround is development-only and is never packaged in ezshow's JAR. Do not remove it based on a compile-only check; verify both development runs first.

The development server is configured for port `25566`, not the default `25565`. The ignored `run/eula.txt` belongs to the local test instance.

Start a local server with:

```powershell
$env:JAVA_HOME='C:\Program Files\Eclipse Adoptium\jdk-8.0.492.9-hotspot'
$env:GRADLE_USER_HOME="$PWD\.gradle-user-home"
.\gradlew.bat runServer --offline --no-daemon
```

## Verification requirements

Run automated checks for every code or build change:

```powershell
.\gradlew.bat clean build --offline --no-daemon
```

The tests must cover at least:

- cooldown first use, rejection, deadline expiry, independent keys, disabled duration, and timer wraparound;
- main-hand priority, off-hand fallback, and both-hands-empty selection;
- default item translation keys, literal custom names, and NBT translatable names.

Before a release, perform a real Forge runtime matrix:

1. Dedicated server with ezshow; unmodified client without ezshow.
2. Client with ezshow in single player or as LAN host.
3. Chinese client viewing a default diamond sword: visible label should be Chinese.
4. A second client language viewing the same message: visible label should use that client's language.
5. An anvil-renamed item such as `宝剑`: literal name should remain unchanged and italic.
6. Main hand occupied plus off hand occupied: main hand wins.
7. Main hand empty plus off hand occupied: off hand is shared.
8. Both hands empty, extra arguments, and cooldown rejection: localized usage ending in `/show`.
9. Enchanted, damaged, lore-bearing, and representative modded items: hover details remain correct.
10. Permission defaults and a PermissionAPI provider, if available.

Static build success is not evidence of dedicated-server or client-rendering behavior. Record which runtime cases were actually observed.

## Future loader/version strategy

Do not create a multi-loader build until a second platform implementation is being added. At that point, prefer this extraction:

```text
ezshow-common
ezshow-forge-1.12.2
ezshow-fabric-<minecraft-version>
ezshow-neoforge-<minecraft-version>
```

Move code into `common` only when at least two adapters share a stable rule. Command APIs, item component formats, serialization, permissions, and configuration differ enough across Minecraft versions that they should remain platform-owned.

Likely portable concepts:

- held-item preference as a value-level rule;
- cooldown policy and configuration semantics;
- stable permission identifiers;
- product behavior tests expressed without loader types.

Likely platform-specific concepts:

- command registration and exceptions;
- player/item types;
- chat component and hover serialization;
- permission integration;
- configuration loading;
- loader metadata and client/server compatibility declarations.

Avoid a universal wrapper around Minecraft objects. Introduce small adapter interfaces only after the second implementation demonstrates a shared boundary.

## Coding rules

- Keep production code Java 8 compatible; do not use newer language or library APIs.
- Prefer final classes, explicit dependencies, small methods, and immutable values where practical.
- Use official public Forge/Minecraft APIs before reflection or duplicated infrastructure.
- Avoid client-only imports, custom packets, event handlers, and dependencies unless required by a concrete feature.
- Preserve the one-command product scope.
- Keep `core` free of loader imports.
- Add focused tests for business-rule changes; do not start Minecraft for a rule that can be tested as plain Java.
- Do not rewrite unrelated MDK/ForgeGradle compatibility code during feature work.
- Keep `build.gradle` and `EzShowForgeMod.VERSION` synchronized.
- Keep README behavior/config tables synchronized in English and Chinese.
- Store contributor architecture here rather than reintroducing a second architecture document.
- Do not add or change a repository license without the project owner's explicit choice.

## Release checklist

1. Confirm version consistency in `build.gradle`, `EzShowForgeMod`, `CHANGELOG.md`, and output filename documentation.
2. Run `clean build` with Java 8 and verify all tests.
3. Confirm class-file major version 52.
4. Inspect the reobfuscated JAR: expected classes/resources present; tests, development workaround classes, and ignored files absent.
5. Confirm entry content is stable and record the final release JAR SHA-256. A raw hash changes between builds because ForgeGradle 5.1.40 rewrites ZIP entry timestamps; see the build-system note above.
6. Run the dedicated-server and integrated-server manual matrix.
7. Verify the server binds to port `25566` in the local development run.
8. Review both READMEs, metadata, the shared 256×256 logo, changelog, and permissions/config examples.
9. Review `git status` for generated files, secrets, logs, local run state, or the Forge MDK archive.
10. Choose or confirm the repository license before public distribution.

## Primary references

- Forge 1.12.x loading stages and command registration: <https://docs.minecraftforge.net/en/1.12.x/conventions/loadstages/>
- Forge 1.12.x PermissionAPI: <https://docs.minecraftforge.net/en/1.12.x/utilities/permissionapi/>
- Forge 1.12.x config annotations: <https://docs.minecraftforge.net/en/1.12.x/config/annotations/>
- Forge 1.12.x internationalization: <https://docs.minecraftforge.net/en/1.12.x/concepts/internationalization/>
- Forge 1.12.x mod structure and `@Mod` metadata: <https://docs.minecraftforge.net/en/1.12.x/gettingstarted/structuring/>

When documentation and the mapped 1.12.2 implementation differ, inspect the exact local Forge/Minecraft classes used by this build and write a regression test around the required behavior.
