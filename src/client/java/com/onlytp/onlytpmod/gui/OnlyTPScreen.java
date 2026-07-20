package com.onlytp.onlytpmod.gui;

import com.onlytp.onlytpmod.config.OnlyTPConfig;
import com.onlytp.onlytpmod.network.ConfigUpdatePacket;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;
import net.minecraft.client.input.MouseButtonEvent;
import net.minecraft.client.input.KeyEvent;
import net.minecraft.client.input.CharacterEvent;
import net.minecraft.util.Mth;

import java.util.ArrayList;
import java.util.List;

/**
 * OnlyTP 配置界面 — 完全在 extractRenderState 中手动绘制，不依赖 Screen widget 管线。
 */
public class OnlyTPScreen extends Screen {

    private static final int W = 300, H = 260;
    private static final String[] MODE_KEYS = {"allow_tp_only", "block_non_tp", "both", "disabled"};

    private static final Theme VANILLA = new VanillaTheme();
    private static final Theme MODERN = new ModernTheme();

    private String mode;
    private boolean showPauseBtn, useVanillaStyle;
    private final List<String> blAllow = new ArrayList<>();
    private final List<String> blBlock = new ArrayList<>();
    private final List<String> blDisabled = new ArrayList<>();

    private boolean canEdit;
    private int left, top, scroll;
    private int hoverRow = -1;

    // ── 手动文本输入（替代 EditBox）──
    private int inputX, inputY, inputW = 160, inputH = 18;
    private String inputText = "";
    private boolean inputFocused;

    // 黑名单滚动条
    private static final int BL_SCROLLBAR_W = 4;
    private static final int BL_MAX_VISIBLE = 3;

    private final CheckItem[] modeItems = new CheckItem[4];
    private CheckItem chkPause, chkStyle;
    private Theme theme;

    // ── 按钮区域（用于鼠标点击检测）──
    private int btnSaveX, btnSaveY, btnAddX, btnAddY, btnCancelX, btnCancelY;
    private boolean btnSaveHovered, btnAddHovered, btnCancelHovered;
    private boolean btnAddActive;   // 添加按钮是否可点击

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
    //  主题 — 不变
    // ═══════════════════════════════════════════

    interface Theme {
        void drawPanel(GuiGraphicsExtractor g, int x, int y, int w, int h);
        void drawCard(GuiGraphicsExtractor g, int x, int y, int w, int h);
        void drawBox(GuiGraphicsExtractor g, int x, int y, boolean on, boolean enabled);
        void drawBtn(GuiGraphicsExtractor g, int x, int y, int w, int h, boolean active, boolean primary);
        int clrTitle();
        int clrLabel();
        int clrText();
        int clrDisabled();
        int clrOk();
        int clrWarn();
        boolean isVanilla();
    }

    static final class VanillaTheme implements Theme {
        @Override public void drawPanel(GuiGraphicsExtractor g, int x, int y, int w, int h) {
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
        @Override public void drawCard(GuiGraphicsExtractor g, int x, int y, int w, int h) {
            g.fill(x, y, x + w, y + h, 0xFF9D9D9D);
            g.fill(x, y, x + w, y + 1, 0xFF373737);
            g.fill(x, y, x + 1, y + h, 0xFF373737);
            g.fill(x, y + h - 1, x + w, y + h, 0xFFFFFFFF);
            g.fill(x + w - 1, y, x + w, y + h, 0xFFFFFFFF);
        }
        @Override public void drawBox(GuiGraphicsExtractor g, int x, int y, boolean on, boolean enabled) {
            drawCard(g, x, y, 10, 10);
            if (on) g.fill(x + 2, y + 2, x + 8, y + 8, enabled ? 0xFF202020 : 0xFF666666);
        }
        @Override public void drawBtn(GuiGraphicsExtractor g, int x, int y, int w, int h, boolean active, boolean primary) {
            if (!active) { g.fill(x, y, x + w, y + h, 0xFF5A5A5A); return; }
            // 原版按钮：灰底 + 白左上边框 + 黑右下边框
            g.fill(x, y, x + w, y + h, 0xFFC6C6C6);
            g.fill(x, y, x + w - 1, y + 1, 0xFFFFFFFF);
            g.fill(x, y, x + 1, y + h - 1, 0xFFFFFFFF);
            g.fill(x + 1, y + h - 1, x + w, y + h, 0xFF555555);
            g.fill(x + w - 1, y + 1, x + w, y + h, 0xFF555555);
        }
        @Override public int clrTitle() { return 0xFF404040; }
        @Override public int clrLabel() { return 0xFF404040; }
        @Override public int clrText() { return 0xFF303030; }
        @Override public int clrDisabled() { return 0xFF808080; }
        @Override public int clrOk() { return 0xFF2E7D32; }
        @Override public int clrWarn() { return 0xFFB02020; }
        @Override public boolean isVanilla() { return true; }
    }

    static final class ModernTheme implements Theme {
        @Override public void drawPanel(GuiGraphicsExtractor g, int x, int y, int w, int h) {
            g.fillGradient(x, y, x + w, y + h, 0xFF1A1428, 0xFF0D0A1A);
            g.fill(x + 3, y + 3, x + w + 3, y + h + 3, 0x44000000);
            g.fill(x, y, x + w, y + 1, 0xFF5A4A7A);
            g.fill(x, y + h - 1, x + w, y + h, 0xFF5A4A7A);
            g.fill(x, y, x + 1, y + h, 0xFF5A4A7A);
            g.fill(x + w - 1, y, x + w, y + h, 0xFF5A4A7A);
        }
        @Override public void drawCard(GuiGraphicsExtractor g, int x, int y, int w, int h) {
            g.fill(x, y, x + w, y + h, 0xFF16111E);
            g.fill(x, y, x + w, y + 1, 0xFF3A2D52);
            g.fill(x, y + h - 1, x + w, y + h, 0xFF3A2D52);
            g.fill(x, y, x + 1, y + h, 0xFF3A2D52);
            g.fill(x + w - 1, y, x + w, y + h, 0xFF3A2D52);
        }
        @Override public void drawBox(GuiGraphicsExtractor g, int x, int y, boolean on, boolean enabled) {
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
        @Override public void drawBtn(GuiGraphicsExtractor g, int x, int y, int w, int h, boolean active, boolean primary) {
            int c;
            if (!active) c = 0xFF2A2538;
            else if (primary) c = 0xFF4A2D7A;
            else c = 0xFF2E2A3E;
            g.fill(x, y, x + w, y + h, c);
        }
        @Override public int clrTitle() { return 0xFFEDE6FA; }
        @Override public int clrLabel() { return 0xFFB47AE8; }
        @Override public int clrText() { return 0xFFC8C2D8; }
        @Override public int clrDisabled() { return 0xFF5A5568; }
        @Override public int clrOk() { return 0xFF7FE0A0; }
        @Override public int clrWarn() { return 0xFFE05555; }
        @Override public boolean isVanilla() { return false; }
    }

    // ═══════════════════════════════════════════
    //  勾选条目
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

        void draw(GuiGraphicsExtractor g, Font f, Theme t) {
            t.drawBox(g, x, y, checked, enabled);
            int c = !enabled ? t.clrDisabled() : checked ? t.clrTitle() : t.clrText();
            g.text(f, text, x + 24, y + 1, c, !t.isVanilla());
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
    //  初始化
    // ═══════════════════════════════════════════

    @Override
    protected void init() {
        left = (width - W) / 2;
        top = Math.max(10, (height - H) / 2);
        canEdit = true; // GUI 设置界面，所有玩家均可编辑；服务器端 NetworkHandler 会校验 OP 权限
        scroll = Math.max(0, scroll);

        for (int i = 0; i < 4; i++)
            modeItems[i] = new CheckItem(left + 16, ay(28 + i * 12),
                    Component.translatable("gui.onlytp.mode_" + MODE_KEYS[i]),
                    MODE_KEYS[i].equals(mode), canEdit);

        chkPause = new CheckItem(left + 162, ay(28),
                Component.translatable("gui.onlytp.toggle_show_button"), showPauseBtn, canEdit);
        chkStyle = new CheckItem(left + 162, ay(42),
                Component.translatable("gui.onlytp.toggle_vanilla_texture"), useVanillaStyle, canEdit);

        // 记录输入框和按钮位置
        inputX = left + 8; inputY = ay(160);
        inputText = "";
        inputFocused = false;
        btnAddX = left + 172;  btnAddY = ay(159);
        btnSaveX = left + W - 118; btnSaveY = ay(194);
        btnCancelX = left + W - 58; btnCancelY = ay(194);
        btnAddActive = false;
    }

    // ═══════════════════════════════════════════
    //  渲染 — 手动绘制所有元素
    // ═══════════════════════════════════════════

    @Override
    public void extractRenderState(GuiGraphicsExtractor g, int mx, int my, float pt) {
        boolean shadow = !theme.isVanilla();

        // 1. 画主面板
        theme.drawPanel(g, left, ay(6), W, ay(232) - ay(6));

        // 2. 模式选择卡
        theme.drawCard(g, left + 8, ay(14), 146, 76);
        g.text(font, Component.translatable("gui.onlytp.mode_label"),
                left + 16, ay(18), theme.clrLabel(), shadow);
        for (CheckItem item : modeItems) item.draw(g, font, theme);

        // 3. 开关卡
        theme.drawCard(g, left + 154, ay(14), 138, 76);
        g.text(font, Component.translatable("gui.onlytp.toggle_label"),
                left + 162, ay(18), theme.clrLabel(), shadow);
        chkPause.draw(g, font, theme);
        chkStyle.draw(g, font, theme);

        // 4. 黑名单
        hoverRow = -1;
        if (showBlacklist()) drawBlacklistArea(g, mx, my);

        // 5. 手动绘制输入框
        if (canEdit && showBlacklist()) {
            drawInputBox(g, mx, my);
        }

        // 6. 手动渲染按钮
        if (canEdit && showBlacklist()) {
            drawButton(g, btnAddX, btnAddY, 55, 20, Component.translatable("gui.onlytp.add"),
                    btnAddActive, true, mx, my);
        }
        if (canEdit) {
            drawButton(g, btnSaveX, btnSaveY, 55, 20, Component.translatable("gui.onlytp.save"),
                    true, true, mx, my);
        }
        drawButton(g, btnCancelX, btnCancelY, 55, 20, Component.translatable("gui.onlytp.cancel"),
                true, false, mx, my);

        // 7. 状态文字
        g.text(font,
                Component.translatable(canEdit ? "gui.onlytp.can_edit" : "gui.onlytp.view_only"),
                left + 8, ay(198), canEdit ? theme.clrOk() : theme.clrWarn(), shadow);
    }

    private void drawButton(GuiGraphicsExtractor g, int x, int y, int w, int h,
                            Component label, boolean active, boolean primary, int mx, int my) {
        // 检测悬停
        boolean hover = mx >= x && mx <= x + w && my >= y && my <= y + h;
        // 背景
        theme.drawBtn(g, x, y, w, h, active, primary);
        // 悬停高亮
        if (hover && active) {
            g.fill(x, y, x + w, y + h, 0x33FFFFFF);
        }
        // 文字
        int c = active ? (theme.isVanilla() ? 0xFF303030 : 0xFFFFFFFF) : theme.clrDisabled();
        g.text(font, label,
                x + (w - font.width(label)) / 2,
                y + (h - 8) / 2, c, !theme.isVanilla());
    }

    private void drawBlacklistArea(GuiGraphicsExtractor g, int mx, int my) {
        int bx = left + 8, by = ay(98), bw = W - 16, bh = 56;
        theme.drawCard(g, bx, by, bw, bh);
        boolean shadow = !theme.isVanilla();
        g.text(font, Component.translatable("gui.onlytp.blacklist_label"),
                bx + 10, by + 4, theme.clrLabel(), shadow);

        List<String> bl = curBl();
        int listTop = by + 14;
        int listH = bh - 14;
        int rowH = 14;
        int vis = Math.min(BL_MAX_VISIBLE, bl.size());
        int maxScroll = Math.max(0, bl.size() - BL_MAX_VISIBLE);
        scroll = Mth.clamp(scroll, 0, maxScroll);

        // 滚动条轨道
        int sbx = bx + bw - BL_SCROLLBAR_W;
        g.fill(sbx, listTop, sbx + BL_SCROLLBAR_W, listTop + listH,
                theme.isVanilla() ? 0xFF666666 : 0xFF2A2538);
        // 滚动条滑块
        if (bl.size() > BL_MAX_VISIBLE) {
            int thumbH = Math.max(8, listH * BL_MAX_VISIBLE / bl.size());
            int thumbY = listTop + (listH - thumbH) * scroll / maxScroll;
            g.fill(sbx, thumbY, sbx + BL_SCROLLBAR_W, thumbY + thumbH,
                    theme.isVanilla() ? 0xFFAAAAAA : 0xFF5A4A7A);
        }

        for (int i = 0; i < vis; i++) {
            int idx = scroll + i;
            if (idx >= bl.size()) break;
            int ry = listTop + i * rowH;
            boolean hov = canEdit && mx >= bx + 4 && mx <= sbx
                    && my >= ry && my < ry + rowH;
            if (hov) {
                hoverRow = idx;
                g.fill(bx + 4, ry, sbx, ry + rowH,
                        theme.isVanilla() ? 0x28000000 : 0x2AB47AE8);
            }
            g.text(font, bl.get(idx), bx + 12, ry + 3,
                    canEdit ? theme.clrText() : theme.clrDisabled(), !theme.isVanilla());
            if (canEdit && hov) {
                g.text(font, Component.translatable("gui.onlytp.click_to_remove"),
                        sbx - 50, ry + 3, 0xFFFF5555, !theme.isVanilla());
            }
        }
    }

    private void drawInputBox(GuiGraphicsExtractor g, int mx, int my) {
        // 黑色边框 + 白色背景
        g.fill(inputX, inputY, inputX + inputW, inputY + inputH, 0xFF000000);
        g.fill(inputX + 1, inputY + 1, inputX + inputW - 1, inputY + inputH - 1, 0xFFFFFFFF);
        // 文字
        String display = inputText.isEmpty() ? "..." : inputText;
        g.text(font, display, inputX + 3, inputY + 4, 0xFF303030, false);
        // 光标闪烁
        if (inputFocused && ((int)(System.currentTimeMillis() / 500) % 2 == 0)) {
            int cx = inputX + 3 + font.width(inputText);
            if (cx < inputX + inputW - 2)
                g.fill(cx, inputY + 2, cx + 1, inputY + inputH - 2, 0xFF000000);
        }
    }

    // ═══════════════════════════════════════════
    //  交互
    // ═══════════════════════════════════════════

    @Override
    public boolean mouseClicked(MouseButtonEvent event, boolean handled) {
        double mx = event.x();
        double my = event.y();
        int btn = event.button();
        if (btn == 0 && canEdit) {
            for (int i = 0; i < 4; i++) {
                if (modeItems[i].hit(mx, my, font)) {
                    if (!MODE_KEYS[i].equals(mode)) {
                        mode = MODE_KEYS[i];
                        scroll = 0;
                        init(); // 重新初始化
                    }
                    return true;
                }
            }
            if (chkPause.hit(mx, my, font)) {
                showPauseBtn = !showPauseBtn;
                chkPause.checked = showPauseBtn;
                return true;
            }
            if (chkStyle.hit(mx, my, font)) {
                useVanillaStyle = !useVanillaStyle;
                chkStyle.checked = useVanillaStyle;
                theme = useVanillaStyle ? VANILLA : MODERN;
                init();
                return true;
            }
            // 手动输入框聚焦
            if (showBlacklist()) {
                boolean onInput = mx >= inputX && mx <= inputX + inputW
                        && my >= inputY && my <= inputY + inputH;
                inputFocused = onInput;
                if (onInput) { return true; }
            }
            // 黑名单点击删除（排除输入框区域）
            if (hoverRow >= 0 && showBlacklist() && canEdit) {
                List<String> bl = curBl();
                if (hoverRow < bl.size()) {
                    bl.remove(hoverRow);
                    scroll = Mth.clamp(scroll, 0, Math.max(0, bl.size() - BL_MAX_VISIBLE));
                    hoverRow = -1;
                    return true;
                }
            }
            // 按钮点击检测
            if (showBlacklist() && isHit(mx, my, btnAddX, btnAddY, 55, 20) && btnAddActive) {
                doAdd();
                return true;
            }
            if (isHit(mx, my, btnSaveX, btnSaveY, 55, 20)) {
                doSave();
                return true;
            }
            if (isHit(mx, my, btnCancelX, btnCancelY, 55, 20)) {
                onClose();
                return true;
            }
        }
        return super.mouseClicked(event, handled);
    }

    @Override
    public boolean keyPressed(KeyEvent event) {
        if (inputFocused) {
            int kc = event.key();
            if (kc == 259) { // BACKSPACE
                if (!inputText.isEmpty()) {
                    inputText = inputText.substring(0, inputText.length() - 1);
                    btnAddActive = !inputText.trim().isEmpty();
                }
                return true;
            }
            if (kc == 257) { // ENTER → 添加
                if (btnAddActive) doAdd();
                return true;
            }
            if (kc == 256) { // ESC → 取消聚焦
                inputFocused = false;
                return true;
            }
        }
        return super.keyPressed(event);
    }

    @Override
    public boolean charTyped(CharacterEvent event) {
        if (inputFocused) {
            char c = (char) event.codepoint();
            if (inputText.length() < 32 && c >= ' ' && c != 127) {
                inputText += c;
                btnAddActive = !inputText.trim().isEmpty();
            }
            return true;
        }
        return super.charTyped(event);
    }

    private boolean isHit(double mx, double my, int x, int y, int w, int h) {
        return mx >= x && mx <= x + w && my >= y && my <= y + h;
    }

    @Override
    public boolean mouseScrolled(double mx, double my, double dx, double dy) {
        if (showBlacklist() && canEdit && mx >= left + 8 && mx <= left + W - 8
                && my >= ay(98) && my <= ay(98) + 56) {
            List<String> bl = curBl();
            int maxScroll = Math.max(0, bl.size() - BL_MAX_VISIBLE);
            scroll = Mth.clamp(scroll - (int)dy, 0, maxScroll);
            return true;
        }
        return super.mouseScrolled(mx, my, dx, dy);
    }

    // ═══════════════════════════════════════════
    //  业务逻辑
    // ═══════════════════════════════════════════

    private void doAdd() {
        String name = inputText.trim();
        if (name.isEmpty()) return;
        List<String> bl = curBl();
        if (bl.size() >= 20) { notify("gui.onlytp.blacklist_full"); return; }
        if (bl.contains(name)) { notify("gui.onlytp.already_in_blacklist"); return; }
        bl.add(name);
        inputText = "";
        btnAddActive = false;
        scroll = Mth.clamp(bl.size() - BL_MAX_VISIBLE, 0, bl.size());
    }

    private void doSave() {
        if (!canEdit) return;
        ClientPlayNetworking.send(new ConfigUpdatePacket(
                mode, showPauseBtn, useVanillaStyle ? 1 : 0,
                blAllow, blBlock, blDisabled));
        OnlyTPConfig.mode = mode;
        OnlyTPConfig.showPauseButton = showPauseBtn;
        OnlyTPConfig.guiButtonStyle = useVanillaStyle ? 1 : 0;
        OnlyTPConfig.blacklistAllowTp = new ArrayList<>(blAllow);
        OnlyTPConfig.blacklistBlockNonTp = new ArrayList<>(blBlock);
        OnlyTPConfig.blacklistDisabled = new ArrayList<>(blDisabled);
        OnlyTPConfig.save();
        onClose();
    }

    private void notify(String key) {
        if (minecraft != null && minecraft.player != null)
            minecraft.player.sendSystemMessage(Component.translatable(key));
    }

    @Override
    public boolean isPauseScreen() { return false; }
}
