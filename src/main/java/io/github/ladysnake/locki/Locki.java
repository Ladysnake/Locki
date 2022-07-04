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
package io.github.ladysnake.locki;

import com.google.common.annotations.VisibleForTesting;
import com.google.common.base.Preconditions;
import com.mojang.brigadier.arguments.StringArgumentType;
import io.github.ladysnake.locki.impl.InventoryLockArgumentType;
import io.github.ladysnake.locki.impl.InventoryNodeArgumentType;
import io.github.ladysnake.locki.impl.LockiCommand;
import me.lucko.fabric.api.permissions.v0.PermissionCheckEvent;
import net.fabricmc.fabric.api.util.TriState;
import net.minecraft.command.argument.IdentifierArgumentType;
import net.minecraft.command.argument.SingletonArgumentInfo;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.server.command.ServerCommandSource;
import net.minecraft.util.Identifier;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.Nullable;
import org.quiltmc.loader.api.ModContainer;
import org.quiltmc.qsl.base.api.entrypoint.ModInitializer;
import org.quiltmc.qsl.command.api.CommandRegistrationCallback;
import org.quiltmc.qsl.command.api.ServerArgumentType;

import java.util.HashMap;
import java.util.Map;
import java.util.function.Predicate;
import java.util.regex.Pattern;
import java.util.stream.Stream;

/**
 * Main entrypoint, contains static methods to register locki objects.
 *
 * @see #registerLock(Identifier)
 * @see #registerNode(InventoryNode, String)
 */
public final class Locki implements ModInitializer {
    public static final Logger LOGGER = LogManager.getLogger("Locki");
    public static final Pattern NODE_NAME_PART = Pattern.compile("[a-z0-9_-]+");

    private static final Map<Identifier, InventoryLock> locks = new HashMap<>();
    private static final Map<String, InventoryNode> nodes = new HashMap<>();
    private static int nextId;

    /**
     * Registers an {@link InventoryLock} if it does not already exist.
     *
     * <p>Calling this method is equivalent to {@code registerLock(id, true)}.
     *
     * @param id a unique identifier for the created lock
     * @return the (created or previously registered) lock
     */
    public static synchronized InventoryLock registerLock(Identifier id) {
        return registerLock(id, true);
    }

    /**
     * Registers an {@link InventoryLock} if it does not already exist.
     *
     * <p>Persistent locks will be saved with the player's data.
     * They will persist through re-logging and coming back from the End.
     * Non-persistent locks depend on an external persistence mechanism to not randomly disappear.
     *
     * @param id a unique identifier for the created lock
     * @param persistent whether this lock should be independently saved with the player's data
     * @return the (created or previously registered) lock
     * @throws IllegalStateException if a lock was previously registered with a different {@code persistent} value
     */
    public static synchronized InventoryLock registerLock(Identifier id, boolean persistent) {
        Preconditions.checkNotNull(id);
        InventoryLock l = locks.computeIfAbsent(id, id1 -> new InventoryLock(id1, nextId++, persistent));
        if (l.shouldSave() != persistent) throw new IllegalStateException("Lock %s registered twice with differing persistence (%s, %s)".formatted(id, l.shouldSave(), persistent));
        return l;
    }

    /**
     * Gets a previously registered {@link InventoryLock}.
     *
     * @param id the identifier with which the desired lock was registered
     * @return the previously registered lock, or {@code null}
     */
    @Contract(value = "null -> null; !null -> _", pure = true)
    public static @Nullable InventoryLock getLock(@Nullable Identifier id) {
        return locks.get(id);
    }

    /**
     * Registers an {@link InventoryNode} if it does not already exist.
     *
     * <p>The passed in {@code name} will be appended to the parent's {@linkplain InventoryNode#getFullName() full name}
     * to form the new node's full name.
     *
     * @param parent the parent of the registered node
     * @param name   the last part of the new node's name
     * @return the (created or previously registered) inventory node
     * @see DefaultInventoryNodes
     */
    public static synchronized InventoryNode registerNode(InventoryNode parent, String name) {
        Preconditions.checkNotNull(parent);
        Preconditions.checkNotNull(name);
        Preconditions.checkArgument(NODE_NAME_PART.matcher(name).matches(), "Invalid node name");

        String fullName = parent == InventoryNode.ROOT ? name : parent.getFullName() + "." + name;

        return nodes.computeIfAbsent(fullName, n -> {
            InventoryNode created = new InventoryNode(parent, n);
            parent.addDescendant(created);
            return created;
        });
    }

    /**
     * Gets a previously registered {@link InventoryLock}.
     *
     * @param fullName the full path describing the node and its ancestors, as returned by {@link InventoryNode#getFullName()}
     * @return the previously registered inventory node, or {@code null}
     */
    @Contract(value = "null -> null; !null -> _", pure = true)
    public static @Nullable InventoryNode getNode(@Nullable String fullName) {
        return nodes.get(fullName);
    }

    /**
     * @return a stream describing all identifiers that have been used to register locks up until this method is called
     */
    public static Stream<Identifier> streamLockIds() {
        return locks.keySet().stream();
    }

    /**
     * @return a stream describing the full names of all nodes that have been registered up until this method is called
     */
    public static Stream<String> streamNodeNames() {
        return nodes.keySet().stream();
    }

    @Override
    public void onInitialize(ModContainer mod) {
        DefaultInventoryNodes.init();
        ModdedInventoryNodes.init();

        if (mod != null) { // Unit testing
            ServerArgumentType.register(new Identifier("locki", "inventory_lock"), InventoryLockArgumentType.class, SingletonArgumentInfo.contextFree(InventoryLockArgumentType::inventoryLock), t -> IdentifierArgumentType.identifier());
            ServerArgumentType.register(new Identifier("locki", "inventory_node"), InventoryNodeArgumentType.class, SingletonArgumentInfo.contextFree(InventoryNodeArgumentType::inventoryNode), t -> StringArgumentType.string());
        }

        CommandRegistrationCallback.EVENT.register((dispatcher, ctx, dedicated) -> LockiCommand.register(dispatcher));
        PermissionCheckEvent.EVENT.register((source, permission) -> {
            if (source instanceof ServerCommandSource && permission.startsWith("locki.access.")) {
                Entity entity = ((ServerCommandSource) source).getEntity();
                if (entity instanceof PlayerEntity) {
                    InventoryNode node = getNode(permission.substring(13));
                    if (node != null && InventoryKeeper.get((PlayerEntity) entity).isLocked(node)) {
                        return TriState.FALSE;
                    }
                }
            }
            return TriState.DEFAULT;
        });
    }

    @VisibleForTesting
    synchronized static void reset(Predicate<String> nodeRemovalFilter) {
        locks.clear();
        nodes.keySet().removeIf(nodeRemovalFilter);
        nextId = 0;
    }
}
