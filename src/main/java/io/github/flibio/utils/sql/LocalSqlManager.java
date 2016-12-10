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

package io.github.flibio.utils.sql;

import org.slf4j.Logger;
import org.spongepowered.api.Sponge;
import org.spongepowered.api.plugin.Plugin;

import java.util.Optional;

public class LocalSqlManager extends SqlManager {

    protected LocalSqlManager(Logger logger, String folderName, String file) {
        super(logger, "jdbc:h2:" + appendSlash(folderName) + file + ";AUTO_SERVER=TRUE");
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
