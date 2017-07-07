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

import org.slf4j.Logger;
import org.spongepowered.api.text.Text;
import org.spongepowered.api.text.format.TextColors;
import org.spongepowered.api.text.serializer.TextSerializers;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Locale;
import java.util.Properties;
import java.util.ResourceBundle;

public class MessageStorage {

    private File file;
    private Properties props;

    private MessageStorage(Path folder, String bundle, Logger logger) {
        // Create directory if it doesn't exist
        folder.toFile().mkdirs();
        // Setup file
        file = Paths.get(folder.toString(), bundle + ".properties").toFile();
        if (!file.exists()) {
            try {
                file.createNewFile();
            } catch (IOException e) {
                logger.error("Failed to create message file: " + e.getMessage());
            }
        }
        // Load the file
        props = new Properties();
        try {
            FileInputStream stream = new FileInputStream(file);
            props.load(stream);
            stream.close();
        } catch (Exception e) {
            logger.error("Error loading message file: " + e.getMessage());
        }
        // Load the message bundle and check default values
        ResourceBundle rb = ResourceBundle.getBundle(bundle, Locale.getDefault());
        rb.keySet().forEach(key -> {
            props.putIfAbsent(key, rb.getString(key));
        });
        // Save the messages file
        try {
            FileOutputStream stream = new FileOutputStream(file);
            props.store(stream, "HELLO");
            stream.close();
        } catch (Exception e) {
            logger.error("Error saving message file: " + e.getMessage());
        }
    }

    public MessageStorage create(Path folder, String bundle, Logger logger) {
        return new MessageStorage(folder, bundle, logger);
    }

    public Text getMessage(String key) {
        if (props.containsKey(key)) {
            return TextSerializers.FORMATTING_CODE.deserialize(props.getProperty(key));
        } else {
            return Text.of(TextColors.RED, "!-----!");
        }
    }

}
