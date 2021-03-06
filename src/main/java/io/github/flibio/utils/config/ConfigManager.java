/*
 * This file is part of Utils, licensed under the MIT License (MIT). See the LICENSE file at the root of this project for more information.
 */

package io.github.flibio.utils.config;

import com.google.common.reflect.TypeToken;
import ninja.leaping.configurate.commented.CommentedConfigurationNode;
import ninja.leaping.configurate.hocon.HoconConfigurationLoader;
import org.slf4j.Logger;

import java.io.File;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Optional;

public class ConfigManager {

    private Logger logger;
    private File configFile;
    private HoconConfigurationLoader loader;
    private CommentedConfigurationNode node;

    private ConfigManager(Path folder, String file, Logger logger) {
        this.logger = logger;
        // Make the folder directory
        folder.toFile().mkdirs();
        configFile = Paths.get(folder.toString(), file).toFile();
        // Make sure the file exists
        if (!configFile.exists()) {
            try {
                configFile.createNewFile();
            } catch (Exception e) {
                logger.error("Failed to create config file: " + e.getMessage());
            }
        }
        // Load the file
        loader = HoconConfigurationLoader.builder().setFile(configFile).build();
        try {
            node = loader.load();
        } catch (Exception e) {
            logger.error("Failed to load config file: " + e.getMessage());
        }
    }

    /**
     * Creates a new ConfigManager.
     *
     * @param folder The folder where the configuration file resides.
     * @param file The name of the configuration file, including the extension.
     * @param logger An instance of the logger.
     * @return An instance of the ConfigManager.
     */
    public static ConfigManager create(Path folder, String file, Logger logger) {
        return new ConfigManager(folder, file, logger);
    }

    /**
     * Sets the comment of a configuration option.
     *
     * @param comment The comment of the configuration option.
     * @param nodes Path to the configuration option.
     */
    public void setComment(String comment, String... nodes) {
        try {
            CommentedConfigurationNode node = this.node.getNode((Object[]) nodes);
            // Set the comment
            node.setComment(comment);
        } catch (Exception e) {
            logger.error("Failed to set comment: " + e.getMessage());
        }
    }

    /**
     * Sets the value of a configuration option if it does not already exist.
     * Comments will always be saved.
     *
     * @param <T> The type of the configuration option.
     * @param comment The comment to place above the configuration option.
     * @param type The class type of the configuration option.
     * @param value The default value of the configuration option.
     * @param nodes Path to the configuration option.
     */
    public <T> void setDefault(String comment, Class<T> type, T value, String... nodes) {
        try {
            CommentedConfigurationNode node = this.node.getNode((Object[]) nodes);
            // Make sure the node does not already have a value
            if (node.isVirtual()) {
                node.setValue(TypeToken.of(type), value);
            }
            // Set the comment
            if (!comment.isEmpty()) {
                node.setComment(comment);
            }
        } catch (Exception e) {
            logger.error("Failed to set default: " + e.getMessage());
        }
    }

    /**
     * Sets the value of a configuration option if it does not already exist.
     * Comments will always be saved.
     *
     * @param <T> The type of the configuration option.
     * @param type The class type of the configuration option.
     * @param value The default value of the configuration option.
     * @param nodes Path to the configuration option.
     */
    public <T> void setDefault(Class<T> type, T value, String... nodes) {
        setDefault("", type, value, nodes);
    }

    /**
     * Gets the value of a configuration option. Will return null if the option
     * does not exist.
     *
     * @param <T> The type of the configuration option.
     * @param type The class type of the configuration option.
     * @param nodes Path to the configuration option.
     * @return The value of the configuration option.
     */
    public <T> Optional<T> getValue(Class<T> type, String... nodes) {
        CommentedConfigurationNode node = this.node.getNode((Object[]) nodes);
        if (node.isVirtual()) {
            return Optional.empty();
        }
        try {
            return Optional.of(node.getValue(TypeToken.of(type)));
        } catch (Exception e) {
            logger.error("Failed to map object: " + e.getMessage());
            return Optional.empty();
        }
    }

    /**
     * Gets the value of a configuration option. Will return default if option
     * does not exist.
     *
     * @param <T> The type of the configuration option.
     * @param type The class type of the configuration option.
     * @param defaultValue The default value used if option does not exist.
     * @param nodes Path to the configuration option.
     * @return The value of the configuration option.
     */
    public <T> T getValue(Class<T> type, T defaultValue, String... nodes) {
        CommentedConfigurationNode node = this.node.getNode((Object[]) nodes);
        if (node.isVirtual()) {
            return defaultValue;
        }
        try {
            return node.getValue(TypeToken.of(type));
        } catch (Exception e) {
            logger.error("Failed to map object: " + e.getMessage());
            return defaultValue;
        }
    }

    /**
     * Gets the value of a configuration option. Will return default if option
     * does not exist.
     *
     * @param <T> The type of the configuration option.
     * @param type The class type of the configuration option.
     * @param defaultValue The default value used if option does not exist.
     * @param nodes Path to the configuration option.
     * @return The value of the configuration option.
     */
    public <T> T getValue(T defaultValue, Class<T> type, String... nodes) {
        return getValue(type, defaultValue, nodes);
    }

    /**
     * Forcefully sets a configuration nodes value.
     *
     * @param <T> The type of the configuration option.
     * @param value The value to force upon the node.
     * @param nodes Path to the configuration option.
     */
    public <T> void forceValue(T value, String... nodes) {
        try {
            CommentedConfigurationNode node = this.node.getNode((Object[]) nodes);
            node.setValue(value);
        } catch (Exception e) {
            logger.error("Failed to force value: " + e.getMessage());
        }
    }

    /**
     * Gets the configuration file directly for use.
     *
     * @return The configuration file.
     */
    public CommentedConfigurationNode getNode() {
        return node;
    }

    /**
     * Forces the update of the configuration node. Also saves the node.
     *
     * @param node The node to overwrite with.
     */
    public void overwriteNode(CommentedConfigurationNode node) {
        this.node = node;
        save();
    }

    /**
     * Saves the configuration file.
     */
    public void save() {
        try {
            loader.save(node);
        } catch (Exception e) {
            logger.error("Failed to save config file: " + e.getMessage());
        }
    }

}
