package org.monday.backrooms.transition;

public record TransitionTarget(
        TransitionTargetType type,
        String level,
        String world,
        TransitionSpawnMode spawnMode,
        double x,
        double y,
        double z,
        float yaw,
        float pitch
) {

    public String describe() {
        String destination = type == TransitionTargetType.LEVEL ? level : world;
        return type.name().toLowerCase() + ":" + destination + "@" + spawnMode.name().toLowerCase();
    }
}
