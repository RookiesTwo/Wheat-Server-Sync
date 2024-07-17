package top.rookiestwo.wheatsync.database.requests;

public class GetSLIRequest {
    public String playerUUID;
    public int communicationID;

    public GetSLIRequest(String playerUUID, int communicationID) {
        this.playerUUID = playerUUID;
        this.communicationID = communicationID;
    }
}
