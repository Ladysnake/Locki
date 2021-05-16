package io.github.ladysnake.locki;

import com.google.common.base.Preconditions;
import me.lucko.fabric.api.permissions.v0.PermissionCheckEvent;
import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.util.TriState;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.server.command.ServerCommandSource;
import net.minecraft.util.Identifier;
import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.Nullable;

import java.util.HashMap;
import java.util.Map;

public final class Locki implements ModInitializer {
	private static final Map<Identifier, InventoryLock> locks = new HashMap<>();
	private static int nextId;

	/**
	 *
	 * @param id a unique identifier for the created lock
	 */
	public static synchronized InventoryLock registerLock(Identifier id) {
		Preconditions.checkNotNull(id);
		return locks.computeIfAbsent(id, id1 -> new InventoryLock(id1, nextId++));
	}

	@Contract("null -> null; !null -> _")
	public static @Nullable InventoryLock getLock(@Nullable Identifier id) {
		return locks.get(id);
	}

	@Override
	public void onInitialize() {
		PermissionCheckEvent.EVENT.register((source, permission) -> {
			if (source instanceof ServerCommandSource && permission.startsWith("locki.inventory.")) {
				Entity entity = ((ServerCommandSource) source).getEntity();
				if (entity instanceof PlayerEntity) {
					if (InventoryKeeper.get((PlayerEntity) entity).isLocked(permission.substring(6))) {
						return TriState.FALSE;
					}
				}
			}
			return TriState.DEFAULT;
		});
	}
}
