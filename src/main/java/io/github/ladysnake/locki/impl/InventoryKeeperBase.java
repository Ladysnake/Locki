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

import com.google.common.annotations.VisibleForTesting;
import dev.onyxstudios.cca.api.v3.component.Component;
import io.github.ladysnake.locki.InventoryKeeper;
import io.github.ladysnake.locki.InventoryLock;
import io.github.ladysnake.locki.InventoryNode;
import io.github.ladysnake.locki.Locki;
import it.unimi.dsi.fastutil.objects.Reference2BooleanMap;
import it.unimi.dsi.fastutil.objects.Reference2BooleanMaps;
import it.unimi.dsi.fastutil.objects.Reference2BooleanOpenHashMap;
import it.unimi.dsi.fastutil.objects.Reference2ObjectOpenHashMap;
import net.fabricmc.fabric.api.util.NbtType;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.Tag;
import net.minecraft.util.Identifier;
import org.jetbrains.annotations.Nullable;

import java.util.*;

public class InventoryKeeperBase implements Component, InventoryKeeper {

    private final Map<InventoryNode, Reference2BooleanMap<InventoryLock>> locks = new TreeMap<>();
    protected final Map<InventoryNode, BitSet> cache = new Reference2ObjectOpenHashMap<>();

    @Override
    public boolean isLocked(InventoryNode invNode) {
        return !this.lookup(invNode).isEmpty();
    }

    @Override
    public boolean isLockedBy(InventoryLock lock, InventoryNode invNode) {
        return lookup(invNode).get(lock.getRawId());
    }

    @Override
    public Set<InventoryLock> getAllPlacedLocks(InventoryNode invNode) {
        return getLocks().get(invNode).keySet();
    }

    @Override
    public void addLock(InventoryLock lock, InventoryNode invNode) {
        this.updateLock(lock, invNode, true);
    }

    @Override
    public void removeLock(InventoryLock lock, InventoryNode invNode) {
        this.updateLock(lock, invNode, false);
    }

    @Override
    public boolean isSlotLocked(int slot) {
        throw new UnsupportedOperationException();
    }

    protected boolean updateLock(InventoryLock lock, InventoryNode invNode, boolean locking) {
        if (this.doUpdateLock(lock, invNode, locking)) {
            // drop all the child locks
            for (InventoryNode subNode : invNode.getAllChildren()) {
                Reference2BooleanMap<InventoryLock> subLocks = this.getLocks().get(subNode);
                if (subLocks != null) subLocks.removeBoolean(lock);
            }
            this.propagateChange(invNode, lock, locking);
            return true;
        }
        return false;
    }

    private boolean doUpdateLock(InventoryLock lock, InventoryNode invNode, boolean locking) {
        Reference2BooleanMap<InventoryLock> lockSet = this.getLocks().computeIfAbsent(invNode, n -> new Reference2BooleanOpenHashMap<>());
        @SuppressWarnings("deprecation")    // using the deprecated overload on purpose to check former presence
        @Nullable Boolean previous = lockSet.put(lock, (Boolean) locking);
        return previous == null || previous != locking;
    }

    protected void propagateChange(InventoryNode inventoryNode, InventoryLock lock, boolean locking) {
        this.lookup(inventoryNode).set(lock.getRawId(), locking);
        for (InventoryNode child : inventoryNode.getAllChildren()) {
            this.lookup(child).set(lock.getRawId(), locking);
        }
    }

    @Override
    public void forceRefresh() {
        this.getCache().clear();
        for (Map.Entry<InventoryNode, Reference2BooleanMap<InventoryLock>> entry : this.getLocks().entrySet()) {
            for (Reference2BooleanMap.Entry<InventoryLock> e : entry.getValue().reference2BooleanEntrySet()) {
                this.propagateChange(entry.getKey(), e.getKey(), e.getBooleanValue());
            }
        }
    }

    protected Map<InventoryNode, Reference2BooleanMap<InventoryLock>> getLocks() {
        return this.locks;
    }

    protected Map<InventoryNode, BitSet> getCache() {
        return this.cache;
    }

    protected void clearCache() {
        this.cache.clear();
    }

    protected BitSet lookup(InventoryNode inventoryNode) {
        return this.getCache().computeIfAbsent(inventoryNode, n -> new BitSet());
    }

    @Override
    public void readFromNbt(CompoundTag tag) {
        if (tag.contains("locks", NbtType.COMPOUND)) {
            clearCache();
            getLocks().clear();
            CompoundTag dict = tag.getCompound("locks");
            for (String key : dict.getKeys()) {
                InventoryNode node = Locki.getNode(key);
                if (node != null) {
                    ListTag list = dict.getList(key, NbtType.COMPOUND);
                    for (int i = 0; i < list.size(); i++) {
                        CompoundTag lockInfo = list.getCompound(i);
                        InventoryLock lock = Locki.getLock(Identifier.tryParse(lockInfo.getString("id")));
                        if (lock != null) {
                            this.updateLock(lock, node, lockInfo.getBoolean("locking"));
                        } else {
                            Locki.LOGGER.error("Dropping unregistered inventory lock " + lockInfo.asString());
                        }
                    }
                } else {
                    Locki.LOGGER.error("Dropping lock data for unregistered inventory node " + key);
                }
            }
        }
    }

    @Override
    public void writeToNbt(CompoundTag tag) {
        CompoundTag dict = new CompoundTag();
        for (Map.Entry<InventoryNode, Reference2BooleanMap<InventoryLock>> nodeEntry : getLocks().entrySet()) {
            ListTag list = new ListTag();
            for (Reference2BooleanMap.Entry<InventoryLock> lockEntry : nodeEntry.getValue().reference2BooleanEntrySet()) {
                CompoundTag l = new CompoundTag();
                l.putString("id", lockEntry.getKey().getId().toString());
                l.putBoolean("locking", lockEntry.getBooleanValue());
                list.add(l);
            }
            dict.put(nodeEntry.getKey().getFullName(), list);
        }
        tag.put("locks", dict);
    }
}
