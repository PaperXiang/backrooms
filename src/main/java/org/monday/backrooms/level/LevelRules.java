package org.monday.backrooms.level;

public record LevelRules(
        boolean allowBlockBreak,
        boolean allowBlockPlace,
        boolean resourceInteraction
) {
}
