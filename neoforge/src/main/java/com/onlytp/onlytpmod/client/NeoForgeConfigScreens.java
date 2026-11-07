package com.onlytp.onlytpmod.client;

import com.camelot.ui.ConfigScreens;
import com.onlytp.onlytpmod.AvalonLink;
import com.onlytp.onlytpmod.Constants;
import com.onlytp.onlytpmod.gui.ConfigScreenEntry;
import net.neoforged.fml.ModList;
import net.neoforged.neoforge.client.gui.IConfigScreenFactory;

/**
 * 原生 NeoForge 配置界面扩展点：模组列表（以及 Catalogue、Configured）据此显示「配置」按钮。
 *
 * <p>只有在确实有界面可开时才注册（Cloth 自动生成界面，或 AvalonBase 提供界面），否则不注册，
 * 按钮不显示而不是点了报错。
 */
public final class NeoForgeConfigScreens {

    private NeoForgeConfigScreens() {
    }

    public static void register() {
        if (!ConfigScreens.clothAvailable() && !AvalonLink.isAvalonLoaded()) {
            return;
        }
        ModList.get().getModContainerById(Constants.MOD_ID).ifPresent(container -> {
            IConfigScreenFactory factory = (modContainer, parent) -> ConfigScreenEntry.create(parent);
            container.registerExtensionPoint(IConfigScreenFactory.class, factory);
        });
    }
}
