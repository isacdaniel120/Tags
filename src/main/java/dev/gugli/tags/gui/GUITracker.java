package dev.gugli.tags.gui;

import org.bukkit.entity.Player;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class GUITracker {

    public enum GUIType { TAGS }

    private final Map<UUID, GUIType> states = new HashMap<>();

    public void trackTags(Player player) { states.put(player.getUniqueId(), GUIType.TAGS); }
    public void remove(Player player) { states.remove(player.getUniqueId()); }
    public GUIType getType(Player player) { return states.get(player.getUniqueId()); }
    public boolean isInGUI(Player player) { return states.containsKey(player.getUniqueId()); }
}
