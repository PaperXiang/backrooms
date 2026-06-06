package org.monday.backrooms.transition;

import java.util.Locale;
import java.util.Optional;

public enum TransitionSpawnMode {
    LEVEL_SPAWN,
    WORLD_SPAWN,
    POINT;

    public static Optional<TransitionSpawnMode> fromConfig(String input) {
        String normalized = input == null ? "" : input.toLowerCase(Locale.ROOT).replace('-', '_');
        return switch (normalized) {
            case "level_spawn" -> Optional.of(LEVEL_SPAWN);
            case "world_spawn" -> Optional.of(WORLD_SPAWN);
            case "point", "location" -> Optional.of(POINT);
            default -> Optional.empty();
        };
    }
}
