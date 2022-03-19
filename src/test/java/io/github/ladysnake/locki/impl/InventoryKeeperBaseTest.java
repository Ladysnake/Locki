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
package io.github.ladysnake.locki.impl;

import io.github.ladysnake.locki.DefaultInventoryNodes;
import io.github.ladysnake.locki.InventoryLock;
import io.github.ladysnake.locki.Locki;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.util.Identifier;
import net.minecraft.util.Util;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import static org.junit.Assert.*;

public class InventoryKeeperBaseTest {

    @Before
    public void setUp() {
    }

    @After
    public void tearDown() {
    }

    @Test
    public void isLocked() {
        InventoryKeeperBase instance = new InventoryKeeperBase();
        InventoryLock lock = Locki.registerLock(new Identifier("test", "test"));
        assertFalse(instance.isLocked(DefaultInventoryNodes.HANDS));
        instance.addLock(lock, DefaultInventoryNodes.HANDS);
        assertTrue(instance.isLocked(DefaultInventoryNodes.HANDS));
        assertTrue(instance.isLocked(DefaultInventoryNodes.MAIN_HAND));
        assertFalse(instance.isLocked(DefaultInventoryNodes.INVENTORY));
        assertFalse(instance.isLocked(DefaultInventoryNodes.ARMOR));
        instance.removeLock(lock, DefaultInventoryNodes.MAIN_HAND);
        assertTrue(instance.isLocked(DefaultInventoryNodes.HANDS));
        assertFalse(instance.isLocked(DefaultInventoryNodes.MAIN_HAND));
        assertTrue(instance.isLocked(DefaultInventoryNodes.OFF_HAND));
        InventoryLock lock2 = Locki.registerLock(new Identifier("test", "test2"));
        instance.addLock(lock2, DefaultInventoryNodes.INVENTORY);
        assertEquals(2, instance.getCache().get(DefaultInventoryNodes.HANDS).cardinality());
        assertTrue(instance.isLocked(DefaultInventoryNodes.OFF_HAND));
        assertTrue(instance.isLocked(DefaultInventoryNodes.MAIN_HAND));
        instance.removeLock(lock2, DefaultInventoryNodes.HANDS);
        assertEquals(1, instance.getCache().get(DefaultInventoryNodes.HANDS).cardinality());
        assertTrue(instance.isLocked(DefaultInventoryNodes.ARMOR));
        assertFalse(instance.isLocked(DefaultInventoryNodes.MAIN_HAND));
        assertTrue(instance.isLocked(DefaultInventoryNodes.OFF_HAND));
        instance.removeLock(lock, DefaultInventoryNodes.HANDS);
        assertEquals(0, instance.getCache().get(DefaultInventoryNodes.HANDS).cardinality());
        instance.addLock(lock, DefaultInventoryNodes.HANDS);
        assertEquals(1, instance.getCache().get(DefaultInventoryNodes.HANDS).cardinality());
        System.out.println("Before: " + instance.getCache());
        instance.forceRefresh();
        System.out.println("After: " + instance.getCache());
    }

    @Test
    public void serializesCorrectly() {
        InventoryKeeperBase instance = new InventoryKeeperBase();
        InventoryLock lock1 = Locki.registerLock(new Identifier("test", "test1"), true);
        InventoryLock lock2 = Locki.registerLock(new Identifier("test", "test2"), true);
        InventoryLock lock3 = Locki.registerLock(new Identifier("test", "test3"), false);
        instance.addLock(lock1, DefaultInventoryNodes.HANDS);
        instance.addLock(lock1, DefaultInventoryNodes.ARMOR);
        instance.addLock(lock2, DefaultInventoryNodes.HEAD);
        instance.addLock(lock3, DefaultInventoryNodes.INVENTORY);
        assertTrue(instance.isLockedBy(lock1, DefaultInventoryNodes.HANDS));
        assertTrue(instance.isLockedBy(lock1, DefaultInventoryNodes.ARMOR));
        assertTrue(instance.isLockedBy(lock1, DefaultInventoryNodes.HEAD));
        assertTrue(instance.isLockedBy(lock2, DefaultInventoryNodes.HEAD));
        assertTrue(instance.isLockedBy(lock3, DefaultInventoryNodes.HEAD));
        assertTrue(instance.isLockedBy(lock3, DefaultInventoryNodes.INVENTORY));
        InventoryKeeperBase copy = new InventoryKeeperBase();
        copy.readFromNbt(Util.make(new NbtCompound(), instance::writeToNbt));
        assertTrue(copy.isLockedBy(lock1, DefaultInventoryNodes.HANDS));
        assertTrue(copy.isLockedBy(lock1, DefaultInventoryNodes.ARMOR));
        assertTrue(copy.isLockedBy(lock1, DefaultInventoryNodes.HEAD));
        assertTrue(copy.isLockedBy(lock2, DefaultInventoryNodes.HEAD));
        assertFalse(copy.isLockedBy(lock3, DefaultInventoryNodes.HEAD));
        assertFalse(copy.isLockedBy(lock3, DefaultInventoryNodes.INVENTORY));
    }
}