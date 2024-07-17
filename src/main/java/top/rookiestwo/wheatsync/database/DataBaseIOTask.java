package top.rookiestwo.wheatsync.database;

import top.rookiestwo.wheatsync.WheatSync;

import java.sql.SQLException;

public class DataBaseIOTask implements Runnable {
    private final Object lock = new Object();

    @Override
    public void run() {
        while (true) {
            long startTime; // 记录循环开始时间
            synchronized (lock) {
                try {
                    lock.wait();
                    startTime = System.currentTimeMillis(); // 记录循环开始时间
                    WheatSync.databaseHelper.processCreateSLIRequests(SLICache.createSLIRequestQueue);
                    WheatSync.databaseHelper.processDeleteSLIRequests(SLICache.deleteSLIRequestQueue);
                    WheatSync.databaseHelper.processChangeCommunicationIDRequests(SLICache.changeCommunicationIDRequestQueue);
                    WheatSync.databaseHelper.processUpdateInventoryRequests(SLICache.updateInventoryRequestQueue);
                    WheatSync.databaseHelper.processGetSLIRequests(SLICache.getSLIRequestQueue);

                } catch (InterruptedException e) {
                    WheatSync.LOGGER.error("Work thread Interrupted.");
                    break;
                } catch (SQLException e) {
                    throw new RuntimeException(e);
                }
            }
            long endTime = System.currentTimeMillis(); // 记录循环结束时间
            long duration = endTime - startTime; // 计算循环消耗时间
            WheatSync.LOGGER.info("Loop duration: " + duration + " ms"); // 输出循环消耗时间
        }
    }

    public void continueLoop() {
        synchronized (lock) {
            lock.notify();
        }
    }
}
