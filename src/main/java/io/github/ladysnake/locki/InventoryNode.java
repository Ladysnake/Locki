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

import it.unimi.dsi.fastutil.objects.ReferenceOpenHashSet;
import org.jetbrains.annotations.ApiStatus;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Collections;
import java.util.Set;

/**
 * A tree node describing a set of one or more inventory slots.
 */
public final class InventoryNode implements Comparable<InventoryNode> {
    public static final InventoryNode ROOT = new InventoryNode(null, "");

    private final @Nullable InventoryNode parent;
    private final Set<InventoryNode> descendants = new ReferenceOpenHashSet<>();
    private final Set<InventoryNode> unmodifiableDescendants = Collections.unmodifiableSet(descendants);
    private final String name;

    @ApiStatus.Internal
    InventoryNode(@Nullable InventoryNode parent, String name) {
        this.parent = parent;
        this.name = name;
    }

    @ApiStatus.Internal
    void addDescendant(InventoryNode node) {
        this.descendants.add(node);
        if (this.parent != null) this.parent.addDescendant(node);
    }

    /**
     * @return a set describing all nodes that are directly or indirectly descending from this node
     */
    public Set<InventoryNode> getDescendants() {
        return this.unmodifiableDescendants;
    }

    /**
     * @return a string describing this node and its ancestors in descending order, separated by dots ('.')
     * @see Locki#getNode(String)
     */
    public String getFullName() {
        return this.name;
    }

    @Override
    public int compareTo(@NotNull InventoryNode o) {
        return this.name.compareTo(o.name);
    }

    @Override
    public String toString() {
        return this.name;
    }
}
