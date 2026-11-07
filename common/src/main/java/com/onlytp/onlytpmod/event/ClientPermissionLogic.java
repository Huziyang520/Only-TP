package com.onlytp.onlytpmod.event;

import com.onlytp.onlytpmod.config.OnlyTPConfig;
import net.minecraft.server.permissions.Permission;
import net.minecraft.server.permissions.PermissionSet;
import net.minecraft.server.permissions.Permissions;

public final class ClientPermissionLogic {

    private static final PermissionSet ENTITY_SELECTORS_ONLY = new PermissionSet() {
        @Override
        public boolean hasPermission(Permission permission) {
            return Permissions.COMMANDS_ENTITY_SELECTORS.equals(permission);
        }
    };

    private ClientPermissionLogic() {
    }

    public static boolean shouldGrantEntitySelectors() {
        if (!OnlyTPConfig.allowEntitySelectors()) return false;
        if (OnlyTPConfig.isDisabled()) return false;
        return OnlyTPConfig.isAllowTpOnlyMode()
                || OnlyTPConfig.isBlockNonTpMode()
                || OnlyTPConfig.isBothMode();
    }

    public static PermissionSet grant(PermissionSet original) {
        if (original == null || !shouldGrantEntitySelectors()) return original;
        return original.union(ENTITY_SELECTORS_ONLY);
    }
}
