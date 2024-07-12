package top.rookiestwo.wheatsync.config;


import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import org.apache.logging.log4j.Logger;
import top.rookiestwo.wheatsync.WheatServerSync;

import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

public class ConfigManager {
    private static final Path CONFIG_PATH = Path.of("config/wheatsync.json");
    private static final Gson GSON = new GsonBuilder().setPrettyPrinting().create();
    private static final Logger LOGGER = WheatServerSync.LOGGER;

    private WheatSyncConfig config;

    public ConfigManager(){
        loadConfig();
    }

    private void loadConfig(){
        try{
            if(!Files.exists(CONFIG_PATH.getParent())){
                Files.createDirectories(CONFIG_PATH.getParent());
            }
            if(!Files.exists(CONFIG_PATH)){
                Files.createFile(CONFIG_PATH);
                config=new WheatSyncConfig();
                saveConfig();
            } else {
                config=GSON.fromJson(new FileReader(CONFIG_PATH.toFile()), WheatSyncConfig.class);
            }
        } catch (IOException e) {
            LOGGER.error("Failed to load config file.", e);
        }
    }

    public void saveConfig() {
        try (FileWriter writer = new FileWriter(CONFIG_PATH.toFile())) {
            GSON.toJson(config, writer);
        } catch (IOException e) {
            LOGGER.error("Failed to save config file.", e);
        }
    }

    public WheatSyncConfig getConfig() {
        return config;
    }
}
