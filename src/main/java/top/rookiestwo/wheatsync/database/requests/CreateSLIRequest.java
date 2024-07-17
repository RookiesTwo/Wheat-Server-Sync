package top.rookiestwo.wheatsync.database.requests;

import net.minecraft.item.ItemStack;
import net.minecraft.util.collection.DefaultedList;
import top.rookiestwo.wheatsync.block.entity.StandardLogisticsInterfaceEntity;
import top.rookiestwo.wheatsync.database.SLICache;

import java.util.UUID;

public class CreateSLIRequest {
    public UUID playerUUID;
    public int communicationID;
    public String inventory;

    public CreateSLIRequest(StandardLogisticsInterfaceEntity entity) {
        this(entity.getBLOCK_PLACER(), entity.getCommunicationID(), entity.getInventory());
    }

    public CreateSLIRequest(UUID playerUUID, int communicationID, DefaultedList<ItemStack> inventory) {
        this.playerUUID = playerUUID;
        this.communicationID = communicationID;
        this.inventory = SLICache.serializeInventory(inventory);
    }
}
