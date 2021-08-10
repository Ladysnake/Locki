/*
 * Locki
 * Copyright (C) 2021 Ladysnake
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
package io.github.ladysnake.lockii;

import io.github.ladysnake.locki.InventoryLock;
import io.github.ladysnake.locki.InventoryNode;
import io.github.ladysnake.locki.Locki;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.text.LiteralText;
import net.minecraft.util.Hand;
import net.minecraft.util.TypedActionResult;
import net.minecraft.world.World;

public class InventoryLockItem extends Item {
    public static final InventoryLock LOCK = Locki.registerLock(Lockii.id("test_item"));
    private static final InventoryNode[] ALL_DEFAULT_NODES = Locki.streamNodeNames().map(Locki::getNode).toArray(InventoryNode[]::new);

    public InventoryLockItem(Settings settings) {
        super(settings);
    }

    @Override
    public TypedActionResult<ItemStack> use(World world, PlayerEntity user, Hand hand) {
        ItemStack heldStack = user.getStackInHand(hand);

        if (!world.isClient) {
            NbtCompound data = heldStack.getOrCreateSubNbt("lockii");
            int currentDebug = data.getInt("debug");

            if (user.isSneaking()) {
                int newDebug = (currentDebug + 1) % ALL_DEFAULT_NODES.length;
                data.putInt("debug", newDebug);
                user.sendMessage(new LiteralText("Now managing locking for " + ALL_DEFAULT_NODES[newDebug]), true);
            } else {
                LOCK.toggle(user, ALL_DEFAULT_NODES[currentDebug]);
            }
        }
        return TypedActionResult.success(heldStack);
    }
}
