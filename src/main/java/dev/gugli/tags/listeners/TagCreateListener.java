package dev.gugli.tags.listeners;

import dev.gugli.tags.Tags;
import dev.gugli.tags.managers.TagManager;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.minimessage.MiniMessage;
import net.kyori.adventure.text.serializer.legacy.LegacyComponentSerializer;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.AsyncPlayerChatEvent;
import org.bukkit.event.player.PlayerQuitEvent;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class TagCreateListener implements Listener {

    private final Tags plugin;
    private final TagManager tagManager;
    // Players currently in tag creation mode
    private final Map<UUID, Boolean> awaitingInput = new HashMap<>();

    public TagCreateListener(Tags plugin, TagManager tagManager) {
        this.plugin = plugin;
        this.tagManager = tagManager;
    }

    public void awaitTagInput(Player player) {
        awaitingInput.put(player.getUniqueId(), true);
        player.sendMessage(plugin.color("&8[&bTags&8] &7Type the tag display in chat. Supports:"));
        player.sendMessage(plugin.color("&8  &f& codes &7→ &a&lExample"));
        player.sendMessage(plugin.color("&8  &fGradient &7→ &d<gradient:#ff0000:#0000ff>Text</gradient>"));
        player.sendMessage(plugin.color("&8  &fEmojis &7→ ⭐ 🔥 💎 etc."));
        player.sendMessage(plugin.color("&8  &fType &ccancel &7to abort."));
    }

    public boolean isAwaiting(UUID uuid) {
        return awaitingInput.containsKey(uuid);
    }

    @EventHandler(priority = EventPriority.LOWEST)
    public void onChat(AsyncPlayerChatEvent e) {
        Player player = e.getPlayer();
        if (!awaitingInput.containsKey(player.getUniqueId())) return;

        e.setCancelled(true);
        awaitingInput.remove(player.getUniqueId());

        String input = e.getMessage().trim();

        if (input.equalsIgnoreCase("cancel")) {
            player.sendMessage(plugin.color("&8[&bTags&8] &cTag creation cancelled."));
            plugin.getServer().getScheduler().runTask(plugin, () -> plugin.getTagsGUI().open(player));
            return;
        }

        // Parse the display: support legacy & codes and MiniMessage gradient
        String displayLegacy;
        String displayMini = null;

        if (input.contains("<gradient") || input.contains("<rainbow") || input.contains("<color")) {
            // MiniMessage format
            try {
                MiniMessage mm = MiniMessage.miniMessage();
                Component comp = mm.deserialize(input);
                displayLegacy = LegacyComponentSerializer.legacySection().serialize(comp);
                displayMini = input; // store original MiniMessage string
            } catch (Exception ex) {
                player.sendMessage(plugin.color("&8[&bTags&8] &cInvalid gradient format! Try: <gradient:#ff0000:#00ff00>Text</gradient>"));
                plugin.getServer().getScheduler().runTask(plugin, () -> plugin.getTagsGUI().open(player));
                return;
            }
        } else {
            // Legacy & color codes
            displayLegacy = plugin.color(input);
        }

        // Generate unique id from stripped text
        String stripped = displayLegacy.replaceAll("§.", "").replaceAll("[^a-zA-Z0-9_]", "").toLowerCase();
        if (stripped.isEmpty()) stripped = "custom";
        String id = tagManager.generateUniqueId(stripped);

        // Add to TagManager and save
        plugin.getServer().getScheduler().runTask(plugin, () -> {
            tagManager.createTag(id, displayMini != null ? displayMini : input, "tag.custom." + id);
            player.sendMessage(plugin.color("&8[&bTags&8] &aTag &r" + displayLegacy + " &acreated! ID: &f" + id));
            player.sendMessage(plugin.color("&8[&bTags&8] &7Give permission &ftag.custom." + id + " &7to players."));
            plugin.getTagsGUI().open(player);
        });
    }

    @EventHandler
    public void onQuit(PlayerQuitEvent e) {
        awaitingInput.remove(e.getPlayer().getUniqueId());
    }
}
