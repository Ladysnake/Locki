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
import net.minecraft.entity.player.PlayerEntity;

import java.util.Set;

public interface InventoryKeeper {
    static InventoryKeeper get(PlayerEntity player) {
        return LockiComponents.INVENTORY_KEEPER.get(player);
    }

    boolean isLocked(InventoryNode invNode);

    boolean isLockedBy(InventoryLock lock, InventoryNode invNode);

    void addLock(InventoryLock lock, InventoryNode invNode);

    void removeLock(InventoryLock lock, InventoryNode invNode);

    /**
     * Rebuilds the lock cache
     */
    void forceRefresh();

    boolean isSlotLocked(int slot);

    Set<InventoryLock> getAllPlacedLocks(InventoryNode invNode);
}
