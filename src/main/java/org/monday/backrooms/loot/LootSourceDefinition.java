package org.monday.backrooms.loot;

import java.util.List;
import java.util.Locale;
import java.util.Set;
import org.bukkit.Material;
import org.bukkit.block.Block;

public record LootSourceDefinition(
        String id,
        boolean enabled,
        LootSourceType type,
        Set<String> levels,
        Set<Material> materials,
        Set<LootSourcePosition> locations,
        List<String> lootTables,
        boolean oneTime,
        boolean fillEmptyOnly
) {

    public boolean appliesToLevel(String levelId) {
        return levels.isEmpty() || levels.contains(levelId.toLowerCase(Locale.ROOT));
    }

    public boolean matches(Block block) {
        if (!materials.contains(block.getType())) {
            return false;
        }
        return locations.isEmpty() || locations.stream().anyMatch(position -> position.matches(block));
    }
}
