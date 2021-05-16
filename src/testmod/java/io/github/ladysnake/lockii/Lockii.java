package io.github.ladysnake.lockii;

import net.fabricmc.api.ModInitializer;
import net.minecraft.item.Item;
import net.minecraft.util.Identifier;
import net.minecraft.util.registry.Registry;

public final class Lockii implements ModInitializer {
    public static Identifier id(String path) {
        return new Identifier("lockii", path);
    }

    @Override
    public void onInitialize() {
        Registry.register(Registry.ITEM, id("inventory_lock"), new InventoryLockItem(new Item.Settings()));
    }
}
