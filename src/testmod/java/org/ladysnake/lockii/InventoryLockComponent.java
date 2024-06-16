package org.ladysnake.lockii;

import com.mojang.serialization.Codec;
import io.netty.buffer.ByteBuf;
import net.minecraft.component.DataComponentType;
import net.minecraft.network.codec.PacketCodec;
import net.minecraft.network.codec.PacketCodecs;
import org.jetbrains.annotations.NotNull;
import org.ladysnake.locki.InventoryNode;
import org.ladysnake.locki.Locki;

public record InventoryLockComponent(int debugMode) {
    public static final Codec<InventoryLockComponent> CODEC = Codec.INT.xmap(InventoryLockComponent::new, InventoryLockComponent::debugMode);
    public static final PacketCodec<ByteBuf, InventoryLockComponent> PACKET_CODEC = PacketCodecs.VAR_INT.map(InventoryLockComponent::new, InventoryLockComponent::debugMode);
    public static final DataComponentType<InventoryLockComponent> TYPE = new DataComponentType.Builder<InventoryLockComponent>().codec(CODEC).packetCodec(PACKET_CODEC).build();
    private static final InventoryNode[] ALL_DEFAULT_NODES = Locki.streamNodeNames().map(Locki::getNode).toArray(InventoryNode[]::new);
    public static final InventoryLockComponent DEFAULT = new InventoryLockComponent(0);

    public @NotNull InventoryLockComponent cycle() {
        return new InventoryLockComponent((debugMode + 1) % ALL_DEFAULT_NODES.length);
    }

    public InventoryNode node() {
        return ALL_DEFAULT_NODES[debugMode];
    }
}
