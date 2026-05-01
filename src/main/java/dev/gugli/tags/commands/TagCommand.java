package dev.gugli.tags.commands;

import dev.gugli.tags.Tags;
import dev.gugli.tags.models.Tag;
import net.kyori.adventure.text.minimessage.MiniMessage;
import net.kyori.adventure.text.serializer.legacy.LegacyComponentSerializer;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import java.util.ArrayList;
import java.util.List;

public class TagCommand implements CommandExecutor, TabCompleter {

    private final Tags plugin;

    public TagCommand(Tags plugin) {
        this.plugin = plugin;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args) {
        String prefix = plugin.getConfig().getString("messages.prefix", "");

        if (args.length > 0) {

            // /tag reload
            if (args[0].equalsIgnoreCase("reload") && sender.hasPermission("tag.admin")) {
                plugin.reloadConfig();
                plugin.getTagManager().reload();
                sender.sendMessage(plugin.color(prefix + plugin.getConfig().getString("messages.reload")));
                return true;
            }

            // /tag give <player> <tagId> [quantity]
            if (args[0].equalsIgnoreCase("give") && sender.hasPermission("tag.admin")) {
                if (args.length < 3) {
                    sender.sendMessage(plugin.color(prefix + "&cUsage: /tag give <player> <tagId> [quantity]"));
                    return true;
                }

                Player target = Bukkit.getPlayer(args[1]);
                if (target == null) {
                    sender.sendMessage(plugin.color(prefix + "&cPlayer not found!"));
                    return true;
                }

                String tagId = args[2];
                Tag tag = plugin.getTagManager().getAllTags().stream()
                        .filter(t -> t.getId().equalsIgnoreCase(tagId))
                        .findFirst().orElse(null);

                if (tag == null) {
                    sender.sendMessage(plugin.color(prefix + "&cTag &f" + tagId + " &cnot found!"));
                    return true;
                }

                int quantity = 1;
                if (args.length >= 4) {
                    try {
                        quantity = Math.max(1, Math.min(64, Integer.parseInt(args[3])));
                    } catch (NumberFormatException e) {
                        sender.sendMessage(plugin.color(prefix + "&cInvalid quantity!"));
                        return true;
                    }
                }

                ItemStack nameTag = buildTagItem(tag, quantity);
                target.getInventory().addItem(nameTag);

                // Resolve display for message
                String displayResolved = resolveDisplay(tag.getDisplay());
                sender.sendMessage(plugin.color(prefix + "&aGave &f" + quantity + "x &r" + displayResolved + " &aName Tag to &f" + target.getName() + "&a."));
                target.sendMessage(plugin.color(prefix + "&aYou received &f" + quantity + "x &r" + displayResolved + " &aName Tag!"));
                return true;
            }
        }

        // /tag — open GUI (players only)
        if (!(sender instanceof Player player)) {
            sender.sendMessage("Only players can use this!");
            return true;
        }

        if (!player.hasPermission("tag.use")) {
            player.sendMessage(plugin.color(prefix + plugin.getConfig().getString("messages.no-permission")));
            return true;
        }

        plugin.getTagsGUI().open(player);
        return true;
    }

    private ItemStack buildTagItem(Tag tag, int quantity) {
        ItemStack item = new ItemStack(Material.NAME_TAG, quantity);
        ItemMeta meta = item.getItemMeta();

        String displayResolved = resolveDisplay(tag.getDisplay());
        meta.setDisplayName(displayResolved);
        meta.setLore(List.of(
                plugin.color("&8─────────────"),
                plugin.color("&7Tag ID: &f" + tag.getId()),
                plugin.color("&7Right-click to apply tag"),
                plugin.color("&8─────────────")
        ));
        // Store tag ID in item lore marker for identification
        List<String> persistentLore = new ArrayList<>(meta.getLore());
        persistentLore.add(plugin.color("&0TAG_ID:" + tag.getId()));
        meta.setLore(persistentLore);
        item.setItemMeta(meta);
        return item;
    }

    private String resolveDisplay(String display) {
        if (display.contains("<gradient") || display.contains("<rainbow") || display.contains("<color")) {
            try {
                return LegacyComponentSerializer.legacySection()
                        .serialize(MiniMessage.miniMessage().deserialize(display));
            } catch (Exception e) {
                return display;
            }
        }
        return plugin.color(display);
    }

    @Override
    public List<String> onTabComplete(CommandSender sender, Command cmd, String label, String[] args) {
        if (!sender.hasPermission("tag.admin")) return List.of();
        if (args.length == 1) return List.of("give", "reload");
        if (args.length == 2 && args[0].equalsIgnoreCase("give")) {
            return Bukkit.getOnlinePlayers().stream().map(Player::getName).toList();
        }
        if (args.length == 3 && args[0].equalsIgnoreCase("give")) {
            return plugin.getTagManager().getAllTags().stream().map(Tag::getId).toList();
        }
        if (args.length == 4 && args[0].equalsIgnoreCase("give")) {
            return List.of("1", "8", "16", "32", "64");
        }
        return List.of();
    }
}
