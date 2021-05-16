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
    public void lock(PlayerEntity player, String invNode) {
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
    public void unlock(PlayerEntity player, String invNode) {
        InventoryKeeper.get(player).removeLock(this, invNode);
    }

    /**
     * @return {@code true} if this lock is actively locking or keeping unlocked {@code invNode}
     */
    public boolean isPlacedOn(PlayerEntity player, String invNode) {
        return InventoryKeeper.get(player).getAllPlacedLocks(invNode).contains(this);
    }

    /**
     * @return {@code true} if this lock is locking the specified {@code invNode} or one of its parents
     */
    public boolean isLocking(PlayerEntity player, String invNode) {
        return InventoryKeeper.get(player).isLockedBy(this, invNode);
    }

    public void toggle(PlayerEntity player, String invNode) {
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
