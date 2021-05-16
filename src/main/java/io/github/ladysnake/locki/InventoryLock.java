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

import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.util.Identifier;

public final class InventoryLock {
    private final Identifier id;
    private final int rawId;

    InventoryLock(Identifier id, int rawId) {
        this.id = id;
        this.rawId = rawId;
    }

    public Identifier getId() {
        return id;
    }

    public int getRawId() {
        return this.rawId;
    }

    /**
     * Adds a lock on the specified inventory node and all of its children.
     *
     * <p>If this lock was already effecting children of the target node, that previous state will be cleared.
     *
     * @param player the player which inventory is getting modified
     * @param invNode the inventory node describing the slots getting locked
     * @see DefaultInventoryNodes
     */
    public void lock(PlayerEntity player, InventoryNode invNode) {
        InventoryKeeper.get(player).addLock(this, invNode);
    }

    /**
     * Removes a lock from the specified inventory node and all of its children.
     *
     * <p>If this lock was already effecting children of the target node, that previous state will be cleared.
     *
     * @param player the player which inventory is getting modified
     * @param invNode the inventory node describing the slots getting locked
     * @see DefaultInventoryNodes
     */
    public void unlock(PlayerEntity player, InventoryNode invNode) {
        InventoryKeeper.get(player).removeLock(this, invNode);
    }

    /**
     * @return {@code true} if this lock is actively locking or keeping unlocked {@code invNode}
     */
    public boolean isPlacedOn(PlayerEntity player, InventoryNode invNode) {
        return InventoryKeeper.get(player).getAllPlacedLocks(invNode).contains(this);
    }

    /**
     * @return {@code true} if this lock is locking the specified {@code invNode} or one of its parents
     */
    public boolean isLocking(PlayerEntity player, InventoryNode invNode) {
        return InventoryKeeper.get(player).isLockedBy(this, invNode);
    }

    public void toggle(PlayerEntity player, InventoryNode invNode) {
        InventoryKeeper keeper = InventoryKeeper.get(player);
        if (keeper.isLockedBy(this, invNode)) {
            keeper.removeLock(this, invNode);
        } else {
            keeper.addLock(this, invNode);
        }
    }

    @Override
    public String toString() {
        return this.id.toString();
    }
}
