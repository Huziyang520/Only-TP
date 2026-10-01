package com.onlytp.onlytpmod.mixin;

import com.onlytp.onlytpmod.event.CommandLogic;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

/**
 * Fabric 命令执行拦截（等价于原 Forge CommandEvent）。
 * 1.18.2 的 performCommand 签名为 (CommandSourceStack, String)，
 * 因此使用 source + 命令字符串入口判断。
 */
@Mixin(Commands.class)
public class CommandsMixin {

    @Inject(method = "performCommand(Lnet/minecraft/commands/CommandSourceStack;Ljava/lang/String;)I",
            at = @At("HEAD"), cancellable = true)
    private void onlytp$interceptCommand(CommandSourceStack source, String command,
                                         CallbackInfoReturnable<Integer> cir) {
        if (CommandLogic.shouldCancel(source, command)) {
            cir.setReturnValue(0);
        }
    }
}
