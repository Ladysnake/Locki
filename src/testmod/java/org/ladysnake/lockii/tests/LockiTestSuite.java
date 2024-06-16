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
package org.ladysnake.lockii.tests;

import me.lucko.fabric.api.permissions.v0.Permissions;
import net.fabricmc.fabric.api.gametest.v1.FabricGameTest;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.EquipmentSlot;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.test.GameTest;
import net.minecraft.test.TestContext;
import net.minecraft.util.Identifier;
import org.ladysnake.elmendorf.GameTestUtil;
import org.ladysnake.locki.DefaultInventoryNodes;
import org.ladysnake.locki.InventoryLock;
import org.ladysnake.locki.InventoryNode;
import org.ladysnake.locki.Locki;
import org.ladysnake.locki.impl.LockiComponents;
import org.ladysnake.lockii.Lockii;
import static org.ladysnake.elmendorf.ByteBufChecker.any;

public class LockiTestSuite implements FabricGameTest {
    public static final InventoryLock lock = Locki.registerLock(Lockii.id("test_suite"));

    @GameTest(structureName = EMPTY_STRUCTURE)
    public void checkPermission(TestContext ctx) {
        ServerPlayerEntity player = ctx.spawnServerPlayer(0, 0, 0);

        GameTestUtil.assertTrue(
                "New player can access their whole inventory",
                Permissions.check(player, "locki.access.inventory", true)
        );
        InventoryLock lock = Locki.registerLock(new Identifier("test", "test"));
        InventoryNode node = Locki.registerNode(InventoryNode.ROOT, "test");
        Locki.registerNode(node, "child");
        Locki.registerNode(InventoryNode.ROOT, "test-foo");
        player.getInventory().addLock(lock, node);

        GameTestUtil.assertFalse(
                "Player cannot access locked slot",
                Permissions.check(player, "locki.access.test", true)
        );
        GameTestUtil.assertFalse(
                "Player cannot access descendant of locked slot",
                Permissions.check(player, "locki.access.test.child", true)
        );
        GameTestUtil.assertTrue(
                "Player can access siblings of locked slot",
                Permissions.check(player, "locki.access.test-foo", true)
        );
        ctx.complete();
    }

    @GameTest(structureName = EMPTY_STRUCTURE)
    public void lockingPreventsItemPickup(TestContext ctx) {
        ServerPlayerEntity player = ctx.spawnServerPlayer(1, 0, 1);
        player.getInventory().addLock(lock, DefaultInventoryNodes.INVENTORY);
        ctx.spawnItemEntity(Items.DIAMOND, 1, 1, 2);
        ctx.expectEntity(EntityType.ITEM);
        player.playerTick();
        ctx.expectEntity(EntityType.ITEM);
        ctx.complete();
    }

    @GameTest(structureName = EMPTY_STRUCTURE)
    public void lockingPreventsDropFromMainHand(TestContext ctx) {
        ServerPlayerEntity player = ctx.spawnServerPlayer(1, 0, 1);
        player.equipStack(EquipmentSlot.MAINHAND, new ItemStack(Items.ENCHANTED_GOLDEN_APPLE));
        player.getInventory().addLock(lock, DefaultInventoryNodes.MAIN_HAND);
        player.dropSelectedItem(true);
        GameTestUtil.assertTrue(
                "Selected item should not be dropped when main hand is locked",
                player.getMainHandStack().getItem() == Items.ENCHANTED_GOLDEN_APPLE
        );
        player.getInventory().removeLock(lock, DefaultInventoryNodes.MAIN_HAND);
        player.dropSelectedItem(true);
        GameTestUtil.assertTrue(
                "Selected item should be dropped when main hand is not locked",
                player.getMainHandStack().isEmpty()
        );
        ctx.complete();
    }

    @GameTest(structureName = EMPTY_STRUCTURE)
    public void itemPickupWorks(TestContext ctx) {
        ServerPlayerEntity player = ctx.spawnServerPlayer(1, 0, 1);
        ctx.spawnItemEntity(Items.REDSTONE, 1, 1, 1);
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
