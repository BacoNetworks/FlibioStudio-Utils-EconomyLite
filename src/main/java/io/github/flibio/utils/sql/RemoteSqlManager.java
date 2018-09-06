/*
 * This file is part of Utils, licensed under the MIT License (MIT). See the LICENSE file at the root of this project for more information.
 */

package io.github.flibio.utils.sql;

import org.slf4j.Logger;
import org.spongepowered.api.Sponge;
import org.spongepowered.api.plugin.Plugin;

import java.util.Optional;

public class RemoteSqlManager extends SqlManager {

    protected RemoteSqlManager(Logger logger, String source) {
        super(logger, source);
    }

    /**
     * Creates a new RemoteSqlManager instance. Uses MySQL databse.
     * 
     * @param plugin An instance of the main plugin class.
     * @param hostname The hostname of the MySQL server.
     * @param port The port of the MySQL server.
     * @param database The database on the MySQL server.
     * @param username The username for the MySQL server.
     * @param password The password for the MySQL server.
     * @return The new RemoteSqlManager instance, if the plugin class is valid.
     */
    public static Optional<RemoteSqlManager> createInstance(Object plugin, String hostname, String port, String database, String username,
            String password) {
        if (plugin.getClass().isAnnotationPresent(Plugin.class)) {
            Plugin annotation = plugin.getClass().getAnnotation(Plugin.class);
            Logger logger = Sponge.getGame().getPluginManager().getPlugin(annotation.id()).get().getLogger();
            return Optional.of(new RemoteSqlManager(logger, "jdbc:mysql://" + hostname + ":" + port + "/" + database + "?user=" + username
                    + "&password=" + password));
        } else {
            return Optional.empty();
        }
    }
}
