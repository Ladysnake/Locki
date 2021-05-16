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

import it.unimi.dsi.fastutil.objects.ReferenceOpenHashSet;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Collections;
import java.util.Set;

public final class InventoryNode implements Comparable<InventoryNode> {
    public static final InventoryNode ROOT = new InventoryNode(null, "");

    private final @Nullable InventoryNode parent;
    private final Set<InventoryNode> allChildren = new ReferenceOpenHashSet<>();
    private final Set<InventoryNode> unmodifiableAllChildren = Collections.unmodifiableSet(allChildren);
    private final String name;

    InventoryNode(@Nullable InventoryNode parent, String name) {
        this.parent = parent;
        this.name = name;
    }

    void addChild(InventoryNode node) {
        this.allChildren.add(node);
        if (this.parent != null) this.parent.addChild(node);
    }

    public Set<InventoryNode> getAllChildren() {
        return this.unmodifiableAllChildren;
    }

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
