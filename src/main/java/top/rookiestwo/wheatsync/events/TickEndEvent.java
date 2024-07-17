package top.rookiestwo.wheatsync.events;

import net.fabricmc.fabric.api.event.lifecycle.v1.ServerLifecycleEvents;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerTickEvents;
import top.rookiestwo.wheatsync.WheatSync;

public class TickEndEvent {
    public static void register() {
        ServerTickEvents.END_SERVER_TICK.register((server) -> {
            WheatSync.sliCache.addAllSLIToGetQueue();
            WheatSync.dataBaseIOTask.continueLoop();
        });

        ServerLifecycleEvents.SERVER_STARTED.register((server) -> {
            WheatSync.dataBaseIOTaskThread.start();
        });
    }
}
