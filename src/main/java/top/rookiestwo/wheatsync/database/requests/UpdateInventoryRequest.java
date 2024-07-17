package top.rookiestwo.wheatsync.database.requests;

import top.rookiestwo.wheatsync.block.entity.StandardLogisticsInterfaceEntity;
import top.rookiestwo.wheatsync.database.SLICache;

import java.util.UUID;

public class UpdateInventoryRequest {
    public UUID playerUUID;
    public int communicationID;
    public String newInventory;

    public UpdateInventoryRequest(StandardLogisticsInterfaceEntity sliEntity) {
        this(sliEntity.getBLOCK_PLACER(), sliEntity.getCommunicationID(), SLICache.serializeInventory(sliEntity.getInventory()));
    }

    public UpdateInventoryRequest(UUID playerUUID, int communicationID, String newInventory) {
        this.playerUUID = playerUUID;
        this.communicationID = communicationID;
        this.newInventory = newInventory;
    }
}
