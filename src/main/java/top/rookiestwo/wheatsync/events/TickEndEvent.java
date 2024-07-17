package top.rookiestwo.wheatsync.events;

import net.fabricmc.fabric.api.event.lifecycle.v1.ServerLifecycleEvents;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerTickEvents;
import top.rookiestwo.wheatsync.WheatSync;

import static top.rookiestwo.wheatsync.WheatSync.dataBaseIOTask;

public class TickEndEvent {
    public static void register() {
        ServerTickEvents.END_SERVER_TICK.register((server) -> {
            WheatSync.sliCache.addUpdateRequests();
            WheatSync.sliCache.addAllSLIToGetQueue();
            dataBaseIOTask.continueLoop();
        });

        ServerLifecycleEvents.SERVER_STARTED.register((server) -> {
            WheatSync.dataBaseIOTaskThread = new Thread(dataBaseIOTask);
            WheatSync.dataBaseIOTaskThread.start();
        });
    }
}
