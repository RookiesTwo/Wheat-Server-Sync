package top.rookiestwo.wheatsync.database.requests;

import net.minecraft.inventory.Inventories;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.util.collection.DefaultedList;

import java.util.UUID;

public class CreateSLIRequest {
    public UUID playerUUID;
    public int communicationID;
    public String inventory;

    public CreateSLIRequest(UUID playerUUID, int communicationID) {
        this.playerUUID = playerUUID;
        this.communicationID = communicationID;
        NbtCompound nbt = new NbtCompound();
        this.inventory = Inventories.writeNbt(nbt, DefaultedList.ofSize(5, ItemStack.EMPTY)).toString();
    }
}
