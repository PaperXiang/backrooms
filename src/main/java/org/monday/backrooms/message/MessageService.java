package org.monday.backrooms.message;

import java.util.List;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.minimessage.MiniMessage;
import net.kyori.adventure.text.minimessage.tag.resolver.Placeholder;
import net.kyori.adventure.text.minimessage.tag.resolver.TagResolver;
import org.bukkit.command.CommandSender;
import org.monday.backrooms.Backrooms;

public final class MessageService {

    private final Backrooms plugin;
    private final MiniMessage miniMessage = MiniMessage.miniMessage();
    private String prefix = "";

    public MessageService(Backrooms plugin) {
        this.plugin = plugin;
        reload();
    }

    public void reload() {
        this.prefix = plugin.getConfig().getString("messages.prefix", "");
    }

    public void send(CommandSender sender, String key, TagResolver... resolvers) {
        Object value = plugin.getConfig().get("messages." + key);

        if (value instanceof List<?> lines) {
            for (Object line : lines) {
                sender.sendMessage(parseWithPrefix(String.valueOf(line), resolvers));
            }
            return;
        }

        sender.sendMessage(parseWithPrefix(String.valueOf(value == null ? key : value), resolvers));
    }

    public TagResolver text(String key, String value) {
        return Placeholder.unparsed(key, value);
    }

    public TagResolver mini(String key, String value) {
        return Placeholder.parsed(key, value);
    }

    public TagResolver bool(String key, boolean value) {
        return Placeholder.parsed(key, value ? "<green>true</green>" : "<red>false</red>");
    }

    public Component parse(String input, TagResolver... resolvers) {
        return miniMessage.deserialize(input, TagResolver.resolver(resolvers));
    }

    private Component parseWithPrefix(String input, TagResolver... resolvers) {
        return parse(prefix + input, resolvers);
    }
}
