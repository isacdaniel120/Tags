package dev.gugli.tags.listeners;

import dev.gugli.tags.Tags;
import dev.gugli.tags.managers.TagManager;
import dev.gugli.tags.models.Tag;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.ClickType;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryCloseEvent;

import java.util.List;

public class GUIListener implements Listener {

    private final Tags plugin;
    private final TagManager tagManager;
    private final TagCreateListener tagCreateListener;

    public GUIListener(Tags plugin, TagManager tagManager, TagCreateListener tagCreateListener) {
        this.plugin = plugin;
        this.tagManager = tagManager;
        this.tagCreateListener = tagCreateListener;
    }

    @EventHandler
    public void onInventoryClick(InventoryClickEvent e) {
        if (!(e.getWhoClicked() instanceof Player player)) return;
        String title = e.getView().getTitle();

        if (title.contains("Tags")) {
            e.setCancelled(true);
            handleTagsGUI(player, e.getRawSlot(), e.getClick());
        }
    }

    private void handleTagsGUI(Player player, int slot, ClickType click) {
        String prefix = plugin.getConfig().getString("messages.prefix", "");

        // Create Tag button (admin only)
        if (slot == 45 && player.hasPermission("tag.admin")) {
            player.closeInventory();
            tagCreateListener.awaitTagInput(player);
            return;
        }

        // Remove tag button
        if (slot == 49) {
            tagManager.savePlayerTag(player.getUniqueId(), "");
            player.sendMessage(plugin.color(prefix + plugin.getConfig().getString("messages.no-tag")));
            plugin.getTagsGUI().open(player);
            return;
        }

        int[] slots = {10,11,12,13,14,15,16,19,20,21,22,23,24,25,28,29,30,31,32,33,34};
        List<Tag> available = tagManager.getAvailableTags(player);
        for (int i = 0; i < slots.length && i < available.size(); i++) {
            if (slot == slots[i]) {
                Tag tag = available.get(i);
                tagManager.savePlayerTag(player.getUniqueId(), tag.getId());
                player.sendMessage(plugin.color(prefix + plugin.getConfig().getString("messages.tag-selected", "")
                        .replace("{tag}", plugin.color(tag.getDisplay()))));
                plugin.getTagsGUI().open(player);
                return;
            }
        }
    }

    @EventHandler
    public void onInventoryClose(InventoryCloseEvent e) {
        if (e.getPlayer() instanceof Player player) {
            plugin.getGuiTracker().remove(player);
        }
    }
}
