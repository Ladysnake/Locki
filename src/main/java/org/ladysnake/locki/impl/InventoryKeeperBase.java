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

import it.unimi.dsi.fastutil.objects.Reference2BooleanMap;
import it.unimi.dsi.fastutil.objects.Reference2BooleanOpenHashMap;
import it.unimi.dsi.fastutil.objects.Reference2ObjectOpenHashMap;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.nbt.NbtElement;
import net.minecraft.nbt.NbtList;
import net.minecraft.registry.HolderLookup;
import net.minecraft.util.Identifier;
import org.ladysnake.cca.api.v3.component.Component;
import org.ladysnake.locki.InventoryKeeper;
import org.ladysnake.locki.InventoryLock;
import org.ladysnake.locki.InventoryNode;
import org.ladysnake.locki.Locki;

import java.util.BitSet;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;
import java.util.TreeSet;

public class InventoryKeeperBase implements Component, InventoryKeeper {

    private final Map<InventoryNode, Reference2BooleanMap<InventoryLock>> locks = new TreeMap<>();
    protected final Map<InventoryNode, BitSet> cache = new Reference2ObjectOpenHashMap<>();

    @Override
    public boolean isLocked(InventoryNode invNode) {
        return !this.lookup(invNode).isEmpty();
    }

    @Override
    public boolean isEntirelyLocked(InventoryNode invNode) {
        if (!this.isLocked(invNode)) return false;

        for (InventoryNode descendant : invNode.getDescendants()) {
            if (!this.isLocked(descendant)) {
                return false;
            }
        }

        return true;
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

    protected void updateLock(InventoryLock lock, InventoryNode invNode, boolean locking) {
        this.doUpdateLock(lock, invNode, locking);
        // drop all the child locks
        for (InventoryNode subNode : invNode.getDescendants()) {
            Reference2BooleanMap<InventoryLock> subLocks = this.getLocks().get(subNode);
            if (subLocks != null) subLocks.removeBoolean(lock);
        }
        this.propagateChange(invNode, lock, locking);
    }

    private void doUpdateLock(InventoryLock lock, InventoryNode invNode, boolean locking) {
        this.getLocks().computeIfAbsent(invNode, n -> new Reference2BooleanOpenHashMap<>()).put(lock, locking);
    }

    protected boolean propagateChange(InventoryNode inventoryNode, InventoryLock lock, boolean locking) {
        boolean changed = this.updateCachedLockState(lock, locking, inventoryNode);
        for (InventoryNode child : inventoryNode.getDescendants()) {
            changed |= this.updateCachedLockState(lock, locking, child);
        }
        return changed;
    }

    protected boolean updateCachedLockState(InventoryLock lock, boolean locking, InventoryNode inventoryNode) {
        BitSet bitset = this.lookup(inventoryNode);
        if (bitset.get(lock.getRawId()) != locking) {
            bitset.set(lock.getRawId(), locking);
            return true;
        }
        return false;
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
    public void readFromNbt(NbtCompound tag, HolderLookup.Provider registryLookup) {
        if (tag.contains("locks", NbtElement.COMPOUND_TYPE)) {
            this.clearCache();
            this.getLocks().clear();
            NbtCompound dict = tag.getCompound("locks");
            // need to order the keys to avoid overwriting entries as we go
            for (String key : new TreeSet<>(dict.getKeys())) {
                InventoryNode node = Locki.getNode(key);
                if (node != null) {
                    NbtList list = dict.getList(key, NbtElement.COMPOUND_TYPE);
                    for (int i = 0; i < list.size(); i++) {
                        NbtCompound lockInfo = list.getCompound(i);
                        InventoryLock lock = Locki.getLock(Identifier.tryParse(lockInfo.getString("id")));
                        if (lock != null) {
                            this.updateLock(lock, node, lockInfo.getBoolean("locking"));
                        } else {
                            Locki.LOGGER.error("Dropping unregistered inventory lock {}", lockInfo.asString());
                        }
                    }
                } else {
                    Locki.LOGGER.error("Dropping lock data for unregistered inventory node {}", key);
                }
            }
        }
    }

    @Override
    public void writeToNbt(NbtCompound tag, HolderLookup.Provider registryLookup) {
        NbtCompound dict = new NbtCompound();
        for (Map.Entry<InventoryNode, Reference2BooleanMap<InventoryLock>> nodeEntry : this.getLocks().entrySet()) {
            NbtList list = new NbtList();
            for (Reference2BooleanMap.Entry<InventoryLock> lockEntry : nodeEntry.getValue().reference2BooleanEntrySet()) {
                if (lockEntry.getKey().shouldSave()) {
                    NbtCompound l = new NbtCompound();
                    l.putString("id", lockEntry.getKey().getId().toString());
                    l.putBoolean("locking", lockEntry.getBooleanValue());
                    list.add(l);
                }
            }
            if (!list.isEmpty()) {
                dict.put(nodeEntry.getKey().getFullName(), list);
            }
        }
        if (!dict.isEmpty()) {
            tag.put("locks", dict);
        }
    }
}
