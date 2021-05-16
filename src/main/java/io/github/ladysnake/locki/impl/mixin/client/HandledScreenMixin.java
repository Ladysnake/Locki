package io.github.ladysnake.locki.impl.mixin.client;

import com.mojang.datafixers.util.Pair;
import io.github.ladysnake.locki.impl.LockableSlot;
import io.github.ladysnake.locki.impl.LockiClient;
import net.minecraft.client.gui.screen.ingame.HandledScreen;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.screen.PlayerScreenHandler;
import net.minecraft.screen.slot.Slot;
import net.minecraft.util.Identifier;
import net.minecraft.util.Lazy;
import org.jetbrains.annotations.Nullable;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.ModifyVariable;

@Mixin(HandledScreen.class)
public abstract class HandledScreenMixin {
    private static final Lazy<Pair<Identifier, Identifier>> LOCKED_SPRITE_REF = new Lazy<>(() -> com.mojang.datafixers.util.Pair.of(PlayerScreenHandler.BLOCK_ATLAS_TEXTURE, LockiClient.LOCKED_SLOT_SPRITE));

    @ModifyVariable(method = "drawSlot", at = @At(value = "INVOKE_ASSIGN", target = "Lnet/minecraft/screen/slot/Slot;getBackgroundSprite()Lcom/mojang/datafixers/util/Pair;"))
    private @Nullable Pair<Identifier, Identifier> replaceSprite(@Nullable Pair<Identifier, Identifier> baseSprite, MatrixStack matrixStack, Slot slot) {
        if (((LockableSlot) slot).locki$shouldBeLocked()) {
            return HandledScreenMixin.LOCKED_SPRITE_REF.get();
        }
        return baseSprite;
    }
}
