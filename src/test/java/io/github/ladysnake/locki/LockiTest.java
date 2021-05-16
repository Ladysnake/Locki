package io.github.ladysnake.locki;

import io.github.ladysnake.locki.impl.InventoryKeeperBase;
import me.lucko.fabric.api.permissions.v0.PermissionCheckEvent;
import me.lucko.fabric.api.permissions.v0.Permissions;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.server.command.ServerCommandSource;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.util.Identifier;
import org.junit.After;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import org.mockito.Mockito;

import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

import static org.junit.Assert.*;

public class LockiTest {
    private static final Locki instance = new Locki();

    @BeforeClass
    public static void setUpGlobal() {
        instance.onInitialize();
    }

    @Before
    public void setUp() {
        Locki.reset();
    }

    @Test
    public void registerLock() {
        List<InventoryLock> locks = IntStream.range(0, 2000)
                .parallel()
                .boxed()
                .map(String::valueOf)
                .map(Identifier::new)
                .map(Locki::registerLock)
                .collect(Collectors.toList());
        assertEquals(IntStream.range(0, 2000).boxed().collect(Collectors.toList()), locks.stream().map(InventoryLock::getRawId).sorted().collect(Collectors.toList()));
    }

    @Test
    public void getLock() {
        Identifier a = new Identifier("test", "a");
        Identifier b = new Identifier("test", "b");
        InventoryLock lock = Locki.registerLock(a);
        assertEquals(lock, Locki.getLock(a));
        assertNull(Locki.getLock(b));
    }

    @Test
    public void registerNode() {
        assertThrows(NullPointerException.class, () -> Locki.registerNode(null, null));
        assertThrows(NullPointerException.class, () -> Locki.registerNode(DefaultInventoryNodes.INVENTORY, null));
        assertThrows(NullPointerException.class, () -> Locki.registerNode(null, "a"));
        assertThrows(IllegalArgumentException.class, () -> Locki.registerNode(DefaultInventoryNodes.INVENTORY, "a.b"));
        assertNull("registering an invalid node has side effects", Locki.getNode("a.b"));
        assertNull("registering an invalid node has side effects", Locki.getNode(null));
        assertEquals("inventory.main.a", Locki.registerNode(DefaultInventoryNodes.MAIN_INVENTORY, "a").getFullName());
    }

    @Test
    public void getNode() {
        InventoryNode node = Locki.registerNode(DefaultInventoryNodes.ARMOR, "b");
        assertEquals(node, Locki.getNode("inventory.armor.b"));
        assertNull(Locki.getNode("inventory.armor.a"));
    }

    @Test
    public void checkPermission() {
        InventoryKeeper keeper = new InventoryKeeperBase();
        instance.keeperFunction = p -> keeper;

        ServerPlayerEntity player = Mockito.mock(ServerPlayerEntity.class);
        ServerCommandSource source = Mockito.mock(ServerCommandSource.class);
        Mockito.when(source.getEntity()).thenReturn(player);
        Mockito.when(player.getCommandSource()).thenReturn(source);

        assertTrue(Permissions.check(player, "locki.access.inventory", true));
        InventoryLock lock = Locki.registerLock(new Identifier("test", "test"));
        InventoryNode node = Locki.registerNode(InventoryNode.ROOT, "test");
        Locki.registerNode(node, "child");
        Locki.registerNode(InventoryNode.ROOT, "foo");
        keeper.addLock(lock, node);

        assertFalse(Permissions.check(player, "locki.access.test", true));
        assertFalse(Permissions.check(player, "locki.access.test.child", true));
        assertTrue(Permissions.check(player, "locki.access.foo", true));
    }
}