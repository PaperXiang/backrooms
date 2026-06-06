package org.monday.backrooms.level;

import java.util.List;
import java.util.concurrent.ThreadLocalRandom;
import org.bukkit.Location;
import org.bukkit.World;

public record LevelSpawn(
        List<LevelSpawnPoint> points
) {

    public LevelSpawn {
        points = List.copyOf(points);
    }

    public LevelSpawn(double x, double y, double z, float yaw, float pitch) {
        this(List.of(new LevelSpawnPoint(x, y, z, yaw, pitch)));
    }

    public Location toLocation(World world) {
        if (points.isEmpty()) {
            return world.getSpawnLocation();
        }

        LevelSpawnPoint point = points.get(ThreadLocalRandom.current().nextInt(points.size()));
        return point.toLocation(world);
    }

    public int pointCount() {
        return points.size();
    }
}

record LevelSpawnPoint(
        double x,
        double y,
        double z,
        float yaw,
        float pitch
) {

    Location toLocation(World world) {
        return new Location(world, x, y, z, yaw, pitch);
    }
}
