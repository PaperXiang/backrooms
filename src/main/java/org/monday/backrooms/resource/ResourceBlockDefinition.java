package org.monday.backrooms.resource;

import java.util.List;
import java.util.Locale;
import java.util.Set;
import org.bukkit.Material;
import org.bukkit.block.Block;

public record ResourceBlockDefinition(
        String id,
        Set<String> levels,
        Set<Material> materials,
        Set<ResourceBlockPosition> positions,
        Set<ResourceTrigger> triggers,
        boolean cancelOriginalEvent,
        boolean removeBlock,
        Material replacement,
        long cooldownSeconds,
        List<String> lootTables,
        List<ResourceDrop> drops
) {

    public boolean appliesToLevel(String levelId) {
        return levels.isEmpty() || levels.contains(levelId.toLowerCase(Locale.ROOT));
    }

    public boolean matches(Block block, ResourceTrigger trigger) {
        return materials.contains(block.getType())
                && triggers.contains(trigger)
                && (positions.isEmpty() || positions.stream().anyMatch(position -> position.matches(block)));
    }
}
