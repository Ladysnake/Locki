/*
 * Locki
 * Copyright (C) 2021-2023 Ladysnake
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
package io.github.ladysnake.locki.impl;

import com.mojang.brigadier.CommandDispatcher;
import io.github.ladysnake.locki.InventoryLock;
import io.github.ladysnake.locki.InventoryNode;
import io.github.ladysnake.locki.Locki;
import me.lucko.fabric.api.permissions.v0.Permissions;
import net.minecraft.server.command.ServerCommandSource;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.text.Text;
import net.minecraft.util.Identifier;

import java.util.Collection;

import static io.github.ladysnake.locki.impl.InventoryLockArgumentType.getInventoryLock;
import static io.github.ladysnake.locki.impl.InventoryLockArgumentType.inventoryLock;
import static io.github.ladysnake.locki.impl.InventoryNodeArgumentType.getInventoryNode;
import static io.github.ladysnake.locki.impl.InventoryNodeArgumentType.inventoryNode;
import static java.util.Collections.singleton;
import static net.minecraft.command.argument.EntityArgumentType.getPlayers;
import static net.minecraft.command.argument.EntityArgumentType.players;
import static net.minecraft.server.command.CommandManager.argument;
import static net.minecraft.server.command.CommandManager.literal;

public final class LockiCommand {
    public static final InventoryLock COMMAND_LOCK = Locki.registerLock(new Identifier("locki", "commands"), true);

    public static void register(CommandDispatcher<ServerCommandSource> dispatcher) {
        dispatcher.register(literal("locki")
                // Require perms at the root to avoid showing empty "/locki" command to regular players
                .requires(Permissions.require("locki.command.lock.self", 2))
                .then(literal("lock")
                        // Require perms again in case we add more subcommands in the future
                        .requires(Permissions.require("locki.command.lock.self", 2))
                        .then(argument("node", inventoryNode())
                                .executes(ctx -> lock(ctx.getSource(), singleton(ctx.getSource().getPlayer()), getInventoryNode(ctx, "node"), COMMAND_LOCK))
                                .then(argument("targets", players())
                                        // Require another permission for disguising other people
                                        .requires(Permissions.require("locki.command.lock", 2))
                                        .executes(ctx -> lock(ctx.getSource(), getPlayers(ctx, "targets"), getInventoryNode(ctx, "node"), COMMAND_LOCK))
                                        .then(argument("lock", inventoryLock())
                                                .executes(ctx -> lock(ctx.getSource(), getPlayers(ctx, "targets"), getInventoryNode(ctx, "node"), getInventoryLock(ctx, "lock")))
                                        )
                                )
                        )
                )
                .then(literal("unlock")
                        // Require perms again in case we add more subcommands in the future
                        .requires(Permissions.require("locki.command.lock.self", 2))
                        .then(argument("node", inventoryNode())
                                .executes(ctx -> unlock(ctx.getSource(), singleton(ctx.getSource().getPlayer()), getInventoryNode(ctx, "node"), COMMAND_LOCK))
                                .then(argument("targets", players())
                                        // Require another permission for disguising other people
                                        .requires(Permissions.require("locki.command.lock", 2))
                                        .executes(ctx -> unlock(ctx.getSource(), getPlayers(ctx, "targets"), getInventoryNode(ctx, "node"), COMMAND_LOCK))
                                        .then(argument("lock", inventoryLock())
                                                .executes(ctx -> unlock(ctx.getSource(), getPlayers(ctx, "targets"), getInventoryNode(ctx, "node"), getInventoryLock(ctx, "lock")))
                                        )
                                )
                        )
                )
        );
    }

    private static int lock(ServerCommandSource source, Collection<ServerPlayerEntity> players, InventoryNode invNode, InventoryLock lock) {
        int count = 0;
        for (ServerPlayerEntity player : players) {
            lock.lock(player, invNode);
            sendFeedback(source, player, invNode, "lock");
            ++count;
        }
        return count;
    }

    private static int unlock(ServerCommandSource source, Collection<ServerPlayerEntity> players, InventoryNode invNode, InventoryLock lock) {
        int count = 0;
        for (ServerPlayerEntity player : players) {
            lock.unlock(player, invNode);
            sendFeedback(source, player, invNode, "unlock");
            ++count;
        }
        return count;
    }

    private static void sendFeedback(ServerCommandSource source, ServerPlayerEntity player, InventoryNode invNode, String command) {
        String name = invNode.getFullName();
        if (source.getEntity() == player) {
            source.sendFeedback(() -> Text.translatable("locki:commands." + command + ".success.self", name), true);
        } else {
            source.sendFeedback(() -> Text.translatable("locki:commands." + command + ".success.other", player.getDisplayName(), name), true);
        }
    }
}
