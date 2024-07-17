package top.rookiestwo.wheatsync.database.requests;

import java.util.UUID;

public class UpdateInventoryRequest {
    public UUID playerUUID;
    public int communicationID;
    public String newInventory;

    public UpdateInventoryRequest(UUID playerUUID, int communicationID, String newInventory) {
        this.playerUUID = playerUUID;
        this.communicationID = communicationID;
        this.newInventory = newInventory;
    }
}
