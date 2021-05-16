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
