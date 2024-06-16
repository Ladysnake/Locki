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
package org.ladysnake.locki.impl;

import net.fabricmc.api.ClientModInitializer;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.screen.ingame.InventoryScreen;
import net.minecraft.client.gui.screen.recipe_book.RecipeBookProvider;
import net.minecraft.client.gui.widget.recipe_book.RecipeBookWidget;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.util.Identifier;
import org.ladysnake.locki.DefaultInventoryNodes;
import org.ladysnake.locki.InventoryLockingChangeCallback;
import org.ladysnake.locki.InventoryNode;
import org.ladysnake.locki.Locki;

public class LockiClient implements ClientModInitializer {
    public static final Identifier LOCKED_SLOT_SPRITE = Locki.id("gui/locked_slot");

    private static void updateCraftingBookVisibility(PlayerEntity player, InventoryNode invNode, boolean locked) {
        MinecraftClient mc = MinecraftClient.getInstance();

        if (player == mc.player) {
            if (invNode == DefaultInventoryNodes.CRAFTING_GRID) {
                if (mc.currentScreen instanceof InventoryScreenAccessor) {
                    ((InventoryScreenAccessor) mc.currentScreen).locki$getRecipeBookButton().visible = !locked;
                }
            } else if (invNode == DefaultInventoryNodes.CRAFTING_BOOK && locked) {
                if (mc.currentScreen instanceof InventoryScreen) {
                    RecipeBookWidget recipeBookWidget = ((RecipeBookProvider) mc.currentScreen).getRecipeBookWidget();
                    if (recipeBookWidget.isOpen()) {
                        recipeBookWidget.toggleOpen();
                    }
                }
            }
        }
    }

    @Override
    public void onInitializeClient() {
        InventoryLockingChangeCallback.EVENT.register(LockiClient::updateCraftingBookVisibility);
    }
}
