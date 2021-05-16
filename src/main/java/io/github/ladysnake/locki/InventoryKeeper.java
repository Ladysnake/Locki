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
package io.github.ladysnake.locki;

import io.github.ladysnake.locki.impl.LockiComponents;
import net.minecraft.entity.EquipmentSlot;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.player.PlayerInventory;

import java.util.Set;

/**
 * Component managing locks on a given player's inventory.
 */
public interface InventoryKeeper {
    /**
     * @return the {@link InventoryKeeper} managing locks on {@code player}'s inventory
     */
    static InventoryKeeper get(PlayerEntity player) {
        return LockiComponents.INVENTORY_KEEPER.get(player);
    }

    /**
     * Checks for locks directly or transitively applying to a set of inventory slots.
     *
     * @param invNode the node describing the slots to check for locking
     * @return {@code true} if one or more locks are applied to {@code invNode} or one of its ancestors
     */
    boolean isLocked(InventoryNode invNode);

    /**
     * Checks for locks directly or transitively applying to an inventory slot.
     *
     * <p>This method only works for vanilla slots. Modded inventory slots must use
     * {@link #isLocked(InventoryNode)} with an adequate node.
     *
     * @param slot a valid slot index as accepted by {@link PlayerInventory#getStack(int)}
     * @return {@code true} if one or more locks are applied to {@code slot}
     */
    boolean isSlotLocked(int slot);

    /**
     * Checks for a specific lock directly or transitively applying to a set of inventory slots.
     *
     * @param lock    the lock which effect to check
     * @param invNode the node describing the slots to check for locking
     * @return {@code true} if {@code lock} is applied to {@code invNode} or one of its ancestors
     */
    boolean isLockedBy(InventoryLock lock, InventoryNode invNode);

    /**
     * @see InventoryLock#lock(PlayerEntity, InventoryNode)
     */
    void addLock(InventoryLock lock, InventoryNode invNode);

    /**
     * @see InventoryLock#unlock(PlayerEntity, InventoryNode)
     */
    void removeLock(InventoryLock lock, InventoryNode invNode);

    /**
     * Rebuilds the lock cache
     */
    void forceRefresh();

    /**
     * Returns the set of locks that have been directly placed on a set of slots.
     *
     * <p>The locks in the returned set may be either locking or keeping the slots unlocked.
     *
     * @param invNode the node describing the slots for which to retrieve placed locks
     * @return all locks that have been directly placed on {@code invNode}
     */
    Set<InventoryLock> getAllPlacedLocks(InventoryNode invNode);
}
