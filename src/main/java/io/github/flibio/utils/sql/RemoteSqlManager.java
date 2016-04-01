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
