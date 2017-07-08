/*
 * This file is part of Utils, licensed under the MIT License (MIT).
 *
 * Copyright (c) 2016 - 2017 FlibioStudio
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

package io.github.flibio.utils.file;

import com.google.common.collect.Maps;
import com.google.common.reflect.TypeToken;
import ninja.leaping.configurate.ConfigurationNode;
import ninja.leaping.configurate.hocon.HoconConfigurationLoader;
import ninja.leaping.configurate.objectmapping.ObjectMappingException;
import org.slf4j.Logger;
import org.spongepowered.api.Sponge;

import java.io.File;
import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.HashMap;

public class FileManager {

    private Object plugin;
    private Logger logger;
    private Path folder;

    private HashMap<String, ConfigurationNode> cache = Maps.newHashMap();

    private FileManager(Path folder, Logger logger, Object plugin) {
        this.logger = logger;
        this.folder = folder;
        this.plugin = plugin;
        // Make the folder directory
        folder.toFile().mkdirs();
        // Start the save thread
    }

    public static FileManager create(Path folder, Logger logger, Object plugin) {
        return new FileManager(folder, logger, plugin);
    }

    /**
     * Checks if a node exists.
     * 
     * @param fileName The name of the file.
     * @param path The path of the node.
     * @return If the node exists or not.
     */
    public boolean nodeExists(String fileName, String... path) {
        return getFile(fileName).getNode((Object[]) path).isVirtual();
    }

    /**
     * Gets the value of a node. Will return null if the option does not exist.
     * 
     * @param <T> The type of the node.
     * @param type The class type of the node.
     * @param path Path to the node.
     * @return The value of the node.
     */
    public <T> T getValue(String fileName, Class<T> type, String... path) {
        ConfigurationNode node = getFile(fileName).getNode((Object[]) path);
        try {
            return node.getValue(TypeToken.of(type));
        } catch (ObjectMappingException e) {
            logger.error("Failed to get node " + path + ": " + e.getMessage());
            return null;
        }
    }

    /**
     * Sets the value of a node.
     * 
     * @param <T> The type of the node.
     * @param fileName The name of the file.
     * @param type The class type of the node.
     * @param value The value to set the node to.
     * @param path The path to the node.
     * @return If the value was set successfully or not.
     */
    public <T> boolean setValue(String fileName, Class<T> type, T value, String... path) {
        ConfigurationNode node = getFile(fileName).getNode((Object[]) path);
        try {
            node.setValue(TypeToken.of(type), value);
            saveFile(fileName);
            return true;
        } catch (ObjectMappingException e) {
            logger.error("Failed to set node " + path + ": " + e.getMessage());
            return false;
        }
    }

    /**
     * Deletes a node.
     * 
     * @param fileName The name of the file.
     * @param path The path to the node.
     */
    public void deleteValue(String fileName, String... path) {
        ConfigurationNode node = getFile(fileName).getNode((Object[]) path);
        node.setValue(null);
        saveFile(fileName);
    }

    /**
     * Gets a file. The file will be created if it doesn't exist.
     * 
     * @param fileName The name of the file.
     * @return The file.
     */
    public File getRawFile(String fileName) {
        File file = Paths.get(folder.toString(), fileName).toFile();
        // Make the file if it doesn't exist
        if (!file.exists()) {
            try {
                file.createNewFile();
            } catch (IOException e) {
                logger.error("Failed to create " + file.getName() + ": " + e.getMessage());
            }
        }
        return file;
    }

    /**
     * Gets the node from a file. The file will be created if it doesn't exist.
     * 
     * @param fileName The name of the file.
     * @return The node generated from the file.
     */
    public ConfigurationNode getFile(String fileName) {
        if (cache.containsKey(fileName)) {
            // Get the file from the cache
            return cache.get(fileName);
        } else {
            // Reload the file
            return reloadFile(fileName);
        }
    }

    /**
     * Forces a file to be loaded. The file will be created if it doesn't exist.
     * 
     * @param fileName The name of the file.
     * @return The node generated from the file.
     */
    public ConfigurationNode reloadFile(String fileName) {
        File file = getRawFile(fileName);
        try {
            ConfigurationNode node = HoconConfigurationLoader.builder().setFile(file).build().load();
            cache.put(fileName, node);
            return node;
        } catch (IOException e) {
            logger.error("Failed to load " + fileName + ": " + e.getMessage());
            return null;
        }
    }

    /**
     * Saves a node to a file.
     * 
     * @param fileName The name of the file.
     * @param node The node to write to the file.
     */
    public void saveFile(String fileName, ConfigurationNode node) {
        cache.put(fileName, node);
        saveFile(fileName);
    }

    /**
     * Saves a file.
     * 
     * @param fileName The name of the file.
     */
    public void saveFile(String fileName) {
        if (!cache.containsKey(fileName))
            return;
        // Save the file
        Sponge.getScheduler().createTaskBuilder().execute(c -> {
            try {
                HoconConfigurationLoader.builder().setFile(getRawFile(fileName)).build().save(cache.get(fileName));
            } catch (IOException e) {
                logger.error("Failed to save " + fileName + ": " + e.getMessage());
            }
        }).async().submit(plugin);
    }

}
