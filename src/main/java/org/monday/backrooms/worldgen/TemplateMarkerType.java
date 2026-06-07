package org.monday.backrooms.worldgen;

import java.util.Locale;

public enum TemplateMarkerType {
    RESOURCE,
    LIGHT,
    LOOT,
    TRANSITION,
    MAINTENANCE_DOOR,
    STAIRWELL;

    public String configName() {
        return name().toLowerCase(Locale.ROOT).replace('_', '-');
    }
}
