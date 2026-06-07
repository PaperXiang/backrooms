package org.monday.backrooms.items;

public record SanityItemEffect(
        double restore,
        double stabilizeSeconds
) {

    public static SanityItemEffect none() {
        return new SanityItemEffect(0.0D, 0.0D);
    }

    public boolean hasEffect() {
        return restore != 0.0D || stabilizeSeconds > 0.0D;
    }
}
