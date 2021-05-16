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
package io.github.ladysnake.locki;

import com.google.common.annotations.VisibleForTesting;
import com.google.common.base.Preconditions;
import com.google.common.collect.Iterables;
import io.github.ladysnake.locki.impl.LockiCommand;
import me.lucko.fabric.api.permissions.v0.PermissionCheckEvent;
import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.command.v1.CommandRegistrationCallback;
import net.fabricmc.fabric.api.util.TriState;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.server.command.ServerCommandSource;
import net.minecraft.util.Identifier;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.Nullable;

import java.util.HashMap;
import java.util.Map;
import java.util.function.Function;
import java.util.regex.Pattern;
import java.util.stream.Stream;

public final class Locki implements ModInitializer {
	public static final Logger LOGGER = LogManager.getLogger("Locki");
	public static final Pattern NODE_NAME_PART = Pattern.compile("[a-z0-9_-]+");

	private static final Map<Identifier, InventoryLock> locks = new HashMap<>();
	private static final Map<String, InventoryNode> nodes = new HashMap<>();
	private static int nextId;

	/**
	 * @param id a unique identifier for the created lock
	 */
	public static synchronized InventoryLock registerLock(Identifier id) {
		Preconditions.checkNotNull(id);
		return locks.computeIfAbsent(id, id1 -> new InventoryLock(id1, nextId++));
	}

	@Contract(value = "null -> null; !null -> _", pure = true)
	public static @Nullable InventoryLock getLock(@Nullable Identifier id) {
		return locks.get(id);
	}

	public static synchronized InventoryNode registerNode(InventoryNode parent, String name) {
		Preconditions.checkNotNull(parent);
		Preconditions.checkNotNull(name);
		Preconditions.checkArgument(NODE_NAME_PART.matcher(name).matches(), "Invalid node name");

		return nodes.computeIfAbsent(parent == InventoryNode.ROOT ? name : parent.getFullName() + "." + name, n -> {
			InventoryNode created = new InventoryNode(parent, n);
			parent.addChild(created);
			return created;
		});
	}

	@Contract(value = "null -> null; !null -> _", pure = true)
	public static @Nullable InventoryNode getNode(@Nullable String fullName) {
		return nodes.get(fullName);
	}

	public static Stream<Identifier> lockIds() {
		return locks.keySet().stream();
	}

	public static Stream<String> nodeNames() {
		return nodes.keySet().stream();
	}

	@VisibleForTesting
	Function<PlayerEntity, InventoryKeeper> keeperFunction = InventoryKeeper::get;

	@Override
	public void onInitialize() {
		DefaultInventoryNodes.init();

		CommandRegistrationCallback.EVENT.register((dispatcher, dedicated) -> LockiCommand.register(dispatcher));
		PermissionCheckEvent.EVENT.register((source, permission) -> {
			if (source instanceof ServerCommandSource && permission.startsWith("locki.access.")) {
				Entity entity = ((ServerCommandSource) source).getEntity();
				if (entity instanceof PlayerEntity) {
					InventoryNode node = getNode(permission.substring(13));
					if (node != null && keeperFunction.apply((PlayerEntity) entity).isLocked(node)) {
						return TriState.FALSE;
					}
				}
			}
			return TriState.DEFAULT;
		});
	}

	@VisibleForTesting
	synchronized static void reset() {
		locks.clear();
		nodes.clear();
		nextId = 0;
	}
}
