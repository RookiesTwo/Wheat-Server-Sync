package top.rookiestwo.wheatsync.database;

import com.mojang.brigadier.exceptions.CommandSyntaxException;
import top.rookiestwo.wheatsync.block.entity.StandardLogisticsInterfaceEntity;
import top.rookiestwo.wheatsync.database.requests.GetSLIRequest;
import top.rookiestwo.wheatsync.database.requests.UpdateInventoryRequest;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class SLICache {
    private Map<UUID, Map<Integer, StandardLogisticsInterfaceEntity>> cache;

    public SLICache() {
        this.cache = new HashMap<>();
    }

    public void addSLICache(StandardLogisticsInterfaceEntity entity) {
        this.addSLICache(entity.getBLOCK_PLACER(), entity.getCommunicationID(), entity);
    }

    public void addSLICache(UUID playerUUID, int communicationID, StandardLogisticsInterfaceEntity entity) {
        cache.computeIfAbsent(playerUUID, k -> new HashMap<>()).put(communicationID, entity);
    }

    public void removeSLICache(StandardLogisticsInterfaceEntity entity) {
        this.removeSLICache(entity.getBLOCK_PLACER(), entity.getCommunicationID());
    }

    public void removeSLICache(UUID playerUUID, int communicationID) {
        Map<Integer, StandardLogisticsInterfaceEntity> idMap = cache.get(playerUUID);
        if (idMap != null) {
            idMap.remove(communicationID);
            if (idMap.isEmpty()) {
                cache.remove(playerUUID);
            }
        }
    }

    public void addAllSLIToGetQueue() {
        for (Map<Integer, StandardLogisticsInterfaceEntity> playerCache : cache.values()) {
            for (StandardLogisticsInterfaceEntity entity : playerCache.values()) {
                DataBaseIOManager.addGetSLIRequest(new GetSLIRequest(entity));
            }
        }
    }

    public void updateSLIInventory(UUID playerUUID, int communicationID, String inventoryData) throws CommandSyntaxException {
        StandardLogisticsInterfaceEntity sli = getSLI(playerUUID, communicationID);
        if (sli != null) {
            if (!inventoryData.equals(DataBaseIOManager.serializeInventory(sli.getInventory()))) {
                sli.setInventory(inventoryData);
            }
        }
    }

    public void addUpdateRequests() {
        for (Map<Integer, StandardLogisticsInterfaceEntity> playerCache : cache.values()) {
            for (StandardLogisticsInterfaceEntity entity : playerCache.values()) {
                if (entity.ifInventoryChanged())
                    DataBaseIOManager.addUpdateInventoryRequest(new UpdateInventoryRequest(entity));
            }
        }
    }

    public void updateCommunicationID(StandardLogisticsInterfaceEntity entity, int newCommunicationID) {
        removeSLICache(entity);
        addSLICache(entity.getBLOCK_PLACER(), newCommunicationID, entity);
    }

    public StandardLogisticsInterfaceEntity getSLI(UUID playerUUID, int communicationID) {
        Map<Integer, StandardLogisticsInterfaceEntity> idMap = cache.get(playerUUID);
        return (idMap != null) ? idMap.get(communicationID) : null;
    }
}