/*
 * This file is part of Utils, licensed under the MIT License (MIT). See the LICENSE file at the root of this project for more information.
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
import org.spongepowered.api.command.source.ProxySource;
import org.spongepowered.api.command.source.RconSource;
import org.spongepowered.api.command.source.RemoteSource;
import org.spongepowered.api.command.source.SignSource;
import org.spongepowered.api.command.spec.CommandExecutor;
import org.spongepowered.api.command.spec.CommandSpec;
import org.spongepowered.api.command.spec.CommandSpec.Builder;
import org.spongepowered.api.entity.living.player.Player;
import org.spongepowered.api.entity.vehicle.minecart.CommandBlockMinecart;
import org.spongepowered.api.text.serializer.TextSerializers;

public abstract class BaseCommandExecutor<T extends CommandSource> implements CommandExecutor {

    public boolean async = false;
    private Class<T> type;
    public Object plugin;
    public String invalidSource;

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
            src.sendMessage(TextSerializers.FORMATTING_CODE.deserialize(invalidSource.replaceAll("\\{sourcetype\\}", "command block")));
            return false;
        } else if (type.equals(CommandBlockMinecart.class) && !(src instanceof CommandBlockMinecart)) {
            src.sendMessage(TextSerializers.FORMATTING_CODE.deserialize(invalidSource.replaceAll("\\{sourcetype\\}", "command block minecart")));
            return false;
        } else if (type.equals(CommandBlockSource.class) && !(src instanceof CommandBlockSource)) {
            src.sendMessage(TextSerializers.FORMATTING_CODE.deserialize(invalidSource.replaceAll("\\{sourcetype\\}", "solid command block")));
            return false;
        } else if (type.equals(ConsoleSource.class) && !(src instanceof ConsoleSource)) {
            src.sendMessage(TextSerializers.FORMATTING_CODE.deserialize(invalidSource.replaceAll("\\{sourcetype\\}", "console")));
            return false;
        } else if (type.equals(Player.class) && !(src instanceof Player)) {
            src.sendMessage(TextSerializers.FORMATTING_CODE.deserialize(invalidSource.replaceAll("\\{sourcetype\\}", "player")));
            return false;
        } else if (type.equals(ProxySource.class) && !(src instanceof ProxySource)) {
            src.sendMessage(TextSerializers.FORMATTING_CODE.deserialize(invalidSource.replaceAll("\\{sourcetype\\}", "proxy source")));
            return false;
        } else if (type.equals(RconSource.class) && !(src instanceof RconSource)) {
            src.sendMessage(TextSerializers.FORMATTING_CODE.deserialize(invalidSource.replaceAll("\\{sourcetype\\}", "rcon client")));
            return false;
        } else if (type.equals(RemoteSource.class) && !(src instanceof RemoteSource)) {
            src.sendMessage(TextSerializers.FORMATTING_CODE.deserialize(invalidSource.replaceAll("\\{sourcetype\\}", "remote source")));
            return false;
        } else if (type.equals(SignSource.class) && !(src instanceof SignSource)) {
            src.sendMessage(TextSerializers.FORMATTING_CODE.deserialize(invalidSource.replaceAll("\\{sourcetype\\}", "sign source")));
            return false;
        } else {
            return true;
        }
    }

}
