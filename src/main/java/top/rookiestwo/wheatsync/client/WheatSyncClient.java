package top.rookiestwo.wheatsync.client;

import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import top.rookiestwo.wheatsync.WheatSync;

@Environment(EnvType.CLIENT)
public class WheatSyncClient implements ClientModInitializer {

    public static final String MOD_ID = WheatSync.MOD_ID;

    @Override
    public void onInitializeClient() {
        WheatSyncClientRegistry.registerScreen();
    }
}
