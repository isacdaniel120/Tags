package dev.gugli.tags.managers;

import dev.gugli.tags.Tags;
import dev.gugli.tags.models.Tag;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;

import java.io.File;
import java.io.IOException;
import java.util.*;

public class TagManager {

    private final Tags plugin;
    private final List<Tag> tags = new ArrayList<>();
    private final Map<UUID, String> playerTags = new HashMap<>();
    private File playerDataFile;
    private FileConfiguration playerData;

    public TagManager(Tags plugin) {
        this.plugin = plugin;
        loadTags();
        loadPlayerData();
    }

    private void loadTags() {
        tags.clear();
        File tagsFile = new File(plugin.getDataFolder(), "tags.yml");
        if (!tagsFile.exists()) plugin.saveResource("tags.yml", false);
        FileConfiguration cfg = YamlConfiguration.loadConfiguration(tagsFile);

        for (String category : Objects.requireNonNull(cfg.getConfigurationSection("tags")).getKeys(false)) {
            List<Map<?, ?>> tagList = cfg.getMapList("tags." + category);
            for (Map<?, ?> map : tagList) {
                String id = (String) map.get("id");
                String display = (String) map.get("display");
                String permission = (String) map.get("permission");
                tags.add(new Tag(id, display, permission));
            }
        }
    }

    private void loadPlayerData() {
        playerDataFile = new File(plugin.getDataFolder(), "player_tags.yml");
        if (!playerDataFile.exists()) {
            try { playerDataFile.createNewFile(); } catch (IOException ignored) {}
        }
        playerData = YamlConfiguration.loadConfiguration(playerDataFile);
        for (String key : playerData.getKeys(false)) {
            playerTags.put(UUID.fromString(key), playerData.getString(key));
        }
    }

    public void savePlayerTag(UUID uuid, String tagId) {
        playerTags.put(uuid, tagId);
        playerData.set(uuid.toString(), tagId);
        try { playerData.save(playerDataFile); } catch (IOException ignored) {}
    }

    public String getPlayerTag(UUID uuid) {
        return playerTags.getOrDefault(uuid, "");
    }

    public String getPlayerTagDisplay(UUID uuid) {
        String tagId = getPlayerTag(uuid);
        if (tagId == null || tagId.isEmpty()) return "";
        return tags.stream()
                .filter(t -> t.getId().equals(tagId))
                .map(Tag::getDisplay)
                .findFirst()
                .orElse("");
    }

    public List<Tag> getAvailableTags(Player player) {
        List<Tag> available = new ArrayList<>();
        for (Tag tag : tags) {
            if (player.hasPermission(tag.getPermission())) {
                available.add(tag);
            }
        }
        return available;
    }

    public List<Tag> getAllTags() { return tags; }

    public void createTag(String id, String display, String permission) {
        Tag tag = new Tag(id, display, permission);
        tags.add(tag);
        saveTagToFile(tag);
    }

    private void saveTagToFile(Tag tag) {
        File tagsFile = new File(plugin.getDataFolder(), "tags.yml");
        FileConfiguration cfg = YamlConfiguration.loadConfiguration(tagsFile);

        List<Map<?, ?>> customList = cfg.getMapList("tags.custom");
        Map<String, String> entry = new java.util.LinkedHashMap<>();
        entry.put("id", tag.getId());
        entry.put("display", tag.getDisplay());
        entry.put("permission", tag.getPermission());
        customList.add(entry);
        cfg.set("tags.custom", customList);

        try { cfg.save(tagsFile); } catch (IOException ignored) {}
    }

    public String generateUniqueId(String base) {
        String id = base;
        int counter = 1;
        Set<String> existing = new java.util.HashSet<>();
        for (Tag t : tags) existing.add(t.getId());
        while (existing.contains(id)) {
            id = base + counter++;
        }
        return id;
    }

    public void reload() { loadTags(); }
}
