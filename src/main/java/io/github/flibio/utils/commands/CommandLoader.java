/*
 * This file is part of Utils, licensed under the MIT License (MIT).
 *
 * Copyright (c) 2016 - 2016 Flibio
 * Copyright (c) Contributors
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in
 * all copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN
 * THE SOFTWARE.
 */
package io.github.flibio.utils.commands;

import org.spongepowered.api.Sponge;
import org.spongepowered.api.command.spec.CommandSpec;
import org.spongepowered.api.command.spec.CommandSpec.Builder;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class CommandLoader {

    /**
     * Registers all of the commands presented.
     * 
     * @param plugin An instance of the main plugin class.
     * @param invalidSource The message that will be sent to a CommandSource if
     *        they do not meet the required CommandSource type.
     * @param commands All of the commands that need to be registered.
     */
    public static void registerCommands(Object plugin, String invalidSource, BaseCommandExecutor<?>... commands) {
        List<BaseCommandExecutor<?>> cmds = Arrays.asList(commands);
        for (BaseCommandExecutor<?> c : cmds) {
            if (c.getClass().isAnnotationPresent(ParentCommand.class) || !c.getClass().isAnnotationPresent(Command.class)) {
                continue;
            }
            // Inject the invalid source message
            try {
                c.getClass().getField("invalidSource").set(c, invalidSource);
            } catch (IllegalArgumentException | IllegalAccessException | NoSuchFieldException | SecurityException e) {
                e.printStackTrace();
            }
            String[] aliases = c.getClass().getAnnotation(Command.class).aliases();
            // Check if the class is async
            if (c.getClass().isAnnotationPresent(AsyncCommand.class)) {
                try {
                    c.getClass().getField("async").set(c, true);
                    c.getClass().getField("plugin").set(c, plugin);
                } catch (IllegalArgumentException | IllegalAccessException | NoSuchFieldException | SecurityException e) {
                    e.printStackTrace();
                }
            }
            Builder spec = c.getCommandSpecBuilder();
            List<BaseCommandExecutor<?>> subCommands = getSubCommands(c, cmds, plugin);
            for (BaseCommandExecutor<?> subCommand : subCommands) {
                CommandSpec childSpec =
                        subCommand.getCommandSpecBuilder().permission(subCommand.getClass().getAnnotation(Command.class).permission()).build();
                spec = spec.child(childSpec, subCommand.getClass().getAnnotation(Command.class).aliases());
            }
            Sponge.getCommandManager().register(plugin, spec.build(), aliases);
        }
    }

    private static List<BaseCommandExecutor<?>> getSubCommands(BaseCommandExecutor<?> parent, List<? extends BaseCommandExecutor<?>> commands,
            Object plugin) {
        ArrayList<BaseCommandExecutor<?>> subCommands = new ArrayList<>();
        for (BaseCommandExecutor<?> command : commands) {
            if (command.getClass().isAnnotationPresent(ParentCommand.class) && command.getClass().isAnnotationPresent(Command.class)) {
                if (command.getClass().getAnnotation(ParentCommand.class).parentCommand().equals(parent.getClass())) {
                    // Check if the class is async
                    if (command.getClass().isAnnotationPresent(AsyncCommand.class)) {
                        try {
                            command.getClass().getField("async").set(command, true);
                            command.getClass().getField("plugin").set(command, plugin);
                        } catch (IllegalArgumentException | IllegalAccessException | NoSuchFieldException | SecurityException e) {
                            e.printStackTrace();
                        }
                    }
                    subCommands.add(command);
                }
            }
        }
        return subCommands;
    }
}
