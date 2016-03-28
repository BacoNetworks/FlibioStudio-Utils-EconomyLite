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
package io.github.flibio.utils.message;

import org.spongepowered.api.text.Text;
import org.spongepowered.api.text.format.TextColors;
import org.spongepowered.api.text.serializer.TextSerializers;

import io.github.flibio.utils.file.FileManager;

import java.util.HashMap;
import java.util.Locale;
import java.util.Map;
import java.util.Optional;
import java.util.ResourceBundle;

public class MessageStorage {

    private FileManager fileManager;

    protected MessageStorage(Object plugin) {
        this.fileManager = FileManager.createInstance(plugin).get();
    }

    /**
     * Creates a new MessageStorage instance.
     * 
     * @param plugin An instance of the plugin.
     * @return A new MessageStorage instance.
     */
    public static MessageStorage createInstance(Object plugin) {
        return new MessageStorage(plugin);
    }

    /**
     * Checks if all of the message keys have a value present. If they do not,
     * the value provided in the Map is set as the key's value.
     * 
     * @param defaultMessages The default messages map.
     */
    public void defaultMessages(Map<String, String> defaultMessages) {
        defaultMessages.entrySet().forEach(entry -> {
            fileManager.setDefault("messages.conf", entry.getKey(), String.class, entry.getValue(), false);
        });
    }

    /**
     * Checks if all the message keys have a value present. The keys are loaded
     * from the file provided. If the keys do not have a value, the value is set
     * to the value found in the file.
     * 
     * @param containingPackage The package the resources are found in
     */
    public void defaultMessages(String containingPackage) {
        ResourceBundle rb = ResourceBundle.getBundle(containingPackage, Locale.getDefault());
        rb.keySet().forEach(key -> {
            String value = rb.getString(key);
            fileManager.setDefault("messages.conf", key, String.class, value, false);
        });
    }

    /**
     * Locates and deserializes a message. The message supports formatting using
     * the ampersand symbol. Returns a red error message if a message could not
     * be found.
     * 
     * @param key The key the message was stored with.
     * @param variables The variables to replace in the message. Every instance
     *        of the key will be replaced by its corresponding value.
     * @return The deserialized message.
     */
    public Text getMessage(String key, Map<String, Text> variables) {
        Optional<String> sOpt = fileManager.getValue("messages.conf", key, String.class, false);
        if (sOpt.isPresent()) {
            String value = sOpt.get();
            for (Map.Entry<String, Text> entry : variables.entrySet()) {
                value = value.replaceAll("{" + entry.getKey() + "}", TextSerializers.FORMATTING_CODE.serialize(entry.getValue()));
            }
            return TextSerializers.FORMATTING_CODE.deserialize(value);
        } else {
            return Text.of(TextColors.RED, "error");
        }
    }

    /**
     * Locates and deserializes a message. The message supports formatting using
     * the ampersand symbol. Returns a red error message if a message could not
     * be found.
     * 
     * @param key The key the message was stored with.
     * @param search What the replacement will replace.
     * @param replacement What to replace the search with.
     * @return The deserialized message.
     */
    public Text getMessage(String key, String search, Text replacement) {
        HashMap<String, Text> vars = new HashMap<>();
        vars.put("{" + search + "}", replacement);
        return getMessage(key, vars);
    }

    /**
     * Locates and deserializes a message. The message supports formatting using
     * the ampersand symbol. Returns a red error message if a message could not
     * be found.
     * 
     * @param key The key the message was stored with.
     * @param search What the replacement will replace.
     * @param replacement What to replace the search with.
     * @return The deserialized message.
     */
    public Text getMessage(String key, String search, String replacement) {
        HashMap<String, Text> vars = new HashMap<>();
        vars.put("{" + search + "}", Text.of(replacement));
        return getMessage(key, vars);
    }

    /**
     * Locates and deserializes a message. The message supports formatting using
     * the ampersand symbol. Returns a red error message if a message could not
     * be found.
     * 
     * @param key The key the message was stored with.
     * @param search What the replacement will replace.
     * @param replacement What to replace the search with.
     * @param search2 What the second replacement will replace.
     * @param replacement2 What to replace the second search with.
     * @return The deserialized message.
     */
    public Text getMessage(String key, String search, Text replacement, String search2, Text replacement2) {
        HashMap<String, Text> vars = new HashMap<>();
        vars.put("{" + search + "}", replacement);
        vars.put("{" + search2 + "}", replacement2);
        return getMessage(key, vars);
    }

    /**
     * Locates and deserializes a message. The message supports formatting using
     * the ampersand symbol. Returns a red error message if a message could not
     * be found.
     * 
     * @param key The key the message was stored with.
     * @param search What the replacement will replace.
     * @param replacement What to replace the search with.
     * @param search2 What the second replacement will replace.
     * @param replacement2 What to replace the second search with.
     * @return The deserialized message.
     */
    public Text getMessage(String key, String search, String replacement, String search2, String replacement2) {
        HashMap<String, Text> vars = new HashMap<>();
        vars.put("{" + search + "}", Text.of(replacement));
        vars.put("{" + search2 + "}", Text.of(replacement2));
        return getMessage(key, vars);
    }

    /**
     * Locates and deserializes a message. The message supports formatting using
     * the ampersand symbol.
     * 
     * @param key The key the message was stored with.
     * @return The deserialized message.
     */
    public Text getMessage(String key) {
        return getMessage(key, new HashMap<>());
    }
}
