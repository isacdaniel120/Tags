package dev.gugli.tags.models;

public class Tag {

    private final String id;
    private final String display;
    private final String permission;

    public Tag(String id, String display, String permission) {
        this.id = id;
        this.display = display;
        this.permission = permission;
    }

    public String getId() { return id; }
    public String getDisplay() { return display; }
    public String getPermission() { return permission; }
}
