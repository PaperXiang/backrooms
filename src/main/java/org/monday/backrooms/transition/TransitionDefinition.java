package org.monday.backrooms.transition;

import java.util.List;
import java.util.Set;
import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.block.Block;

public record TransitionDefinition(
        String id,
        boolean enabled,
        String displayName,
        String fromLevel,
        String triggerWorld,
        TransitionTriggerType triggerType,
        CuboidRegion region,
        Set<Material> materials,
        List<BlockPosition> blockPositions,
        TransitionTarget target,
        long cooldownSeconds,
        String permission,
        String messageKey,
        boolean showTitle,
        Sound sound
) {

    public boolean matchesBlock(Block block) {
        if (triggerType != TransitionTriggerType.RIGHT_CLICK_BLOCK || !block.getWorld().getName().equalsIgnoreCase(triggerWorld)) {
            return false;
        }
        if (!materials.isEmpty() && !materials.contains(block.getType())) {
            return false;
        }
        return blockPositions.isEmpty() || blockPositions.stream().anyMatch(position -> position.matches(block));
    }

    public String triggerDescription() {
        if (triggerType == TransitionTriggerType.REGION) {
            return "region:" + triggerWorld + ":" + (region == null ? "none" : region.describe());
        }
        if (triggerType == TransitionTriggerType.RIGHT_CLICK_BLOCK) {
            return "right_click_block:" + triggerWorld + ":materials=" + materials.size() + ":positions=" + blockPositions.size();
        }
        return triggerType.name().toLowerCase() + ":" + triggerWorld;
    }
}
