package top.rookiestwo.wheatsync.database;

import net.minecraft.item.ItemStack;
import net.minecraft.util.collection.DefaultedList;

public class SLIStatus {
    String Inventory;
    boolean isLoaded;
    boolean ifOnOtherServer;

    public SLIStatus(String inventory, boolean isLoaded, boolean ifOnOtherServer) {
        Inventory = inventory;
        this.isLoaded = isLoaded;
        this.ifOnOtherServer = ifOnOtherServer;
    }

    public SLIStatus(DefaultedList<ItemStack> inventory, boolean isLoaded, boolean ifOnOtherServer) {
        this(SLICache.serializeInventory(inventory), isLoaded, ifOnOtherServer);
    }
}
