<p align="center">
  <img src="src/main/resources/assets/ezshow/logo.png" width="256" height="256" alt="ezshow 标志">
</p>

<h1 align="center">ezshow</h1>

<p align="center">
  <a href="README.md">English</a>
  ·
  简体中文
</p>

<p align="center">
  <img alt="Minecraft 1.12.2" src="https://img.shields.io/badge/Minecraft-1.12.2-62b47a?style=flat-square">
  <img alt="Forge 14.23.5.2859" src="https://img.shields.io/badge/Forge-14.23.5.2859-e56b2f?style=flat-square">
  <img alt="Java 8" src="https://img.shields.io/badge/Java-8-5382a1?style=flat-square">
  <img alt="客户端或服务端" src="https://img.shields.io/badge/安装位置-客户端或服务端-11a8cd?style=flat-square">
</p>

ezshow 是一个专注于聊天物品展示的 Minecraft Forge 1.12.2 模组。输入 `/show` 后，聊天中会以普通玩家消息的外层格式显示类似 `<Steve> [钻石剑]` 的内容；鼠标悬停在物品上，可以查看附魔、耐久、Lore 以及其他模组写入的 NBT 数据。

ezshow 专注于独立、轻量且极简，实现服务端单装、冷却、权限，以及让普通物品名由不同语言的客户端分别本地化。

## 功能

- 只有一个指令：`/show`。
- 优先展示主手；主手为空时自动展示副手。
- 使用原版 `SHOW_ITEM` 悬停数据，兼容正常注册的其他模组物品。
- 默认物品名由每位查看者的客户端自行翻译；铁砧改名以及由其他模组动态组合的名称保留物品自身的显示文本。
- 为防止刷屏，玩家拥有独立冷却，可设为 `0` 关闭。
- 使用 Forge PermissionAPI，权限模组可以接管权限节点。
- 同一 JAR 可用于单人游戏、LAN 主机或仅安装在独立服务端。

## 安装

1. 使用 Minecraft 1.12.2，以及 Forge 14.23.5.2859 或兼容的更新版 1.12.2 Forge。
2. 将 ezshow.jar 放入对应的 `mods` 文件夹。
3. 启动或完整重启游戏/服务端。

| 使用场景 | ezshow 安装位置 | 普通客户端需要安装吗？ |
| --- | --- | --- |
| 独立服务端 | 服务端 | 不需要 |
| 单人游戏 / LAN 主机 | 主机客户端 | 主机需要 |
| 远程服务器未安装 ezshow | 仅普通客户端 | 不支持 |

## 使用

手持物品并输入：

```text
/show
```

选择规则固定如下：

1. 主手非空时展示主手物品。
2. 主手为空、副手非空时展示副手物品。
3. 双手都为空时，显示由原版客户端本地化的正确用法：`/show`。
4. 默认存在 3 秒冷却。

指令不接受任何参数。参数错误或仍处于冷却时，同样只显示正确的 `/show` 用法，不会广播消息。

## 配置

首次启动后，Forge 会生成 `config/ezshow.cfg`：

```text
general {
    I:cooldownSeconds=3

    permissions {
        S:bypassCooldown=OP
        S:showCommand=ALL
    }
}
```

- `cooldownSeconds`：两次成功展示之间的秒数，范围 `0` 至 `86400`；设为 `0` 可关闭冷却。
- 权限默认值可选 `ALL`、`OP`、`NONE`，修改后需要重启。

| 权限节点 | 默认值 | 用途 |
| --- | --- | --- |
| `ezshow.command.show` | `ALL` | 允许使用 `/show`。 |
| `ezshow.cooldown.bypass` | `OP` | 绕过冷却。 |

提供 Forge 1.12.2 PermissionAPI 处理器的权限模组可以直接管理这些节点。
