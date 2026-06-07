package org.monday.backrooms.loot;

import org.bukkit.Material;

public record LootEntry(
        Material material,
        double chance,
        int min,
        int max
) {
}
