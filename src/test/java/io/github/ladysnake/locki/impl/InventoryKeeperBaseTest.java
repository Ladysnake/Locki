package io.github.ladysnake.locki.impl;

import com.google.common.collect.ImmutableSet;
import com.mojang.datafixers.util.Unit;
import io.github.ladysnake.locki.DefaultInventoryNodes;
import io.github.ladysnake.locki.InventoryLock;
import io.github.ladysnake.locki.Locki;
import net.minecraft.util.Identifier;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import java.util.NavigableMap;
import java.util.TreeMap;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.Stream;

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
}