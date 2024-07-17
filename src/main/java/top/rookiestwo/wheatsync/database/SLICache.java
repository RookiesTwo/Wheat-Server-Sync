package top.rookiestwo.wheatsync.database;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class SLICache {
    private Map<UUID, Map<Integer, SLIData>> cache;

    public SLICache() {
        this.cache = new HashMap<>();
    }

    public void addSLI(SLIData data) {
        cache.computeIfAbsent(data.playerUUID, k -> new HashMap<>()).put(data.communicationID, data);
    }

    public void removeSLI(SLIData data) {
        Map<Integer, SLIData> idMap = cache.get(data.playerUUID);
        if (idMap != null) {
            idMap.remove(data.communicationID);
            if (idMap.isEmpty()) {
                cache.remove(data);
            }
        }
    }

    public SLIData getSLI(UUID playerUUID, int inventoryCommunicationID) {
        Map<Integer, SLIData> idMap = cache.get(playerUUID);
        return (idMap != null) ? idMap.get(inventoryCommunicationID) : null;
    }
}