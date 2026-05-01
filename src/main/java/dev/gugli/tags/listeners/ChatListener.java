package dev.gugli.tags.listeners;

import dev.gugli.tags.Tags;
import dev.gugli.tags.managers.TagManager;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.AsyncPlayerChatEvent;

public class ChatListener implements Listener {

    private final Tags plugin;
    private final TagManager tagManager;

    public ChatListener(Tags plugin, TagManager tagManager) {
        this.plugin = plugin;
        this.tagManager = tagManager;
    }

    @EventHandler(priority = EventPriority.HIGHEST)
    public void onChat(AsyncPlayerChatEvent e) {
        if (!plugin.getConfig().getBoolean("tags.enabled", true)) return;

        String tagDisplay = tagManager.getPlayerTagDisplay(e.getPlayer().getUniqueId());
        String format = plugin.getConfig().getString("tags.chat-format", "{tag} &f{player}&7: &f{message}");

        if (tagDisplay.isEmpty()) {
            format = format.replace("{tag} ", "").replace("{tag}", "");
        } else {
            format = format.replace("{tag}", plugin.color(tagDisplay));
        }

        format = format.replace("{player}", e.getPlayer().getName());
        format = format.replace("{message}", "%2$s");
        e.setFormat(plugin.color(format));
    }
}
