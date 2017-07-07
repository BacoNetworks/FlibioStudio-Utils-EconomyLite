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

package io.github.flibio.utils.config;

import com.google.common.reflect.TypeToken;
import ninja.leaping.configurate.commented.CommentedConfigurationNode;
import ninja.leaping.configurate.hocon.HoconConfigurationLoader;
import org.slf4j.Logger;

import java.io.File;
import java.nio.file.Path;
import java.nio.file.Paths;

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

    public static ConfigManager create(Path folder, String file, Logger logger) {
        return new ConfigManager(folder, file, logger);
    }

    public <T> void setDefault(String comment, Class<T> type, T value, String... nodes) {
        try {
            CommentedConfigurationNode node = this.node.getNode((Object[]) nodes);
            // Make sure the node does not already have a value
            if (node.isVirtual()) {
                node.setValue(TypeToken.of(type), value);
                node.setComment(comment);
            }
        } catch (Exception e) {
            logger.error("Failed to set default: " + e.getMessage());
        }
    }

    public <T> T getValue(Class<T> type, String... nodes) {
        CommentedConfigurationNode node = this.node.getNode((Object[]) nodes);
        if (node.isVirtual()) {
            return null;
        }
        try {
            return node.getValue(TypeToken.of(type));
        } catch (Exception e) {
            logger.error("Failed to map object: " + e.getMessage());
            return null;
        }
    }

    public void save() {
        try {
            loader.save(node);
        } catch (Exception e) {
            logger.error("Failed to save config file: " + e.getMessage());
        }
    }

}
