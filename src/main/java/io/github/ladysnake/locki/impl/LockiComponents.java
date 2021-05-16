package io.github.ladysnake.locki.impl;

import dev.onyxstudios.cca.api.v3.component.ComponentKey;
import dev.onyxstudios.cca.api.v3.component.ComponentRegistry;
import dev.onyxstudios.cca.api.v3.entity.EntityComponentFactoryRegistry;
import dev.onyxstudios.cca.api.v3.entity.EntityComponentInitializer;
import nerdhub.cardinal.components.api.util.RespawnCopyStrategy;
import net.minecraft.util.Identifier;

public final class LockiComponents implements EntityComponentInitializer {
    public static final ComponentKey<InventoryKeeperBase> INVENTORY_KEEPER = ComponentRegistry.getOrCreate(new Identifier("locki", "inventory_keeper"), InventoryKeeperBase.class);

    @Override
    public void registerEntityComponentFactories(EntityComponentFactoryRegistry registry) {
        registry.registerForPlayers(INVENTORY_KEEPER, PlayerInventoryKeeper::new, RespawnCopyStrategy.INVENTORY);
    }
}
