package top.rookiestwo.wheatsync;

import net.fabricmc.api.ModInitializer;
import top.rookiestwo.wheatsync.config.ConfigManager;
import top.rookiestwo.wheatsync.config.WheatSyncConfig;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;


public class WheatServerSync implements ModInitializer {

    public static final String MOD_ID = "wheatsync";
    public static final Logger LOGGER= LogManager.getLogger("WheatServerSync");
    public static ConfigManager CONFIG_MANAGER = null;
    public static WheatSyncConfig CONFIG=null;


    @Override
    public void onInitialize() {
        //配置文件
        CONFIG_MANAGER = new ConfigManager();
        CONFIG=CONFIG_MANAGER.getConfig();

        //注册
        WheatServerSyncRegistry.registerBlocks();
        WheatServerSyncRegistry.registerBlockEntities();

        //LOGGER.info("WheatServerSync initialized.");
    }
}