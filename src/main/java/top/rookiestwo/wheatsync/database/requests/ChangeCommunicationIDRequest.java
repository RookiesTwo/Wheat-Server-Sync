package top.rookiestwo.wheatsync.database.requests;

import java.util.UUID;

public class ChangeCommunicationIDRequest {
    public UUID playerUUID;
    public int communicationID;
    public int newCommunicationID;

    public ChangeCommunicationIDRequest(UUID playerUUID, int communicationID, int newCommunicationID) {
        this.playerUUID = playerUUID;
        this.communicationID = communicationID;
        this.newCommunicationID = newCommunicationID;
    }
}
