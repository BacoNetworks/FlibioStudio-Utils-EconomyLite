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
import org.spongepowered.api.block.tileentity.CommandBlock;
import org.spongepowered.api.command.CommandException;
import org.spongepowered.api.command.CommandResult;
import org.spongepowered.api.command.CommandSource;
import org.spongepowered.api.command.args.CommandContext;
import org.spongepowered.api.command.source.CommandBlockSource;
import org.spongepowered.api.command.source.ConsoleSource;
import org.spongepowered.api.command.source.LocatedSource;
import org.spongepowered.api.command.source.ProxySource;
import org.spongepowered.api.command.source.RconSource;
import org.spongepowered.api.command.source.RemoteSource;
import org.spongepowered.api.command.source.SignSource;
import org.spongepowered.api.command.spec.CommandExecutor;
import org.spongepowered.api.command.spec.CommandSpec;
import org.spongepowered.api.command.spec.CommandSpec.Builder;
import org.spongepowered.api.entity.living.player.Player;
import org.spongepowered.api.entity.vehicle.minecart.CommandBlockMinecart;
import org.spongepowered.api.text.Text;
import org.spongepowered.api.text.format.TextColors;

public abstract class BaseCommandExecutor<T extends CommandSource> implements CommandExecutor {

    private boolean async = false;
    private Class<T> type;
    private Object plugin;

    @SuppressWarnings("unchecked")
    public BaseCommandExecutor() {
        Class<?> rClass = GenericHelper.findSubClassParameterType(this, BaseCommandExecutor.class, 0);
        this.type = (Class<T>) rClass;
    }

    @Override
    public CommandResult execute(CommandSource src, CommandContext args) throws CommandException {
        if (!compareType(src)) {
            return CommandResult.empty();
        } else {
            @SuppressWarnings("unchecked")
            T tSrc = (T) src;
            if (async) {
                Sponge.getScheduler().createTaskBuilder().execute(r -> {
                    run(tSrc, args);
                }).async().submit(plugin);
            } else {
                run(tSrc, args);
            }
            return CommandResult.success();
        }
    }

    /**
     * Gets the CommandSpec builder. All changes to the CommandSpec should be
     * made before returning the builder.
     * 
     * @return The CommandSpec builder, with user changes already made.
     */
    public abstract Builder getCommandSpecBuilder();

    /**
     * Runs the command for the given source.
     * 
     * @param src The CommandSource.
     * @param args The command arguments.
     */
    public abstract void run(T src, CommandContext args);

    /**
     * Gets the built CommandSpec.
     * 
     * @return The built CommandSpec.
     */
    public CommandSpec getCommandSpec() {
        return this.getCommandSpecBuilder().build();
    }

    private boolean compareType(CommandSource src) {
        if (type.equals(CommandBlock.class) && !(src instanceof CommandBlock)) {
            src.sendMessage(Text.of(TextColors.RED, "You must be a command block to run this command!"));
            return false;
        } else if (type.equals(CommandBlockMinecart.class) && !(src instanceof CommandBlockMinecart)) {
            src.sendMessage(Text.of(TextColors.RED, "You must be a command block minecart to run this command!"));
            return false;
        } else if (type.equals(CommandBlockSource.class) && !(src instanceof CommandBlockSource)) {
            src.sendMessage(Text.of(TextColors.RED, "You must be a solid command block to run this command!"));
            return false;
        } else if (type.equals(ConsoleSource.class) && !(src instanceof ConsoleSource)) {
            src.sendMessage(Text.of(TextColors.RED, "You must be a console to run this command!"));
            return false;
        } else if (type.equals(LocatedSource.class) && !(src instanceof LocatedSource)) {
            src.sendMessage(Text.of(TextColors.RED, "You must be a located source to run this command!"));
            return false;
        } else if (type.equals(Player.class) && !(src instanceof Player)) {
            src.sendMessage(Text.of(TextColors.RED, "You must be a player to run this command!"));
            return false;
        } else if (type.equals(ProxySource.class) && !(src instanceof ProxySource)) {
            src.sendMessage(Text.of(TextColors.RED, "You must be a proxy source to run this command!"));
            return false;
        } else if (type.equals(RconSource.class) && !(src instanceof RconSource)) {
            src.sendMessage(Text.of(TextColors.RED, "You must be an Rcon client to run this command!"));
            return false;
        } else if (type.equals(RemoteSource.class) && !(src instanceof RemoteSource)) {
            src.sendMessage(Text.of(TextColors.RED, "You must be a remote source to run this command!"));
            return false;
        } else if (type.equals(SignSource.class) && !(src instanceof SignSource)) {
            src.sendMessage(Text.of(TextColors.RED, "You must be a sign source to run this command!"));
            return false;
        } else {
            return true;
        }
    }

}
