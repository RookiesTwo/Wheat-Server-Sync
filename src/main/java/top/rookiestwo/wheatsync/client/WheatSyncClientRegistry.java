package top.rookiestwo.wheatsync.client;

import net.minecraft.client.gui.screen.ingame.HandledScreens;
import top.rookiestwo.wheatsync.WheatSync;
import top.rookiestwo.wheatsync.WheatSyncRegistry;
import top.rookiestwo.wheatsync.client.gui.screen.SLIScreen;

public class WheatSyncClientRegistry {

    private static final String MOD_ID = WheatSync.MOD_ID;

    public static void registerScreen() {
        HandledScreens.register(WheatSyncRegistry.SLI_SCREEN_HANDLER, SLIScreen::new);
    }
}
