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

import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.hud.InGameHud;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
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
    private static final int X_CENTER_SHIFT = 77;

    @Unique
    private boolean cancelNextItem;
    @Unique
    private boolean renderMainHandOnly;

    @Shadow
    protected abstract PlayerEntity getCameraPlayer();

    @Inject(
        method = "renderHotbar",
        at = @At(value = "FIELD", target = "Lnet/minecraft/client/gui/hud/InGameHud;WIDGETS_TEXTURE:Lnet/minecraft/util/Identifier;"),
        cancellable = true
    )
    private void checkInventoryLimit(float tickDelta, GuiGraphics graphics, CallbackInfo ci) {
        this.renderMainHandOnly = false;
        InventoryKeeper inventoryKeeper = InventoryKeeper.get(this.getCameraPlayer());
        if (inventoryKeeper.isLocked(DefaultInventoryNodes.MAIN_HAND)) {
            ci.cancel();
        } else {
            this.renderMainHandOnly = inventoryKeeper.isLocked(DefaultInventoryNodes.HOTBAR);
        }
    }

    @ModifyVariable(method = "renderHotbar", at = @At(value = "INVOKE_ASSIGN", target = "Lnet/minecraft/entity/player/PlayerEntity;getOffHandStack()Lnet/minecraft/item/ItemStack;"))
    private ItemStack lockOffHand(ItemStack base) {
        if (InventoryKeeper.get(this.getCameraPlayer()).isLocked(DefaultInventoryNodes.OFF_HAND)) {
            return ItemStack.EMPTY;
        }
        return base;
    }

    @ModifyArg(
        method = "renderHotbar",
        at = @At(value = "INVOKE", target = "Lnet/minecraft/client/gui/GuiGraphics;drawTexture(Lnet/minecraft/util/Identifier;IIIIII)V", ordinal = 0),
        index = 1
    )
    private int centerCroppedHotbar(int x) {
        if (this.renderMainHandOnly) {
            return x + X_CENTER_SHIFT;
        }
        return x;
    }

    @ModifyArg(
        method = "renderHotbar",
        at = @At(value = "INVOKE", target = "Lnet/minecraft/client/gui/GuiGraphics;drawTexture(Lnet/minecraft/util/Identifier;IIIIII)V", ordinal = 1),
        index = 1
    )
    private int centerSelectedSlot(int x) {
        if (this.renderMainHandOnly) {
            return x + X_CENTER_SHIFT;
        }
        return x;
    }

    @ModifyArg(
        method = "renderHotbar",
        at = @At(value = "INVOKE", target = "Lnet/minecraft/client/gui/GuiGraphics;drawTexture(Lnet/minecraft/util/Identifier;IIIIII)V", ordinal = 0),
        index = 5
    )
    private int cropHotbar_width(int width) {
        if (this.renderMainHandOnly) {
            return 21;
        }
        return width;
    }

    @ModifyArg(
        method = "renderHotbar",
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
        method = "renderHotbar",
        slice = @Slice(
            from = @At(value = "INVOKE", target = "Lnet/minecraft/client/util/math/MatrixStack;pop()V"),
            to = @At(value = "INVOKE", target = "Lnet/minecraft/item/ItemStack;isEmpty()Z", ordinal = 1)
        ),
        at = @At(value = "INVOKE", target = "Lnet/minecraft/client/gui/hud/InGameHud;renderHotbarItem(Lnet/minecraft/client/gui/GuiGraphics;IIFLnet/minecraft/entity/player/PlayerEntity;Lnet/minecraft/item/ItemStack;I)V")
    )
    private ItemStack cancelLockedItemRender(ItemStack stack) {
        if (this.cancelNextItem) {
            return ItemStack.EMPTY;
        }
        return stack;
    }

    @ModifyArg(
        method = "renderHotbar",
        slice = @Slice(
            from = @At(value = "INVOKE", target = "Lnet/minecraft/client/util/math/MatrixStack;pop()V"),
            to = @At(value = "INVOKE", target = "Lnet/minecraft/item/ItemStack;isEmpty()Z", ordinal = 1)
        ),
        at = @At(value = "INVOKE", target = "Lnet/minecraft/client/gui/hud/InGameHud;renderHotbarItem(Lnet/minecraft/client/gui/GuiGraphics;IIFLnet/minecraft/entity/player/PlayerEntity;Lnet/minecraft/item/ItemStack;I)V"),
        index = 1
    )
    private int shiftMainHandItem(int x) {
        if (this.renderMainHandOnly && !this.cancelNextItem) {
            return x + X_CENTER_SHIFT;
        }
        return x;
    }
}
