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

import io.github.ladysnake.locki.DefaultInventoryNodes;
import io.github.ladysnake.locki.InventoryKeeper;
import io.github.ladysnake.locki.InventoryLock;
import io.github.ladysnake.locki.InventoryNode;
import io.github.ladysnake.locki.impl.LockiComponents;
import io.github.ladysnake.locki.impl.PlayerInventoryKeeper;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.item.ItemStack;
import net.minecraft.util.collection.DefaultedList;
import org.jetbrains.annotations.Nullable;
import org.objectweb.asm.Opcodes;
import org.quiltmc.loader.api.minecraft.ClientOnly;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.ModifyArg;
import org.spongepowered.asm.mixin.injection.ModifyVariable;
import org.spongepowered.asm.mixin.injection.Slice;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.util.Set;

@Mixin(PlayerInventory.class)
public abstract class PlayerInventoryMixin implements InventoryKeeper {
    @Shadow
    public int selectedSlot;

    @Shadow
    @Final
    @Nullable
    public PlayerEntity player;

    @Shadow
    @Final
    public DefaultedList<ItemStack> main;

    @Nullable
    private InventoryKeeper locki$keeper;

    @Override
    public boolean isLocked(InventoryNode invNode) {
        return this.locki$keeper != null && locki$keeper.isLocked(invNode);
    }

    @Override
    public boolean isEntirelyLocked(InventoryNode invNode) {
        return this.locki$keeper != null && locki$keeper.isEntirelyLocked(invNode);
    }

    @Override
    public boolean isSlotLocked(int slot) {
        return this.locki$keeper != null && locki$keeper.isSlotLocked(slot);
    }

    @Override
    public boolean isLockedBy(InventoryLock lock, InventoryNode invNode) {
        return this.locki$keeper != null && locki$keeper.isLockedBy(lock, invNode);
    }

    @Override
    public void addLock(InventoryLock lock, InventoryNode invNode) {
        if (this.locki$keeper != null) {
            locki$keeper.addLock(lock, invNode);
        }
    }

    @Override
    public void removeLock(InventoryLock lock, InventoryNode invNode) {
        if (this.locki$keeper != null) {
            locki$keeper.removeLock(lock, invNode);
        }
    }

    @Override
    public void forceRefresh() {
        if (this.locki$keeper != null) {
            locki$keeper.forceRefresh();
        }
    }

    @Override
    public Set<InventoryLock> getAllPlacedLocks(InventoryNode invNode) {
        return this.locki$keeper != null ? locki$keeper.getAllPlacedLocks(invNode) : Set.of();
    }

    @Inject(method = "<init>", at = @At("RETURN"))
    private void constructor(PlayerEntity player, CallbackInfo ci) {
        this.locki$keeper = LockiComponents.INVENTORY_KEEPER.maybeGet(player).orElse(null);
    }

    @ClientOnly
    @Inject(method = {"addPickBlock", "scrollInHotbar"},
        at = @At(value = "FIELD", opcode = Opcodes.PUTFIELD, target = "Lnet/minecraft/entity/player/PlayerInventory;selectedSlot:I", shift = At.Shift.AFTER)
    )
    private void preventClientHotbarSelection(CallbackInfo ci) {
        if (this.player != null) {
            this.selectedSlot = PlayerInventoryKeeper.fixSelectedSlot(this.player, this.selectedSlot);
        }
    }

    @Inject(method = "clone",
        at = @At(value = "FIELD", opcode = Opcodes.PUTFIELD, target = "Lnet/minecraft/entity/player/PlayerInventory;selectedSlot:I", shift = At.Shift.AFTER)
    )
    private void preventHotbarSelection(CallbackInfo ci) {
        if (this.player != null) {
            this.selectedSlot = PlayerInventoryKeeper.fixSelectedSlot(this.player, this.selectedSlot);
        }
    }

    @Inject(method = "swapSlotWithHotbar", at = @At("HEAD"), cancellable = true)
    private void preventHotbarSwap(int hotbarSlot, CallbackInfo ci) {
        if (this.locki$keeper != null && this.locki$keeper.isLocked(DefaultInventoryNodes.MAIN_HAND)) {
            ci.cancel();
        }
    }

    @ModifyVariable(method = "getEmptySlot", at = @At(value = "LOAD", ordinal = 0))
    private int skipLockedSlots(int slot) {
        InventoryKeeper keeper = this.locki$keeper;
        if (keeper != null) {
            while (keeper.isSlotLocked(slot) && slot < this.main.size()) {
                slot++;
            }
        }
        return slot;
    }

    @Inject(method = "addStack(ILnet/minecraft/item/ItemStack;)I", at = @At("HEAD"), cancellable = true)
    private void preventAddStack(int slot, ItemStack stack, CallbackInfoReturnable<Integer> cir) {
        if (this.locki$keeper != null && this.locki$keeper.isSlotLocked(slot)) {
            cir.setReturnValue(stack.getCount());
        }
    }

    @ModifyArg(
        method = "getOccupiedSlotWithRoomForStack",
        at = @At(
            value = "INVOKE",
            target = "Lnet/minecraft/entity/player/PlayerInventory;canStackAddMore(Lnet/minecraft/item/ItemStack;Lnet/minecraft/item/ItemStack;)Z",
            ordinal = 0
        ),
        index = 0
    )
    private ItemStack preventMainHandStackAttempt(ItemStack stack) {
        if (this.locki$keeper != null && this.locki$keeper.isSlotLocked(this.selectedSlot)) {
            return ItemStack.EMPTY;
        }
        return stack;
    }

    @ModifyArg(
        method = "getOccupiedSlotWithRoomForStack",
        at = @At(
            value = "INVOKE",
            target = "Lnet/minecraft/entity/player/PlayerInventory;canStackAddMore(Lnet/minecraft/item/ItemStack;Lnet/minecraft/item/ItemStack;)Z",
            ordinal = 1
        ),
        index = 0
    )
    private ItemStack preventOffHandStackAttempt(ItemStack stack) {
        if (this.locki$keeper != null && this.locki$keeper.isLocked(DefaultInventoryNodes.OFF_HAND)) {
            return ItemStack.EMPTY;
        }
        return stack;
    }

    /**
     * If a player somehow gets a stackable in a locked inventory slot,
     * any future attempt to insert the same item into your inventory will fail.
     *
     * <p>This injection prevents the stacking attempt, letting items go to empty slots in the aforementioned
     * scenario.
     */
    @ModifyVariable(
        method = "getOccupiedSlotWithRoomForStack",
        slice = @Slice(
            from = @At(value = "INVOKE", target = "Lnet/minecraft/entity/player/PlayerInventory;canStackAddMore(Lnet/minecraft/item/ItemStack;Lnet/minecraft/item/ItemStack;)Z", ordinal = 1),
            to = @At(value = "INVOKE", target = "Lnet/minecraft/entity/player/PlayerInventory;canStackAddMore(Lnet/minecraft/item/ItemStack;Lnet/minecraft/item/ItemStack;)Z", ordinal = 2)
        ),
        at = @At(value = "LOAD", ordinal = 0)
    )
    private int preventStackAttempt(int slot) {
        InventoryKeeper limiter = this.locki$keeper;
        if (limiter != null) {
            while (limiter.isSlotLocked(slot) && slot < this.main.size()) {
                slot++;
            }
        }
        return slot;
    }
}
