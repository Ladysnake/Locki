package io.github.ladysnake.locki;

import com.google.common.annotations.VisibleForTesting;
import com.google.common.base.Preconditions;
import me.lucko.fabric.api.permissions.v0.PermissionCheckEvent;
import net.fabricmc.api.ModInitializer;
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

public final class Locki implements ModInitializer {
	public static final Logger LOGGER = LogManager.getLogger("Locki");

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
		Preconditions.checkArgument(name.length() > 0, "Node name cannot be empty");
		Preconditions.checkArgument(name.indexOf('.') < 0, "Illegal character '.' in node name");

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

	@VisibleForTesting
	Function<PlayerEntity, InventoryKeeper> keeperFunction = InventoryKeeper::get;

	@Override
	public void onInitialize() {
		DefaultInventoryNodes.init();

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
