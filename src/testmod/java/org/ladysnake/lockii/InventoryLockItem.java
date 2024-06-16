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

import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.text.Text;
import net.minecraft.util.Hand;
import net.minecraft.util.TypedActionResult;
import net.minecraft.world.World;
import org.ladysnake.locki.InventoryLock;
import org.ladysnake.locki.Locki;

public class InventoryLockItem extends Item {
    public static final InventoryLock LOCK = Locki.registerLock(Lockii.id("test_item"));

    public InventoryLockItem(Settings settings) {
        super(settings);
    }

    @Override
    public TypedActionResult<ItemStack> use(World world, PlayerEntity user, Hand hand) {
        ItemStack heldStack = user.getStackInHand(hand);

        if (!world.isClient) {
            InventoryLockComponent currentDebug = heldStack.getOrDefault(InventoryLockComponent.TYPE, InventoryLockComponent.DEFAULT);

            if (user.isSneaking()) {
                InventoryLockComponent newDebug = currentDebug.cycle();
                heldStack.set(InventoryLockComponent.TYPE, newDebug);
                user.sendMessage(Text.literal("Now managing locking for " + newDebug.node()), true);
            } else {
                user.getInventory().toggleLock(LOCK, currentDebug.node());
            }
        }
        return TypedActionResult.success(heldStack);
    }
}
