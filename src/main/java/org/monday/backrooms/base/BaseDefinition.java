package org.monday.backrooms.base;

import org.monday.backrooms.transition.BlockPosition;
import org.monday.backrooms.transition.CuboidRegion;

public record BaseDefinition(
        String id,
        boolean enabled,
        String displayName,
        String level,
        String world,
        CuboidRegion region,
        BlockPosition terminal
) {

    public String regionDescription() {
        return region.describe();
    }

    public String terminalDescription() {
        return terminal == null ? "none" : terminal.describe();
    }
}
