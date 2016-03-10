package io.github.flibio.utils.commands;

import org.spongepowered.api.Sponge;

public class CommandLoader {

    /**
     * Registers all of the commands presented.
     * 
     * @param plugin An instance of the main plugin class.
     * @param cmds All of the commands that need to be registered.
     */
    public static void registerCommands(Object plugin, BaseCommandExecutor<?>... cmds) {
        for (BaseCommandExecutor<?> c : cmds) {
            // Check if the class is async
            if (c.getClass().isAnnotationPresent(AsyncCommand.class)) {
                try {
                    c.getClass().getField("async").set(c, true);
                    c.getClass().getField("plugin").set(c, plugin);
                } catch (IllegalArgumentException | IllegalAccessException | NoSuchFieldException | SecurityException e) {
                    e.printStackTrace();
                }
            }
            Sponge.getCommandManager().register(plugin, c.getCommandSpec());
        }
    }
}
