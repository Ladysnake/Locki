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
package org.ladysnake.locki.impl.mixin.client;

import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import net.minecraft.class_9779;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.hud.in_game.InGameHud;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.util.Identifier;
import org.ladysnake.locki.DefaultInventoryNodes;
import org.ladysnake.locki.InventoryKeeper;
import org.ladysnake.locki.impl.PlayerInventoryKeeper;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.ModifyArg;
import org.spongepowered.asm.mixin.injection.ModifyVariable;
import org.spongepowered.asm.mixin.injection.Slice;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

// Lower priority to load before Bedrockify's mixin, fix for issue Ladysnake/Requiem#198
@Mixin(value = InGameHud.class, priority = 995)
public abstract class InGameHudMixin {
    @Unique
    private static final int LOCKI$X_CENTER_SHIFT = 77;
    @Unique
    private static final int LOCKI$SINGLE_SLOT_WIDTH = 21;

    @Unique
    private boolean cancelNextItem;
    @Unique
    private boolean renderMainHandOnly;

    @Shadow
    protected abstract PlayerEntity getCameraPlayer();

    @Inject(
        method = "renderSurvivalHotbar",
        at = @At(value = "INVOKE", target = "Lnet/minecraft/client/util/math/MatrixStack;push()V"),
        allow = 1,
        cancellable = true
    )
    private void checkInventoryLimit(GuiGraphics graphics, class_9779 arg, CallbackInfo ci) {
        this.renderMainHandOnly = false;
        InventoryKeeper inventoryKeeper = InventoryKeeper.get(this.getCameraPlayer());
        if (inventoryKeeper.isLocked(DefaultInventoryNodes.MAIN_HAND)) {
            ci.cancel();
        } else {
            this.renderMainHandOnly = inventoryKeeper.isLocked(DefaultInventoryNodes.HOTBAR);
        }
    }

    @ModifyVariable(method = "renderSurvivalHotbar", at = @At(value = "INVOKE_ASSIGN", target = "Lnet/minecraft/entity/player/PlayerEntity;getOffHandStack()Lnet/minecraft/item/ItemStack;"))
    private ItemStack lockOffHand(ItemStack base) {
        if (InventoryKeeper.get(this.getCameraPlayer()).isLocked(DefaultInventoryNodes.OFF_HAND)) {
            return ItemStack.EMPTY;
        }
        return base;
    }

    @WrapOperation(
        method = "renderSurvivalHotbar",
        at = @At(value = "INVOKE", target = "Lnet/minecraft/client/gui/GuiGraphics;drawGuiTexture(Lnet/minecraft/util/Identifier;IIII)V", ordinal = 0)
    )
    private void centerCroppedHotbar(GuiGraphics instance, Identifier texture, int x, int y, int width, int height, Operation<Void> original) {
        if (this.renderMainHandOnly) {
            int centeredX = x + LOCKI$X_CENTER_SHIFT;
            instance.enableScissor(centeredX, y, centeredX + LOCKI$SINGLE_SLOT_WIDTH, y + height);
            instance.drawGuiTexture(texture, centeredX, y, width, height);
            instance.disableScissor();
        } else {
            original.call(instance, texture, x, y, width, height);
        }
    }

    @ModifyArg(
        method = "renderSurvivalHotbar",
        at = @At(value = "INVOKE", target = "Lnet/minecraft/client/gui/GuiGraphics;drawGuiTexture(Lnet/minecraft/util/Identifier;IIII)V", ordinal = 1),
        index = 1
    )
    private int centerSelectedSlot(int x) {
        if (this.renderMainHandOnly) {
            return x + LOCKI$X_CENTER_SHIFT;
        }
        return x;
    }

    @ModifyArg(
        method = "renderSurvivalHotbar",
        slice = @Slice(
            from = @At(value = "INVOKE", target = "Lnet/minecraft/client/util/math/MatrixStack;pop()V"),
            to = @At(value = "INVOKE", target = "Lnet/minecraft/item/ItemStack;isEmpty()Z", ordinal = 1)
        ),
        at = @At(value = "INVOKE", target = "Lnet/minecraft/util/collection/DefaultedList;get(I)Ljava/lang/Object;")
    )
    private int cancelLockedItemRender(int index) {
        this.cancelNextItem = renderMainHandOnly && index != PlayerInventoryKeeper.MAINHAND_SLOT;
        return index;
    }

    @ModifyArg(
        method = "renderSurvivalHotbar",
        slice = @Slice(
            from = @At(value = "INVOKE", target = "Lnet/minecraft/client/util/math/MatrixStack;pop()V"),
            to = @At(value = "INVOKE", target = "Lnet/minecraft/item/ItemStack;isEmpty()Z", ordinal = 1)
        ),
        at = @At(value = "INVOKE", target = "Lnet/minecraft/client/gui/hud/in_game/InGameHud;renderHotbarItem(Lnet/minecraft/client/gui/GuiGraphics;IILnet/minecraft/class_9779;Lnet/minecraft/entity/player/PlayerEntity;Lnet/minecraft/item/ItemStack;I)V")
    )
    private ItemStack cancelLockedItemRender(ItemStack stack) {
        if (this.cancelNextItem) {
            return ItemStack.EMPTY;
        }
        return stack;
    }

    @ModifyArg(
        method = "renderSurvivalHotbar",
        slice = @Slice(
            from = @At(value = "INVOKE", target = "Lnet/minecraft/client/util/math/MatrixStack;pop()V"),
            to = @At(value = "INVOKE", target = "Lnet/minecraft/item/ItemStack;isEmpty()Z", ordinal = 1)
        ),
        at = @At(value = "INVOKE", target = "Lnet/minecraft/client/gui/hud/in_game/InGameHud;renderHotbarItem(Lnet/minecraft/client/gui/GuiGraphics;IILnet/minecraft/class_9779;Lnet/minecraft/entity/player/PlayerEntity;Lnet/minecraft/item/ItemStack;I)V"),
        index = 1
    )
    private int shiftMainHandItem(int x) {
        if (this.renderMainHandOnly && !this.cancelNextItem) {
            return x + LOCKI$X_CENTER_SHIFT;
        }
        return x;
    }
}
