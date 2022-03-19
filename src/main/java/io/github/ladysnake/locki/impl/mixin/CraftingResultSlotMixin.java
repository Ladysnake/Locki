/*
 * Locki
 * Copyright (C) 2021-2022 Ladysnake
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
package io.github.ladysnake.locki.impl.mixin;

import io.github.ladysnake.locki.InventoryKeeper;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.inventory.CraftingInventory;
import net.minecraft.inventory.Inventory;
import net.minecraft.screen.PlayerScreenHandler;
import net.minecraft.screen.slot.CraftingResultSlot;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(CraftingResultSlot.class)
public abstract class CraftingResultSlotMixin extends SlotMixin {
    @Inject(method = "<init>", at = @At("RETURN"))
    private void constructor(PlayerEntity player, CraftingInventory input, Inventory inventory, int index, int x, int y, CallbackInfo ci) {
        if (player != null && ((CraftingInventoryAccessor) input).locki$getHandler() instanceof PlayerScreenHandler) {
            this.locki$keeper = InventoryKeeper.get(player);
        }
        this.locki$craftingSlot = true;
    }
}
