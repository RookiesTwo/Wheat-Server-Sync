package top.rookiestwo.wheatsync.client;

import net.fabricmc.api.ClientModInitializer;
import top.rookiestwo.wheatsync.WheatServerSync;

public class WheatServerSyncClient implements ClientModInitializer {

    public static final String MOD_ID = WheatServerSync.MOD_ID;

    @Override
    public void onInitializeClient() {
        WheatServerSyncClientRegistry.registerScreen();
    }
}
