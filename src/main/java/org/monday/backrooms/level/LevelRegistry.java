package org.monday.backrooms.level;

import java.util.Collection;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.Locale;
import java.util.Map;
import java.util.Optional;

public final class LevelRegistry {

    private final Map<String, BackroomsLevel> levels = new LinkedHashMap<>();

    public void register(BackroomsLevel level) {
        levels.put(normalize(level.id()), level);
    }

    public Optional<BackroomsLevel> get(String id) {
        return Optional.ofNullable(levels.get(normalize(id)));
    }

    public Optional<BackroomsLevel> getByWorld(String worldName) {
        String normalizedWorld = normalize(worldName);
        return levels.values().stream()
                .filter(BackroomsLevel::enabled)
                .filter(level -> normalize(level.world()).equals(normalizedWorld))
                .findFirst();
    }

    public Collection<BackroomsLevel> all() {
        return Collections.unmodifiableCollection(levels.values());
    }

    public int size() {
        return levels.size();
    }

    public long enabledCount() {
        return levels.values().stream().filter(BackroomsLevel::enabled).count();
    }

    public long disabledCount() {
        return levels.values().stream().filter(level -> !level.enabled()).count();
    }

    public void clear() {
        levels.clear();
    }

    private String normalize(String id) {
        return id.toLowerCase(Locale.ROOT);
    }
}
