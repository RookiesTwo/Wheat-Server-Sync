package top.rookiestwo.wheatsync.database.requests;

import top.rookiestwo.wheatsync.block.entity.StandardLogisticsInterfaceEntity;

import java.util.UUID;

public class GetSLIRequest {
    public UUID playerUUID;
    public int communicationID;

    public GetSLIRequest(StandardLogisticsInterfaceEntity entity) {
        this(entity.getBLOCK_PLACER(), entity.getCommunicationID());
    }

    public GetSLIRequest(UUID playerUUID, int communicationID) {
        this.playerUUID = playerUUID;
        this.communicationID = communicationID;
    }
}
