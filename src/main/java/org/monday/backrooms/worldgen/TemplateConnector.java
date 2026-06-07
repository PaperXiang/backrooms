package org.monday.backrooms.worldgen;

import java.util.Locale;
import java.util.Optional;

public enum TemplateConnector {
    NORTH(0, -1),
    EAST(1, 0),
    SOUTH(0, 1),
    WEST(-1, 0);

    private static final TemplateConnector[] CLOCKWISE = {NORTH, EAST, SOUTH, WEST};
    private final int deltaX;
    private final int deltaZ;

    TemplateConnector(int deltaX, int deltaZ) {
        this.deltaX = deltaX;
        this.deltaZ = deltaZ;
    }

    public static Optional<TemplateConnector> fromConfig(String input) {
        String normalized = input == null ? "" : input.toLowerCase(Locale.ROOT).replace('-', '_');
        for (TemplateConnector connector : values()) {
            if (connector.name().toLowerCase(Locale.ROOT).equals(normalized)) {
                return Optional.of(connector);
            }
        }
        return Optional.empty();
    }

    public TemplateConnector opposite() {
        return CLOCKWISE[(ordinal() + 2) % CLOCKWISE.length];
    }

    public TemplateConnector rotateClockwise(int degrees) {
        int turns = Math.floorMod(degrees / 90, CLOCKWISE.length);
        return CLOCKWISE[(ordinal() + turns) % CLOCKWISE.length];
    }

    public String configName() {
        return name().toLowerCase(Locale.ROOT);
    }

    public int deltaX() {
        return deltaX;
    }

    public int deltaZ() {
        return deltaZ;
    }
}
