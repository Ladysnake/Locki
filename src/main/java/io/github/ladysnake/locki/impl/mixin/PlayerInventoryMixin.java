/*
 * Requiem
 * Copyright (C) 2017-2021 Ladysnake
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program. If not, see <https://www.gnu.org/licenses>.
 *
 * Linking this mod statically or dynamically with other
 * modules is making a combined work based on this mod.
 * Thus, the terms and conditions of the GNU General Public License cover the whole combination.
 *
 * In addition, as a special exception, the copyright holders of
 * this mod give you permission to combine this mod
 * with free software programs or libraries that are released under the GNU LGPL
 * and with code included in the standard release of Minecraft under All Rights Reserved (or
 * modified versions of such code, with unchanged license).
 * You may copy and distribute such a system following the terms of the GNU GPL for this mod
 * and the licenses of the other code concerned.
 *
 * Note that people who make modified versions of this mod are not obligated to grant
 * this special exception for their modified versions; it is their choice whether to do so.
 * The GNU General Public License gives permission to release a modified version without this exception;
 * this exception also makes it possible to release a modified version which carries forward this exception.
 */
package io.github.ladysnake.locki.impl.mixin;

import io.github.ladysnake.locki.DefaultInventoryNodes;
import io.github.ladysnake.locki.InventoryKeeper;
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
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.*;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(PlayerInventory.class)
public abstract class PlayerInventoryMixin {
    @Shadow
    public int selectedSlot;

    @Shadow
    @Final
    public PlayerEntity player;

    @Shadow
    @Final
    public DefaultedList<ItemStack> main;

    @Nullable
    private InventoryKeeper locki$keeper;

    @Inject(method = "<init>", at = @At("RETURN"))
    private void constructor(PlayerEntity player, CallbackInfo ci) {
        this.locki$keeper = LockiComponents.INVENTORY_KEEPER.maybeGet(player).orElse(null);
    }

    @Environment(EnvType.CLIENT)
    @Inject(method = {"addPickBlock", "scrollInHotbar"},
        at = @At(value = "FIELD", opcode = Opcodes.PUTFIELD, target = "Lnet/minecraft/entity/player/PlayerInventory;selectedSlot:I", shift = At.Shift.AFTER)
    )
    private void preventClientHotbarSelection(CallbackInfo ci) {
        if (this.locki$keeper != null && this.locki$keeper.isSlotLocked(this.selectedSlot)) {
            this.selectedSlot = PlayerInventoryKeeper.MAINHAND_SLOT;
        }
    }

    @Inject(method = "clone",
        at = @At(value = "FIELD", opcode = Opcodes.PUTFIELD, target = "Lnet/minecraft/entity/player/PlayerInventory;selectedSlot:I", shift = At.Shift.AFTER)
    )
    private void preventHotbarSelection(CallbackInfo ci) {
        if (this.locki$keeper != null && this.locki$keeper.isSlotLocked(this.selectedSlot)) {
            this.selectedSlot = PlayerInventoryKeeper.MAINHAND_SLOT;
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
