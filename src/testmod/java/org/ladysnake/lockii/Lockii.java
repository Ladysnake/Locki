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
package org.ladysnake.lockii;

import net.fabricmc.api.ModInitializer;
import net.minecraft.item.Item;
import net.minecraft.registry.Registries;
import net.minecraft.registry.Registry;
import net.minecraft.util.Identifier;

public final class Lockii implements ModInitializer {
    public static Identifier id(String path) {
        return Identifier.of("lockii", path);
    }

    @Override
    public void onInitialize() {
        Registry.register(Registries.DATA_COMPONENT_TYPE, id("debug"), InventoryLockComponent.TYPE);
        Registry.register(Registries.ITEM, id("inventory_lock"), new InventoryLockItem(new Item.Settings().component(InventoryLockComponent.TYPE, InventoryLockComponent.DEFAULT)));
    }
}
