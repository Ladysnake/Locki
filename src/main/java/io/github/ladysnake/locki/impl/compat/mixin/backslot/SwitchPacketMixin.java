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
package io.github.ladysnake.locki.impl.compat.mixin.backslot;

import io.github.ladysnake.locki.InventoryKeeper;
import net.backslot.network.SwitchPacket;
import net.minecraft.item.ItemStack;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.network.ServerPlayerEntity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

@Mixin(SwitchPacket.SwitchPacketReceiver.class)
public abstract class SwitchPacketMixin {
    @Redirect(method = "receive", at = @At(value = "INVOKE", target = "Lnet/backslot/network/SwitchPacket;isItemAllowed(Lnet/minecraft/item/ItemStack;I)Z"))
    private static boolean lockAdditionalSlots(ItemStack stack, int slot, MinecraftServer server, ServerPlayerEntity player) {
        if (InventoryKeeper.get(player).isSlotLocked(slot)) {
            return false;
        }
        return SwitchPacket.isItemAllowed(stack, slot);
    }
}
