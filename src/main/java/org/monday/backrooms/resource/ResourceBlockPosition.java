package org.monday.backrooms.resource;

import org.bukkit.block.Block;

public record ResourceBlockPosition(
        int x,
        int y,
        int z
) {

    public boolean matches(Block block) {
        return block.getX() == x && block.getY() == y && block.getZ() == z;
    }
}
