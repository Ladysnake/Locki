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
import io.github.ladysnake.locki.InventoryNode;
import io.github.ladysnake.locki.Locki;
import io.github.ladysnake.locki.impl.LockiComponents;
import io.github.ladysnake.lockii.Lockii;
import me.lucko.fabric.api.permissions.v0.Permissions;
import net.minecraft.entity.EntityType;
import net.minecraft.item.Items;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.test.GameTest;
import net.minecraft.test.TestContext;
import net.minecraft.util.Identifier;
import org.quiltmc.qsl.testing.api.game.QuiltGameTest;
import org.quiltmc.qsl.testing.api.game.QuiltTestContext;

import static io.github.ladysnake.elmendorf.ByteBufChecker.any;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

public class LockiTestSuite implements QuiltGameTest {
    public static final InventoryLock lock = Locki.registerLock(Lockii.id("test_suite"));

    @GameTest(structureName = EMPTY_STRUCTURE)
    public void checkPermission(TestContext ctx) {
        ServerPlayerEntity player = ctx.spawnServerPlayer(0, 0, 0);

        assertTrue(Permissions.check(player, "locki.access.inventory", true));
        InventoryLock lock = Locki.registerLock(new Identifier("test", "test"));
        InventoryNode node = Locki.registerNode(InventoryNode.ROOT, "test");
        Locki.registerNode(node, "child");
        Locki.registerNode(InventoryNode.ROOT, "test-foo");
        player.getInventory().addLock(lock, node);

        assertFalse(Permissions.check(player, "locki.access.test", true));
        assertFalse(Permissions.check(player, "locki.access.test.child", true));
        assertTrue(Permissions.check(player, "locki.access.test-foo", true));
        ctx.complete();
    }

    @GameTest(structureName = EMPTY_STRUCTURE)
    public void lockingPreventsItemPickup(QuiltTestContext ctx) {
        ServerPlayerEntity player = ctx.spawnServerPlayer(1, 0, 1);
        player.getInventory().addLock(lock, DefaultInventoryNodes.INVENTORY);
        ctx.spawnItemEntity(Items.DIAMOND, 1, 0, 2);
        ctx.expectEntity(EntityType.ITEM);
        player.playerTick();
        ctx.expectEntity(EntityType.ITEM);
        ctx.complete();
    }

    @GameTest(structureName = EMPTY_STRUCTURE)
    public void itemPickupWorks(QuiltTestContext ctx) {
        ServerPlayerEntity player = ctx.spawnServerPlayer(1, 0, 1);
        ctx.spawnItemEntity(Items.REDSTONE, 1, 0, 1);
        ctx.expectEntity(EntityType.ITEM);
        player.playerTick();
        ctx.expectNoEntity(EntityType.ITEM);
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
