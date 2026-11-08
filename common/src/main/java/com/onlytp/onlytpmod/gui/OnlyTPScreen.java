package com.onlytp.onlytpmod.gui;

import com.camelot.ui.Screens;

import com.avalon.base.gui.anim.ScreenAnim;
import com.avalon.base.gui.anim.ScreenAnimType;
import com.avalon.base.gui.anim.ScrollAnim;
import com.avalon.base.gui.panel.PanelHover;
import com.avalon.base.gui.theme.GuiTheme;
import com.avalon.base.gui.theme.ModernTheme;
import com.avalon.base.gui.theme.ThemedButton;
import com.avalon.base.gui.theme.ThemedRadio;
import com.avalon.base.gui.theme.ThemedToggle;
import com.avalon.base.gui.theme.VanillaTheme;
import com.avalon.base.gui.GuiCursor;
import com.avalon.base.gui.util.MouseButtons;
import com.camelot.net.CamelotNet;
import com.camelot.platform.Perms;
import com.onlytp.onlytpmod.config.OnlyTPConfig;
import com.onlytp.onlytpmod.util.ModMsg;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.EditBox;
import net.minecraft.client.resources.sounds.SimpleSoundInstance;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.util.Mth;

import java.util.ArrayList;
import java.util.List;

public class OnlyTPScreen extends Screen {

    private static final int GUI_WIDTH = 300;
    private static final int GUI_HEIGHT = 260;
    private static final int CONTENT_MIN_Y = 10;
    private static final int CONTENT_MAX_Y = 218;
    private static final int MAX_VISIBLE_ITEMS = 3;
    private static final int LIST_ITEM_H = 14;

    private static final GuiTheme VANILLA_THEME = new VanillaTheme();

    private static final GuiTheme MODERN_THEME = new ModernTheme();

    private String selectedMode;
    private boolean localShowPause;
    private boolean localUseVanilla;
    private final List<String> blacklistAllowTp = new ArrayList<>();
    private final List<String> blacklistBlockNonTp = new ArrayList<>();

    private boolean canEdit;

    private Screen parentScreen;
    private int guiLeft, guiTop;
    private int blScroll;

    private final ScrollAnim blAnim = new ScrollAnim(0);

    private EditBox playerNameInput;
    private ThemedButton addButton;
    private final ThemedRadio[] modeRadios = new ThemedRadio[4];
    private ThemedToggle showPauseToggle;
    private ThemedToggle styleToggle;
    private ThemedToggle animToggle;
    private ThemedToggle selectorToggle;

    private boolean localEnableAnimations;

    private boolean localAllowSelectors;
    private ScreenAnim anim = ScreenAnim.disabled();

    private boolean closeDone;

    private GuiTheme theme;
    private int hoveredRow = -1;

    private String rowRemoveHint;

    private static final String[] MODES = {"allow_tp_only", "block_non_tp", "both", "disabled"};

    public OnlyTPScreen(Screen parent) {
        super(Component.translatable("gui.onlytp.title"));
        this.parentScreen = parent;
        this.selectedMode = OnlyTPConfig.mode();
        this.localShowPause = OnlyTPConfig.enableConfigEntry();
        this.localUseVanilla = OnlyTPConfig.guiButtonStyle() == 1;
        this.theme = localUseVanilla ? VANILLA_THEME : MODERN_THEME;
        this.blacklistAllowTp.addAll(OnlyTPConfig.blacklistAllowTp());
        this.blacklistBlockNonTp.addAll(OnlyTPConfig.blacklistBlockNonTp());

        this.localEnableAnimations = OnlyTPConfig.enableAnimations();
        this.localAllowSelectors = OnlyTPConfig.allowEntitySelectors();
        applyAnimationConfig();
        anim.playOpen();
    }

    private void applyAnimationConfig() {
        anim = localEnableAnimations
                ? new ScreenAnim(ScreenAnimType.SCALE_BOUNCE, ScreenAnimType.SCALE_BOUNCE)
                : ScreenAnim.disabled();
    }

    private boolean isBlacklistVisible() {
        return "allow_tp_only".equals(selectedMode) || "block_non_tp".equals(selectedMode);
    }

    private int yo(int relY) { return guiTop + relY; }

    @Override
    protected void init() {
        guiLeft = (width - GUI_WIDTH) / 2;

        int relPanelBottom = CONTENT_MAX_Y + 14;
        guiTop = Math.min(Math.max(6, (height - GUI_HEIGHT) / 2), height - 6 - relPanelBottom);
        canEdit = Perms.canEdit(minecraft == null ? null : minecraft.player);
        blScroll = Math.max(0, blScroll);
        blAnim.setTarget(blScroll);

        for (int i = 0; i < 4; i++) {
            modeRadios[i] = new ThemedRadio(guiLeft + 8 + 6, 28 + i * 12,
                    Component.translatable("gui.onlytp.mode." + MODES[i]),
                    MODES[i].equals(selectedMode), canEdit, 146 - 34);
        }

        showPauseToggle = new ThemedToggle(guiLeft + 154 + 6, 28,
                Component.translatable("gui.onlytp.enable_config_entry"), localShowPause, canEdit, false, 138 - 34);
        styleToggle = new ThemedToggle(guiLeft + 154 + 6, 40,
                Component.translatable("gui.onlytp.gui_button_style"), localUseVanilla, canEdit, false, 138 - 34);

        animToggle = new ThemedToggle(guiLeft + 154 + 6, 52,
                Component.translatable("gui.onlytp.enable_animations"), localEnableAnimations, canEdit, false, 138 - 34);

        selectorToggle = new ThemedToggle(guiLeft + 154 + 6, 64,
                Component.translatable("gui.onlytp.allow_entity_selectors"), localAllowSelectors, canEdit, false, 138 - 34);

        playerNameInput = null;
        addButton = null;
        if (isBlacklistVisible() && canEdit) {
            playerNameInput = new EditBox(font, guiLeft + 8, yo(166), 184, 18,
                    Component.translatable("gui.onlytp.input_hint"));
            playerNameInput.setMaxLength(32);
            playerNameInput.setResponder(s -> updateAddButtonState());
            addRenderableWidget(playerNameInput);

            addButton = new ThemedButton(guiLeft + GUI_WIDTH - 68, yo(165), 55, 20,
                    Component.translatable("gui.onlytp.add"), b -> addPlayer(),
                    theme, GuiTheme.ButtonRole.PRIMARY, font);
            addButton.active = false;
            addRenderableWidget(addButton);

            setInitialFocus(playerNameInput);
            playerNameInput.setFocused(true);
        }

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

    @Override
    public void render(GuiGraphics graphics, int mouseX, int mouseY, float partialTick) {

        renderBackground(graphics, mouseX, mouseY, partialTick);
        mouseX = (int) anim.localX(mouseX, width);
        mouseY = (int) anim.localY(mouseY, height);
        anim.beginFrame(graphics, width, height);

        int panelH = yo(CONTENT_MAX_Y) + 14 - (yo(CONTENT_MIN_Y) - 4);
        theme.drawPanel(graphics, guiLeft, yo(CONTENT_MIN_Y) - 4, GUI_WIDTH, panelH);

        int cardLeftX = 8, cardLeftW = 146;
        theme.drawCard(graphics, guiLeft + cardLeftX, yo(14), cardLeftW, 76);
        graphics.drawString(font, Component.translatable("gui.onlytp.mode_label"),
                guiLeft + cardLeftX + 8, yo(18), theme.labelColor(), false);
        for (ThemedRadio r : modeRadios) r.render(graphics, font, theme, yo(r.relY), mouseX, mouseY);

        int cardRightX = 154, cardRightW = 138;
        theme.drawCard(graphics, guiLeft + cardRightX, yo(14), cardRightW, 76);
        graphics.drawString(font, Component.translatable("gui.onlytp.toggle_label"),
                guiLeft + cardRightX + 8, yo(18), theme.labelColor(), false);
        showPauseToggle.render(graphics, font, theme, yo(showPauseToggle.relY), mouseX, mouseY);
        styleToggle.render(graphics, font, theme, yo(styleToggle.relY), mouseX, mouseY);
        animToggle.render(graphics, font, theme, yo(animToggle.relY), mouseX, mouseY);
        selectorToggle.render(graphics, font, theme, yo(selectorToggle.relY), mouseX, mouseY);

        hoveredRow = -1;
        rowRemoveHint = null;
        if (isBlacklistVisible()) {
            int blCardY = 98;
            theme.drawCard(graphics, guiLeft + cardLeftX, yo(blCardY), GUI_WIDTH - 16, 64);
            graphics.drawString(font, Component.translatable("gui.onlytp.blacklist_label"),
                    guiLeft + 18, yo(blCardY + 4), theme.labelColor(), false);

            List<String> bl = getCurrentBlacklist();

            int listStartY = blCardY + 22;
            int visible = Math.min(MAX_VISIBLE_ITEMS, bl.size());

            int rowRightX = guiLeft + (bl.size() > MAX_VISIBLE_ITEMS ? GUI_WIDTH - 20 : GUI_WIDTH - 14);
            for (int i = 0; i < visible; i++) {
                int idx = blAnim.displayValue() + i;
                if (idx >= bl.size()) break;
                int y = yo(listStartY + i * LIST_ITEM_H) - 6;
                boolean rowHover = canEdit && mouseX >= guiLeft + 12 && mouseX <= rowRightX
                        && mouseY >= y - 2 && mouseY < y + LIST_ITEM_H - 2;
                if (rowHover) {
                    hoveredRow = idx;
                    graphics.fill(guiLeft + 12, y - 2, rowRightX, y + LIST_ITEM_H - 2,
                            theme.vanillaButtons() ? 0x28000000 : 0x2AB47AE8);

                    rowRemoveHint = Component.translatable("gui.onlytp.click_to_remove").getString();
                }
                graphics.drawString(font, bl.get(idx),
                        guiLeft + 22, y, canEdit ? theme.textColor() : theme.disabledColor(), false);
            }

            if (bl.size() > MAX_VISIBLE_ITEMS) {
                int tx = guiLeft + GUI_WIDTH - 16;
                int tTop = yo(listStartY) - 6;
                int tBot = yo(listStartY + MAX_VISIBLE_ITEMS * LIST_ITEM_H) - 8;
                theme.drawScrollTrack(graphics, tx, tTop, 4, tBot - tTop);
                int trackH = tBot - tTop;
                int thumbH = Math.max(8, trackH * MAX_VISIBLE_ITEMS / bl.size());
                float p = blAnim.value() / Math.max(1, bl.size() - MAX_VISIBLE_ITEMS);
                theme.drawScrollThumb(graphics, tx, tTop + Math.round((trackH - thumbH) * p), 4, thumbH);
            }
        }

        int bottomY = 200;
        graphics.drawString(font,
                Component.translatable(canEdit ? "gui.onlytp.can_edit" : "gui.onlytp.view_only"),
                guiLeft + 8, yo(bottomY), canEdit ? theme.okColor() : theme.warnColor(), false);

        if (!canEdit) {
            boolean suspended = anim.suspend(graphics);
            graphics.fill(0, 0, this.width, this.height, 0x50000000);
            if (suspended) anim.resume(graphics, width, height);
        }

        super.render(graphics, mouseX, mouseY, partialTick);

        if (!theme.vanillaButtons() && theme instanceof ModernTheme modern) {
            ButtonFrames.render(graphics, this, modern.palette());
        }

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
                    || styleToggle.isClicked(mouseX, mouseY, font, yo(styleToggle.relY))
                    || animToggle.isClicked(mouseX, mouseY, font, yo(animToggle.relY))
                    || selectorToggle.isClicked(mouseX, mouseY, font, yo(selectorToggle.relY)))) {
                showHand = true;
            }
            if (!showHand && hoveredRow >= 0 && isBlacklistVisible()) {
                showHand = true;
            }
        }

        GuiCursor.apply(showHand);

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
        if (tip == null) {
            tip = animToggle.truncatedTooltip(font, mouseX, mouseY, yo(animToggle.relY));
        }
        if (tip == null) {
            tip = selectorToggle.truncatedTooltip(font, mouseX, mouseY, yo(selectorToggle.relY));
        }
        if (tip != null) {
            showTip(graphics, tip, mouseX, mouseY);
        }

        if (rowRemoveHint != null) {
            showTip(graphics, rowRemoveHint, mouseX, mouseY, 0xFFFF5555);
        }

        anim.endFrame(graphics, width, height);
        if (anim.isCloseFinished()) doClose();
    }

    @Override
    public boolean mouseClicked(double mouseX, double mouseY, int button) {
        if (MouseButtons.isLeft(button) && canEdit) {

            for (int i = 0; i < 4; i++) {
                if (modeRadios[i].isClicked(mouseX, mouseY, font, yo(modeRadios[i].relY))) {
                    if (!MODES[i].equals(selectedMode)) {
                        playClickSound();
                        selectedMode = MODES[i];
                        blScroll = 0;
                        blAnim.snapTo(0);
                        reloadWidgets();
                    }
                    return true;
                }
            }

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

            if (animToggle.isClicked(mouseX, mouseY, font, yo(animToggle.relY))) {
                playClickSound();
                localEnableAnimations = !localEnableAnimations;
                animToggle.setChecked(localEnableAnimations);
                applyAnimationConfig();
                return true;
            }

            if (selectorToggle.isClicked(mouseX, mouseY, font, yo(selectorToggle.relY))) {
                playClickSound();
                localAllowSelectors = !localAllowSelectors;
                selectorToggle.setChecked(localAllowSelectors);
                return true;
            }

            if (hoveredRow >= 0 && isBlacklistVisible()) {
                List<String> bl = getCurrentBlacklist();
                if (hoveredRow < bl.size()) {
                    playClickSound();
                    bl.remove(hoveredRow);
                    blScroll = Mth.clamp(blScroll, 0, Math.max(0, bl.size() - MAX_VISIBLE_ITEMS));
                    blAnim.setTarget(blScroll);
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
    public boolean mouseScrolled(double mouseX, double mouseY, double scrollX, double scrollY) {
        if (isBlacklistVisible()) {
            List<String> bl = getCurrentBlacklist();
            if (bl.size() > MAX_VISIBLE_ITEMS) {

                int listStartY = 120;
                int top = yo(listStartY) - 8, bot = yo(listStartY + MAX_VISIBLE_ITEMS * LIST_ITEM_H) - 8;
                if (mouseY >= top && mouseY < bot) {
                    blScroll = Mth.clamp(blScroll - (int) Math.signum(scrollY), 0, bl.size() - MAX_VISIBLE_ITEMS);
                    blAnim.setTarget(blScroll);
                    return true;
                }
            }
        }
        return false;
    }

    private void showTip(GuiGraphics g, String text, int mouseX, int mouseY) {
        if (localUseVanilla) {
            g.renderTooltip(font, Component.literal(text), mouseX, mouseY);
            return;
        }
        PanelHover.render(g, font, text, mouseX, mouseY, this.width, this.height);
    }

    private void showTip(GuiGraphics g, String text, int mouseX, int mouseY, int textColor) {
        if (localUseVanilla) {
            g.renderTooltip(font, Component.literal(text), mouseX, mouseY);
            return;
        }
        PanelHover.render(g, font, text, mouseX, mouseY, this.width, this.height, textColor);
    }

    private void reloadWidgets() {
        String typed = playerNameInput != null ? playerNameInput.getValue() : "";
        clearWidgets();
        init();
        if (playerNameInput != null) playerNameInput.setValue(typed);
    }

    private List<String> getCurrentBlacklist() {
        return switch (selectedMode) {
            case "block_non_tp" -> blacklistBlockNonTp;
            case "disabled" -> new ArrayList<>();
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
        blAnim.setTarget(blScroll);

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

    private void msg(String key) {
        if (minecraft != null && minecraft.player != null)
            minecraft.player.displayClientMessage(ModMsg.red(minecraft.player, key), false);
    }

    private void updateAddButtonState() {
        if (addButton != null && playerNameInput != null)
            addButton.active = !playerNameInput.getValue().trim().isEmpty();
    }

    private void saveConfig() {
        if (!canEdit) return;
        OnlyTPConfig.applyAndSave(selectedMode, localAllowSelectors, localShowPause, localEnableAnimations,
                localUseVanilla ? 1 : 0, blacklistAllowTp, blacklistBlockNonTp);

        CamelotNet.sendUpdate(OnlyTPConfig.CONFIG);
        onClose();
    }

    private void doClose() {
        if (closeDone) return;
        closeDone = true;
        if (parentScreen != null && minecraft != null) {
            Screens.open(parentScreen);
        } else {
            super.onClose();
        }
    }

    @Override
    public void onClose() {

        if (anim.beginClose()) return;
        doClose();
    }

    @Override
    public void tick() {
        super.tick();
        blAnim.tick();
        if (anim.isCloseFinished()) doClose();
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