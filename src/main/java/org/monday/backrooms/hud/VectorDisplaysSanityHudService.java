package org.monday.backrooms.hud;

import java.util.HashMap;
import java.util.Locale;
import java.util.Map;
import java.util.UUID;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.entity.Player;
import org.monday.backrooms.Backrooms;
import top.mrxiaom.hologram.vector.displays.TerminalManager;
import top.mrxiaom.hologram.vector.displays.hologram.RenderMode;
import top.mrxiaom.hologram.vector.displays.ui.EnumAlign;
import top.mrxiaom.hologram.vector.displays.ui.SimpleTerminal;
import top.mrxiaom.hologram.vector.displays.ui.widget.Label;
import top.mrxiaom.hologram.vector.displays.ui.widget.Line;

public final class VectorDisplaysSanityHudService implements SanityHudService {

    private static final String DEFAULT_TITLE = "<yellow>MENTAL SIGNAL</yellow> <dark_gray>//</dark_gray> <gray><level></gray>";
    private static final String DEFAULT_LINE = "<state_color><sanity></state_color><gray>/<max></gray> <dark_gray>|</dark_gray> <aqua>stable <stable>s</aqua>";

    private final Backrooms plugin;
    private final Map<UUID, PlayerHud> huds = new HashMap<>();
    private boolean enabled;
    private RenderMode renderMode = RenderMode.VIEWER_LIST;
    private String titleFormat = DEFAULT_TITLE;
    private String lineFormat = DEFAULT_LINE;
    private double width = 112.0D;
    private double height = 34.0D;
    private double distance = 1.55D;
    private double yOffset = 0.72D;
    private double pitch = -14.0D;
    private double nearbyDistance = 24.0D;
    private double barWidth = 96.0D;
    private float titleScale = 0.28F;
    private float lineScale = 0.33F;
    private int backgroundColor = 0x50000000;
    private int barBackgroundColor = 0x50FFFFFF;
    private int goodColor = 0xC05AF27A;
    private int lowColor = 0xC0FFD35A;
    private int criticalColor = 0xC0FF4040;

    public VectorDisplaysSanityHudService(Backrooms plugin) {
        this.plugin = plugin;
    }

    @Override
    public void reload() {
        ConfigurationSection section = plugin.configFiles().items().getConfigurationSection("sanity.hud");
        if (section == null) {
            enabled = false;
            clear();
            return;
        }

        enabled = section.getBoolean("enabled", true)
                && "VECTOR_DISPLAYS".equalsIgnoreCase(section.getString("provider", "VECTOR_DISPLAYS"));
        titleFormat = section.getString("title-format", DEFAULT_TITLE);
        lineFormat = section.getString("line-format", section.getString("format", DEFAULT_LINE));

        ConfigurationSection vector = section.getConfigurationSection("vector-displays");
        if (vector != null) {
            renderMode = parseRenderMode(vector.getString("render-mode", "VIEWER_LIST"));
            width = Math.max(24.0D, vector.getDouble("width", width));
            height = Math.max(12.0D, vector.getDouble("height", height));
            distance = Math.max(0.5D, vector.getDouble("distance", distance));
            yOffset = vector.getDouble("y-offset", yOffset);
            pitch = vector.getDouble("pitch", pitch);
            nearbyDistance = Math.max(1.0D, vector.getDouble("nearby-distance", nearbyDistance));
            barWidth = Math.max(8.0D, vector.getDouble("bar-width", Math.min(96.0D, width - 16.0D)));
            titleScale = (float) Math.max(0.05D, vector.getDouble("title-scale", titleScale));
            lineScale = (float) Math.max(0.05D, vector.getDouble("line-scale", lineScale));
            backgroundColor = color(vector, "background-color", backgroundColor);
            barBackgroundColor = color(vector, "bar-background-color", barBackgroundColor);
            goodColor = color(vector, "bar-good-color", goodColor);
            lowColor = color(vector, "bar-low-color", lowColor);
            criticalColor = color(vector, "bar-critical-color", criticalColor);
        }

        if (!enabled) {
            clear();
            return;
        }

        plugin.getLogger().info("Loaded VectorDisplays sanity HUD: renderMode=" + renderMode
                + ", size=" + width + "x" + height
                + ", distance=" + distance + ".");
    }

    @Override
    public void update(Player player, SanityHudSnapshot snapshot) {
        if (!enabled || !player.isOnline()) {
            hide(player);
            return;
        }

        try {
            PlayerHud hud = huds.get(player.getUniqueId());
            if (hud == null || hud.world() != player.getWorld()) {
                hide(player);
                hud = spawn(player, snapshot);
                huds.put(player.getUniqueId(), hud);
            }

            moveToPlayerView(player, hud.terminal());
            hud.title().setText(render(titleFormat, snapshot));
            hud.line().setText(render(lineFormat, snapshot));
            updateBar(hud.bar(), snapshot);
            hud.terminal().ensureViewersAdded();
        } catch (RuntimeException exception) {
            hide(player);
            plugin.getLogger().warning("Failed to update VectorDisplays sanity HUD for " + player.getName() + ": " + exception.getMessage());
        }
    }

    @Override
    public void hide(Player player) {
        PlayerHud hud = huds.remove(player.getUniqueId());
        if (hud == null) {
            return;
        }
        destroy(hud.terminal().getId());
    }

    @Override
    public void clear() {
        for (PlayerHud hud : huds.values()) {
            destroy(hud.terminal().getId());
        }
        huds.clear();
    }

    private PlayerHud spawn(Player player, SanityHudSnapshot snapshot) {
        SimpleTerminal terminal = new SimpleTerminal(renderMode, terminalId(player), terminalLocation(player), width, height);
        terminal.setNearbyDistance(nearbyDistance);
        terminal.getHologram().setBackgroundColor(backgroundColor);
        moveToPlayerView(player, terminal);

        Label title = new Label("title");
        title.setText(render(titleFormat, snapshot));
        title.setScale(titleScale);
        title.setAlign(EnumAlign.LEFT_TOP);
        title.setPos(6.0D, 4.0D);
        title.setFullBrightness();
        title.setEnabled(false);
        terminal.addElement(title);

        Label line = new Label("line");
        line.setText(render(lineFormat, snapshot));
        line.setScale(lineScale);
        line.setAlign(EnumAlign.LEFT_TOP);
        line.setPos(6.0D, 15.0D);
        line.setFullBrightness();
        line.setEnabled(false);
        terminal.addElement(line);

        Line barBackground = new Line("bar_bg");
        barBackground.setPos1(6.0D, 29.0D);
        barBackground.setPos2(6.0D + barWidth, 29.0D);
        barBackground.setThickness(1.2D);
        barBackground.setBackgroundColor(barBackgroundColor);
        barBackground.setFullBrightness();
        terminal.addElement(barBackground);

        Line bar = new Line("bar");
        bar.setPos1(6.0D, 29.0D);
        bar.setThickness(1.8D);
        bar.setFullBrightness();
        terminal.addElement(bar);
        updateBar(bar, snapshot);

        TerminalManager.inst().spawn(terminal);
        terminal.addViewer(player);
        terminal.ensureViewersAdded();
        return new PlayerHud(terminal, title, line, bar, player.getWorld());
    }

    private void updateBar(Line bar, SanityHudSnapshot snapshot) {
        double ratio = snapshot.maxSanity() <= 0.0D ? 0.0D : snapshot.sanity() / snapshot.maxSanity();
        double filled = Math.max(1.0D, barWidth * clamp(ratio, 0.0D, 1.0D));
        bar.setPos2(6.0D + filled, 29.0D);
        bar.setBackgroundColor(barColor(snapshot));
        if (!bar.getEntity().isDead()) {
            bar.updatePos();
            bar.updateLocation();
        }
    }

    private void moveToPlayerView(Player player, SimpleTerminal terminal) {
        Location eye = player.getEyeLocation().clone();
        eye.setPitch(0.0F);
        terminal.setLocation(terminalLocation(player));
        terminal.setRotation(180.0F - eye.getYaw(), (float) pitch);
    }

    private Location terminalLocation(Player player) {
        Location eye = player.getEyeLocation().clone();
        eye.setPitch(0.0F);
        return player.getLocation().clone()
                .add(0.0D, yOffset, 0.0D)
                .add(eye.getDirection().multiply(distance));
    }

    private String terminalId(Player player) {
        return "backrooms_sanity_" + player.getUniqueId().toString().replace("-", "");
    }

    private String render(String template, SanityHudSnapshot snapshot) {
        String color = sanityColorTag(snapshot);
        return template
                .replace("<state_color>", color)
                .replace("</state_color>", "</" + color.substring(1))
                .replace("<sanity>", snapshot.sanityText())
                .replace("<max>", snapshot.maxSanityText())
                .replace("<level>", snapshot.levelId())
                .replace("<stable>", String.valueOf(snapshot.stableSeconds()));
    }

    private String sanityColorTag(SanityHudSnapshot snapshot) {
        if (snapshot.sanity() <= snapshot.criticalThreshold()) {
            return "<red>";
        }
        if (snapshot.sanity() <= snapshot.lowThreshold()) {
            return "<yellow>";
        }
        return "<green>";
    }

    private int barColor(SanityHudSnapshot snapshot) {
        if (snapshot.sanity() <= snapshot.criticalThreshold()) {
            return criticalColor;
        }
        if (snapshot.sanity() <= snapshot.lowThreshold()) {
            return lowColor;
        }
        return goodColor;
    }

    private RenderMode parseRenderMode(String value) {
        if (value == null || value.isBlank()) {
            return RenderMode.VIEWER_LIST;
        }
        try {
            return RenderMode.valueOf(value.toUpperCase(Locale.ROOT));
        } catch (IllegalArgumentException ignored) {
            plugin.getLogger().warning("Unknown VectorDisplays render mode '" + value + "'; using VIEWER_LIST.");
            return RenderMode.VIEWER_LIST;
        }
    }

    private int color(ConfigurationSection section, String key, int fallback) {
        Object value = section.get(key);
        if (value == null) {
            return fallback;
        }
        if (value instanceof Number number) {
            return number.intValue();
        }
        try {
            return Long.decode(String.valueOf(value)).intValue();
        } catch (NumberFormatException ignored) {
            plugin.getLogger().warning("Invalid color value for sanity.hud.vector-displays." + key + ": " + value);
            return fallback;
        }
    }

    private void destroy(String terminalId) {
        try {
            TerminalManager.inst().destroy(terminalId);
        } catch (RuntimeException ignored) {
            // VectorDisplays can be reloaded independently; stale HUD terminals are safe to ignore here.
        }
    }

    private double clamp(double value, double min, double max) {
        return Math.max(min, Math.min(max, value));
    }

    private record PlayerHud(SimpleTerminal terminal, Label title, Label line, Line bar, World world) {
    }
}
