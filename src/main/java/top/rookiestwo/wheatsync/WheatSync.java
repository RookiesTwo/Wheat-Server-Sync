package top.rookiestwo.wheatsync;

import net.fabricmc.api.ModInitializer;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import top.rookiestwo.wheatsync.config.ConfigManager;
import top.rookiestwo.wheatsync.config.WheatSyncConfig;
import top.rookiestwo.wheatsync.database.DatabaseHelper;
import top.rookiestwo.wheatsync.database.SLICache;
import top.rookiestwo.wheatsync.events.AsyncAndEvents;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;


public class WheatSync implements ModInitializer {

    public static final String MOD_ID = "wheatsync";
    public static final Logger LOGGER= LogManager.getLogger("WheatServerSync");
    public static ConfigManager CONFIG_MANAGER = null;
    public static WheatSyncConfig CONFIG=null;

    public static DatabaseHelper databaseHelper = null;
    public static SLICache sliCache = null;
    public static ExecutorService asyncExecutor = Executors.newFixedThreadPool(4);

    static {
        //注册
        WheatSyncRegistry.registerAll();
        WheatSyncRegistry.registerServerPacketReceiver();
        AsyncAndEvents.register();
    }

    @Override
    public void onInitialize() {
        LOGGER.info("WheatSync Initializing..");
        //配置文件
        CONFIG_MANAGER = new ConfigManager();
        CONFIG=CONFIG_MANAGER.getConfig();

        //database
        databaseHelper = new DatabaseHelper();

        LOGGER.info("WheatSync Initialized.");
    }
}