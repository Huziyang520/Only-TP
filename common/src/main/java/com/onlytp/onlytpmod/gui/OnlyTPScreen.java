package com.onlytp.onlytpmod.gui;

import com.avalon.base.gui.panel.PanelHover;
import com.avalon.base.gui.theme.GuiTheme;
import com.avalon.base.gui.theme.ModernTheme;
import com.avalon.base.gui.theme.ThemedButton;
import com.avalon.base.gui.theme.ThemedRadio;
import com.avalon.base.gui.theme.ThemedToggle;
import com.avalon.base.gui.theme.VanillaTheme;
import com.avalon.base.gui.util.MouseButtons;
import com.onlytp.onlytpmod.AvalonLink;
import com.onlytp.onlytpmod.config.OnlyTPConfig;
import com.onlytp.onlytpmod.network.ConfigUpdatePacket;
import com.avalon.base.network.AvalonNetwork;
import com.onlytp.onlytpmod.network.NetworkChannels;
import com.onlytp.onlytpmod.util.ModMsg;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.components.EditBox;
import net.minecraft.client.resources.sounds.SimpleSoundInstance;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.input.KeyEvent;
import net.minecraft.client.input.MouseButtonEvent;
import net.minecraft.network.chat.Component;
import net.minecraft.server.permissions.Permissions;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.util.Mth;
import com.mojang.blaze3d.platform.cursor.CursorTypes;

import java.util.ArrayList;
import java.util.List;

/**
 * OnlyTP 配置界面（双主题：原版箱子风格 / 现代末影紫风格）。
 * 现代主题贴图放在 assets/onlytp/textures/gui/modern/。
 */
public class OnlyTPScreen extends Screen {

    private static final int GUI_WIDTH = 300;
    private static final int GUI_HEIGHT = 260;
    private static final int CONTENT_MIN_Y = 10;
    private static final int CONTENT_MAX_Y = 218;
    private static final int MAX_VISIBLE_ITEMS = 3;
    private static final int LIST_ITEM_H = 14;

    private static final GuiTheme VANILLA_THEME = new VanillaTheme();
    // Stock ModernTheme (Ender Purple); the one-pixel button frame is drawn by
    // ButtonFrames after the widgets render. A wrapper GuiTheme would fail the
    // instanceof ModernTheme check inside ThemedButton, whose fallback text
    // color 0xFFFFFF has zero alpha, so labels would render transparent.
    private static final GuiTheme MODERN_THEME = new ModernTheme();

    // ─── 配置状态 ───
    private String selectedMode;
    private boolean localShowPause;
    private boolean localUseVanilla;
    private final List<String> blacklistAllowTp = new ArrayList<>();
    private final List<String> blacklistBlockNonTp = new ArrayList<>();

    private boolean canEdit;
    // Parent screen (mod list) to return to; null falls back to the default close behavior.
    private Screen parentScreen;
    // Local-edit mode is opened from the main menu: edits write the local toml and never send packets.
    private boolean localEdit;
    private int guiLeft, guiTop;
    private int blScroll;

    // ─── 控件 ───
    private EditBox playerNameInput;
    private ThemedButton addButton;
    private final ThemedRadio[] modeRadios = new ThemedRadio[4];
    private ThemedToggle showPauseToggle;
    private ThemedToggle styleToggle;

    // ─── 主题 & 动画 ───
    private GuiTheme theme;
    private int hoveredRow = -1;
    // Red "click to remove" hint shown in a PanelHover box while a row is hovered.
    private String rowRemoveHint;

    private static final String[] MODES = {"allow_tp_only", "block_non_tp", "both", "disabled"};

    public OnlyTPScreen() {
        this(null, false);
    }

    public OnlyTPScreen(Screen parent, boolean localEdit) {
        super(Component.translatable("gui.onlytp.title"));
        this.parentScreen = parent;
        this.localEdit = localEdit;
        this.selectedMode = OnlyTPConfig.mode;
        this.localShowPause = OnlyTPConfig.showPauseButton;
        this.localUseVanilla = OnlyTPConfig.guiButtonStyle == 1;
        this.theme = localUseVanilla ? VANILLA_THEME : MODERN_THEME;
        this.blacklistAllowTp.addAll(OnlyTPConfig.blacklistAllowTp);
        this.blacklistBlockNonTp.addAll(OnlyTPConfig.blacklistBlockNonTp);
    }

    // ═══════════ 布局方法 ═══════════

    private boolean isBlacklistVisible() {
        return "allow_tp_only".equals(selectedMode) || "block_non_tp".equals(selectedMode);
    }

    private int yo(int relY) { return guiTop + relY; }

    // ═══════════ 初始化 ═══════════

    @Override
    protected void init() {
        guiLeft = (width - GUI_WIDTH) / 2;
        // 优先居中，但保证面板底边框（CONTENT_MAX_Y+14）留在屏幕内：小窗时上移，避免下边框跑出屏幕
        int relPanelBottom = CONTENT_MAX_Y + 14;
        guiTop = Math.min(Math.max(6, (height - GUI_HEIGHT) / 2), height - 6 - relPanelBottom);
        canEdit = localEdit || (minecraft != null && minecraft.player != null
                && minecraft.player.permissions().hasPermission(Permissions.COMMANDS_GAMEMASTER));
        blScroll = Math.max(0, blScroll);

        // ─── 模式单选（4个，12px行距，用勾选框样式） ───
        for (int i = 0; i < 4; i++) {
            modeRadios[i] = new ThemedRadio(guiLeft + 16, 28 + i * 12,
                    Component.translatable("gui.onlytp.mode_" + MODES[i]),
                    MODES[i].equals(selectedMode), canEdit);
        }

        // ─── 开关 ───
        showPauseToggle = new ThemedToggle(guiLeft + 162, 28,
                Component.translatable("gui.onlytp.toggle_show_button"), localShowPause, canEdit, false);
        styleToggle = new ThemedToggle(guiLeft + 162, 42,
                Component.translatable("gui.onlytp.toggle_vanilla_texture"), localUseVanilla, canEdit, false);

        // ─── 黑名单输入（仅 allow_tp_only / block_non_tp） ───
        playerNameInput = null;
        addButton = null;
        if (isBlacklistVisible() && canEdit) {
            playerNameInput = new EditBox(font, guiLeft + 8, yo(166), 184, 18,
                    Component.translatable("gui.onlytp.input_hint"));
            playerNameInput.setMaxLength(32);
            playerNameInput.setResponder(s -> updateAddButtonState());
            addRenderableWidget(playerNameInput);

            addButton = new ThemedButton(guiLeft + 237, yo(165), 55, 20,
                    Component.translatable("gui.onlytp.add"), b -> addPlayer(),
                    theme, GuiTheme.ButtonRole.PRIMARY, font);
            addButton.active = false;
            addRenderableWidget(addButton);

            // 聚焦输入框：确保光标闪烁并可直接键入
            setInitialFocus(playerNameInput);
            playerNameInput.setFocused(true);
        }

        // ─── 保存/取消（整体左移 10px） ───
        if (canEdit) {
            ThemedButton save = new ThemedButton(guiLeft + GUI_WIDTH - 128, yo(194), 55, 20,
                    Component.translatable("gui.onlytp.save"), b -> saveConfig(),
                    theme, GuiTheme.ButtonRole.PRIMARY, font);
            addRenderableWidget(save);
        }
        ThemedButton cancel = new ThemedButton(guiLeft + GUI_WIDTH - 68, yo(194), 55, 20,
                Component.translatable("gui.onlytp.cancel"), b -> onClose(),
                theme, GuiTheme.ButtonRole.NEUTRAL, font);
        addRenderableWidget(cancel);
    }

    // ═══════════ 渲染 ═══════════

    @Override
    public void extractBackground(GuiGraphicsExtractor graphics, int mouseX, int mouseY, float partialTick) {
        // 背景通道铺近不透明深色：26.2 若背景置空，打开本屏（暂停屏）时原版模糊暗背景需一帧才生效，
        // 首帧会露出明亮游戏画面透过半透明层，造成"闪一下"。用不透明深色从背景趟就压暗画面。
        graphics.fill(0, 0, this.width, this.height, 0xC0101010);
    }

    @Override
    public void extractRenderState(GuiGraphicsExtractor graphics, int mouseX, int mouseY, float partialTick) {

        // 主面板背景
        int panelH = yo(CONTENT_MAX_Y) + 14 - (yo(CONTENT_MIN_Y) - 4);
        theme.drawPanel(graphics, guiLeft, yo(CONTENT_MIN_Y) - 4, GUI_WIDTH, panelH);

        // ─── 已移除标题文字，上边距缩小 ───

        // ─── 模式卡片（左半，放大至76px高容纳4条） ───
        int cardLeftX = 8, cardLeftW = 146;
        theme.drawCard(graphics, guiLeft + cardLeftX, yo(14), cardLeftW, 76);
        graphics.text(font, Component.translatable("gui.onlytp.mode_label"),
                guiLeft + cardLeftX + 8, yo(18), theme.labelColor(), false);
        for (ThemedRadio r : modeRadios) r.render(graphics, font, theme, yo(r.relY), mouseX, mouseY);

        // ─── 开关卡片（右半） ───
        int cardRightX = 154, cardRightW = 138;
        theme.drawCard(graphics, guiLeft + cardRightX, yo(14), cardRightW, 76);
        graphics.text(font, Component.translatable("gui.onlytp.toggle_label"),
                guiLeft + cardRightX + 8, yo(18), theme.labelColor(), false);
        showPauseToggle.render(graphics, font, theme, yo(showPauseToggle.relY), mouseX, mouseY);
        styleToggle.render(graphics, font, theme, yo(styleToggle.relY), mouseX, mouseY);

        // ─── 黑名单卡片（仅 allow_tp_only / block_non_tp，56px高，可见3行） ───
        hoveredRow = -1;
        rowRemoveHint = null;
        if (isBlacklistVisible()) {
            int blCardY = 98;
            theme.drawCard(graphics, guiLeft + cardLeftX, yo(blCardY), GUI_WIDTH - 16, 64);
            graphics.text(font, Component.translatable("gui.onlytp.blacklist_label"),
                    guiLeft + 18, yo(blCardY + 4), theme.labelColor(), false);

            List<String> bl = getCurrentBlacklist();
            // +22 (not +20): keeps the first row hover band below the card title,
            // otherwise the grey band/name overlap the "exempt players" title.
            int listStartY = blCardY + 22;
            int visible = Math.min(MAX_VISIBLE_ITEMS, bl.size());
            // Row highlight right edge: hug the card's right inner border (6px inset, matching AuthCmd)
            // when there is no scrollbar; when a scrollbar exists (at GUI_WIDTH-16), stop 4px left of it
            // so the highlight never runs under the bar.
            int rowRightX = guiLeft + (bl.size() > MAX_VISIBLE_ITEMS ? GUI_WIDTH - 20 : GUI_WIDTH - 14);
            for (int i = 0; i < visible; i++) {
                int idx = blScroll + i;
                if (idx >= bl.size()) break;
                int y = yo(listStartY + i * LIST_ITEM_H) - 6;
                boolean rowHover = canEdit && mouseX >= guiLeft + 12 && mouseX <= rowRightX
                        && mouseY >= y - 2 && mouseY < y + LIST_ITEM_H - 2;
                if (rowHover) {
                    hoveredRow = idx;
                    graphics.fill(guiLeft + 12, y - 2, rowRightX, y + LIST_ITEM_H - 2,
                            theme.vanillaButtons() ? 0x28000000 : 0x2AB47AE8);
                    // The red "click to remove" hint is now shown in a PanelHover box
                    // (rendered on top at the end of the frame) instead of inside the row,
                    // matching AuthCmd, so it can never overlap long localized entry text.
                    rowRemoveHint = Component.translatable("gui.onlytp.click_to_remove").getString();
                    // ─────────────────────────────────────────────────────────────
                    // 备用方案：行内右对齐红字提示（悬停时直接绘制在名单行右侧）。
                    // 该方案作为备用方案，不得随意删除。
                    // String hint = Component.translatable("gui.onlytp.click_to_remove").getString();
                    // // 颜色必须带 FF 不透明 alpha（0xFFFF5555）；写成 0xFF5555 会被当作 ARGB → alpha=0 → 全透明不可见
                    // graphics.text(font, hint, rowRightX - font.width(hint) - 6, y, 0xFFFF5555, false);
                    // ─────────────────────────────────────────────────────────────
                }
                graphics.text(font, bl.get(idx),
                        guiLeft + 22, y, canEdit ? theme.textColor() : theme.disabledColor(), false);
            }

            // 黑名单滚动条（4px宽，在卡片最右侧）
            if (bl.size() > MAX_VISIBLE_ITEMS) {
                int tx = guiLeft + GUI_WIDTH - 16;
                int tTop = yo(listStartY) - 6;
                int tBot = yo(listStartY + MAX_VISIBLE_ITEMS * LIST_ITEM_H) - 8;
                theme.drawScrollTrack(graphics, tx, tTop, 4, tBot - tTop);
                int trackH = tBot - tTop;
                int thumbH = Math.max(8, trackH * MAX_VISIBLE_ITEMS / bl.size());
                float p = (float) blScroll / Math.max(1, bl.size() - MAX_VISIBLE_ITEMS);
                theme.drawScrollThumb(graphics, tx, tTop + Math.round((trackH - thumbH) * p), 4, thumbH);
            }
        }

        // ─── 底部状态（与黑名单卡片左对齐） ───
        int bottomY = 198;
        graphics.text(font,
                Component.translatable(canEdit ? "gui.onlytp.can_edit" : "gui.onlytp.view_only"),
                guiLeft + 8, yo(bottomY), canEdit ? theme.okColor() : theme.warnColor(), false);

        // 无编辑权限（只读）时叠加遮罩，令自绘内容呈模糊观感；原版按钮在 super.render 中绘制于遮罩之上仍清晰
        if (!canEdit) graphics.fill(0, 0, this.width, this.height, 0x50000000);

        super.extractRenderState(graphics, mouseX, mouseY, partialTick);

        // One-pixel right-angle frames on modern-theme buttons only;
        // vanilla styled buttons keep their stock sprite look.
        if (!theme.vanillaButtons() && theme instanceof ModernTheme modern) {
            ButtonFrames.render(graphics, this, modern.palette());
        }

        // ─── 光标：可交互元素显示手形 ───
        boolean showHand = false;
        if (canEdit) {
            for (var w : children()) {
                if (w instanceof ThemedButton btn && btn.active && btn.isHoveredOrFocused()) {
                    showHand = true;
                    break;
                }
            }
            if (!showHand) {
                for (int i = 0; i < 4; i++) {
                    if (modeRadios[i].isClicked(mouseX, mouseY, font, yo(modeRadios[i].relY))) {
                        showHand = true;
                        break;
                    }
                }
            }
            if (!showHand && (showPauseToggle.isClicked(mouseX, mouseY, font, yo(showPauseToggle.relY))
                    || styleToggle.isClicked(mouseX, mouseY, font, yo(styleToggle.relY)))) {
                showHand = true;
            }
            if (!showHand && hoveredRow >= 0 && isBlacklistVisible()) {
                showHand = true;
            }
        }
        // 26.3：光标经 GuiGraphicsExtractor.requestCursor 请求（帧末统一 apply）；直接改窗口光标会被覆盖。
        // 只在需要手形时请求，不请求箭头，避免盖掉原版控件（按钮/输入框）自己请求的光标。
        if (showHand) graphics.requestCursor(CursorTypes.POINTING_HAND);

        // Hover box for truncated radio/toggle labels (full original text). Drawn last so it
        // stays above cards, widgets and the read-only overlay. No-op while labels fit.
        String tip = null;
        for (ThemedRadio r : modeRadios) {
            tip = r.truncatedTooltip(font, mouseX, mouseY, yo(r.relY));
            if (tip != null) break;
        }
        if (tip == null) {
            tip = showPauseToggle.truncatedTooltip(font, mouseX, mouseY, yo(showPauseToggle.relY));
        }
        if (tip == null) {
            tip = styleToggle.truncatedTooltip(font, mouseX, mouseY, yo(styleToggle.relY));
        }
        if (tip != null) {
            PanelHover.render(graphics, font, tip, mouseX, mouseY, this.width, this.height);
        }

        // Red row-action hint ("click to remove") in the gold hover box.
        if (rowRemoveHint != null) {
            PanelHover.render(graphics, font, rowRemoveHint, mouseX, mouseY, this.width, this.height, 0xFFFF5555);
        }
    }

    // ═══════════ 交互 ═══════════

    @Override
    public boolean mouseClicked(MouseButtonEvent event, boolean param) {
        double mouseX = event.x();
        double mouseY = event.y();
        if (MouseButtons.isLeft(event) && canEdit) {
            // 模式单选
            for (int i = 0; i < 4; i++) {
                if (modeRadios[i].isClicked(mouseX, mouseY, font, yo(modeRadios[i].relY))) {
                    if (!MODES[i].equals(selectedMode)) {
                        playClickSound();
                        selectedMode = MODES[i];
                        blScroll = 0;
                        reloadWidgets();
                    }
                    return true;
                }
            }
            // 开关
            if (showPauseToggle.isClicked(mouseX, mouseY, font, yo(showPauseToggle.relY))) {
                playClickSound();
                localShowPause = !localShowPause;
                showPauseToggle.setChecked(localShowPause);
                return true;
            }
            if (styleToggle.isClicked(mouseX, mouseY, font, yo(styleToggle.relY))) {
                playClickSound();
                localUseVanilla = !localUseVanilla;
                theme = localUseVanilla ? VANILLA_THEME : MODERN_THEME;
                reloadWidgets();
                return true;
            }
            // 黑名单删除
            if (hoveredRow >= 0 && isBlacklistVisible()) {
                List<String> bl = getCurrentBlacklist();
                if (hoveredRow < bl.size()) {
                    playClickSound();
                    bl.remove(hoveredRow);
                    blScroll = Mth.clamp(blScroll, 0, Math.max(0, bl.size() - MAX_VISIBLE_ITEMS));
                    hoveredRow = -1;
                    return true;
                }
            }
        }
        return super.mouseClicked(event, param);
    }

    private void playClickSound() {
        Minecraft.getInstance().getSoundManager().play(SimpleSoundInstance.forUI(SoundEvents.UI_BUTTON_CLICK, 1.0F));
    }

    @Override
    public boolean mouseScrolled(double mouseX, double mouseY, double scrollX, double scrollY) {
        if (isBlacklistVisible()) {
            List<String> bl = getCurrentBlacklist();
            if (bl.size() > MAX_VISIBLE_ITEMS) {
                // Must match listStartY (= blCardY 98 + 22) used while rendering.
                int listStartY = 120;
                int top = yo(listStartY) - 8, bot = yo(listStartY + MAX_VISIBLE_ITEMS * LIST_ITEM_H) - 8;
                if (mouseY >= top && mouseY < bot) {
                    blScroll = Mth.clamp(blScroll - (int) Math.signum(scrollY), 0, bl.size() - MAX_VISIBLE_ITEMS);
                    return true;
                }
            }
        }
        return false;
    }

    // ═══════════ 辅助方法 ═══════════

    private void reloadWidgets() {
        String typed = playerNameInput != null ? playerNameInput.getValue() : "";
        clearWidgets();
        init();
        if (playerNameInput != null) playerNameInput.setValue(typed);
    }

    private List<String> getCurrentBlacklist() {
        return switch (selectedMode) {
            case "block_non_tp" -> blacklistBlockNonTp;
            case "disabled" -> new ArrayList<>(); // disabled 模式无黑名单
            default -> blacklistAllowTp;
        };
    }

    private void addPlayer() {
        if (playerNameInput == null) return;
        String name = playerNameInput.getValue().trim();
        if (name.isEmpty()) return;
        List<String> bl = getCurrentBlacklist();
        if (bl.contains(name)) { msg("gui.onlytp.already_in_blacklist"); return; }
        bl.add(name);
        playerNameInput.setValue("");
        playClickSound();
        blScroll = Math.max(0, bl.size() - MAX_VISIBLE_ITEMS);
        // 添加后保持输入框聚焦，光标继续闪烁可连续输入
        setInitialFocus(playerNameInput);
        playerNameInput.setFocused(true);
    }

    @Override
    public boolean keyPressed(KeyEvent event) {
        if (event.isConfirmation() && addButton != null && addButton.active) {
            addPlayer();
            return true;
        }
        return super.keyPressed(event);
    }

    private void msg(String key) {
        if (minecraft != null && minecraft.player != null)
            minecraft.player.sendSystemMessage(ModMsg.red(minecraft.player, key));
    }

    private void updateAddButtonState() {
        if (addButton != null && playerNameInput != null)
            addButton.active = !playerNameInput.getValue().trim().isEmpty();
    }

    private void saveConfig() {
        if (!canEdit) return;
        // Local edits (opened from the main-menu mod list) only persist to the local toml.
        if (!localEdit && AvalonLink.isAvalonLoaded()) {
            AvalonNetwork.sendToServer(NetworkChannels.UPDATE, new ConfigUpdatePacket(
                    selectedMode, localShowPause, localUseVanilla ? 1 : 0,
                    blacklistAllowTp, blacklistBlockNonTp));
        }
        OnlyTPConfig.mode = selectedMode;
        OnlyTPConfig.showPauseButton = localShowPause;
        OnlyTPConfig.guiButtonStyle = localUseVanilla ? 1 : 0;
        OnlyTPConfig.blacklistAllowTp = new ArrayList<>(blacklistAllowTp);
        OnlyTPConfig.blacklistBlockNonTp = new ArrayList<>(blacklistBlockNonTp);
        if (localEdit) {
            OnlyTPConfig.save();
        }
        onClose();
    }

    @Override
    public void onClose() {
        if (parentScreen != null && minecraft != null) {
            minecraft.setScreenAndShow(parentScreen);
        } else {
            super.onClose();
        }
    }

    @Override
    public void removed() {
        super.removed();
    }

    @Override
    public boolean isPauseScreen() {
        return true;
    }

}