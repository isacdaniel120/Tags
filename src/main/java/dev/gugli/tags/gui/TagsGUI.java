package dev.gugli.tags.gui;

import dev.gugli.tags.Tags;
import dev.gugli.tags.managers.TagManager;
import dev.gugli.tags.models.Tag;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemFlag;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import java.util.ArrayList;
import java.util.List;

public class TagsGUI {

    private final Tags plugin;
    private final TagManager tagManager;

    public TagsGUI(Tags plugin, TagManager tagManager) {
        this.plugin = plugin;
        this.tagManager = tagManager;
    }

    public void open(Player player) {
        List<Tag> available = tagManager.getAvailableTags(player);
        String currentTagId = tagManager.getPlayerTag(player.getUniqueId());

        Inventory inv = Bukkit.createInventory(null, 54,
                plugin.color("&8» &b&lTags &8| &7Select a tag"));

        fillBorder(inv);

        int[] slots = {10,11,12,13,14,15,16,19,20,21,22,23,24,25,28,29,30,31,32,33,34};
        for (int i = 0; i < slots.length && i < available.size(); i++) {
            inv.setItem(slots[i], buildTagItem(available.get(i), currentTagId));
        }

        ItemStack remove = new ItemStack(Material.BARRIER);
        ItemMeta removeMeta = remove.getItemMeta();
        removeMeta.setDisplayName(plugin.color("&c&lRemove Tag"));
        removeMeta.setLore(List.of(plugin.color("&7Click to remove your current tag")));
        remove.setItemMeta(removeMeta);
        inv.setItem(49, remove);

        ItemStack current = new ItemStack(Material.NAME_TAG);
        ItemMeta currentMeta = current.getItemMeta();
        currentMeta.setDisplayName(plugin.color("&e&lCurrent Tag"));
        String display = tagManager.getPlayerTagDisplay(player.getUniqueId());
        currentMeta.setLore(List.of(
                plugin.color("&7Tag: " + (display.isEmpty() ? "&7None" : plugin.color(display)))
        ));
        current.setItemMeta(currentMeta);
        inv.setItem(47, current);

        // Admin-only: Create Tag button
        if (player.hasPermission("tag.admin")) {
            ItemStack create = new ItemStack(Material.NAME_TAG);
            ItemMeta createMeta = create.getItemMeta();
            createMeta.setDisplayName(plugin.color("&a&l+ Create Tag"));
            createMeta.setLore(List.of(
                    plugin.color("&7Click to create a new tag."),
                    plugin.color("&7Supports & colors, gradients & emojis.")
            ));
            create.setItemMeta(createMeta);
            inv.setItem(45, create);
        }

        player.openInventory(inv);
        plugin.getGuiTracker().trackTags(player);
    }

    private ItemStack buildTagItem(Tag tag, String currentTagId) {
        boolean selected = tag.getId().equals(currentTagId);
        ItemStack item = new ItemStack(selected ? Material.LIME_DYE : Material.GRAY_DYE);
        ItemMeta meta = item.getItemMeta();
        meta.setDisplayName(plugin.color(tag.getDisplay()));
        List<String> lore = new ArrayList<>();
        lore.add(plugin.color("&8─────────────"));
        lore.add(plugin.color("&7Preview: " + plugin.color(tag.getDisplay()) + " &fPlayerName"));
        lore.add(plugin.color("&8─────────────"));
        if (selected) {
            lore.add(plugin.color("&a▶ Currently selected"));
            meta.addEnchant(Enchantment.LUCK_OF_THE_SEA, 1, true);
            meta.addItemFlags(ItemFlag.HIDE_ENCHANTS);
        } else {
            lore.add(plugin.color("&eClick to select!"));
        }
        meta.setLore(lore);
        item.setItemMeta(meta);
        return item;
    }

    private void fillBorder(Inventory inv) {
        ItemStack black = makeGlass(Material.BLACK_STAINED_GLASS_PANE);
        ItemStack gray = makeGlass(Material.GRAY_STAINED_GLASS_PANE);
        for (int i = 0; i < 9; i++) inv.setItem(i, black);
        for (int i = 45; i < 54; i++) {
            if (inv.getItem(i) == null) inv.setItem(i, gray);
        }
        for (int row = 1; row <= 3; row++) {
            inv.setItem(row * 9, gray);
            inv.setItem(row * 9 + 8, gray);
        }
        inv.setItem(36, black); inv.setItem(37, black); inv.setItem(38, black);
        inv.setItem(39, black); inv.setItem(43, black); inv.setItem(44, black);
        inv.setItem(40, gray); inv.setItem(41, gray); inv.setItem(42, gray);
    }

    private ItemStack makeGlass(Material mat) {
        ItemStack item = new ItemStack(mat);
        ItemMeta meta = item.getItemMeta();
        meta.setDisplayName(" ");
        item.setItemMeta(meta);
        return item;
    }
}
