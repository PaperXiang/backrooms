package org.monday.backrooms.worldgen;

public record WorldGenerationResult(
        boolean success,
        String messageKey,
        String levelId,
        String world,
        String regionId,
        int cells,
        int templates,
        int blocksChanged,
        String markers,
        String reason
) {
}
