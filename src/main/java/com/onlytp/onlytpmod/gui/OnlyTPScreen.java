package com.onlytp.onlytpmod.gui;

import com.mojang.blaze3d.systems.RenderSystem;
import com.onlytp.onlytpmod.config.OnlyTPConfig;
import com.onlytp.onlytpmod.network.ConfigUpdatePacket;
import net.neoforged.neoforge.network.PacketDistributor;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.components.EditBox;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;
import net.minecraft.util.Mth;

import java.util.ArrayList;
import java.util.List;

/**
 * OnlyTP 配置界面 — 支持原版箱子风格与现代末影紫风格。
 */
public class OnlyTPScreen extends Screen {

    // ── 尺寸 ──
    private static final int W = 300, H = 260;
    private static final int BTN_W = 55, BTN_H = 20;

    // ── 模式 ──
    private static final String[] MODE_KEYS = {"allow_tp_only", "block_non_tp", "both", "disabled"};

    // ── 主题实例 ──
    private static final Theme VANILLA = new VanillaTheme();
    private static final Theme MODERN = new ModernTheme();

    // ═══════════════════════════════════════════
    //  状态
    // ═══════════════════════════════════════════

    private String mode;
    private boolean showPauseBtn, useVanillaStyle;
    private final List<String> blAllow = new ArrayList<>();
    private final List<String> blBlock = new ArrayList<>();
    private final List<String> blDisabled = new ArrayList<>();

    private boolean canEdit;
    private int left, top, scroll;
    private int hoverRow = -1;

    // ═══════════════════════════════════════════
    //  控件引用
    // ═══════════════════════════════════════════

    private EditBox inputName;
    private Button btnAdd, btnSave, btnCancel;
    private final CheckItem[] modeItems = new CheckItem[4];
    private CheckItem chkPause, chkStyle;
    private Theme theme;

    // ═══════════════════════════════════════════
    //  构造
    // ═══════════════════════════════════════════

    public OnlyTPScreen() {
        super(Component.translatable("gui.onlytp.title"));
        mode = OnlyTPConfig.mode;
        showPauseBtn = OnlyTPConfig.showPauseButton;
        useVanillaStyle = OnlyTPConfig.guiButtonStyle == 1;
        theme = useVanillaStyle ? VANILLA : MODERN;
        blAllow.addAll(OnlyTPConfig.blacklistAllowTp);
        blBlock.addAll(OnlyTPConfig.blacklistBlockNonTp);
        blDisabled.addAll(OnlyTPConfig.blacklistDisabled);
    }

    // ═══════════════════════════════════════════
    //  主题接口
    // ═══════════════════════════════════════════

    interface Theme {
        void drawPanel(GuiGraphics g, int x, int y, int w, int h);
        void drawCard(GuiGraphics g, int x, int y, int w, int h);
        void drawBox(GuiGraphics g, int x, int y, boolean on, boolean enabled);
        void drawBtn(GuiGraphics g, int x, int y, int w, int h, boolean active, boolean primary);
        int clrTitle();
        int clrLabel();
        int clrText();
        int clrDisabled();
        int clrOk();
        int clrWarn();
        boolean isVanilla();
    }

    // ── 原版箱子风格 ──
    static final class VanillaTheme implements Theme {
        @Override public void drawPanel(GuiGraphics g, int x, int y, int w, int h) {
            g.fill(x - 1, y - 1, x + w + 1, y + h + 1, 0xFF000000);
            g.fill(x, y, x + w, y + h, 0xFFC6C6C6);
            g.fill(x, y, x + w - 2, y + 2, 0xFFFFFFFF);
            g.fill(x, y, x + 2, y + h - 2, 0xFFFFFFFF);
            g.fill(x + 2, y + h - 2, x + w, y + h, 0xFF555555);
            g.fill(x + w - 2, y + 2, x + w, y + h, 0xFF555555);
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
        @Override public void drawBox(GuiGraphics g, int x, int y, boolean on, boolean enabled) {
            drawCard(g, x + 4, y, 10, 10);
            if (on) g.fill(x + 6, y + 2, x + 12, y + 8, enabled ? 0xFF202020 : 0xFF666666);
        }
        @Override public void drawBtn(GuiGraphics g, int x, int y, int w, int h, boolean active, boolean primary) {}
        @Override public int clrTitle() { return 0x404040; }
        @Override public int clrLabel() { return 0x404040; }
        @Override public int clrText() { return 0x303030; }
        @Override public int clrDisabled() { return 0x808080; }
        @Override public int clrOk() { return 0x2E7D32; }
        @Override public int clrWarn() { return 0xB02020; }
        @Override public boolean isVanilla() { return true; }
    }

    // ── 现代末影紫风格 ──
    static final class ModernTheme implements Theme {
        @Override public void drawPanel(GuiGraphics g, int x, int y, int w, int h) {
            g.fillGradient(x, y, x + w, y + h, 0xFF1A1428, 0xFF0D0A1A);
            RenderSystem.enableBlend();
            g.fill(x + 3, y + 3, x + w + 3, y + h + 3, 0x44000000);
            RenderSystem.disableBlend();
            g.fill(x, y, x + w, y + 1, 0xFF5A4A7A);
            g.fill(x, y + h - 1, x + w, y + h, 0xFF5A4A7A);
            g.fill(x, y, x + 1, y + h, 0xFF5A4A7A);
            g.fill(x + w - 1, y, x + w, y + h, 0xFF5A4A7A);
        }
        @Override public void drawCard(GuiGraphics g, int x, int y, int w, int h) {
            g.fill(x, y, x + w, y + h, 0xFF16111E);
            g.fill(x, y, x + w, y + 1, 0xFF3A2D52);
            g.fill(x, y + h - 1, x + w, y + h, 0xFF3A2D52);
            g.fill(x, y, x + 1, y + h, 0xFF3A2D52);
            g.fill(x + w - 1, y, x + w, y + h, 0xFF3A2D52);
        }
        @Override public void drawBox(GuiGraphics g, int x, int y, boolean on, boolean enabled) {
            int bg = enabled ? 0xFF2E2A3E : 0xFF1A1628;
            int fill = enabled ? 0xFFB47AE8 : 0xFF5A5568;
            int border = enabled ? 0xFF5A4A7A : 0xFF3A2D52;
            g.fill(x, y, x + 10, y + 10, bg);
            g.fill(x, y, x + 10, y + 1, border);
            g.fill(x, y + 9, x + 10, y + 10, border);
            g.fill(x, y, x + 1, y + 10, border);
            g.fill(x + 9, y, x + 10, y + 10, border);
            if (on) g.fill(x + 2, y + 2, x + 8, y + 8, fill);
        }
        @Override public void drawBtn(GuiGraphics g, int x, int y, int w, int h, boolean active, boolean primary) {
            int c;
            if (!active) c = 0xFF2A2538;
            else if (primary) c = 0xFF4A2D7A;
            else c = 0xFF2E2A3E;
            g.fill(x, y, x + w, y + h, c);
        }
        @Override public int clrTitle() { return 0xEDE6FA; }
        @Override public int clrLabel() { return 0xB47AE8; }
        @Override public int clrText() { return 0xC8C2D8; }
        @Override public int clrDisabled() { return 0x5A5568; }
        @Override public int clrOk() { return 0x7FE0A0; }
        @Override public int clrWarn() { return 0xE05555; }
        @Override public boolean isVanilla() { return false; }
    }

    // ═══════════════════════════════════════════
    //  勾选条目（单选 / 开关共用）
    // ═══════════════════════════════════════════

    static final class CheckItem {
        final int x, y;
        final Component text;
        final boolean enabled;
        boolean checked;

        CheckItem(int x, int y, Component text, boolean checked, boolean enabled) {
            this.x = x; this.y = y; this.text = text; this.checked = checked; this.enabled = enabled;
        }

        boolean hit(double mx, double my, Font f) {
            return enabled && mx >= x && mx <= x + 24 + f.width(text) && my >= y && my <= y + 10;
        }

        void draw(GuiGraphics g, Font f, Theme t) {
            t.drawBox(g, x, y, checked, enabled);
            int c = !enabled ? t.clrDisabled()
                    : checked ? t.clrTitle() : t.clrText();
            g.drawString(f, text, x + 24, y + 1, c, !t.isVanilla());
        }
    }

    // ═══════════════════════════════════════════
    //  布局辅助
    // ═══════════════════════════════════════════

    private int ay(int rel) { return top + rel; }

    private boolean showBlacklist() {
        return "allow_tp_only".equals(mode) || "block_non_tp".equals(mode);
    }

    private List<String> curBl() {
        return switch (mode) {
            case "block_non_tp" -> blBlock;
            case "disabled" -> blDisabled;
            default -> blAllow;
        };
    }

    // ═══════════════════════════════════════════
    //  初始化控件
    // ═══════════════════════════════════════════

    @Override
    protected void init() {
        left = (width - W) / 2;
        top = Math.max(10, (height - H) / 2);
        canEdit = minecraft != null && minecraft.player != null && minecraft.player.hasPermissions(2);
        scroll = Math.max(0, scroll);

        // 模式选项 (4行, 行距12)
        for (int i = 0; i < 4; i++)
            modeItems[i] = new CheckItem(left + 20, ay(28 + i * 12),
                    Component.translatable("gui.onlytp.mode_" + MODE_KEYS[i]),
                    MODE_KEYS[i].equals(mode), canEdit);

        // 开关
        chkPause = new CheckItem(left + 164, ay(28),
                Component.translatable("gui.onlytp.toggle_show_button"), showPauseBtn, canEdit);
        chkStyle = new CheckItem(left + 164, ay(42),
                Component.translatable("gui.onlytp.toggle_vanilla_texture"), useVanillaStyle, canEdit);

        // 黑名单输入区
        clearWidgets();
        inputName = null;
        btnAdd = null;

        if (showBlacklist() && canEdit) {
            inputName = new EditBox(font, left + 16, ay(158), 176, 18,
                    Component.translatable("gui.onlytp.input_hint"));
            inputName.setMaxLength(32);
            inputName.setResponder(s -> {
                if (btnAdd != null) btnAdd.active = !inputName.getValue().trim().isEmpty();
            });
            addRenderableWidget(inputName);

            btnAdd = makeBtn(left + 200, ay(157), "gui.onlytp.add", ButtonRole.PRIMARY, b -> doAdd());
            btnAdd.active = false;
            addRenderableWidget(btnAdd);
        }

        if (canEdit) {
            btnSave = makeBtn(left + 160, ay(194), "gui.onlytp.save", ButtonRole.PRIMARY, b -> doSave());
            addRenderableWidget(btnSave);
        }

        btnCancel = makeBtn(left + 220, ay(194), "gui.onlytp.cancel", ButtonRole.NEUTRAL, b -> onClose());
        addRenderableWidget(btnCancel);
    }

    private Button makeBtn(int x, int y, String key, ButtonRole role, Button.OnPress action) {
        return new ThemeBtn(x, y, BTN_W, BTN_H, Component.translatable(key), action, theme, role, font);
    }

    enum ButtonRole { PRIMARY, NEUTRAL }

    // ═══════════════════════════════════════════
    //  渲染
    // ═══════════════════════════════════════════

    @Override
    public void renderBackground(GuiGraphics g, int mx, int my, float pt) {
        // 只绘制纯黑色背景，避免重影
        RenderSystem.disableBlend();
        g.fill(0, 0, width, height, 0xFF000000);
        RenderSystem.enableBlend();
    }

    @Override
    public void render(GuiGraphics g, int mx, int my, float pt) {
        // 1. 绘制背景
        renderBackground(g, mx, my, pt);

        // 原版风格不使用阴影，避免文字重影
        boolean shadow = !theme.isVanilla();

        // 2. 主面板
        theme.drawPanel(g, left, ay(6), W, ay(232) - ay(6));

        // 左卡片 — 模式
        theme.drawCard(g, left + 8, ay(14), 146, 76);
        g.drawString(font, Component.translatable("gui.onlytp.mode_label"),
                left + 16, ay(18), theme.clrLabel(), shadow);
        for (CheckItem item : modeItems) item.draw(g, font, theme);

        // 右卡片 — 开关
        theme.drawCard(g, left + 154, ay(14), 138, 76);
        g.drawString(font, Component.translatable("gui.onlytp.toggle_label"),
                left + 162, ay(18), theme.clrLabel(), shadow);
        chkPause.draw(g, font, theme);
        chkStyle.draw(g, font, theme);

        // 黑名单卡片
        hoverRow = -1;
        if (showBlacklist()) drawBlacklist(g, mx, my);

        // 底部状态
        g.drawString(font,
                Component.translatable(canEdit ? "gui.onlytp.can_edit" : "gui.onlytp.view_only"),
                left + 15, ay(198), canEdit ? theme.clrOk() : theme.clrWarn(), shadow);

        // 3. 手动渲染所有控件（输入框、按钮），确保在最上层
        for (var widget : this.renderables) {
            widget.render(g, mx, my, pt);
        }
    }

    private void drawBlacklist(GuiGraphics g, int mx, int my) {
        theme.drawCard(g, left + 8, ay(98), W - 16, 88);
        boolean shadow = !theme.isVanilla();
        g.drawString(font, Component.translatable("gui.onlytp.blacklist_label"),
                left + 18, ay(102), theme.clrLabel(), shadow);

        List<String> bl = curBl();
        int vis = Math.min(4, bl.size());
        for (int i = 0; i < vis; i++) {
            int idx = scroll + i;
            if (idx >= bl.size()) break;
            int ry = ay(122 + i * 14) - 6;
            boolean hov = canEdit && mx >= left + 14 && mx <= left + W - 22
                    && my >= ry - 2 && my < ry + 12;
            if (hov) {
                hoverRow = idx;
                g.fill(left + 14, ry - 2, left + W - 22, ry + 12,
                        theme.isVanilla() ? 0x28000000 : 0x2AB47AE8);
                g.drawString(font, Component.translatable("gui.onlytp.click_to_remove"),
                        left + W - 65, ry, 0xFF5555, !theme.isVanilla());
            }
            g.drawString(font, bl.get(idx), left + 22, ry,
                    canEdit ? theme.clrText() : theme.clrDisabled(), !theme.isVanilla());
        }
    }

    // ═══════════════════════════════════════════
    //  交互
    // ═══════════════════════════════════════════

    @Override
    public boolean mouseClicked(double mx, double my, int btn) {
        if (btn == 0 && canEdit) {
            // 模式选择
            for (int i = 0; i < 4; i++) {
                if (modeItems[i].hit(mx, my, font)) {
                    if (!MODE_KEYS[i].equals(mode)) {
                        mode = MODE_KEYS[i];
                        scroll = 0;
                        rebuild();
                    }
                    return true;
                }
            }
            // 开关
            if (chkPause.hit(mx, my, font)) {
                showPauseBtn = !showPauseBtn;
                chkPause.checked = showPauseBtn;
                return true;
            }
            if (chkStyle.hit(mx, my, font)) {
                useVanillaStyle = !useVanillaStyle;
                chkStyle.checked = useVanillaStyle;
                theme = useVanillaStyle ? VANILLA : MODERN;
                rebuild();
                return true;
            }
            // 黑名单删除
            if (hoverRow >= 0 && showBlacklist()) {
                List<String> bl = curBl();
                if (hoverRow < bl.size()) {
                    bl.remove(hoverRow);
                    scroll = Mth.clamp(scroll, 0, Math.max(0, bl.size() - 4));
                    hoverRow = -1;
                    return true;
                }
            }
        }
        return super.mouseClicked(mx, my, btn);
    }

    @Override
    public boolean mouseScrolled(double mx, double my, double dx, double dy) {
        return false;
    }

    // ═══════════════════════════════════════════
    //  业务逻辑
    // ═══════════════════════════════════════════

    private void rebuild() {
        String saved = inputName != null ? inputName.getValue() : "";
        clearWidgets();
        init();
        if (inputName != null) inputName.setValue(saved);
    }

    private void doAdd() {
        if (inputName == null) return;
        String name = inputName.getValue().trim();
        if (name.isEmpty()) return;
        List<String> bl = curBl();
        if (bl.size() >= 20) { notify("gui.onlytp.blacklist_full"); return; }
        if (bl.contains(name)) { notify("gui.onlytp.already_in_blacklist"); return; }
        bl.add(name);
        inputName.setValue("");
        scroll = Math.max(0, bl.size() - 4);
    }

    private void doSave() {
        if (!canEdit) return;
        PacketDistributor.sendToServer(new ConfigUpdatePacket(
                mode, showPauseBtn, useVanillaStyle ? 1 : 0,
                blAllow, blBlock, blDisabled));
        OnlyTPConfig.mode = mode;
        OnlyTPConfig.showPauseButton = showPauseBtn;
        OnlyTPConfig.guiButtonStyle = useVanillaStyle ? 1 : 0;
        OnlyTPConfig.blacklistAllowTp = new ArrayList<>(blAllow);
        OnlyTPConfig.blacklistBlockNonTp = new ArrayList<>(blBlock);
        OnlyTPConfig.blacklistDisabled = new ArrayList<>(blDisabled);
        onClose();
    }

    private void notify(String key) {
        if (minecraft != null && minecraft.player != null)
            minecraft.player.displayClientMessage(Component.translatable(key), false);
    }

    @Override
    public boolean isPauseScreen() { return false; }

    // ═══════════════════════════════════════════
    //  主题按钮
    // ═══════════════════════════════════════════

    static final class ThemeBtn extends Button {
        private final Theme theme;
        private final ButtonRole role;
        private final Font f;

        ThemeBtn(int x, int y, int w, int h, Component label, OnPress action,
                 Theme theme, ButtonRole role, Font font) {
            super(x, y, w, h, label, action, DEFAULT_NARRATION);
            this.theme = theme;
            this.role = role;
            this.f = font;
        }

        @Override
        public void renderWidget(GuiGraphics g, int mx, int my, float pt) {
            if (theme.isVanilla()) {
                super.renderWidget(g, mx, my, pt);
                return;
            }
            theme.drawBtn(g, getX(), getY(), getWidth(), getHeight(), active, role == ButtonRole.PRIMARY);
            int c = active ? 0xFFFFFF : theme.clrDisabled();
            g.drawString(f, getMessage(),
                    getX() + (getWidth() - f.width(getMessage())) / 2,
                    getY() + (getHeight() - 8) / 2, c, false);
        }
    }
}
