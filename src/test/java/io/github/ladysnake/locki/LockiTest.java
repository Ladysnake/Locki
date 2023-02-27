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
package io.github.ladysnake.locki;

import net.minecraft.util.Identifier;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;

import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

import static org.junit.Assert.*;

public class LockiTest {
    private static final Locki instance = new Locki();

    @BeforeClass
    public static void setUpGlobal() {
        instance.onInitialize(null);
    }

    @Before
    public void setUp() {
        Locki.reset(s -> s.contains("test"));
    }

    @Test
    public void registerLock() {
        List<InventoryLock> locks = IntStream.range(0, 2000)
                .parallel()
                .boxed()
                .map(String::valueOf)
                .map(Identifier::new)
                .map(Locki::registerLock)
                .toList();
        assertEquals(IntStream.range(0, 2000).boxed().collect(Collectors.toList()), locks.stream().map(InventoryLock::getRawId).sorted().collect(Collectors.toList()));
    }

    @Test
    public void registerLockSeveralTimes() {
        InventoryLock a = Locki.registerLock(new Identifier("a"));
        assertEquals(a, Locki.registerLock(new Identifier("a"), true));
        assertThrows(IllegalStateException.class, () -> Locki.registerLock(new Identifier("a"), false));
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
        assertThrows(NullPointerException.class, () -> Locki.registerNode(null, "test-a"));
        assertThrows(IllegalArgumentException.class, () -> Locki.registerNode(DefaultInventoryNodes.INVENTORY, "test-a.b"));
        assertNull("registering an invalid node has side effects", Locki.getNode("test-a.b"));
        assertNull("registering an invalid node has side effects", Locki.getNode(null));
        assertEquals("inventory.main.test-a", Locki.registerNode(DefaultInventoryNodes.MAIN_INVENTORY, "test-a").getFullName());
    }

    @Test
    public void getNode() {
        InventoryNode node = Locki.registerNode(DefaultInventoryNodes.ARMOR, "test-b");
        assertEquals(node, Locki.getNode("inventory.armor.test-b"));
        assertNull(Locki.getNode("inventory.armor.test-a"));
    }
}