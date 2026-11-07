package com.onlytp.onlytpmod.event;

import com.mojang.brigadier.tree.CommandNode;
import com.onlytp.onlytpmod.config.OnlyTPConfig;
import net.minecraft.commands.CommandSourceStack;

import java.lang.reflect.Field;
import java.util.function.Predicate;

/**
 * After command registration, relaxes the requirement of tp/teleport so non-operators can see and complete them.
 * Uses reflection instead of a Mixin accessor to avoid a LiteralCommandNode cast failure.
 * <p>Called by the platform layer (Forge RegisterCommandsEvent / Fabric CommandRegistrationCallback)
 */
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
            // Reads the mode dynamically so in-game config changes apply immediately
            REQUIREMENT_FIELD.set(node, (Predicate<CommandSourceStack>) src -> {
                // For the non-operator teleport mode and the both mode:
                // non-operators must also see and complete teleport commands
                if (OnlyTPConfig.isAllowTpOnlyMode() || OnlyTPConfig.isBothMode()) {
                    return true;
                }
                return original.test(src);
            });
        } catch (IllegalAccessException e) {
            // should not happen with setAccessible(true)
        }
    }
}