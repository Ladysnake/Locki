package io.github.ladysnake.locki.impl;

import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.event.client.ClientSpriteRegistryCallback;
import net.minecraft.screen.PlayerScreenHandler;
import net.minecraft.util.Identifier;

public class LockiClient implements ClientModInitializer {
    public static final Identifier LOCKED_SLOT_SPRITE = new Identifier("locki", "gui/locked_slot");

    @Override
    public void onInitializeClient() {
        ClientSpriteRegistryCallback.event(PlayerScreenHandler.BLOCK_ATLAS_TEXTURE).register((spriteAtlasTexture, registry) ->
                registry.register(LOCKED_SLOT_SPRITE)
        );
    }
}
