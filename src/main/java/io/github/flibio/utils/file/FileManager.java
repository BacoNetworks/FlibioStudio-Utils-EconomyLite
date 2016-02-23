package io.github.flibio.utils.file;

import ninja.leaping.configurate.ConfigurationNode;
import ninja.leaping.configurate.hocon.HoconConfigurationLoader;
import ninja.leaping.configurate.loader.ConfigurationLoader;

import org.slf4j.Logger;

import com.google.common.reflect.TypeToken;

import java.io.File;
import java.util.HashMap;
import java.util.Optional;

public class FileManager {

    private Logger logger;
    private String folderName;

    private HashMap<String, ConfigurationNode> files = new HashMap<String, ConfigurationNode>();

    protected FileManager(Logger logger, String folderName) {
        this.logger = logger;
        this.folderName = folderName;
    }

    public static FileManager createInstance(Logger logger, String folderName) {
        return new FileManager(logger, folderName);
    }

    public <T> void setDefault(String fileName, String path, Class<T> type, T value) {
        try {
            Optional<ConfigurationNode> oRoot = getFile(fileName);
            if (oRoot.isPresent()) {
                ConfigurationNode root = oRoot.get();
                // Check if the configuration file doesn't contain the path
                if (root.getNode((Object[]) path.split("\\.")).getValue(TypeToken.of(type)) == null) {
                    // Set the path to the default value
                    root.getNode((Object[]) path.split("\\.")).setValue(TypeToken.of(type), value);
                    saveFile(fileName, root);
                }
            }
        } catch (Exception e) {
            logger.error(e.getMessage());
        }
    }

    public <T> Optional<T> getValue(String fileName, String path, Class<T> type) {
        try {
            Optional<ConfigurationNode> oRoot = getFile(fileName);
            if (oRoot.isPresent()) {
                ConfigurationNode root = oRoot.get();
                // Check if the configuration file contains the path
                if (root.getNode((Object[]) path.split("\\.")).getValue(TypeToken.of(type)) != null) {
                    // Get the value and return it
                    return Optional.of(root.getNode((Object[]) path.split("\\.")).getValue(TypeToken.of(type)));
                } else {
                    return Optional.empty();
                }
            } else {
                return Optional.empty();
            }
        } catch (Exception e) {
            logger.error(e.getMessage());
            return Optional.empty();
        }
    }

    public Optional<ConfigurationNode> getFile(String fileName) {
        File folder = new File("config/" + folderName);
        File file = new File("config/" + folderName + fileName);
        try {
            folder.mkdirs();
            file.createNewFile();
            ConfigurationLoader<?> manager = HoconConfigurationLoader.builder().setFile(new File("config/" + folderName + fileName)).build();
            ConfigurationNode root = manager.load();
            files.put(fileName, root);
            return Optional.of(root);
        } catch (Exception e) {
            logger.error(e.getMessage());
            return Optional.empty();
        }
    }

    public void saveFile(String fileName, ConfigurationNode root) {
        files.put(fileName, root);
        File folder = new File("config/" + folderName);
        File file = new File("config/" + folderName + fileName);
        try {
            folder.mkdirs();
            file.createNewFile();
            ConfigurationLoader<?> manager = HoconConfigurationLoader.builder().setFile(new File("config/" + folderName + fileName)).build();
            manager.save(root);
        } catch (Exception e) {
            logger.error(e.getMessage());
        }
    }

    public void saveAllFiles() {
        for (String fileName : files.keySet()) {
            saveFile(fileName, files.get(fileName));
        }
    }

}
