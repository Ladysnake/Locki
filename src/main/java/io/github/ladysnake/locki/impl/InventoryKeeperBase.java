package io.github.ladysnake.locki.impl;

import com.google.common.annotations.VisibleForTesting;
import dev.onyxstudios.cca.api.v3.component.Component;
import io.github.ladysnake.locki.InventoryKeeper;
import io.github.ladysnake.locki.InventoryLock;
import io.github.ladysnake.locki.Locki;
import it.unimi.dsi.fastutil.objects.Reference2BooleanMap;
import it.unimi.dsi.fastutil.objects.Reference2BooleanMaps;
import it.unimi.dsi.fastutil.objects.Reference2BooleanOpenHashMap;
import net.fabricmc.fabric.api.util.NbtType;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.Tag;
import net.minecraft.util.Identifier;
import org.jetbrains.annotations.Nullable;

import java.util.*;

public class InventoryKeeperBase implements Component, InventoryKeeper {

    private final NavigableMap<String, Reference2BooleanMap<InventoryLock>> locks = new TreeMap<>();  // TreeMap purely for display
    protected final NavigableMap<String, BitSet> cache = new TreeMap<>();    // TreeMap for lookup

    @Override
    public boolean isLocked(String invNode) {
        return !this.lookup(invNode).isEmpty();
    }

    @Override
    public boolean isLockedBy(InventoryLock lock, String invNode) {
        return lookup(invNode).get(lock.getRawId());
    }

    @Override
    public Set<InventoryLock> getAllPlacedLocks(String invNode) {
        return getLocks().get(invNode).keySet();
    }

    @Override
    public void addLock(InventoryLock lock, String invNode) {
        this.updateLock(lock, invNode, true);
    }

    @Override
    public void removeLock(InventoryLock lock, String invNode) {
        this.updateLock(lock, invNode, false);
    }

    @Override
    public boolean isSlotLocked(int slot) {
        throw new UnsupportedOperationException();
    }

    protected boolean updateLock(InventoryLock lock, String invNode, boolean locking) {
        if (this.doUpdateLock(lock, invNode, locking)) {
            // drop all the child locks
            for (Reference2BooleanMap<InventoryLock> subLocks : getSubTree(this.locks, invNode).values()) {
                subLocks.removeBoolean(lock);
            }
            this.propagateChange(invNode, lock, locking);
            return true;
        }
        return false;
    }

    private boolean doUpdateLock(InventoryLock lock, String invNode, boolean locking) {
        Reference2BooleanMap<InventoryLock> lockSet = this.getLocks().computeIfAbsent(invNode, n -> new Reference2BooleanOpenHashMap<>());
        @SuppressWarnings("deprecation")    // using the deprecated overload on purpose to check former presence
        @Nullable Boolean previous = lockSet.put(lock, (Boolean) locking);
        return previous == null || previous != locking;
    }

    protected void propagateChange(String inventoryNode, InventoryLock lock, boolean locking) {
        this.lookup(inventoryNode).set(lock.getRawId(), locking);
        for (BitSet cachedChild : getSubTree(this.getCache(), inventoryNode).values()) {
            cachedChild.set(lock.getRawId(), locking);
        }
    }

    @Override
    public void forceRefresh() {
        this.getCache().clear();
        for (Map.Entry<String, Reference2BooleanMap<InventoryLock>> entry : this.getLocks().entrySet()) {
            for (Reference2BooleanMap.Entry<InventoryLock> e : entry.getValue().reference2BooleanEntrySet()) {
                this.propagateChange(entry.getKey(), e.getKey(), e.getBooleanValue());
            }
        }
    }

    protected NavigableMap<String, Reference2BooleanMap<InventoryLock>> getLocks() {
        return this.locks;
    }

    protected NavigableMap<String, BitSet> getCache() {
        return this.cache;
    }

    protected void clearCache() {
        this.cache.clear();
    }

    protected BitSet lookup(String inventoryNode) {
        NavigableMap<String, BitSet> cache = this.getCache();
        BitSet ret = cache.get(inventoryNode);
        if (ret == null) {  // Need to build the cache for this node
            String parent = getParent(inventoryNode);
            ret = new BitSet();

            if (parent != null) {
                ret.or(this.lookup(parent)); // recursive lookup
            }

            cache.put(inventoryNode, ret);
        }
        return ret;
    }

    @Nullable
    private String getParent(String inventoryNode) {
        int parentEnd = inventoryNode.lastIndexOf('.');
        if (parentEnd >= 0) {
            return inventoryNode.substring(0, parentEnd);
        }
        return null;
    }

    @VisibleForTesting
    static <T> NavigableMap<String, T> getSubTree(NavigableMap<String, T> nodeTree, String node) {
        String upperBound = node.substring(0, node.length() - 1) + (char) (node.charAt(node.length() - 1) + 1);
        return nodeTree.subMap(node, false, upperBound, false);
    }@Override
    public void readFromNbt(CompoundTag tag) {
        if (tag.contains("locks", NbtType.COMPOUND)) {
            clearCache();
            getLocks().clear();
            CompoundTag dict = tag.getCompound("locks");
            for (String key : dict.getKeys()) {
                for (Tag id : dict.getList(key, NbtType.COMPOUND)) {
                    InventoryLock lock = Locki.getLock(Identifier.tryParse(id.asString()));
                    if (lock != null) {
                        this.addLock(lock, key);
                    }
                }
            }
        }
    }

    @Override
    public void writeToNbt(CompoundTag tag) {
        CompoundTag dict = new CompoundTag();
        for (Map.Entry<String, Reference2BooleanMap<InventoryLock>> nodeEntry : getLocks().entrySet()) {
            ListTag list = new ListTag();
            for (Reference2BooleanMap.Entry<InventoryLock> lockEntry : nodeEntry.getValue().reference2BooleanEntrySet()) {
                CompoundTag l = new CompoundTag();
                l.putString("id", lockEntry.getKey().getId().toString());
                l.putBoolean("locking", lockEntry.getBooleanValue());
                list.add(l);
            }
            dict.put(nodeEntry.getKey(), list);
        }
        tag.put("locks", dict);
    }
}
