package org.monday.backrooms.level;

import org.bukkit.Location;
import org.bukkit.World;

public record LevelSpawn(
        double x,
        double y,
        double z,
        float yaw,
        float pitch
) {

    public Location toLocation(World world) {
        return new Location(world, x, y, z, yaw, pitch);
    }
}
