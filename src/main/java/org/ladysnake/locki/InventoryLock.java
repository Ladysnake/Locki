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

import net.minecraft.entity.EquipmentSlot;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.util.Identifier;

/**
 * A lock that can be applied to one or more player inventories, selectively
 * affecting slots described by {@link InventoryNode}s.
 *
 * @see Locki#registerLock(Identifier)
 * @see Locki#getLock(Identifier)
 */
public final class InventoryLock {
    private final Identifier id;
    private final int rawId;
    private final boolean persistent;

    InventoryLock(Identifier id, int rawId, boolean persistent) {
        this.id = id;
        this.rawId = rawId;
        this.persistent = persistent;
    }

    /**
     * @return the identifier with which this lock was registered
     */
    public Identifier getId() {
        return id;
    }

    /**
     * Gets the numerical identifier uniquely representing this lock in the current session.
     *
     * <p>The returned id is not saved to disk nor synchronized between clients and servers.
     * It should only be used for identity checks within a single game session.
     *
     * @return this lock's raw identifier
     */
    public int getRawId() {
        return this.rawId;
    }

    /**
     * @return {@code true} if this lock should be saved with the player, {@code false} otherwise
     */
    public boolean shouldSave() {
        return this.persistent;
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
     * Applies this lock to the specified equipment slot.
     *
     * <p>This method works as a shorthand for {@code lock(player, DefaultInventoryNodes.get(equipmentSlot)}.
     *
     * @param player the player which inventory is getting modified
     * @param equipmentSlot the {@link EquipmentSlot} describing the slot getting locked
     * @see DefaultInventoryNodes
     */
    public void lock(PlayerEntity player, EquipmentSlot equipmentSlot) {
        this.lock(player, DefaultInventoryNodes.get(equipmentSlot));
    }

    /**
     * Applies this lock to a player's whole inventory.
     *
     * <p>If this lock was already effecting children of the target node, that previous state will be cleared.
     *
     * <p>This method works as a shorthand for {@code lock(player, DefaultInventoryNodes.INVENTORY}.
     *
     * @param player the player which inventory should be locked
     */
    public void lockInventory(PlayerEntity player) {
        this.lock(player, DefaultInventoryNodes.INVENTORY);
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
     * Frees the specified equipment slot from this lock.
     *
     * <p>This method works as a shorthand for {@code unlock(player, DefaultInventoryNodes.get(equipmentSlot)}.
     *
     * @param player the player which inventory is getting modified
     * @param equipmentSlot the {@link EquipmentSlot} describing the slot getting unlocked
     * @see DefaultInventoryNodes
     */
    public void unlock(PlayerEntity player, EquipmentSlot equipmentSlot) {
        this.unlock(player, DefaultInventoryNodes.get(equipmentSlot));
    }

    /**
     * Frees a player's whole inventory from this lock.
     *
     * <p>This method works as a shorthand for {@code unlock(player, DefaultInventoryNodes.INVENTORY}.
     *
     * @param player the player which inventory should be unlocked
     */
    public void unlockInventory(PlayerEntity player) {
        this.unlock(player, DefaultInventoryNodes.INVENTORY);
    }

    /**
     * Checks if {@link #lock(PlayerEntity, InventoryNode)} or {@link #unlock(PlayerEntity, InventoryNode)} has been
     * called for the given {@code player} and {@code invNode} and not been cleared since.
     *
     * @param player the player which inventory is being checked
     * @param invNode the node describing the slots to check for locking
     * @return {@code true} if this lock is actively locking or keeping unlocked {@code invNode}
     */
    public boolean isPlacedOn(PlayerEntity player, InventoryNode invNode) {
        return InventoryKeeper.get(player).getAllPlacedLocks(invNode).contains(this);
    }

    /**
     * Checks if this lock is directly or transitively applying to a set of inventory slots.
     *
     * @param player the player which inventory is being checked
     * @param invNode the node describing the slots to check for locking
     * @return {@code true} if this lock is locking the specified {@code invNode} or one of its ancestors
     */
    public boolean isLocking(PlayerEntity player, InventoryNode invNode) {
        return InventoryKeeper.get(player).isLockedBy(this, invNode);
    }

    /**
     * If this lock is currently {@linkplain #isLocking(PlayerEntity, InventoryNode) locking} the given node,
     * disables it; otherwise, locks {@code invNode}.
     *
     * @param player the player which inventory to affect
     * @param invNode the node describing the slots to lock or unlock
     */
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
        return "🔒[" + this.id.toString() + "]";
    }
}
