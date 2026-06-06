package org.monday.backrooms.transition;

import org.bukkit.block.Block;

public record BlockPosition(
        int x,
        int y,
        int z
) {

    public boolean matches(Block block) {
        return block.getX() == x && block.getY() == y && block.getZ() == z;
    }

    public String describe() {
        return x + "," + y + "," + z;
    }
}
