package org.monday.backrooms.resource;

import java.util.List;
import java.util.Set;
import org.bukkit.Material;

public record ResourceBlockDefinition(
        String id,
        Set<String> levels,
        Set<Material> materials,
        Set<ResourceTrigger> triggers,
        boolean cancelOriginalEvent,
        boolean removeBlock,
        Material replacement,
        long cooldownSeconds,
        List<ResourceDrop> drops
) {

    public boolean appliesToLevel(String levelId) {
        return levels.isEmpty() || levels.contains(levelId.toLowerCase());
    }

    public boolean matches(Material material, ResourceTrigger trigger) {
        return materials.contains(material) && triggers.contains(trigger);
    }
}
