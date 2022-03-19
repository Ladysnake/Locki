/*
 * Locki
 * Copyright (C) 2021-2022 Ladysnake
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
package io.github.ladysnake.lockii.tests;

import io.github.ladysnake.locki.DefaultInventoryNodes;
import io.github.ladysnake.locki.InventoryLock;
import io.github.ladysnake.locki.Locki;
import io.github.ladysnake.locki.impl.LockiComponents;
import io.github.ladysnake.lockii.Lockii;
import net.fabricmc.fabric.api.gametest.v1.FabricGameTest;
import net.minecraft.entity.EntityType;
import net.minecraft.item.Items;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.test.GameTest;
import net.minecraft.test.TestContext;

import static io.github.ladysnake.elmendorf.ByteBufChecker.any;

public class LockiTestSuite implements FabricGameTest {
    public static final InventoryLock lock = Locki.registerLock(Lockii.id("test_suite"));

    @GameTest(structureName = EMPTY_STRUCTURE)
    public void lockingPreventsItemPickup(TestContext ctx) {
        ServerPlayerEntity player = ctx.spawnServerPlayer(1, 0, 1);
        player.getInventory().addLock(lock, DefaultInventoryNodes.INVENTORY);
        ctx.spawnItem(Items.DIAMOND, 1, 0, 2);
        ctx.expectEntity(EntityType.ITEM);
        player.playerTick();
        ctx.expectEntity(EntityType.ITEM);
        ctx.complete();
    }

    @GameTest(structureName = EMPTY_STRUCTURE)
    public void itemPickupWorks(TestContext ctx) {
        ServerPlayerEntity player = ctx.spawnServerPlayer(1, 0, 1);
        ctx.spawnItem(Items.REDSTONE, 1, 0, 1);
        ctx.expectEntity(EntityType.ITEM);
        player.playerTick();
        ctx.dontExpectEntity(EntityType.ITEM);
        ctx.complete();
    }

    @GameTest(structureName = EMPTY_STRUCTURE)
    public void componentGetsSynced(TestContext ctx) {
        ServerPlayerEntity player = ctx.spawnServerPlayer(1, 0, 1);
        player.getInventory().addLock(lock, DefaultInventoryNodes.INVENTORY);
        ctx.verifyConnection(player, conn -> conn.sentEntityComponentUpdate(player, LockiComponents.INVENTORY_KEEPER, c -> c.checkVarInt(any())
                .checkString(any())
                .checkVarInt(1)
                .checkByte((byte)(1 << lock.getRawId()))));
        ctx.complete();
    }
}
