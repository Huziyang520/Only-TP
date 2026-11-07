package com.onlytp.onlytpmod.config;

import net.minecraft.network.chat.Component;
import net.neoforged.neoforge.common.TranslatableEnum;

/** NeoForge 界面用的模式枚举：取值与通用枚举同名，额外提供译文。 */
public enum NeoMode implements TranslatableEnum {
    allow_tp_only,
    block_non_tp,
    both,
    disabled;

    @Override
    public Component getTranslatedName() {
        return Component.translatable("gui.onlytp.mode." + name());
    }
}
