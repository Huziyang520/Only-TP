package com.onlytp.onlytpmod.mixin;

import com.mojang.brigadier.ParseResults;
import com.onlytp.onlytpmod.event.CommandLogic;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(Commands.class)
public class CommandsMixin {

    @Inject(method = "performCommand(Lcom/mojang/brigadier/ParseResults;Ljava/lang/String;)V",
            at = @At("HEAD"), cancellable = true, require = 1)
    private void onlytp$interceptCommand(ParseResults<CommandSourceStack> parseResults, String command,
                                         CallbackInfo ci) {
        if (CommandLogic.shouldCancel(parseResults)) {
            ci.cancel();
        }
    }
}
