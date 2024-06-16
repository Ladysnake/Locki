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

import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.screen.ingame.InventoryScreen;
import net.minecraft.client.gui.widget.recipe_book.RecipeBookWidget;
import org.ladysnake.locki.DefaultInventoryNodes;
import org.ladysnake.locki.InventoryKeeper;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(RecipeBookWidget.class)
public abstract class RecipeBookWidgetMixin {
    @Shadow protected MinecraftClient client;

    @Inject(method = "isGuiOpen", at = @At("HEAD"), cancellable = true)
    private void forceCloseGui(CallbackInfoReturnable<Boolean> cir) {
        if (MinecraftClient.getInstance().currentScreen instanceof InventoryScreen && InventoryKeeper.get(this.client.player).isLocked(DefaultInventoryNodes.CRAFTING_BOOK)) {
            cir.setReturnValue(false);
        }
    }
}
