<p align="center">
  <img src="src/main/resources/assets/ezshow/logo.png" width="256" height="256" alt="ezshow logo">
</p>

<h1 align="center">ezshow</h1>

<p align="center">
  English
  ·
  <a href="README-zh.md">简体中文</a>
</p>

<p align="center">
  <img alt="Minecraft 1.12.2" src="https://img.shields.io/badge/Minecraft-1.12.2-62b47a?style=flat-square">
  <img alt="Forge 14.23.5.2859" src="https://img.shields.io/badge/Forge-14.23.5.2859-e56b2f?style=flat-square">
  <img alt="Java 8" src="https://img.shields.io/badge/Java-8-5382a1?style=flat-square">
  <img alt="Client or server" src="https://img.shields.io/badge/Install-client%20or%20server-11a8cd?style=flat-square">
</p>

ezshow is a focused Minecraft Forge 1.12.2 mod for sharing held items in chat. Type `/show` to send a message such as `Steve: [Diamond Sword]`. Hover the item to see its normal tooltip, including enchantments, durability, lore, and modded NBT data.

ezshow stays independent, lightweight, and minimal while supporting server-only installation, cooldowns, permissions, and correct item-name display for clients using different languages.

## Features

- One command: `/show`.
- Main hand first; automatically falls back to the off hand when the main hand is empty.
- Vanilla `SHOW_ITEM` hover data for broad compatibility with registered mod items.
- Default item names are translated by each viewer's client; anvil and other custom names remain unchanged.
- A per-player cooldown prevents chat spam and can be disabled with `0`.
- Forge PermissionAPI nodes that permission mods can override.
- The same JAR works in single player, on a LAN host, or as a dedicated-server-only mod.

## Installation

1. Use Minecraft 1.12.2 with Forge 14.23.5.2859 or a compatible newer 1.12.2 Forge build.
2. Put `ezshow.jar` in the relevant `mods` folder.
3. Start or restart the game/server.

| Where you play | Install ezshow on | Client installation required? |
| --- | --- | --- |
| Dedicated server | Server | No |
| Single player / LAN host | Hosting client | Yes, on the host |
| Remote server without ezshow | Client only | Unsupported |

## Usage

Hold an item and run:

```text
/show
```

Selection is deterministic:

1. A non-empty main hand is shared.
2. Otherwise, a non-empty off hand is shared.
3. If both hands are empty, the command shows the localized correct usage: `/show`.
4. A 3-second cooldown is enabled by default.

Arguments are not supported. Invalid arguments and requests rejected by the cooldown also show the correct `/show` usage without broadcasting a message.

## Configuration

Forge creates `config/ezshow.cfg` after the first launch:

```text
general {
    I:cooldownSeconds=3

    permissions {
        S:bypassCooldown=OP
        S:showCommand=ALL
    }
}
```

- `cooldownSeconds`: seconds between successful shares, from `0` to `86400`; `0` disables the cooldown.
- Permission defaults accept `ALL`, `OP`, or `NONE` and require a restart after editing.

| Permission node | Default | Purpose |
| --- | --- | --- |
| `ezshow.command.show` | `ALL` | Allows `/show`. |
| `ezshow.cooldown.bypass` | `OP` | Bypasses the cooldown. |

Permission mods that provide a Forge 1.12.2 PermissionAPI handler can manage these nodes directly.
