package org.monday.backrooms.loot;

import java.util.List;

public record LootTableDefinition(
        String id,
        boolean enabled,
        String displayName,
        int rollsMin,
        int rollsMax,
        List<LootEntry> entries
) {

    public String rollsDescription() {
        if (rollsMin == rollsMax) {
            return String.valueOf(rollsMin);
        }
        return rollsMin + "-" + rollsMax;
    }
}
