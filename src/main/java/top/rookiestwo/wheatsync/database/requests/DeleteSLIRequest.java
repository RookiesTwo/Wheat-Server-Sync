package top.rookiestwo.wheatsync.database.requests;

import java.util.UUID;

public class DeleteSLIRequest {
    public UUID playerUUID;
    public int communicationID;

    public DeleteSLIRequest(UUID playerUUID, int communicationID) {
        this.playerUUID = playerUUID;
        this.communicationID = communicationID;
    }
}
