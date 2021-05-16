package io.github.ladysnake.locki.impl;

import com.google.common.base.Preconditions;
import dev.onyxstudios.cca.api.v3.component.sync.AutoSyncedComponent;
import io.github.ladysnake.locki.DefaultInventoryNodes;
import io.github.ladysnake.locki.InventoryLock;
import it.unimi.dsi.fastutil.objects.Reference2BooleanMap;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.network.PacketByteBuf;
import net.minecraft.server.network.ServerPlayerEntity;

import java.util.BitSet;
import java.util.Map;
import java.util.NavigableMap;

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
    protected NavigableMap<String, Reference2BooleanMap<InventoryLock>> getLocks() {
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
        for (Map.Entry<String, BitSet> entry : this.getCache().entrySet()) {
            buf.writeString(entry.getKey());
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
            this.getCache().computeIfAbsent(key, s -> new BitSet()).set(CLIENT_LOCK, locked);
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
    protected boolean updateLock(InventoryLock lock, String invNode, boolean locking) {
        if (super.updateLock(lock, invNode, locking)) {
            LockiComponents.INVENTORY_KEEPER.sync(this.player);
            return true;
        }
        return false;
    }
}
