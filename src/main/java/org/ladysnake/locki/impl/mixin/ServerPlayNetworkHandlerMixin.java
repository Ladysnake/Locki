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
package org.ladysnake.locki.impl.mixin;

import net.minecraft.network.packet.c2s.play.PlayerActionC2SPacket;
import net.minecraft.network.packet.c2s.play.SelectedSlotUpdateC2SPacket;
import net.minecraft.server.network.ServerPlayNetworkHandler;
import net.minecraft.server.network.ServerPlayerEntity;
import org.ladysnake.locki.DefaultInventoryNodes;
import org.ladysnake.locki.InventoryKeeper;
import org.ladysnake.locki.impl.PlayerInventoryKeeper;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(ServerPlayNetworkHandler.class)
public abstract class ServerPlayNetworkHandlerMixin {
    @Shadow
    public ServerPlayerEntity player;

    @Inject(method = "onSelectedSlotUpdate", at = @At(value = "INVOKE", target = "Lnet/minecraft/network/packet/c2s/play/SelectedSlotUpdateC2SPacket;getSelectedSlot()I", ordinal = 0))
    private void fixSelectedSlot(SelectedSlotUpdateC2SPacket packet, CallbackInfo ci) {
        ((UpdateSelectedSlotC2SPacketAccessor) packet).locki$setSelectedSlot(PlayerInventoryKeeper.fixSelectedSlot(player, packet.getSelectedSlot()));
    }

    @Inject(method = "onPlayerAction", at = @At(value = "FIELD", target = "Lnet/minecraft/util/Hand;OFF_HAND:Lnet/minecraft/util/Hand;", ordinal = 0), cancellable = true)
    private void preventSwap(PlayerActionC2SPacket packet, CallbackInfo ci) {
        InventoryKeeper inventoryKeeper = InventoryKeeper.get(this.player);
        if (inventoryKeeper.isLocked(DefaultInventoryNodes.OFF_HAND)
                || inventoryKeeper.isLocked(DefaultInventoryNodes.MAIN_HAND)) {
            ci.cancel();
        }
    }
}
