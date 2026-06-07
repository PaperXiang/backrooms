package org.monday.backrooms.resource;

import org.bukkit.Material;

public record ResourceDrop(
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
