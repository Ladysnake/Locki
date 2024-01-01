/*
 * Locki
 * Copyright (C) 2021-2024 Ladysnake
 *
 * This program is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Lesser General Public
 * License as published by the Free Software Foundation; either
 * version 3 of the License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 * Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License
 * along with this program; If not, see <https://www.gnu.org/licenses>.
 */
package org.ladysnake.locki;

import com.google.common.collect.ImmutableList;
import net.minecraft.entity.EquipmentSlot;
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
    /**
     * Controls access to the eight last slots of a player's hotbar. The first one is controlled by {@link #MAIN_HAND}.
     * If {@link #MAIN_HAND} is locked, those slots will only be accessible through the inventory screen.
     */
    public static final InventoryNode HOTBAR         = Locki.registerNode(MAIN_INVENTORY, "hotbar");

    public static final InventoryNode HANDS          = Locki.registerNode(INVENTORY, "hands");
    /**
     * Controls access to the first slot of a player's hotbar, as well as use of the hotbar in-world.
     */
    public static final InventoryNode MAIN_HAND      = Locki.registerNode(HANDS, "main_hand");
    public static final InventoryNode OFF_HAND       = Locki.registerNode(HANDS, "off_hand");

    public static final InventoryNode ARMOR          = Locki.registerNode(INVENTORY, "armor");
    public static final InventoryNode FEET           = Locki.registerNode(ARMOR, "feet");
    public static final InventoryNode LEGS           = Locki.registerNode(ARMOR, "legs");
    public static final InventoryNode CHEST          = Locki.registerNode(ARMOR, "chest");
    public static final InventoryNode HEAD           = Locki.registerNode(ARMOR, "head");
    public static final ImmutableList<InventoryNode> ARMOR_SLOTS = ImmutableList.of(FEET, LEGS, CHEST, HEAD);

    public static final InventoryNode CRAFTING       = Locki.registerNode(INVENTORY, "crafting");
    public static final InventoryNode CRAFTING_GRID  = Locki.registerNode(CRAFTING, "grid");
    public static final InventoryNode CRAFTING_BOOK  = Locki.registerNode(CRAFTING, "book");

    public static InventoryNode get(EquipmentSlot equipmentSlot) {
        return switch (equipmentSlot) {
            case MAINHAND -> MAIN_HAND;
            case OFFHAND -> OFF_HAND;
            case FEET -> FEET;
            case LEGS -> LEGS;
            case CHEST -> CHEST;
            case HEAD -> HEAD;
        };
    }

    static void init() {
        // NO-OP
    }
}
