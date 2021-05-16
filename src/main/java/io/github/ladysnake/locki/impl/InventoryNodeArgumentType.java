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
package io.github.ladysnake.locki.impl;

import com.mojang.brigadier.StringReader;
import com.mojang.brigadier.arguments.ArgumentType;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.mojang.brigadier.exceptions.DynamicCommandExceptionType;
import com.mojang.brigadier.suggestion.Suggestions;
import com.mojang.brigadier.suggestion.SuggestionsBuilder;
import io.github.ladysnake.locki.DefaultInventoryNodes;
import io.github.ladysnake.locki.InventoryNode;
import io.github.ladysnake.locki.Locki;
import net.minecraft.command.CommandSource;
import net.minecraft.server.command.ServerCommandSource;
import net.minecraft.text.TranslatableText;
import net.minecraft.util.Identifier;

import java.util.Arrays;
import java.util.Collection;
import java.util.concurrent.CompletableFuture;

public class InventoryNodeArgumentType implements ArgumentType<InventoryNode> {
    private static final DynamicCommandExceptionType NOT_FOUND_EXCEPTION = new DynamicCommandExceptionType(o -> new TranslatableText("locki:argument.inv_node.not_found", o));

    public static InventoryNodeArgumentType inventoryNode() {
        return new InventoryNodeArgumentType();
    }

    public static InventoryNode getInventoryNode(CommandContext<ServerCommandSource> context, String name) {
        return context.getArgument(name, InventoryNode.class);
    }

    @Override
    public InventoryNode parse(StringReader reader) throws CommandSyntaxException {
        int i = reader.getCursor();

        while(reader.canRead() && Identifier.isCharValid(reader.peek())) {
            reader.skip();
        }

        String string = reader.getString().substring(i, reader.getCursor());

        InventoryNode node = Locki.getNode(string);
        if (node != null) {
            return node;
        } else {
            reader.setCursor(i);
            throw NOT_FOUND_EXCEPTION.createWithContext(reader, string);
        }
    }

    @Override
    public <S> CompletableFuture<Suggestions> listSuggestions(CommandContext<S> context, SuggestionsBuilder builder) {
        return CommandSource.suggestMatching(Locki.nodeNames(), builder);
    }

    @Override
    public Collection<String> getExamples() {
        return Arrays.asList(DefaultInventoryNodes.INVENTORY.getFullName(), DefaultInventoryNodes.FEET.getFullName());
    }
}
