/*
 * Locki
 * Copyright (C) 2021 Ladysnake
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

import com.mojang.authlib.GameProfile;
import io.github.ladysnake.locki.DefaultInventoryNodes;
import io.github.ladysnake.locki.InventoryLock;
import io.github.ladysnake.locki.Locki;
import io.github.ladysnake.lockii.Lockii;
import net.fabricmc.fabric.api.gametest.v1.FabricGameTest;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.Items;
import net.minecraft.test.GameTest;
import net.minecraft.test.TestContext;
import net.minecraft.util.math.BlockPos;

import java.util.UUID;

public class LockiTestSuite implements FabricGameTest {
    public static final InventoryLock lock = Locki.registerLock(Lockii.id("test_suite"));

    @GameTest(structureName = EMPTY_STRUCTURE)
    public void lockingPreventsItemPickup(TestContext ctx) {
        PlayerEntity mockPlayer = createMockSurvivalPlayer(ctx);
        lock.lock(mockPlayer, DefaultInventoryNodes.INVENTORY);
        BlockPos pos = ctx.getAbsolutePos(new BlockPos(1, 0, 1));
        mockPlayer.refreshPositionAndAngles(pos.getX(), pos.getY(), pos.getZ(), mockPlayer.getYaw(), mockPlayer.getPitch());
        ctx.getWorld().spawnEntity(mockPlayer);
        ctx.spawnItem(Items.DIAMOND, 1, 0, 2);
        ctx.expectEntity(EntityType.ITEM);
        ctx.waitAndRun(20, () -> {
            ctx.expectEntity(EntityType.ITEM);
            ctx.complete();
        });
    }

    @GameTest(structureName = EMPTY_STRUCTURE)
    public void itemPickupWorks(TestContext ctx) {
        PlayerEntity mockPlayer = createMockSurvivalPlayer(ctx);
        BlockPos pos = ctx.getAbsolutePos(new BlockPos(1, 0, 1));
        mockPlayer.refreshPositionAndAngles(pos.getX(), pos.getY(), pos.getZ(), mockPlayer.getYaw(), mockPlayer.getPitch());
        ctx.getWorld().spawnEntity(mockPlayer);
        ctx.spawnItem(Items.REDSTONE, 1, 0, 2);
        ctx.waitAndRun(20, () -> {
            ctx.dontExpectEntity(EntityType.ITEM);
            ctx.complete();
        });
    }

    public PlayerEntity createMockSurvivalPlayer(TestContext ctx) {
        return new PlayerEntity(ctx.getWorld(), BlockPos.ORIGIN, 0.0f, new GameProfile(UUID.randomUUID(), "test-mock-player")){

            @Override
            public boolean isSpectator() {
                return false;
            }

            @Override
            public boolean isCreative() {
                return false;
            }
        };
    }
}
