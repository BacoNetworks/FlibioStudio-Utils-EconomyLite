/*
 * This file is part of Utils, licensed under the MIT License (MIT). See the LICENSE file at the root of this project for more information.
 */

package io.github.flibio.utils.sql;

import org.slf4j.Logger;
import org.spongepowered.api.Sponge;
import org.spongepowered.api.plugin.Plugin;

import java.util.Optional;

public class LocalSqlManager extends SqlManager {

    protected LocalSqlManager(Logger logger, String folderName, String file) {
        super(logger, "jdbc:h2:" + appendSlash(folderName) + file);
    }

    /**
     * Creates a new LocalSqlManager instance. Uses H2 database.
     * 
     * @param plugin An instance of the main plugin class.
     * @param file The file associated with the data manager
     * @return The new LocalSqlManager instance, if the plugin class is valid.
     */
    public static Optional<LocalSqlManager> createInstance(Object plugin, String file) {
        if (plugin.getClass().isAnnotationPresent(Plugin.class)) {
            Plugin annotation = plugin.getClass().getAnnotation(Plugin.class);
            Logger logger = Sponge.getGame().getPluginManager().getPlugin(annotation.id()).get().getLogger();
            return Optional.of(new LocalSqlManager(logger, "./config/" + annotation.name().toLowerCase().replaceAll(" ", "_"), file));
        } else {
            return Optional.empty();
        }
    }

    /**
     * Creates a new LocalSqlManager instance. Uses H2 database.
     * 
     * @param plugin An instance of the main plugin class.
     * @param file The file associated with the data manager.
     * @param folder The folder to use.
     * @return The new LocalSqlManager instance, if the plugin class is valid.
     */
    public static Optional<LocalSqlManager> createInstance(Object plugin, String file, String folder) {
        if (plugin.getClass().isAnnotationPresent(Plugin.class)) {
            Plugin annotation = plugin.getClass().getAnnotation(Plugin.class);
            Logger logger = Sponge.getGame().getPluginManager().getPlugin(annotation.id()).get().getLogger();
            return Optional.of(new LocalSqlManager(logger, folder, file));
        } else {
            return Optional.empty();
        }
    }

    private static String appendSlash(String path) {
        // Append a slash if necessary
        if (!path.substring(path.length() - 1).equalsIgnoreCase("/")) {
            return path + "/";
        }
        return path;
    }
}
