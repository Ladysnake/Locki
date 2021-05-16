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
package io.github.ladysnake.locki.impl.mixin;

import io.github.ladysnake.locki.DefaultInventoryNodes;
import io.github.ladysnake.locki.InventoryKeeper;
import io.github.ladysnake.locki.impl.LockableSlot;
import io.github.ladysnake.locki.impl.LockiComponents;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.inventory.CraftingInventory;
import net.minecraft.inventory.Inventory;
import net.minecraft.item.ItemStack;
import net.minecraft.screen.ScreenHandler;
import net.minecraft.screen.slot.Slot;
import org.jetbrains.annotations.Nullable;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(Slot.class)
public abstract class SlotMixin implements LockableSlot {

    @Shadow
    @Final
    private int index;
    @Unique
    protected @Nullable InventoryKeeper limiter;
    @Unique
    protected boolean craftingSlot;

    @Inject(method = "<init>", at = @At("RETURN"))
    private void constructor(Inventory inventory, int index, int x, int y, CallbackInfo ci) {
        if (inventory instanceof PlayerInventory) {
            this.limiter = LockiComponents.INVENTORY_KEEPER.maybeGet(((PlayerInventory) inventory).player).orElse(null);
        } else if (inventory instanceof CraftingInventory) {
            ScreenHandler handler = ((CraftingInventoryAccessor) inventory).locki$getHandler();
            if (handler instanceof PlayerScreenHandlerAccessor) {
                this.limiter = LockiComponents.INVENTORY_KEEPER.maybeGet(((PlayerScreenHandlerAccessor) handler).getOwner()).orElse(null);
                this.craftingSlot = true;
            }
        }
    }

    @Override
    public boolean locki$shouldBeLocked() {
        return this.limiter != null && (this.craftingSlot ? this.limiter.isLocked(DefaultInventoryNodes.CRAFTING_GRID) : this.limiter.isSlotLocked(this.index));
    }

    @Inject(method = "canInsert", at = @At("HEAD"), cancellable = true)
    private void canInsert(ItemStack stack, CallbackInfoReturnable<Boolean> cir) {
        if (this.locki$shouldBeLocked()) cir.setReturnValue(false);
    }

    @Inject(method = "canTakeItems", at = @At("HEAD"), cancellable = true)
    private void canTakeItems(PlayerEntity playerEntity, CallbackInfoReturnable<Boolean> cir) {
        if (this.locki$shouldBeLocked()) cir.setReturnValue(false);
    }
}
