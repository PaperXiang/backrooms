package org.monday.backrooms.room;

import java.util.Locale;
import java.util.Set;
import org.bukkit.Material;

public record RoomDefinition(
        String id,
        boolean enabled,
        String displayName,
        Set<String> levels,
        RoomShape shape,
        int width,
        int length,
        int height,
        Material floor,
        Material wall,
        Material ceiling,
        Material light,
        Material marker
) {

    public boolean appliesToLevel(String levelId) {
        return levels.isEmpty() || levels.contains(levelId.toLowerCase(Locale.ROOT));
    }

    public String sizeDescription() {
        return width + "x" + length + "x" + height;
    }
}
