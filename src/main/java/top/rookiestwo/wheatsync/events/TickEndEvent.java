package top.rookiestwo.wheatsync.events;

import net.fabricmc.fabric.api.event.lifecycle.v1.ServerLifecycleEvents;
import top.rookiestwo.wheatsync.WheatSync;
import top.rookiestwo.wheatsync.database.SLICache;

import java.sql.SQLException;

import static top.rookiestwo.wheatsync.WheatSync.dataBaseIOTask;

public class TickEndEvent {

    public static void runAfterLogic() throws InterruptedException, SQLException {
        WheatSync.sliCache.addUpdateRequests();
        WheatSync.sliCache.addAllCachedEntityToGetQueue();
        dataBaseIOTask.continueLoop();
        WheatSync.databaseHelper.processCreateSLIRequests(SLICache.createSLIRequestQueue);
        WheatSync.databaseHelper.processDeleteSLIRequests(SLICache.deleteSLIRequestQueue);
        WheatSync.databaseHelper.processChangeCommunicationIDRequests(SLICache.changeCommunicationIDRequestQueue);
        WheatSync.databaseHelper.processUpdateInventoryRequests(SLICache.updateInventoryRequestQueue);
        WheatSync.databaseHelper.processGetSLIRequests(SLICache.getSLIRequestQueue);
        WheatSync.sliCache.freshInventoryCacheToEntities();
    }

    public static void register() {
        ServerLifecycleEvents.SERVER_STARTED.register((server) -> {
            //WheatSync.dataBaseIOTaskThread = new Thread(dataBaseIOTask);
            //WheatSync.dataBaseIOTaskThread.start();
        });
    }
}
