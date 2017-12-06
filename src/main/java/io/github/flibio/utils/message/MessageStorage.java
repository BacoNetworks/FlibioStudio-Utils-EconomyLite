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

package io.github.flibio.utils.message;

import ninja.leaping.configurate.ConfigurationNode;
import ninja.leaping.configurate.commented.CommentedConfigurationNode;
import ninja.leaping.configurate.hocon.HoconConfigurationLoader;
import org.slf4j.Logger;
import org.spongepowered.api.text.Text;
import org.spongepowered.api.text.serializer.TextSerializers;

import java.io.File;
import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;
import java.util.ResourceBundle;

public class MessageStorage {

    private File file;
    private CommentedConfigurationNode node;
    private Map<String, String> defaults = new HashMap<>();

    private MessageStorage(Path folder, String bundle, Logger logger) {
        // Create directory if it doesn't exist
        folder.toFile().mkdirs();
        // Setup file
        file = Paths.get(folder.toString(), bundle + ".conf").toFile();
        if (!file.exists()) {
            try {
                file.createNewFile();
            } catch (IOException e) {
                logger.error("Failed to create message file: " + e.getMessage());
            }
        }
        // Load the file
        HoconConfigurationLoader loader = HoconConfigurationLoader.builder().setFile(file).build();
        try {
            node = loader.load();
        } catch (Exception e) {
            logger.error("Failed to load message file: " + e.getMessage());
        }
        // Load the message bundle and check default values
        ResourceBundle rb = ResourceBundle.getBundle(bundle, Locale.getDefault());
        rb.keySet().forEach(key -> {
            String val = rb.getString(key);
            ConfigurationNode childNode = node.getNode(key);
            if (childNode.isVirtual()) {
                childNode.setValue(val);
            }
            defaults.put(key, val);
        });
        // Save the messages file
        try {
            loader.save(node);
        } catch (Exception e) {
            logger.error("Failed to save message file: " + e.getMessage());
        }
    }

    public static MessageStorage create(Path folder, String bundle, Logger logger) {
        return new MessageStorage(folder, bundle, logger);
    }

    public String getRawMessage(String key) {
        ConfigurationNode childNode = node.getNode(key);
        if (!childNode.isVirtual()) {
            return childNode.getString();
        } else {
            return "!-----!";
        }
    }

    public Text getMessage(String key) {
        return getMessage(key, new String[0]);
    }

    public Text getMessage(String key, String... variables) {
        // Verify variable count is even
        if ((variables.length & 1) == 0)
            return Text.of("!-----!");
        String message = getRawMessage(key);
        // Loop variables
        for (int i = 0; i < variables.length / 2; i = i + 2) {
            message = message.replaceAll("\\{" + variables[i] + "\\}", variables[i + 1]);
        }
        return TextSerializers.FORMATTING_CODE.deserialize(message);
    }
}
