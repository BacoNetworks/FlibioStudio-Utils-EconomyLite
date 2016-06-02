/*
 * This file is part of Utils, licensed under the MIT License (MIT).
 *
 * Copyright (c) 2016 - 2016 FlibioStudio
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
            Command commandAnnotation = c.getClass().getAnnotation(Command.class);
            // Inject any variables
            c = parseCommand(c, invalidSource, plugin);
            // Load command details
            String[] aliases = commandAnnotation.aliases();
            String permission = commandAnnotation.permission();
            // Get the command spec
            Builder spec = c.getCommandSpecBuilder();
            spec.permission(permission);
            // Load the subcommands
            List<BaseCommandExecutor<?>> subCommands = getSubCommands(c, cmds, plugin, invalidSource);
            for (BaseCommandExecutor<?> subCommand : subCommands) {
                Command subCmdAnn = subCommand.getClass().getAnnotation(Command.class);
                CommandSpec childSpec = subCommand.getCommandSpecBuilder().permission(subCmdAnn.permission()).build();
                spec = spec.child(childSpec, subCmdAnn.aliases());
            }
            Sponge.getCommandManager().register(plugin, spec.build(), aliases);
        }
    }

    private static List<BaseCommandExecutor<?>> getSubCommands(BaseCommandExecutor<?> parent, List<? extends BaseCommandExecutor<?>> commands,
            Object plugin, String invalidSource) {
        ArrayList<BaseCommandExecutor<?>> subCommands = new ArrayList<>();
        for (BaseCommandExecutor<?> command : commands) {
            if (command.getClass().isAnnotationPresent(ParentCommand.class) && command.getClass().isAnnotationPresent(Command.class)) {
                if (command.getClass().getAnnotation(ParentCommand.class).parentCommand().equals(parent.getClass())) {
                    subCommands.add(parseCommand(command, invalidSource, plugin));
                }
            }
        }
        return subCommands;
    }

    private static BaseCommandExecutor<?> parseCommand(BaseCommandExecutor<?> cmd, String invalidSource, Object plugin) {
        try {
            cmd.getClass().getField("invalidSource").set(cmd, invalidSource);
            if (cmd.getClass().isAnnotationPresent(AsyncCommand.class)) {
                try {
                    cmd.getClass().getField("async").set(cmd, true);
                    cmd.getClass().getField("plugin").set(cmd, plugin);
                } catch (IllegalArgumentException | IllegalAccessException | NoSuchFieldException | SecurityException e) {
                    e.printStackTrace();
                }
            }
            return cmd;
        } catch (IllegalArgumentException | IllegalAccessException | NoSuchFieldException | SecurityException e) {
            e.printStackTrace();
            return null;
        }
    }
}
