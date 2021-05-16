package io.github.ladysnake.locki;

import net.minecraft.entity.player.PlayerInventory;

public final class DefaultInventoryNodes {
    /**
     * Controls access to the entirety of a player's inventory
     */
    public static final InventoryNode INVENTORY = Locki.registerNode(InventoryNode.ROOT, "inventory");
    /**
     * Controls access to {@link PlayerInventory#main}
     */
    public static final InventoryNode MAIN_INVENTORY = Locki.registerNode(INVENTORY, "main");

    public static final InventoryNode HANDS          = Locki.registerNode(INVENTORY, "hands");
    public static final InventoryNode MAIN_HAND      = Locki.registerNode(HANDS, "main_hand");
    public static final InventoryNode OFF_HAND       = Locki.registerNode(HANDS, "off_hand");

    public static final InventoryNode ARMOR          = Locki.registerNode(INVENTORY, "armor");

    public static final InventoryNode CRAFTING_GRID  = Locki.registerNode(INVENTORY, "crafting_grid");

    static void init() {
        // NO-OP
    }
}
