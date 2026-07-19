package com.onlytp.onlytpmod.gui;

import com.mojang.blaze3d.systems.RenderSystem;
import com.onlytp.onlytpmod.config.OnlyTPConfig;
import com.onlytp.onlytpmod.network.ConfigUpdatePacket;
import com.onlytp.onlytpmod.network.NetworkHandler;
import net.minecraft.Util;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.EditBox;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;
import net.minecraft.util.Mth;

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
    private static final int MAX_VISIBLE_ITEMS = 4;
    private static final int LIST_ITEM_H = 14;

    private static final GuiTheme VANILLA_THEME = new VanillaTheme();
    private static final GuiTheme MODERN_THEME = new ModernTheme();

    // ─── 配置状态 ───
    private String selectedMode;
    private boolean localShowPause;
    private boolean localUseVanilla;
    private final List<String> blacklistAllowTp = new ArrayList<>();
    private final List<String> blacklistBlockNonTp = new ArrayList<>();
    private final List<String> blacklistDisabled = new ArrayList<>();

    private boolean canEdit;
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

    private static final String[] MODES = {"allow_tp_only", "block_non_tp", "both", "disabled"};

    public OnlyTPScreen() {
        super(Component.translatable("gui.onlytp.title"));
        this.selectedMode = OnlyTPConfig.mode;
        this.localShowPause = OnlyTPConfig.showPauseButton;
        this.localUseVanilla = OnlyTPConfig.guiButtonStyle == 1;
        this.theme = localUseVanilla ? VANILLA_THEME : MODERN_THEME;
        this.blacklistAllowTp.addAll(OnlyTPConfig.blacklistAllowTp);
        this.blacklistBlockNonTp.addAll(OnlyTPConfig.blacklistBlockNonTp);
        this.blacklistDisabled.addAll(OnlyTPConfig.blacklistDisabled);
    }

    // ═══════════ 动画工具 ═══════════
    static final class Anim {
        private float value, target;
        private long lastTime = Util.getMillis();
        Anim(float initial) { this.value = this.target = initial; }
        void setTarget(float t) { this.target = t; }
        void snap(float v) { this.value = this.target = v; }
        float target() { return target; }
        float value() { return value; }
        float tick(float speed) {
            long now = Util.getMillis();
            float dt = Math.min(0.1f, (now - lastTime) / 1000f);
            lastTime = now;
            value += (target - value) * Math.min(1f, speed * dt);
            if (Math.abs(target - value) < 0.002f) value = target;
            return value;
        }
    }

    enum ButtonRole { PRIMARY, NEUTRAL }

    // ═══════════ 主题抽象 ═══════════
    interface GuiTheme {
        void drawPanel(GuiGraphics g, int x, int y, int w, int h);
        void drawCard(GuiGraphics g, int x, int y, int w, int h);
        void drawButton(GuiGraphics g, int x, int y, int w, int h, float hover, boolean active, ButtonRole role);
        void drawRadio(GuiGraphics g, int x, int y, boolean selected, boolean enabled);
        void drawToggle(GuiGraphics g, int x, int y, float on, boolean enabled);
        void drawScrollTrack(GuiGraphics g, int x, int y, int w, int h);
        void drawScrollThumb(GuiGraphics g, int x, int y, int w, int h);
        void drawIcon(GuiGraphics g, String icon, int x, int y, float alpha);
        int titleColor(); int labelColor(); int textColor(); int disabledColor();
        int okColor(); int warnColor();
        boolean vanillaButtons();
        boolean animated();
    }

    // ═══════════ 原版主题 ═══════════
    static final class VanillaTheme implements GuiTheme {
        @Override public void drawPanel(GuiGraphics g, int x, int y, int w, int h) {
            // 外边框
            g.fill(x - 1, y - 1, x + w + 1, y + h + 1, 0xFF000000);
            // 主填充
            g.fill(x, y, x + w, y + h, 0xFFC6C6C6);
            // 左上高光（模拟圆角减淡）
            g.fill(x, y, x + w - 2, y + 2, 0xFFFFFFFF);
            g.fill(x, y, x + 2, y + h - 2, 0xFFFFFFFF);
            // 右下阴影
            g.fill(x + 2, y + h - 2, x + w, y + h, 0xFF555555);
            g.fill(x + w - 2, y + 2, x + w, y + h, 0xFF555555);
            // 四角圆角过渡（削去尖角）
            g.fill(x, y, x + 1, y + 1, 0xFFAAAAAA);
            g.fill(x + w - 1, y, x + w, y + 1, 0xFF666666);
            g.fill(x, y + h - 1, x + 1, y + h, 0xFF666666);
            g.fill(x + w - 1, y + h - 1, x + w, y + h, 0xFF444444);
        }
        @Override public void drawCard(GuiGraphics g, int x, int y, int w, int h) {
            g.fill(x, y, x + w, y + h, 0xFF9D9D9D);
            g.fill(x, y, x + w, y + 1, 0xFF373737);
            g.fill(x, y, x + 1, y + h, 0xFF373737);
            g.fill(x, y + h - 1, x + w, y + h, 0xFFFFFFFF);
            g.fill(x + w - 1, y, x + w, y + h, 0xFFFFFFFF);
        }
        @Override public void drawButton(GuiGraphics g, int x, int y, int w, int h, float hover, boolean active, ButtonRole role) {}
        @Override public void drawRadio(GuiGraphics g, int x, int y, boolean selected, boolean enabled) {
            drawCard(g, x + 1, y + 1, 10, 10);
            if (selected) g.fill(x + 4, y + 4, x + 9, y + 9, enabled ? 0xFF202020 : 0xFF666666);
        }
        @Override public void drawToggle(GuiGraphics g, int x, int y, float on, boolean enabled) {
            drawCard(g, x + 4, y, 10, 10);
            if (on >= 0.5f) {
                // 居中填充 6x6（卡片内容区中心为 x+9, y+5）
                g.fill(x + 6, y + 2, x + 12, y + 8, enabled ? 0xFF202020 : 0xFF666666);
            }
        }
        @Override public void drawScrollTrack(GuiGraphics g, int x, int y, int w, int h) { g.fill(x, y, x + w, y + h, 0xFF8B8B8B); }
        @Override public void drawScrollThumb(GuiGraphics g, int x, int y, int w, int h) { g.fill(x, y, x + w, y + h, 0xFF373737); }
        @Override public void drawIcon(GuiGraphics g, String icon, int x, int y, float alpha) {
            int col = ((int) (alpha * 255) << 24) | 0x373737;
            if ("x".equals(icon)) {
                // 2px宽叉号，9x9区域内居中
                for (int i = 0; i < 5; i++) {
                    g.fill(x + i, y + i, x + i + 2, y + i + 2, col);
                    g.fill(x + 7 - i, y + i, x + 9 - i, y + i + 2, col);
                }
            } else {
                g.fill(x + 4, y + 1, x + 5, y + 8, col);
                g.fill(x + 1, y + 4, x + 8, y + 5, col);
            }
        }
        @Override public int titleColor() { return 0x404040; }
        @Override public int labelColor() { return 0x404040; }
        @Override public int textColor() { return 0x303030; }
        @Override public int disabledColor() { return 0x808080; }
        @Override public int okColor() { return 0x2E7D32; }
        @Override public int warnColor() { return 0xB02020; }
        @Override public boolean vanillaButtons() { return true; }
        @Override public boolean animated() { return false; }
    }

    // ═══════════ 现代主题-直角风格（纯代码渲染，替换PNG九宫格） ═══════════
    static final class ModernTheme implements GuiTheme {

        @Override public void drawPanel(GuiGraphics g, int x, int y, int w, int h) {
            // 紫黑渐变背景
            g.fillGradient(x, y, x + w, y + h, 0xFF1A1428, 0xFF0D0A1A);
            // 外阴影（右下偏移）
            RenderSystem.enableBlend();
            g.fill(x + 3, y + 3, x + w + 3, y + h + 3, 0x44000000);
            RenderSystem.disableBlend();
            // 直角边框线
            g.fill(x, y, x + w, y + 1, 0xFF5A4A7A);
            g.fill(x, y + h - 1, x + w, y + h, 0xFF5A4A7A);
            g.fill(x, y, x + 1, y + h, 0xFF5A4A7A);
            g.fill(x + w - 1, y, x + w, y + h, 0xFF5A4A7A);
        }
        @Override public void drawCard(GuiGraphics g, int x, int y, int w, int h) {
            // 纯色填充+直角边框
            g.fill(x, y, x + w, y + h, 0xFF16111E);
            g.fill(x, y, x + w, y + 1, 0xFF3A2D52);
            g.fill(x, y + h - 1, x + w, y + h, 0xFF3A2D52);
            g.fill(x, y, x + 1, y + h, 0xFF3A2D52);
            g.fill(x + w - 1, y, x + w, y + h, 0xFF3A2D52);
        }
        @Override public void drawButton(GuiGraphics g, int x, int y, int w, int h, float hover, boolean active, ButtonRole role) {
            int c = !active ? 0xFF2A2538 :
                    (role == ButtonRole.PRIMARY ? (hover > 0.01f ? 0xFF6A4D9A : 0xFF4A2D7A) :
                     (hover > 0.01f ? 0xFF4E4A5E : 0xFF2E2A3E));
            g.fill(x, y, x + w, y + h, c);
        }
        @Override public void drawRadio(GuiGraphics g, int x, int y, boolean selected, boolean enabled) {
            // 不再使用，ThemedRadio 已改用 drawToggle
        }
        @Override public void drawToggle(GuiGraphics g, int x, int y, float on, boolean enabled) {
            // 方形勾选框，10x10
            int boxC = enabled ? 0xFF2E2A3E : 0xFF1A1628;
            int fillC = enabled ? 0xFFB47AE8 : 0xFF5A5568;
            int borderC = enabled ? 0xFF5A4A7A : 0xFF3A2D52;
            g.fill(x, y, x + 10, y + 10, boxC);
            g.fill(x, y, x + 10, y + 1, borderC);
            g.fill(x, y + 9, x + 10, y + 10, borderC);
            g.fill(x, y, x + 1, y + 10, borderC);
            g.fill(x + 9, y, x + 10, y + 10, borderC);
            if (on >= 0.5f) {
                g.fill(x + 2, y + 2, x + 8, y + 8, fillC);
            }
        }
        @Override public void drawScrollTrack(GuiGraphics g, int x, int y, int w, int h) {
            g.fill(x, y, x + w, y + h, 0xFF16111E);
        }
        @Override public void drawScrollThumb(GuiGraphics g, int x, int y, int w, int h) {
            g.fill(x, y, x + w, y + h, 0xFF5A4A7A);
            g.fill(x, y, x + w, y + 1, 0xFF7A6A9A);
        }
        @Override public void drawIcon(GuiGraphics g, String icon, int x, int y, float alpha) {
            int col = ((int) (alpha * 255) << 24) | 0xB47AE8;
            if ("x".equals(icon)) {
                for (int i = 0; i < 5; i++) {
                    g.fill(x + i, y + i, x + i + 2, y + i + 2, col);
                    g.fill(x + 7 - i, y + i, x + 9 - i, y + i + 2, col);
                }
            } else {
                g.fill(x + 4, y + 1, x + 5, y + 8, col);
                g.fill(x + 1, y + 4, x + 8, y + 5, col);
            }
        }
        @Override public int titleColor() { return 0xEDE6FA; }
        @Override public int labelColor() { return 0xB47AE8; }
        @Override public int textColor() { return 0xC8C2D8; }
        @Override public int disabledColor() { return 0x5A5568; }
        @Override public int okColor() { return 0x7FE0A0; }
        @Override public int warnColor() { return 0xE05555; }
        @Override public boolean vanillaButtons() { return false; }
        @Override public boolean animated() { return false; }  // 直角风格无动画
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
        guiTop = Math.max(10, (height - GUI_HEIGHT) / 2);
        canEdit = minecraft != null && minecraft.player != null && minecraft.player.hasPermissions(2);
        blScroll = Math.max(0, blScroll);

        // ─── 模式单选（4个，12px行距，用勾选框样式） ───
        for (int i = 0; i < 4; i++) {
            modeRadios[i] = new ThemedRadio(guiLeft + 20, 28 + i * 12,
                    Component.translatable("gui.onlytp.mode_" + MODES[i]),
                    MODES[i].equals(selectedMode), canEdit);
        }

        // ─── 开关 ───
        showPauseToggle = new ThemedToggle(guiLeft + 164, 28,
                Component.translatable("gui.onlytp.toggle_show_button"), localShowPause, canEdit, false);
        styleToggle = new ThemedToggle(guiLeft + 164, 42,
                Component.translatable("gui.onlytp.toggle_vanilla_texture"), localUseVanilla, canEdit, false);

        // ─── 黑名单输入（仅 allow_tp_only / block_non_tp） ───
        playerNameInput = null;
        addButton = null;
        if (isBlacklistVisible() && canEdit) {
            playerNameInput = new EditBox(font, guiLeft + 16, yo(158), 176, 18,
                    Component.translatable("gui.onlytp.input_hint"));
            playerNameInput.setMaxLength(32);
            playerNameInput.setResponder(s -> updateAddButtonState());
            addRenderableWidget(playerNameInput);

            addButton = new ThemedButton(guiLeft + 200, yo(157), 55, 20,
                    Component.translatable("gui.onlytp.add"), b -> addPlayer(),
                    theme, ButtonRole.PRIMARY, font);
            addButton.active = false;
            addRenderableWidget(addButton);
        }

        // ─── 保存/取消 ───
        if (canEdit) {
            ThemedButton save = new ThemedButton(guiLeft + 160, yo(194), 55, 20,
                    Component.translatable("gui.onlytp.save"), b -> saveConfig(),
                    theme, ButtonRole.PRIMARY, font);
            addRenderableWidget(save);
        }
        ThemedButton cancel = new ThemedButton(guiLeft + 220, yo(194), 55, 20,
                Component.translatable("gui.onlytp.cancel"), b -> onClose(),
                theme, ButtonRole.NEUTRAL, font);
        addRenderableWidget(cancel);
    }

    // ═══════════ 渲染 ═══════════

    @Override
    public void render(GuiGraphics graphics, int mouseX, int mouseY, float partialTick) {
        renderBackground(graphics);

        // 主面板背景
        int panelH = yo(CONTENT_MAX_Y) + 14 - (yo(CONTENT_MIN_Y) - 4);
        theme.drawPanel(graphics, guiLeft, yo(CONTENT_MIN_Y) - 4, GUI_WIDTH, panelH);

        // ─── 已移除标题文字，上边距缩小 ───

        // ─── 模式卡片（左半，放大至76px高容纳4条） ───
        int cardLeftX = 8, cardLeftW = 146;
        theme.drawCard(graphics, guiLeft + cardLeftX, yo(14), cardLeftW, 76);
        graphics.drawString(font, Component.translatable("gui.onlytp.mode_label"),
                guiLeft + cardLeftX + 8, yo(18), theme.labelColor(), false);
        for (ThemedRadio r : modeRadios) r.render(graphics, font, theme, yo(r.relY), mouseX, mouseY);

        // ─── 开关卡片（右半） ───
        int cardRightX = 154, cardRightW = 138;
        theme.drawCard(graphics, guiLeft + cardRightX, yo(14), cardRightW, 76);
        graphics.drawString(font, Component.translatable("gui.onlytp.toggle_label"),
                guiLeft + cardRightX + 8, yo(18), theme.labelColor(), false);
        showPauseToggle.render(graphics, font, theme, yo(showPauseToggle.relY), mouseX, mouseY);
        styleToggle.render(graphics, font, theme, yo(styleToggle.relY), mouseX, mouseY);

        // ─── 黑名单卡片（仅 allow_tp_only / block_non_tp） ───
        hoveredRow = -1;
        if (isBlacklistVisible()) {
            int blCardY = 98;
            theme.drawCard(graphics, guiLeft + cardLeftX, yo(blCardY), GUI_WIDTH - 16, 88);
            graphics.drawString(font, Component.translatable("gui.onlytp.blacklist_label"),
                    guiLeft + 18, yo(blCardY + 4), theme.labelColor(), false);

            List<String> bl = getCurrentBlacklist();
            int listStartY = blCardY + 24;
            int visible = Math.min(MAX_VISIBLE_ITEMS, bl.size());
            for (int i = 0; i < visible; i++) {
                int idx = blScroll + i;
                if (idx >= bl.size()) break;
                int y = yo(listStartY + i * LIST_ITEM_H) - 6;
                boolean rowHover = canEdit && mouseX >= guiLeft + 14 && mouseX <= guiLeft + GUI_WIDTH - 22
                        && mouseY >= y - 2 && mouseY < y + LIST_ITEM_H - 2;
                if (rowHover) {
                    hoveredRow = idx;
                    graphics.fill(guiLeft + 14, y - 2, guiLeft + GUI_WIDTH - 22, y + LIST_ITEM_H - 2,
                            theme.vanillaButtons() ? 0x28000000 : 0x2AB47AE8);
                    // 红字提示"点击删除"
                    graphics.drawString(font, Component.translatable("gui.onlytp.click_to_remove"),
                            guiLeft + GUI_WIDTH - 65, y, 0xFF5555, false);
                }
                graphics.drawString(font, bl.get(idx),
                        guiLeft + 22, y, canEdit ? theme.textColor() : theme.disabledColor(), false);
            }
            // 已移除黑名单滚动条
        }

        // ─── 底部状态（与按钮对齐） ───
        int bottomY = 198;
        graphics.drawString(font,
                Component.translatable(canEdit ? "gui.onlytp.can_edit" : "gui.onlytp.view_only"),
                guiLeft + 15, yo(bottomY), canEdit ? theme.okColor() : theme.warnColor(), false);

        super.render(graphics, mouseX, mouseY, partialTick);
    }

    // ═══════════ 交互 ═══════════

    @Override
    public boolean mouseClicked(double mouseX, double mouseY, int button) {
        if (button == 0 && canEdit) {
            // 模式单选
            for (int i = 0; i < 4; i++) {
                if (modeRadios[i].isClicked(mouseX, mouseY, font, yo(modeRadios[i].relY))) {
                    if (!MODES[i].equals(selectedMode)) {
                        selectedMode = MODES[i];
                        blScroll = 0;
                        reloadWidgets();
                    }
                    return true;
                }
            }
            // 开关
            if (showPauseToggle.isClicked(mouseX, mouseY, font, yo(showPauseToggle.relY))) {
                localShowPause = !localShowPause;
                showPauseToggle.setChecked(localShowPause);
                return true;
            }
            if (styleToggle.isClicked(mouseX, mouseY, font, yo(styleToggle.relY))) {
                localUseVanilla = !localUseVanilla;
                theme = localUseVanilla ? VANILLA_THEME : MODERN_THEME;
                reloadWidgets();
                return true;
            }
            // 黑名单删除
            if (hoveredRow >= 0 && isBlacklistVisible()) {
                List<String> bl = getCurrentBlacklist();
                if (hoveredRow < bl.size()) {
                    bl.remove(hoveredRow);
                    blScroll = Mth.clamp(blScroll, 0, Math.max(0, bl.size() - MAX_VISIBLE_ITEMS));
                    hoveredRow = -1;
                    return true;
                }
            }
        }
        return super.mouseClicked(mouseX, mouseY, button);
    }

    @Override
    public boolean mouseScrolled(double mouseX, double mouseY, double delta) {
        return false; // 已禁用滚轮滚动
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
            case "disabled" -> blacklistDisabled;
            default -> blacklistAllowTp;
        };
    }

    private void addPlayer() {
        if (playerNameInput == null) return;
        String name = playerNameInput.getValue().trim();
        if (name.isEmpty()) return;
        List<String> bl = getCurrentBlacklist();
        if (bl.size() >= 20) { msg("gui.onlytp.blacklist_full"); return; }
        if (bl.contains(name)) { msg("gui.onlytp.already_in_blacklist"); return; }
        bl.add(name);
        playerNameInput.setValue("");
        blScroll = Math.max(0, bl.size() - MAX_VISIBLE_ITEMS);
    }

    private void msg(String key) {
        if (minecraft != null && minecraft.player != null)
            minecraft.player.displayClientMessage(Component.translatable(key), false);
    }

    private void updateAddButtonState() {
        if (addButton != null && playerNameInput != null)
            addButton.active = !playerNameInput.getValue().trim().isEmpty();
    }

    private void saveConfig() {
        if (!canEdit) return;
        NetworkHandler.CHANNEL.sendToServer(new ConfigUpdatePacket(
                selectedMode, localShowPause, localUseVanilla ? 1 : 0,
                blacklistAllowTp, blacklistBlockNonTp, blacklistDisabled));
        OnlyTPConfig.mode = selectedMode;
        OnlyTPConfig.showPauseButton = localShowPause;
        OnlyTPConfig.guiButtonStyle = localUseVanilla ? 1 : 0;
        OnlyTPConfig.blacklistAllowTp = new ArrayList<>(blacklistAllowTp);
        OnlyTPConfig.blacklistBlockNonTp = new ArrayList<>(blacklistBlockNonTp);
        OnlyTPConfig.blacklistDisabled = new ArrayList<>(blacklistDisabled);
        onClose();
    }

    @Override
    public boolean isPauseScreen() { return true; }

    // ═══════════ 控件：主题化按钮 ═══════════

    static final class ThemedButton extends net.minecraft.client.gui.components.Button {
        private final GuiTheme theme;
        private final ButtonRole role;
        private final Font btnFont;
        private final Anim hoverAnim = new Anim(0f);

        ThemedButton(int x, int y, int w, int h, Component label, OnPress onPress,
                     GuiTheme theme, ButtonRole role, Font font) {
            super(x, y, w, h, label, onPress, DEFAULT_NARRATION);
            this.theme = theme;
            this.role = role;
            this.btnFont = font;
        }

        @Override
        public void renderWidget(GuiGraphics g, int mouseX, int mouseY, float partialTick) {
            if (theme.vanillaButtons()) { super.renderWidget(g, mouseX, mouseY, partialTick); return; }
            hoverAnim.setTarget(isHoveredOrFocused() && active ? 1f : 0f);
            float hover = theme.animated() ? hoverAnim.tick(12f) : hoverAnim.target();
            theme.drawButton(g, getX(), getY(), getWidth(), getHeight(), hover, active, role);
            int tc = active ? 0xFFFFFF : theme.disabledColor();
            g.drawString(btnFont, getMessage(),
                    getX() + (getWidth() - btnFont.width(getMessage())) / 2,
                    getY() + (getHeight() - 8) / 2, tc, false);
        }
    }

    // ═══════════ 控件：单选点（改用勾选框同款样式） ═══════════

    static final class ThemedRadio {
        final int x, relY;
        final Component label;
        final boolean selected, enabled;

        ThemedRadio(int x, int relY, Component label, boolean selected, boolean enabled) {
            this.x = x; this.relY = relY;
            this.label = label; this.selected = selected; this.enabled = enabled;
        }

        void render(GuiGraphics g, Font font, GuiTheme theme, int y, int mouseX, int mouseY) {
            boolean hover = enabled && contains(mouseX, mouseY, font, y);
            // 使用勾选框同款样式（drawToggle，on=selected）
            theme.drawToggle(g, x, y, selected ? 1f : 0f, enabled);
            int tc = !enabled ? theme.disabledColor()
                    : (selected ? theme.titleColor() : (hover ? theme.titleColor() : theme.textColor()));
            // 统一留白间距（与 ThemedToggle 一致）
            g.drawString(font, label, x + 24, y + 1, tc, false);
        }

        boolean contains(double mx, double my, Font font, int y) {
            return mx >= x && mx <= x + 24 + font.width(label) && my >= y && my <= y + 10;
        }

        boolean isClicked(double mx, double my, Font font, int y) {
            return enabled && contains(mx, my, font, y);
        }
    }

    // ═══════════ 控件：滑动开关 ═══════════

    static final class ThemedToggle {
        final int x, relY;
        final Component label;
        final boolean enabled;
        private boolean checked;
        private final Anim slide;
        private final boolean animated;

        ThemedToggle(int x, int relY, Component label, boolean checked, boolean enabled, boolean animated) {
            this.x = x; this.relY = relY;
            this.label = label; this.checked = checked; this.enabled = enabled;
            this.animated = animated;
            this.slide = new Anim(checked ? 1f : 0f);
        }

        void setChecked(boolean c) { this.checked = c; this.slide.setTarget(c ? 1f : 0f); }

        void render(GuiGraphics g, Font font, GuiTheme theme, int y, int mouseX, int mouseY) {
            slide.setTarget(checked ? 1f : 0f);
            float on = animated ? slide.tick(11f) : slide.target();
            boolean hover = enabled && contains(mouseX, mouseY, font, y);
            theme.drawToggle(g, x, y, on, enabled);
            int tc = !enabled ? theme.disabledColor() : (hover ? theme.titleColor() : theme.textColor());
            g.drawString(font, label, x + 24, y + 1, tc, false);
        }

        boolean contains(double mx, double my, Font font, int y) {
            return mx >= x && mx <= x + 24 + font.width(label) && my >= y && my <= y + 10;
        }

        boolean isClicked(double mx, double my, Font font, int y) {
            return enabled && contains(mx, my, font, y);
        }
    }
}
