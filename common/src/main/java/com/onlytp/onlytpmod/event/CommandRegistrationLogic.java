package com.onlytp.onlytpmod.event;

import com.mojang.brigadier.tree.CommandNode;
import com.onlytp.onlytpmod.config.OnlyTPConfig;
import net.minecraft.commands.CommandSourceStack;

import java.lang.reflect.Field;
import java.util.function.Predicate;

public class CommandRegistrationLogic {

    private static final Field REQUIREMENT_FIELD;

    static {
        try {
            REQUIREMENT_FIELD = CommandNode.class.getDeclaredField("requirement");
            REQUIREMENT_FIELD.setAccessible(true);
        } catch (NoSuchFieldException e) {
            throw new RuntimeException("Failed to find CommandNode.requirement field", e);
        }
    }

    public static void patch(CommandNode<CommandSourceStack> root) {
        patchNode(root, "tp");
        patchNode(root, "teleport");
    }

    private static void patchNode(CommandNode<CommandSourceStack> root, String name) {
        CommandNode<CommandSourceStack> node = root.getChild(name);
        if (node == null) return;
        try {
            Predicate<CommandSourceStack> original = node.getRequirement();

            REQUIREMENT_FIELD.set(node, (Predicate<CommandSourceStack>) src -> {

                if (OnlyTPConfig.isAllowTpOnlyMode() || OnlyTPConfig.isBothMode()) {
                    return true;
                }
                return original.test(src);
            });
        } catch (IllegalAccessException e) {

        }
    }
}