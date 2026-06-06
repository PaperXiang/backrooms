package org.monday.backrooms.player;

import java.time.Instant;
import java.util.UUID;

public record PlayerLevelState(
        UUID playerId,
        String levelId,
        String worldName,
        Instant enteredAt
) {
}
