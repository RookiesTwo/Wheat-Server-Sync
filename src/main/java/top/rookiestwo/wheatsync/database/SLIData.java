package top.rookiestwo.wheatsync.database;

import top.rookiestwo.wheatsync.block.entity.StandardLogisticsInterfaceEntity;

import java.util.UUID;

public class SLIData {
    public UUID playerUUID;
    public int communicationID;
    public String inventoryData;

    public SLIData(StandardLogisticsInterfaceEntity entity) {
        this(
                entity.getBLOCK_PLACER(),
                entity.getCommunicationID(),
                DataBaseIOManager.serializeInventory(entity.getInventory())
        );
    }

    public SLIData(UUID playerUUID, int CommunicationID, String inventoryData) {
        this.playerUUID = playerUUID;
        this.communicationID = CommunicationID;
        this.inventoryData = inventoryData;
    }
}