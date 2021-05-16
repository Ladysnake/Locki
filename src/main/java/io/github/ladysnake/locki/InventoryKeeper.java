package io.github.ladysnake.locki;

import io.github.ladysnake.locki.impl.LockiComponents;
import net.minecraft.entity.player.PlayerEntity;

import java.util.Set;

public interface InventoryKeeper {
    static InventoryKeeper get(PlayerEntity player) {
        return LockiComponents.INVENTORY_KEEPER.get(player);
    }

    boolean isLocked(String invNode);

    boolean isLockedBy(InventoryLock lock, String invNode);

    void addLock(InventoryLock lock, String invNode);

    void removeLock(InventoryLock lock, String invNode);

    /**
     * Rebuilds the lock cache
     */
    void forceRefresh();

    boolean isSlotLocked(int slot);

    Set<InventoryLock> getAllPlacedLocks(String invNode);
}
