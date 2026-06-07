package org.monday.backrooms.hud;

public record SanityHudSnapshot(
        double sanity,
        double maxSanity,
        String sanityText,
        String maxSanityText,
        String levelId,
        long stableSeconds,
        double lowThreshold,
        double criticalThreshold
) {
}
