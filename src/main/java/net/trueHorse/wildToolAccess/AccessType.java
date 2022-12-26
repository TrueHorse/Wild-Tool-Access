package net.trueHorse.wildToolAccess;

public enum AccessType {

    TOOL("tool"),
    SWORD("sword"),
    RANGED_WEAPON("ranged weapon"),
    POTION("potion"),
    BUCKET("bucket"),
    STUFF("stuff");

    public final String id;

    AccessType(String id) {
        this.id = id;
    }
}
