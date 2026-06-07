package org.monday.backrooms.hud;

import org.bukkit.entity.Player;

public interface SanityHudService {

    void reload();

    void update(Player player, SanityHudSnapshot snapshot);

    void hide(Player player);

    void clear();
}
