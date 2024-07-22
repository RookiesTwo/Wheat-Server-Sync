package top.rookiestwo.wheatsync.config;


import java.util.ArrayList;
import java.util.List;

//used to store configuration values；
//ONLY FOR SERVERSIDE
public class WheatSyncConfig {
    public boolean ifEnable = false;
    public String ServerName = "main_server";
    public String MySQLAddress ="localhost";
    public int MySQLPort=3306;
    public String MySQLAccount ="root";
    public String Password="123456";
    public String MySQLDatabase="wheatsync";
    public List<String> PlayerSyncBlackList = new ArrayList<>();

    public WheatSyncConfig() {
        PlayerSyncBlackList.add("Dimension");
        PlayerSyncBlackList.add("Pos");
        PlayerSyncBlackList.add("Motion");
        PlayerSyncBlackList.add("Rotation");
        PlayerSyncBlackList.add("FallFlying");
        PlayerSyncBlackList.add("foodExhaustionLevel");
        PlayerSyncBlackList.add("foodLevel");
        PlayerSyncBlackList.add("foodSaturationLevel");
        PlayerSyncBlackList.add("foodTickTimer");
        PlayerSyncBlackList.add("HasGraveCompass");
        PlayerSyncBlackList.add("HurtByTimestamp");
        PlayerSyncBlackList.add("OnGround");
        PlayerSyncBlackList.add("HurtTime");
        PlayerSyncBlackList.add("DeathTime");
        PlayerSyncBlackList.add("SpawnAngle");
        PlayerSyncBlackList.add("SpawnDimension");
        PlayerSyncBlackList.add("SpawnX");
        PlayerSyncBlackList.add("SpawnY");
        PlayerSyncBlackList.add("SpawnZ");
    }
}
