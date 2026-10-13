package com.onlytp.onlytpmod.mixin;

import com.mojang.brigadier.ParseResults;
import com.onlytp.onlytpmod.event.CommandLogic;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

/**
 * Fabric 命令执行拦截（等价于原 Forge CommandEvent）。
 * 在 {@code Commands.performCommand(ParseResults, String)} 处拦截（与 Forge CommandEvent 触发点一致），
 * 直接使用解析结果判断是否取消。
 *
 * <p><b>返回类型随版本变化</b>：<b>1.20.1 及更早返回 {@code int}</b>（置 0 = 未执行，等价于取消），
 * <b>1.20.2 起改为 {@code void}</b>。本档必须注入描述符 {@code (...;String;)I} 并用
 * {@link CallbackInfoReturnable}{@code <Integer>} + {@code setReturnValue(0)}；
 * 照抄 1.20.2+ 的 {@code ...)V} + {@code CallbackInfo#cancel()} 会让 Mixin 找不到目标方法并<b>崩游戏</b>。</p>
 */
@Mixin(Commands.class)
public class CommandsMixin {

    @Inject(method = "performCommand(Lcom/mojang/brigadier/ParseResults;Ljava/lang/String;)I",
            at = @At("HEAD"), cancellable = true)
    private void onlytp$interceptCommand(ParseResults<CommandSourceStack> parseResults, String command,
                                         CallbackInfoReturnable<Integer> cir) {
        if (CommandLogic.shouldCancel(parseResults)) {
            cir.setReturnValue(0);
        }
    }
}
