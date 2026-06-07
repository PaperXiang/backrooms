package org.monday.backrooms.loot;

import org.bukkit.block.Block;

public record LootSourcePosition(int x, int y, int z) {

    public boolean matches(Block block) {
        return block.getX() == x && block.getY() == y && block.getZ() == z;
    }
}
