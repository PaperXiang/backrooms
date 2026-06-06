package org.monday.backrooms.room;

import java.util.Locale;
import java.util.Optional;

public enum RoomShape {
    ROOM,
    CORRIDOR;

    public static Optional<RoomShape> fromConfig(String input) {
        String normalized = input == null ? "" : input.toLowerCase(Locale.ROOT).replace('-', '_');
        return switch (normalized) {
            case "room", "box" -> Optional.of(ROOM);
            case "corridor", "hallway" -> Optional.of(CORRIDOR);
            default -> Optional.empty();
        };
    }
}
