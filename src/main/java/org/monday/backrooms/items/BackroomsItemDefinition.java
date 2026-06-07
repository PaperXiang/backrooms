package org.monday.backrooms.items;

import java.util.List;
import org.bukkit.Material;

public record BackroomsItemDefinition(
        String id,
        boolean enabled,
        Material material,
        String displayName,
        List<String> lore,
        int customModelData,
        boolean consumeOnRightClick,
        Material consumeReplacement,
        int useCooldownSeconds,
        SanityItemEffect sanity,
        String consumeMessage
) {

    public boolean hasCustomModelData() {
        return customModelData > 0;
    }

    public boolean hasConsumeMessage() {
        return consumeMessage != null && !consumeMessage.isBlank();
    }
}
