package com.onlytp.onlytpmod.mixin;

import net.minecraft.client.gui.components.events.GuiEventListener;
import net.minecraft.client.gui.narration.NarratableEntry;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.gui.components.Renderable;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Invoker;

/**
 * 访问器：暴露 Screen.addRenderableWidget（protected）供外部调用。
 * 用于在暂停界面注入按钮。
 */
@Mixin(Screen.class)
public interface ScreenAccessor {

    @Invoker("addRenderableWidget")
    <T extends GuiEventListener & Renderable & NarratableEntry> T onlytp$invokeAddRenderableWidget(T widget);
}
