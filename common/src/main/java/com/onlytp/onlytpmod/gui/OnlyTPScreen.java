package com.onlytp.onlytpmod.gui;

import com.avalon.base.gui.theme.GuiTheme;
import com.avalon.base.gui.theme.ModernTheme;
import com.avalon.base.gui.theme.ThemedButton;
import com.avalon.base.gui.theme.ThemedRadio;
import com.avalon.base.gui.theme.ThemedToggle;
import com.avalon.base.gui.theme.VanillaTheme;
import com.onlytp.onlytpmod.AvalonLink;
import com.onlytp.onlytpmod.config.OnlyTPConfig;
import com.onlytp.onlytpmod.network.ConfigUpdatePacket;
import com.avalon.base.network.AvalonNetwork;
import com.onlytp.onlytpmod.network.NetworkChannels;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.EditBox;
import net.minecraft.client.resources.sounds.SimpleSoundInstance;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.util.Mth;
import org.lwjgl.glfw.GLFW;

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
    private static final GuiTheme MODERN_THEME = new ModernTheme();

    // ─── 配置状态 ───
    private String selectedMode;
    private boolean localShowPause;
    private boolean localUseVanilla;
    private final List<String> blacklistAllowTp = new ArrayList<>();
    private final List<String> blacklistBlockNonTp = new ArrayList<>();

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
    private long handCursor;
    private long arrowCursor;

    private static final String[] MODES = {"allow_tp_only", "block_non_tp", "both", "disabled"};

    public OnlyTPScreen() {
        super(Component.translatable("gui.onlytp.title"));
        this.selectedMode = OnlyTPConfig.mode;
        this.localShowPause = OnlyTPConfig.showPauseButton;
        this.localUseVanilla = OnlyTPConfig.guiButtonStyle == 1;
        this.theme = localUseVanilla ? VANILLA_THEME : MODERN_THEME;
        this.blacklistAllowTp.addAll(OnlyTPConfig.blacklistAllowTp);
        this.blacklistBlockNonTp.addAll(OnlyTPConfig.blacklistBlockNonTp);
        this.handCursor = GLFW.glfwCreateStandardCursor(GLFW.GLFW_HAND_CURSOR);
        this.arrowCursor = GLFW.glfwCreateStandardCursor(GLFW.GLFW_ARROW_CURSOR);
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

        // ─── 保存/取消 ───
        if (canEdit) {
            ThemedButton save = new ThemedButton(guiLeft + GUI_WIDTH - 118, yo(194), 55, 20,
                    Component.translatable("gui.onlytp.save"), b -> saveConfig(),
                    theme, GuiTheme.ButtonRole.PRIMARY, font);
            addRenderableWidget(save);
        }
        ThemedButton cancel = new ThemedButton(guiLeft + GUI_WIDTH - 58, yo(194), 55, 20,
                Component.translatable("gui.onlytp.cancel"), b -> onClose(),
                theme, GuiTheme.ButtonRole.NEUTRAL, font);
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

        // ─── 黑名单卡片（仅 allow_tp_only / block_non_tp，56px高，可见3行） ───
        hoveredRow = -1;
        if (isBlacklistVisible()) {
            int blCardY = 98;
            theme.drawCard(graphics, guiLeft + cardLeftX, yo(blCardY), GUI_WIDTH - 16, 64);
            graphics.drawString(font, Component.translatable("gui.onlytp.blacklist_label"),
                    guiLeft + 18, yo(blCardY + 4), theme.labelColor(), false);

            List<String> bl = getCurrentBlacklist();
            int listStartY = blCardY + 20;
            int visible = Math.min(MAX_VISIBLE_ITEMS, bl.size());
            for (int i = 0; i < visible; i++) {
                int idx = blScroll + i;
                if (idx >= bl.size()) break;
                int y = yo(listStartY + i * LIST_ITEM_H) - 6;
                boolean rowHover = canEdit && mouseX >= guiLeft + 12 && mouseX <= guiLeft + GUI_WIDTH - 28
                        && mouseY >= y - 2 && mouseY < y + LIST_ITEM_H - 2;
                if (rowHover) {
                    hoveredRow = idx;
                    graphics.fill(guiLeft + 12, y - 2, guiLeft + GUI_WIDTH - 28, y + LIST_ITEM_H - 2,
                            theme.vanillaButtons() ? 0x28000000 : 0x2AB47AE8);
                    // 红字提示"点击删除"
                    graphics.drawString(font, Component.translatable("gui.onlytp.click_to_remove"),
                            guiLeft + GUI_WIDTH - 65, y, 0xFF5555, false);
                }
                graphics.drawString(font, bl.get(idx),
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
        graphics.drawString(font,
                Component.translatable(canEdit ? "gui.onlytp.can_edit" : "gui.onlytp.view_only"),
                guiLeft + 8, yo(bottomY), canEdit ? theme.okColor() : theme.warnColor(), false);

        super.render(graphics, mouseX, mouseY, partialTick);

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
        long window = Minecraft.getInstance().getWindow().getWindow();
        GLFW.glfwSetCursor(window, showHand ? handCursor : arrowCursor);
    }

    // ═══════════ 交互 ═══════════

    @Override
    public boolean mouseClicked(double mouseX, double mouseY, int button) {
        if (button == 0 && canEdit) {
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
        return super.mouseClicked(mouseX, mouseY, button);
    }

    private void playClickSound() {
        Minecraft.getInstance().getSoundManager().play(SimpleSoundInstance.forUI(SoundEvents.UI_BUTTON_CLICK, 1.0F));
    }

    @Override
    public boolean mouseScrolled(double mouseX, double mouseY, double delta) {
        if (isBlacklistVisible()) {
            List<String> bl = getCurrentBlacklist();
            if (bl.size() > MAX_VISIBLE_ITEMS) {
                int listStartY = 118;
                int top = yo(listStartY) - 8, bot = yo(listStartY + MAX_VISIBLE_ITEMS * LIST_ITEM_H) - 8;
                if (mouseY >= top && mouseY < bot) {
                    blScroll = Mth.clamp(blScroll - (int) Math.signum(delta), 0, bl.size() - MAX_VISIBLE_ITEMS);
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
    public boolean keyPressed(int keyCode, int scanCode, int modifiers) {
        if ((keyCode == 257 || keyCode == 335) && addButton != null && addButton.active) {
            addPlayer();
            return true;
        }
        return super.keyPressed(keyCode, scanCode, modifiers);
    }

    @Override
    public void tick() {
        super.tick();
        // 确保输入框每帧 tick，使光标闪烁定时器持续累积
        if (playerNameInput != null) playerNameInput.tick();
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
        if (AvalonLink.isAvalonLoaded()) {
            AvalonNetwork.sendToServer(NetworkChannels.UPDATE, new ConfigUpdatePacket(
                    selectedMode, localShowPause, localUseVanilla ? 1 : 0,
                    blacklistAllowTp, blacklistBlockNonTp));
        }
        OnlyTPConfig.mode = selectedMode;
        OnlyTPConfig.showPauseButton = localShowPause;
        OnlyTPConfig.guiButtonStyle = localUseVanilla ? 1 : 0;
        OnlyTPConfig.blacklistAllowTp = new ArrayList<>(blacklistAllowTp);
        OnlyTPConfig.blacklistBlockNonTp = new ArrayList<>(blacklistBlockNonTp);
        onClose();
    }

    @Override
    public void removed() {
        super.removed();
        if (this.handCursor != 0) GLFW.glfwDestroyCursor(this.handCursor);
        if (this.arrowCursor != 0) GLFW.glfwDestroyCursor(this.arrowCursor);
    }

    @Override
    public boolean isPauseScreen() { return true; }

}
