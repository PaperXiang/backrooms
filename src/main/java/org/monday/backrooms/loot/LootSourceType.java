package org.monday.backrooms.loot;

import java.util.Locale;
import java.util.Optional;

public enum LootSourceType {
    VANILLA_CONTAINER;

    public static Optional<LootSourceType> fromConfig(String value) {
        if (value == null || value.isBlank()) {
            return Optional.empty();
        }
        String normalized = value.trim().replace('-', '_').toUpperCase(Locale.ROOT);
        try {
            return Optional.of(valueOf(normalized));
        } catch (IllegalArgumentException ignored) {
            return Optional.empty();
        }
    }
}
