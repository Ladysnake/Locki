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
package org.ladysnake.locki.impl;

import com.google.common.base.Preconditions;
import com.google.common.collect.ImmutableList;
import dev.onyxstudios.cca.api.v3.component.sync.AutoSyncedComponent;
import it.unimi.dsi.fastutil.objects.Reference2BooleanMap;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.network.PacketByteBuf;
import net.minecraft.server.network.ServerPlayerEntity;
import org.ladysnake.locki.DefaultInventoryNodes;
import org.ladysnake.locki.InventoryKeeper;
import org.ladysnake.locki.InventoryLock;
import org.ladysnake.locki.InventoryLockingChangeCallback;
import org.ladysnake.locki.InventoryNode;
import org.ladysnake.locki.Locki;
import org.ladysnake.locki.ModdedInventoryNodes;

import java.util.BitSet;
import java.util.Map;

public class PlayerInventoryKeeper extends InventoryKeeperBase implements AutoSyncedComponent {
    public static final int MAINHAND_SLOT = 0;
    public static final int OFFHAND_SLOT = 40;
    // the back and belt slots are provided by the BackSlot mod
    // please someone notify me if the indices change
    public static final int BACK_SLOT = 41;
    public static final int BELT_SLOT = 42;

    public static int fixSelectedSlot(PlayerEntity player, int selectedSlot) {
        InventoryKeeper inventoryKeeper = InventoryKeeper.get(player);
        if (inventoryKeeper.isLocked(DefaultInventoryNodes.MAIN_HAND)) {
            return PlayerInventoryKeeper.MAINHAND_SLOT;
        } else {
            while (inventoryKeeper.isSlotLocked(selectedSlot)) {
                selectedSlot = (selectedSlot + 1) % PlayerInventory.getHotbarSize();
            }
            return selectedSlot;
        }
    }

    private final PlayerEntity player;

    public PlayerInventoryKeeper(PlayerEntity player) {
        this.player = player;
    }

    @Override
    protected Map<InventoryNode, Reference2BooleanMap<InventoryLock>> getLocks() {
        Preconditions.checkState(!this.player.getWorld().isClient, "Locks can only be managed serverside (check !world.isClient)");
        return super.getLocks();
    }

    @Override
    public boolean shouldSyncWith(ServerPlayerEntity player) {
        return player == this.player;
    }

    @Override
    public void writeSyncPacket(PacketByteBuf buf, ServerPlayerEntity recipient) {
        buf.writeVarInt(this.cache.size());
        for (Map.Entry<InventoryNode, BitSet> entry : this.getCache().entrySet()) {
            // Could compress this by not repeating ancestors?
            buf.writeString(entry.getKey().getFullName());
            buf.writeByteArray(entry.getValue().toByteArray());
        }
    }

    @Override
    public void applySyncPacket(PacketByteBuf buf) {
        int size = buf.readVarInt();
        this.clearCache();
        for (int i = 0; i < size; i++) {
            String key = buf.readString();
            byte[] locked = buf.readByteArray();
            InventoryNode node = Locki.getNode(key);
            if (node == null) {
                Locki.LOGGER.error("Received unknown inventory node path during sync: {}", key);
            } else {
                BitSet value = BitSet.valueOf(locked);
                BitSet old = this.cache.put(node, value);
                if ((old == null || old.isEmpty()) != value.isEmpty()) {
                    InventoryLockingChangeCallback.EVENT.invoker().onLockingStateChanged(this.player, node, !value.isEmpty());
                }
            }
        }
    }

    @Override
    public boolean isLocked(InventoryNode invNode) {
        if (this.player.isCreative()) return false;
        return super.isLocked(invNode);
    }

    @Override
    public boolean isSlotLocked(int index) {
        int mainSize = player.getInventory().main.size();

        if (index > MAINHAND_SLOT && index < mainSize) {
            if (index < PlayerInventory.getHotbarSize()) {
                return this.isLocked(DefaultInventoryNodes.HOTBAR);
            } else {
                return this.isLocked(DefaultInventoryNodes.MAIN_INVENTORY);
            }
        }

        int armorIndex = index - mainSize;
        ImmutableList<InventoryNode> armorSlots = DefaultInventoryNodes.ARMOR_SLOTS;

        if (armorIndex >= 0 && armorIndex < armorSlots.size()) {
            return this.isLocked(armorSlots.get(armorIndex));
        }

        return switch (index) {
            case MAINHAND_SLOT -> this.isLocked(DefaultInventoryNodes.MAIN_HAND);
            case OFFHAND_SLOT -> this.isLocked(DefaultInventoryNodes.OFF_HAND);
            case BACK_SLOT -> this.isLocked(ModdedInventoryNodes.BACK);
            case BELT_SLOT -> this.isLocked(ModdedInventoryNodes.BELT);
            default -> false;
        };
    }

    @Override
    protected boolean propagateChange(InventoryNode invNode, InventoryLock lock, boolean locking) {
        if (super.propagateChange(invNode, lock, locking)) {
            LockiComponents.INVENTORY_KEEPER.sync(this.player);
            return true;
        }
        return false;
    }

    @Override
    protected boolean updateCachedLockState(InventoryLock lock, boolean locking, InventoryNode invNode) {
        if (super.updateCachedLockState(lock, locking, invNode)) {
            InventoryLockingChangeCallback.EVENT.invoker().onLockingStateChanged(this.player, invNode, locking);
            return true;
        }
        return false;
    }
}
