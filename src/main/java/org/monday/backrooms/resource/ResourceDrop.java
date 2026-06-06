package org.monday.backrooms.resource;

import org.bukkit.Material;

public record ResourceDrop(
        Material material,
        double chance,
        int min,
        int max
) {
}
