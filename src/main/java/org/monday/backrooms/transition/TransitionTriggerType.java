package org.monday.backrooms.transition;

import java.util.Locale;
import java.util.Optional;

public enum TransitionTriggerType {
    REGION,
    RIGHT_CLICK_BLOCK;

    public static Optional<TransitionTriggerType> fromConfig(String input) {
        String normalized = input == null ? "" : input.toLowerCase(Locale.ROOT).replace('-', '_');
        return switch (normalized) {
            case "region", "region_enter" -> Optional.of(REGION);
            case "block", "right_click", "right_click_block" -> Optional.of(RIGHT_CLICK_BLOCK);
            default -> Optional.empty();
        };
    }
}
