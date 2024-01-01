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

import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import it.unimi.dsi.fastutil.ints.IntList;
import net.minecraft.recipe.InputSlotFiller;
import net.minecraft.recipe.Recipe;
import net.minecraft.recipe.RecipeMatcher;
import net.minecraft.screen.AbstractRecipeScreenHandler;
import net.minecraft.server.network.ServerPlayerEntity;
import org.ladysnake.locki.DefaultInventoryNodes;
import org.ladysnake.locki.InventoryKeeper;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;

@Mixin(InputSlotFiller.class)
public abstract class InputSlotFillerMixin {
    @Shadow protected AbstractRecipeScreenHandler<?> handler;

    @WrapOperation(method = "fillInputSlots(Lnet/minecraft/server/network/ServerPlayerEntity;Lnet/minecraft/recipe/Recipe;Z)V", at = @At(value = "INVOKE", target = "Lnet/minecraft/recipe/RecipeMatcher;match(Lnet/minecraft/recipe/Recipe;Lit/unimi/dsi/fastutil/ints/IntList;)Z"))
    private boolean blockInventoryCrafting(RecipeMatcher instance, Recipe<?> recipe, IntList output, Operation<Boolean> original, ServerPlayerEntity entity) {
        if (this.handler == entity.playerScreenHandler && InventoryKeeper.get(entity).isLocked(DefaultInventoryNodes.CRAFTING_GRID)) {
            return false;
        }
        return original.call(instance, recipe, output);
    }
}
