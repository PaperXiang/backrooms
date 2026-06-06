package org.monday.backrooms.room;

public record RoomGenerationResult(
        boolean success,
        String messageKey,
        int blocksChanged,
        String roomId,
        String levelId,
        String world,
        int originX,
        int originY,
        int originZ
) {
}
