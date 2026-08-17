package com.onlytp.onlytpmod.event;

import com.mojang.brigadier.tree.CommandNode;
import com.onlytp.onlytpmod.config.OnlyTPConfig;
import net.minecraft.commands.CommandSourceStack;

import java.lang.reflect.Field;
import java.util.function.Predicate;

/**
 * 注册命令后修改 tp/teleport 节点的 requirement，使非 OP 玩家也能看到并补全 TP 命令。
 * 使用反射而非 Mixin Accessor，避免 LiteralCommandNode 转型失败。
 *
 * <p>由平台侧（Forge RegisterCommandsEvent / Fabric CommandRegistrationCallback）调用
 * {@link #patch(CommandNode)} 传入命令树的根节点。
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
            // 动态读取当前模式，保证游戏内切换配置后立即生效
            REQUIREMENT_FIELD.set(node, (Predicate<CommandSourceStack>) src -> {
                // 功能1（非OP仅允许TP）与功能3（同时启用）下：
                // 非OP玩家也需要看到并补全 TP 命令
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
