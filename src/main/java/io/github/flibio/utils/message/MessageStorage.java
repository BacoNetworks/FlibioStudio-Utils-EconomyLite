package io.github.flibio.utils.message;

import org.spongepowered.api.text.Text;
import org.spongepowered.api.text.format.TextColors;
import org.spongepowered.api.text.serializer.TextSerializers;

import io.github.flibio.utils.file.FileManager;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

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
     * Checks if all of the keys have a value present. If they do not, the value
     * provided in the Map is set as the key's value.
     * 
     * @param defaultMessages The default messages map.
     */
    public void defaultMessages(Map<String, String> defaultMessages) {
        defaultMessages.entrySet().forEach(entry -> {
            fileManager.setDefault("messages.conf", entry.getKey(), String.class, entry.getValue());
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
    public Text getMessage(String key, Map<String, String> variables) {
        Optional<String> sOpt = fileManager.getValue("messages.conf", key, String.class);
        if (sOpt.isPresent()) {
            String value = sOpt.get();
            for (Map.Entry<String, String> entry : variables.entrySet()) {
                value = value.replaceAll(entry.getKey(), entry.getValue());
            }
            return TextSerializers.FORMATTING_CODE.deserialize(value);
        } else {
            return Text.of(TextColors.RED, "error");
        }
    }

    /**
     * Locates and deserializes a message. The message supports formatting using
     * the '&' symbol.
     * 
     * @param key The key the message was stored with.
     * @return The deserialized message.
     */
    public Text getMessage(String key) {
        return getMessage(key, new HashMap<>());
    }
}
