package org.monday.backrooms.resource;

import java.util.Locale;
import java.util.Optional;

public enum ResourceTrigger {
    BREAK,
    RIGHT_CLICK;

    public static Optional<ResourceTrigger> fromConfig(String value) {
        String normalized = value.toUpperCase(Locale.ROOT).replace('-', '_');
        try {
            return Optional.of(ResourceTrigger.valueOf(normalized));
        } catch (IllegalArgumentException ignored) {
            return Optional.empty();
        }
    }
}
