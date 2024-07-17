package top.rookiestwo.wheatsync.database;

import top.rookiestwo.wheatsync.WheatSync;

import java.sql.SQLException;

public class DataBaseIOTask implements Runnable {
    private final Object lock = new Object();

    @Override
    public void run() {
        while (true) {
            synchronized (lock) {
                try {
                    lock.wait();

                    WheatSync.databaseHelper.processCreateSLIRequests(DataBaseIOManager.createSLIRequestQueue);
                    WheatSync.databaseHelper.processDeleteSLIRequests(DataBaseIOManager.deleteSLIRequestQueue);
                    WheatSync.databaseHelper.processChangeCommunicationIDRequests(DataBaseIOManager.changeCommunicationIDRequestQueue);
                    WheatSync.databaseHelper.processUpdateInventoryRequests(DataBaseIOManager.updateInventoryRequestQueue);
                    WheatSync.databaseHelper.processGetSLIRequests(DataBaseIOManager.getSLIRequestQueue);

                } catch (InterruptedException e) {
                    WheatSync.LOGGER.error("Work thread Interrupted.");
                    break;
                } catch (SQLException e) {
                    throw new RuntimeException(e);
                }
            }
        }
    }

    public void continueLoop() {
        lock.notify();
    }
}
