package top.rookiestwo.wheatsync.client;

import net.minecraft.client.gui.screen.ingame.HandledScreens;
import top.rookiestwo.wheatsync.WheatServerSync;
import top.rookiestwo.wheatsync.WheatServerSyncRegistry;
import top.rookiestwo.wheatsync.client.gui.screen.SLIScreen;

public class WheatServerSyncClientRegistry {

    private static final String MOD_ID = WheatServerSync.MOD_ID;

    public static void registerScreen() {
        HandledScreens.register(WheatServerSyncRegistry.SLI_SCREEN_HANDLER, SLIScreen::new);
    }
}
