package org.monday.backrooms.transition;

import org.bukkit.Location;

public record CuboidRegion(
        double minX,
        double minY,
        double minZ,
        double maxX,
        double maxY,
        double maxZ
) {

    public CuboidRegion {
        double normalizedMinX = Math.min(minX, maxX);
        double normalizedMinY = Math.min(minY, maxY);
        double normalizedMinZ = Math.min(minZ, maxZ);
        double normalizedMaxX = Math.max(minX, maxX);
        double normalizedMaxY = Math.max(minY, maxY);
        double normalizedMaxZ = Math.max(minZ, maxZ);
        minX = normalizedMinX;
        minY = normalizedMinY;
        minZ = normalizedMinZ;
        maxX = normalizedMaxX;
        maxY = normalizedMaxY;
        maxZ = normalizedMaxZ;
    }

    public boolean contains(Location location) {
        double x = location.getX();
        double y = location.getY();
        double z = location.getZ();
        return x >= minX && x <= maxX
                && y >= minY && y <= maxY
                && z >= minZ && z <= maxZ;
    }

    public String describe() {
        return minX + "," + minY + "," + minZ + " -> " + maxX + "," + maxY + "," + maxZ;
    }
}
