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
package io.github.ladysnake.locki.impl;

import com.google.common.base.Preconditions;
import dev.onyxstudios.cca.api.v3.component.sync.AutoSyncedComponent;
import io.github.ladysnake.locki.DefaultInventoryNodes;
import io.github.ladysnake.locki.InventoryLock;
import io.github.ladysnake.locki.InventoryNode;
import io.github.ladysnake.locki.Locki;
import it.unimi.dsi.fastutil.objects.Reference2BooleanMap;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.network.PacketByteBuf;
import net.minecraft.server.network.ServerPlayerEntity;

import java.util.BitSet;
import java.util.Map;

public class PlayerInventoryKeeper extends InventoryKeeperBase implements AutoSyncedComponent {
    public static final int MAINHAND_SLOT = 0;
    public static final int OFFHAND_SLOT = 40;

    /**A lock id that is used clientside when a slot is locked by the server*/
    private static final int CLIENT_LOCK = 0;

    private final PlayerEntity player;

    public PlayerInventoryKeeper(PlayerEntity player) {
        this.player = player;
    }

    @Override
    protected Map<InventoryNode, Reference2BooleanMap<InventoryLock>> getLocks() {
        Preconditions.checkState(!this.player.world.isClient, "Locks can only be managed serverside (check !world.isClient)");
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
            // Could compress this by
            buf.writeString(entry.getKey().getFullName());
            buf.writeBoolean(!entry.getValue().isEmpty());
        }
    }

    @Override
    public void applySyncPacket(PacketByteBuf buf) {
        int size = buf.readVarInt();
        this.clearCache();
        for (int i = 0; i < size; i++) {
            String key = buf.readString();
            boolean locked = buf.readBoolean();
            InventoryNode node = Locki.getNode(key);
            if (node == null) {
                Locki.LOGGER.error("Received unknown inventory node path during sync: {}", key);
            } else {
                this.getCache().computeIfAbsent(node, s -> new BitSet()).set(CLIENT_LOCK, locked);
            }
        }
    }

    @Override
    public boolean isSlotLocked(int index) {
        int mainSize = player.inventory.main.size();

        if (this.isLocked(DefaultInventoryNodes.MAIN_INVENTORY) && index > MAINHAND_SLOT && index < mainSize) {
            return true;
        }

        int armorSize = player.inventory.armor.size();

        if (this.isLocked(DefaultInventoryNodes.ARMOR) && index >= mainSize && index < mainSize + armorSize) {
            return true;
        }

        return (index < 9 && this.isLocked(DefaultInventoryNodes.MAIN_HAND)) || (index == OFFHAND_SLOT && this.isLocked(DefaultInventoryNodes.OFF_HAND));
    }

    @Override
    protected boolean updateLock(InventoryLock lock, InventoryNode invNode, boolean locking) {
        if (super.updateLock(lock, invNode, locking)) {
            LockiComponents.INVENTORY_KEEPER.sync(this.player);
            return true;
        }
        return false;
    }
}
