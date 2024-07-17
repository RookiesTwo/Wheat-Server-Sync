package top.rookiestwo.wheatsync.database;

import com.mojang.brigadier.exceptions.CommandSyntaxException;
import net.minecraft.inventory.Inventories;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.nbt.StringNbtReader;
import net.minecraft.util.collection.DefaultedList;
import top.rookiestwo.wheatsync.database.requests.*;

import java.util.Queue;

public class DataBaseIOManager {

    public static Queue<CreateSLIRequest> createSLIRequestQueue;
    public static Queue<UpdateInventoryRequest> updateInventoryRequestQueue;
    public static Queue<DeleteSLIRequest> deleteSLIRequestQueue;
    public static Queue<ChangeCommunicationIDRequest> changeCommunicationIDRequestQueue;
    public static Queue<GetSLIRequest> getSLIRequestQueue;

    public static void addCreateSLIRequest(CreateSLIRequest request) {
        createSLIRequestQueue.add(request);
    }

    public static void addUpdateInventoryRequest(UpdateInventoryRequest request) {
        updateInventoryRequestQueue.add(request);
    }

    public static void addDeleteSLIRequest(DeleteSLIRequest request) {
        deleteSLIRequestQueue.add(request);
    }

    public static void addChangeCommunicationIDRequest(ChangeCommunicationIDRequest request) {
        changeCommunicationIDRequestQueue.add(request);
    }

    public static void addGetSLIRequest(GetSLIRequest request) {
        getSLIRequestQueue.add(request);
    }

    public static String serializeInventory(DefaultedList<ItemStack> inventory) {
        NbtCompound nbt = new NbtCompound();
        Inventories.writeNbt(nbt, inventory);
        return nbt.toString();
    }

    public static DefaultedList<ItemStack> unSerializeInventory(String inventoryString) {
        DefaultedList<ItemStack> tempInv = DefaultedList.ofSize(5, ItemStack.EMPTY);
        try {
            NbtCompound nbt = StringNbtReader.parse(inventoryString);
            Inventories.readNbt(nbt, tempInv);
        } catch (CommandSyntaxException e) {
            throw new RuntimeException(e);
        }
        return tempInv;
    }
}