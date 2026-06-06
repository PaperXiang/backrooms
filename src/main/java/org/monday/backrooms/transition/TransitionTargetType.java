package org.monday.backrooms.transition;

import java.util.Locale;
import java.util.Optional;

public enum TransitionTargetType {
    LEVEL,
    WORLD;

    public static Optional<TransitionTargetType> fromConfig(String input) {
        String normalized = input == null ? "" : input.toLowerCase(Locale.ROOT).replace('-', '_');
        return switch (normalized) {
            case "level" -> Optional.of(LEVEL);
            case "world", "lobby" -> Optional.of(WORLD);
            default -> Optional.empty();
        };
    }
}
