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

import com.google.common.base.Suppliers;
import com.mojang.datafixers.util.Pair;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screen.ingame.HandledScreen;
import net.minecraft.screen.PlayerScreenHandler;
import net.minecraft.screen.slot.Slot;
import net.minecraft.util.Identifier;
import org.jetbrains.annotations.Nullable;
import org.ladysnake.locki.impl.LockableSlot;
import org.ladysnake.locki.impl.LockiClient;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.ModifyVariable;

import java.util.function.Supplier;

@Mixin(HandledScreen.class)
public abstract class HandledScreenMixin {
    @Unique
    private static final Supplier<Pair<Identifier, Identifier>> LOCKED_SPRITE_REF = Suppliers.memoize(() -> com.mojang.datafixers.util.Pair.of(PlayerScreenHandler.BLOCK_ATLAS_TEXTURE, LockiClient.LOCKED_SLOT_SPRITE));

    @ModifyVariable(method = "drawSlot", at = @At(value = "INVOKE_ASSIGN", target = "Lnet/minecraft/screen/slot/Slot;getBackgroundSprite()Lcom/mojang/datafixers/util/Pair;"))
    private @Nullable Pair<Identifier, Identifier> replaceSprite(@Nullable Pair<Identifier, Identifier> baseSprite, GuiGraphics graphics, Slot slot) {
        if (((LockableSlot) slot).locki$shouldBeLocked()) {
            return HandledScreenMixin.LOCKED_SPRITE_REF.get();
        }
        return baseSprite;
    }
}
