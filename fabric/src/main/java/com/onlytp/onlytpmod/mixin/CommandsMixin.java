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
 * 在 Commands.performCommand(ParseResults, String) 处拦截（与 Forge CommandEvent 触发点一致），
 * 直接使用解析结果判断是否取消。
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
