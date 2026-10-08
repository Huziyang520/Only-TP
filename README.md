**English** | [中文](#中文)

---

# Only TP

> A server-side command limiter for Minecraft: let players use teleport commands only, or block every non-teleport command.

## What it does

| Mode | Effect |
|---|---|
| **Allow TP only** | Regular players may use teleport commands. Other commands keep their vanilla behaviour. |
| **Block non-TP** | Teleport commands are allowed for everyone; every other command is blocked. Operators can be given a whitelist of extra commands. |
| **Enable both** | Both rules apply at the same time, each with its own exemption list. |
| **Disable mod** | No restriction at all. |

- Blocked commands answer with a red `Only TP: This command has been disabled!`.
- Blocked game mode switching answers with its own red message.
- Each mode has its **own exemption list**: players on it are never restricted.

## Quick start

1. Put the mod file into your server's `mods` folder (Fabric or NeoForge).
2. Start the server once — `config/onlytp.toml` is created automatically.
3. Either edit that file, or use the in-game editor.
4. Pick a mode, fill in the exemption list (and the operator command whitelist if you want one), save.

## The in-game editor

- Open it from the **pause menu** (the mod's edit button) or from the **mod list** → *Only TP* → *Config*.
- The visual editor needs the optional library **AvalonBase** on the client, plus operator permission on the server. Without AvalonBase the mod still works — you just edit the config file by hand.
  - AvalonBase: <https://www.curseforge.com/minecraft/mc-mods/avalonbase>
- *Interface* options: **Show pause button**, **Vanilla-style textures** and **Enable interface animations** (all on by default).

## Config file

`config/onlytp.toml` — plain TOML, safe to edit by hand at any time; changes are picked up **without restarting the server**.

| Key | Meaning |
|---|---|
| `mode` | `disabled` / `allow_tp_only` / `block_non_tp` / `both` |
| `show_pause_button` | Show the edit button in the pause menu |
| `enable_animations` | Interface open/close animations |
| `gui_button_style` | `0` = modern look, `1` = vanilla-style buttons |
| `[mode_allow_tp_only]` / `[mode_block_non_tp]` | Exemption lists of that mode |
| `[command_whitelist]` | Extra commands operators may use in "block non-TP" mode |

## Good to know

- Which commands count as "teleport commands" is decided by the mod, not by the config.
- Command names are matched case-insensitively; a leading `/` is ignored.
- Everything is decided on the **server**; installing the mod on the client only adds the visual editor.

## Links

- Project page: <https://www.curseforge.com/minecraft/mc-mods/only-tp>
- Feedback (backup): <https://issue.mengcai.online/>

## License

MIT — author: Huziyang520

---
---

<a id="中文"></a>

[English](#only-tp) | **中文**

---

# Only TP

> Minecraft 服务端的指令限制器：让玩家只能使用传送类指令，或禁止所有非传送指令。

## 它做什么

| 模式 | 效果 |
|---|---|
| **非OP玩家仅允许TP指令** | 普通玩家可以使用传送类指令；其它指令保持原版行为。 |
| **OP玩家禁止非TP指令** | 所有人都能使用传送类指令；其它指令一律禁止。管理员可以额外配一份"命令白名单"。 |
| **同时启用** | 两套规则同时生效，各自带独立的豁免名单。 |
| **关闭模组功能** | 完全不做限制。 |

- 被拦截的指令会收到红字 `Only TP: 该指令已被禁止！`。
- 被拦截的游戏模式切换会显示对应的游戏模式切换提示。
- 每个模式都有**自己的豁免名单**：名单里的玩家完全不受限制。

## 快速开始

1. 把模组文件放进服务端的 `mods` 文件夹（Fabric 或 NeoForge）。
2. 启动一次服务端，会自动生成 `config/onlytp.toml`。
3. 直接改这个文件，或者用游戏内编辑界面。
4. 选好模式、填好豁免名单（需要的话再配管理员命令白名单），保存即可。

## 游戏内编辑界面

- 从**暂停菜单**的编辑按钮进入，或从**模组列表** → *Only TP* → *配置* 进入。
- 可视化编辑界面需要客户端安装可选前置库 **AvalonBase**，并且在服务端拥有管理员权限；不装 AvalonBase 也能正常使用，只是只能手改配置文件。
  - AvalonBase 下载：<https://www.curseforge.com/minecraft/mc-mods/avalonbase>
- 「界面设置」里有 **显示暂停按钮**、**原版风格纹理**、**启用动画效果** 三项（默认都开启）。

## 配置文件

`config/onlytp.toml` —— 纯 TOML 文本，随时可以手改，**改动无需重启服务端**即可生效。

| 键 | 含义 |
|---|---|
| `mode` | `disabled` / `allow_tp_only` / `block_non_tp` / `both` |
| `show_pause_button` | 是否显示暂停菜单里的编辑按钮 |
| `enable_animations` | 是否启用界面开/关动画 |
| `gui_button_style` | `0` = 现代风格，`1` = 原版风格按钮 |
| `[mode_allow_tp_only]` / `[mode_block_non_tp]` | 对应模式的豁免玩家名单 |
| `[command_whitelist]` | "禁止非TP指令"模式下管理员可额外使用的指令 |

## 使用须知

- 哪些指令算"传送类指令"由模组内置决定，不需要在配置里写。
- 指令名不区分大小写，开头的 `/` 会被忽略。
- 所有判定都在**服务端**完成；客户端装本模组只是为了获得可视化编辑界面。

## 相关链接

- 项目主页：<https://www.curseforge.com/minecraft/mc-mods/only-tp>
- 备用反馈地址：<https://issue.mengcai.online/>

## 许可证

MIT —— 作者：Huziyang520
