package com.onlytp.onlytpmod.event;

import com.onlytp.onlytpmod.config.OnlyTPConfig;

public final class ClientPermissionLogic {

    private ClientPermissionLogic() {
    }

    public static boolean shouldGrantEntitySelectors() {
        if (!OnlyTPConfig.allowEntitySelectors()) return false;
        if (OnlyTPConfig.isDisabled()) return false;
        return OnlyTPConfig.isAllowTpOnlyMode()
                || OnlyTPConfig.isBlockNonTpMode()
                || OnlyTPConfig.isBothMode();
    }
}
