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
package io.github.flibio.utils.file;

import ninja.leaping.configurate.ConfigurationNode;
import ninja.leaping.configurate.hocon.HoconConfigurationLoader;
import ninja.leaping.configurate.loader.ConfigurationLoader;

import org.slf4j.Logger;
import org.spongepowered.api.Sponge;
import org.spongepowered.api.plugin.Plugin;

import com.google.common.reflect.TypeToken;

import java.io.File;
import java.util.Optional;

public class ConfigManager {

    private Logger logger;
    private String folderName;

    protected ConfigManager(Logger logger, String folderName) {
        this.logger = logger;
        this.folderName = folderName;
    }

    /**
     * Creates a new ConfigManager instance.
     * 
     * @param plugin An instance of the main plugin class.
     * @return The new ConfigManager instance, if the plugin class is valid.
     */
    public static Optional<ConfigManager> createInstance(Object plugin) {
        if (plugin.getClass().isAnnotationPresent(Plugin.class)) {
            Plugin annotation = plugin.getClass().getAnnotation(Plugin.class);
            Logger logger = Sponge.getGame().getPluginManager().getPlugin(annotation.id()).get().getLogger();
            return Optional.of(new ConfigManager(logger, annotation.name().toLowerCase().replaceAll(" ", "_")));
        } else {
            return Optional.empty();
        }
    }

    /**
     * Sets a value in a file if one doesn't exit. Saves the the changed file to
     * the disc.
     * 
     * @param fileName The name of the file. Must include the extension.
     * @param path The path of the default value. Supports sub-paths using a '.'
     *        seperator, only if subPath is set to true.
     * @param type The type of the default value.
     * @param value The default value.
     * @param subPath If the path should split into sub-paths using the '.'
     *        seperator.
     */
    public <T> void setDefault(String fileName, String path, Class<T> type, T value, boolean subPath) {
        try {
            Optional<ConfigurationNode> oRoot = getFile(fileName);
            if (oRoot.isPresent()) {
                ConfigurationNode root = oRoot.get();
                if (subPath) {
                    if (root.getNode((Object[]) path.split("\\.")).getValue(TypeToken.of(type)) == null) {
                        root.getNode((Object[]) path.split("\\.")).setValue(TypeToken.of(type), value);
                        saveFile(fileName, root);
                    }
                } else {
                    if (root.getNode(path).getValue(TypeToken.of(type)) == null) {
                        root.getNode(path).setValue(TypeToken.of(type), value);
                        saveFile(fileName, root);
                    }
                }
            }
        } catch (Exception e) {
            logger.error(e.getMessage());
        }
    }

    /**
     * Sets a value in a file if one doesn't exit. Saves the the changed file to
     * the disc. Defaults the subPath value to true.
     * 
     * @param fileName The name of the file. Must include the extension.
     * @param path The path of the default value. Supports sub-paths using a '.'
     *        seperator, only if subPath is set to true.
     * @param type The type of the default value.
     * @param value The default value.
     */
    public <T> void setDefault(String fileName, String path, Class<T> type, T value) {
        setDefault(fileName, path, type, value, true);
    }

    /**
     * Deletes a value from a file,
     * 
     * @param fileName The file to delete from.
     * @param path The path to delete.
     * @return If the deletion was successful or not.
     */
    public boolean deleteValue(String fileName, String path) {
        try {
            Optional<ConfigurationNode> oRoot = getFile(fileName);
            if (oRoot.isPresent()) {
                ConfigurationNode root = oRoot.get();
                root.getNode(path).setValue(null);
                saveFile(fileName, root);
                return true;
            } else {
                return false;
            }
        } catch (Exception e) {
            logger.error(e.getMessage());
            return false;
        }
    }

    /**
     * Sets a value in a file. Saves the the changed file to the disc.
     * 
     * @param fileName The name of the file. Must include the extension.
     * @param path The path of the value. Supports sub-paths using a '.'
     *        seperator.
     * @param type The type of the value.
     * @param value The value.
     * @param subPath If the path should split into sub-paths using the '.'
     *        seperator.
     * @return If the value was successfully set or not.
     */
    public <T> boolean setValue(String fileName, String path, Class<T> type, T value, boolean subPath) {
        try {
            Optional<ConfigurationNode> oRoot = getFile(fileName);
            if (oRoot.isPresent()) {
                ConfigurationNode root = oRoot.get();
                if (subPath) {
                    root.getNode((Object[]) path.split("\\.")).setValue(TypeToken.of(type), value);
                } else {
                    root.getNode(path).setValue(TypeToken.of(type), value);
                }
                saveFile(fileName, root);
                return true;
            } else {
                return false;
            }
        } catch (Exception e) {
            logger.error(e.getMessage());
            return false;
        }
    }

    /**
     * Sets a value in a file. Saves the the changed file to the disc. Defaults
     * the subPath value to true.
     * 
     * @param fileName The name of the file. Must include the extension.
     * @param path The path of the value. Supports sub-paths using a '.'
     *        seperator.
     * @param type The type of the value.
     * @param value The value.
     * @return If the value was successfully set or not.
     */
    public <T> boolean setValue(String fileName, String path, Class<T> type, T value) {
        return setValue(fileName, path, type, value, true);
    }

    /**
     * Gets a value from the specified file.
     * 
     * @param fileName The name of the file. Must include the extension.
     * @param path The path of the value. Supports sub-paths using a '.'
     *        seperator.
     * @param type The type of the value.
     * @param subPath If the path should split into sub-paths using the '.'
     *        seperator.
     * @return The value, if it was found.
     */
    public <T> Optional<T> getValue(String fileName, String path, Class<T> type, boolean subPath) {
        try {
            Optional<ConfigurationNode> oRoot = getFile(fileName);
            if (oRoot.isPresent()) {
                ConfigurationNode root = oRoot.get();
                if (subPath) {
                    if (root.getNode((Object[]) path.split("\\.")).getValue(TypeToken.of(type)) != null) {
                        return Optional.of(root.getNode((Object[]) path.split("\\.")).getValue(TypeToken.of(type)));
                    } else {
                        return Optional.empty();
                    }
                } else {
                    if (root.getNode(path).getValue(TypeToken.of(type)) != null) {
                        return Optional.of(root.getNode(path).getValue(TypeToken.of(type)));
                    } else {
                        return Optional.empty();
                    }
                }
            } else {
                return Optional.empty();
            }
        } catch (Exception e) {
            logger.error(e.getMessage());
            return Optional.empty();
        }
    }

    /**
     * Gets a value from the specified file. Defaults the subPath value to true.
     * 
     * @param fileName The name of the file. Must include the extension.
     * @param path The path of the value. Supports sub-paths using a '.'
     *        seperator.
     * @param type The type of the value.
     * @return The value, if it was found.
     */
    public <T> Optional<T> getValue(String fileName, String path, Class<T> type) {
        return getValue(fileName, path, type, true);
    }

    /**
     * Checks if a configuration node exists.
     * 
     * @param fileName The name of the file. Must include the extension.
     * @param path The path of the value. Supports sub-paths using a '.'
     *        seperator.
     * @param subPath If the path should split into sub-paths using the '.'
     *        seperator.
     * @return If the node exists or not.
     */
    public boolean nodeExists(String fileName, String path, boolean subPath) {
        try {
            Optional<ConfigurationNode> oRoot = getFile(fileName);
            if (oRoot.isPresent()) {
                ConfigurationNode root = oRoot.get();
                if (subPath) {
                    return root.getNode((Object[]) path.split("\\.")) != null;
                } else {
                    return root.getNode(path) != null;
                }
            } else {
                return false;
            }
        } catch (Exception e) {
            logger.error(e.getMessage());
            return false;
        }
    }

    /**
     * Checks if a configuration node exists. Defaults the subPath value to
     * true.
     * 
     * @param fileName The name of the file. Must include the extension.
     * @param path The path of the value. Supports sub-paths using a '.'
     *        seperator.
     * @return If the node exists or not.
     */
    public boolean nodeExists(String fileName, String path) {
        return nodeExists(fileName, path, true);
    }

    /**
     * Gets a file using the specified name. Generates a new file if one is not
     * already present.
     * 
     * @param fileName The name of the file. Must include the extension.
     * @return The file, if no error has occurred.
     */
    public Optional<ConfigurationNode> getFile(String fileName) {
        File folder = new File("config/" + folderName);
        File file = new File("config/" + folderName + "/" + fileName);
        try {
            folder.mkdirs();
            file.createNewFile();
            ConfigurationLoader<?> manager = HoconConfigurationLoader.builder().setFile(file).build();
            ConfigurationNode root = manager.load();
            return Optional.of(root);
        } catch (Exception e) {
            logger.error(e.getMessage());
            return Optional.empty();
        }
    }

    /**
     * Saves a file to the disc.
     * 
     * @param fileName The name of the file. Must include the extension.
     * @param root The contents of the file.
     */
    private void saveFile(String fileName, ConfigurationNode root) {
        File folder = new File("config/" + folderName);
        File file = new File("config/" + folderName + "/" + fileName);
        try {
            folder.mkdirs();
            file.createNewFile();
            ConfigurationLoader<?> manager = HoconConfigurationLoader.builder().setFile(file).build();
            manager.save(root);
        } catch (Exception e) {
            logger.error(e.getMessage());
        }
    }

}
