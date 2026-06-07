package org.monday.backrooms.base;

import java.time.Instant;
import java.util.UUID;

public record BaseClaim(
        String baseId,
        UUID owner,
        String ownerName,
        Instant claimedAt
) {
}
