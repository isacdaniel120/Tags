package dev.gugli.tags;

import dev.gugli.tags.commands.TagCommand;
import dev.gugli.tags.gui.GUITracker;
import dev.gugli.tags.gui.TagsGUI;
import dev.gugli.tags.listeners.ChatListener;
import dev.gugli.tags.listeners.GUIListener;
import dev.gugli.tags.listeners.TagCreateListener;
import dev.gugli.tags.managers.TagManager;
import org.bukkit.ChatColor;
import org.bukkit.plugin.java.JavaPlugin;

public class Tags extends JavaPlugin {

    private TagManager tagManager;
    private TagsGUI tagsGUI;
    private GUITracker guiTracker;
    private TagCreateListener tagCreateListener;

    @Override
    public void onEnable() {
        saveDefaultConfig();
        saveResource("tags.yml", false);
        getDataFolder().mkdirs();

        guiTracker = new GUITracker();
        tagManager = new TagManager(this);
        tagsGUI = new TagsGUI(this, tagManager);
        tagCreateListener = new TagCreateListener(this, tagManager);

        getServer().getPluginManager().registerEvents(new GUIListener(this, tagManager, tagCreateListener), this);
        getServer().getPluginManager().registerEvents(new ChatListener(this, tagManager), this);
        getServer().getPluginManager().registerEvents(tagCreateListener, this);

        TagCommand tagCmd = new TagCommand(this);
        getCommand("tag").setExecutor(tagCmd);
        getCommand("tag").setTabCompleter(tagCmd);

        getLogger().info("Tags enabled!");
    }

    @Override
    public void onDisable() {
        getLogger().info("Tags disabled.");
    }

    public String color(String msg) {
        return ChatColor.translateAlternateColorCodes('&', msg);
    }

    public TagManager getTagManager() { return tagManager; }
    public TagsGUI getTagsGUI() { return tagsGUI; }
    public GUITracker getGuiTracker() { return guiTracker; }
    public TagCreateListener getTagCreateListener() { return tagCreateListener; }
}
