package io.github.ladysnake.locki;

public final class DefaultInventoryNodes {
    /**
     * Controls access to the entirety of a player's inventory
     */
    public static final String INVENTORY      = "inventory";
    /**
     * Controls access to {@link net.minecraft.entity.player.PlayerInventory#main}
     */
    public static final String MAIN_INVENTORY = INVENTORY       + ".main";

    public static final String HANDS          = INVENTORY       + ".hands";
    public static final String MAIN_HAND      = HANDS           + ".main_hand";
    public static final String OFF_HAND       = HANDS           + ".off_hand";

    public static final String ARMOR          = INVENTORY       + ".armor";

    public static final String CRAFTING_GRID  = INVENTORY       + ".crafting_grid";
}
