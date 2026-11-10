# Changelog

## 1.7.0

- The config engine and the config screens now live in the shared library **Camelot**, so every mod on this project uses the same implementation and the same look.
  配置引擎与配置界面迁移到共享库 **Camelot**，本项目所有模组共用同一套实现与外观。
- Configuration is split into `*-common.toml` (rules, synced from the server) and `*-client.toml` (local preferences only).
  配置拆分为 `*-common.toml`（规则，由服务端同步）与 `*-client.toml`（仅本机偏好）。
- New `enable_config_entry` option (replaces the old pause-button switch): when off, the mod-list config entry and the pause-menu button are both unavailable.
  新增 `enable_config_entry`（取代原来的暂停按钮开关）：关闭后模组列表配置入口与暂停菜单按钮同时不可用。
- The pause-menu button opens the AvalonBase editor only; the mod-list entry is the way into the mod's own screen.
  暂停菜单按钮只打开 AvalonBase 编辑器；模组自身的配置界面走模组列表入口。
- Mode is now an enum selector; screen switches are queued so opening and closing no longer flashes.
  模式改为枚举下拉选择；界面切换改为排队处理，打开/关闭不再闪烁。
- Per-target loader metadata: the NeoForge version range is declared for each target, and the 26.1-26.2 target builds against 26.1.2.
  按区间声明加载器元数据：每个 target 单独声明 NeoForge 版本区间，26.1-26.2 目标基于 26.1.2 构建。
 — Only TP

## 1.6.3

- Added the "Allow entity selectors" toggle (default on). Non-OP players in restricted modes can now autocomplete and use `@` target selectors; when disabled, the server rejects any command containing one and shows a notice.
  新增「允许目标选择器」开关（默认开启）。受控模式下的非 OP 玩家也能正常补全并使用 `@` 目标选择器；关闭后服务端会拒绝任何含目标选择器的指令并给出提示。
- Added the "Enable animations" toggle (default on). Open/close animations for the config screen and secondary dialogs.
  新增「启用动画效果」开关（默认开启）。配置界面与二级界面的打开/关闭动画。
- With "Vanilla style textures" enabled, hover tooltips now use the vanilla tooltip, so resource packs and other mods can restyle them.
  「原版风格纹理」开启时，悬停提示框改用原版提示框，可被资源包与其它模组正常改写。
- Opening the config screen from the main menu now shows the vanilla menu background (panorama + blur); in-world it uses the standard dim layer instead of an opaque overlay.
  从主菜单打开配置界面时改用原版菜单背景（全景图 + 模糊）；世界内为标准暗化层，不再铺不透明深色。
- Right-hand toggles now line up one-to-one with the left-hand mode options.
  右侧开关与左侧模式选择一项一行对齐。
- The "Add" button keeps its border while the input box is empty, and its right edge is aligned with "Cancel".
  输入框为空时「＋ 添加」按钮保留边框，且右缘与「取消」对齐。
- Moved the permission notice onto the same line as the Save/Cancel buttons.
  权限提示移到与「保存/取消」按钮同一行。
- Added a clickable "Backup feedback address" link on the Mod Menu page.
  新增 Mod Menu 页面可点击的「备用反馈地址」链接。
