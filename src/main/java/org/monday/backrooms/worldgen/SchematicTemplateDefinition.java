package org.monday.backrooms.worldgen;

import java.io.File;
import java.util.List;
import java.util.Locale;
import java.util.Set;
import java.util.stream.Collectors;

public record SchematicTemplateDefinition(
        String id,
        boolean enabled,
        String displayName,
        String level,
        File file,
        int cellSize,
        int footprintX,
        int footprintZ,
        int footprintY,
        Set<TemplateConnector> connectors,
        Set<String> tags,
        int weight,
        List<Integer> rotations,
        int maxPerRegion,
        boolean unique,
        int minDistanceFromSpawnCells,
        boolean pasteAir
) {

    public SchematicTemplateDefinition {
        connectors = Set.copyOf(connectors);
        tags = tags.stream().map(tag -> tag.toLowerCase(Locale.ROOT)).collect(Collectors.toUnmodifiableSet());
        rotations = rotations.isEmpty() ? List.of(0) : List.copyOf(rotations);
    }

    public boolean appliesToLevel(String levelId) {
        return level.equalsIgnoreCase(levelId);
    }

    public boolean hasTag(String tag) {
        return tags.contains(tag.toLowerCase(Locale.ROOT));
    }

    public Set<TemplateConnector> connectorsAfterRotation(int rotation) {
        return connectors.stream()
                .map(connector -> connector.rotateClockwise(rotation))
                .collect(Collectors.toUnmodifiableSet());
    }

    public String footprintDescription() {
        return footprintX + "x" + footprintZ + "x" + footprintY;
    }
}
