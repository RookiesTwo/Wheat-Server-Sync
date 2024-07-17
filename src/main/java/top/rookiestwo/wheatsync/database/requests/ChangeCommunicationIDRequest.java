package top.rookiestwo.wheatsync.database.requests;

import top.rookiestwo.wheatsync.block.entity.StandardLogisticsInterfaceEntity;

import java.util.UUID;

public class ChangeCommunicationIDRequest {
    public UUID playerUUID;
    public int communicationID;
    public int newCommunicationID;

    public ChangeCommunicationIDRequest(StandardLogisticsInterfaceEntity entity, int newCommunicationID) {
        this(entity.getBLOCK_PLACER(), entity.getCommunicationID(), newCommunicationID);
    }

    public ChangeCommunicationIDRequest(UUID playerUUID, int communicationID, int newCommunicationID) {
        this.playerUUID = playerUUID;
        this.communicationID = communicationID;
        this.newCommunicationID = newCommunicationID;
    }
}
