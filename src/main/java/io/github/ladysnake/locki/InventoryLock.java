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

    @Override
    public String toString() {
        return this.id.toString();
    }
}
