package top.rookiestwo.wheatsync.client;

import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import top.rookiestwo.wheatsync.WheatServerSync;

@Environment(EnvType.CLIENT)
public class WheatServerSyncClient implements ClientModInitializer {

    public static final String MOD_ID = WheatServerSync.MOD_ID;

    @Override
    public void onInitializeClient() {
        WheatServerSyncClientRegistry.registerScreen();
    }
}
