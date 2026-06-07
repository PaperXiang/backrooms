package org.monday.backrooms.loot;

import org.bukkit.Material;

public record LootEntry(
        Material material,
        String itemId,
        double chance,
        int min,
        int max
) {

    public boolean customItem() {
        return itemId != null && !itemId.isBlank();
    }
}
