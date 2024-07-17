package top.rookiestwo.wheatsync.database.requests;

import top.rookiestwo.wheatsync.block.entity.StandardLogisticsInterfaceEntity;

import java.util.UUID;

public class DeleteSLIRequest {
    public UUID playerUUID;
    public int communicationID;

    public DeleteSLIRequest(StandardLogisticsInterfaceEntity entity) {
        this(entity.getBLOCK_PLACER(), entity.getCommunicationID());
    }

    public DeleteSLIRequest(UUID playerUUID, int communicationID) {
        this.playerUUID = playerUUID;
        this.communicationID = communicationID;
    }
}
