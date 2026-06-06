package org.monday.backrooms.level;

public record BackroomsLevel(
        String id,
        boolean enabled,
        String displayName,
        String world,
        boolean pvp,
        String title,
        String subtitle,
        String description
) {
}
