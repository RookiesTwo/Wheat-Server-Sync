package top.rookiestwo.wheatsync.database;

import com.mojang.brigadier.exceptions.CommandSyntaxException;
import net.minecraft.inventory.Inventories;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.nbt.StringNbtReader;
import net.minecraft.util.collection.DefaultedList;
import top.rookiestwo.wheatsync.database.requests.UpdateInventoryRequest;

import java.util.HashMap;
import java.util.Map;
import java.util.Queue;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentLinkedQueue;

public class SLICache {
    private final Map<UUID, Map<Integer, SLIStatus>> cache;

    public static Queue<UpdateInventoryRequest> updateInventoryRequestQueue = new ConcurrentLinkedQueue<>();

    public static void addUpdateInventoryRequest(UpdateInventoryRequest request) {
        updateInventoryRequestQueue.add(request);
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

    public SLICache() {
        this.cache = new ConcurrentHashMap<>();
    }

    public void addOrUpdateSLICache(UUID playerUUID, int communicationID, DefaultedList<ItemStack> inventory, boolean ifOnOtherServer) {
        this.addOrUpdateSLICache(playerUUID, communicationID, serializeInventory(inventory), ifOnOtherServer);
    }

    public void addOrUpdateSLICache(UUID playerUUID, int communicationID, String inventory, boolean ifOnOtherServer) {
        this.addOrUpdateSLICache(playerUUID, communicationID, inventory, true, ifOnOtherServer);
    }

    public void addOrUpdateSLICache(UUID playerUUID, int communicationID, String inventory, boolean isLoaded, boolean ifOnOtherServer) {
        cache.compute(playerUUID, (uuid, communicationMap) -> {
            if (communicationMap == null) {
                //原本不存在的情况
                communicationMap = new HashMap<>();
                SLIStatus newStatus = new SLIStatus(inventory, isLoaded, ifOnOtherServer);
                communicationMap.put(communicationID, newStatus);
            } else {
                //原本存在的情况
                SLIStatus oldStatus = communicationMap.get(communicationID);
                SLIStatus newStatus = new SLIStatus(inventory, oldStatus.isLoaded, ifOnOtherServer);
                communicationMap.put(communicationID, newStatus);
            }
            return communicationMap;
        });
    }

    public void removeSLI(UUID playerUUID, int communicationID) {
        Map<Integer, SLIStatus> playerCache = cache.get(playerUUID);
        if (playerCache != null) {
            playerCache.remove(communicationID);
            if (playerCache.isEmpty()) {
                cache.remove(playerUUID);
            }
        }
    }

    public void updateSLIInventory(UUID playerUUID, int communicationID, String newInventory) {
        SLIStatus status = cache.get(playerUUID).get(communicationID);
        status.Inventory = newInventory;
        addUpdateInventoryRequest(new UpdateInventoryRequest(playerUUID, communicationID, status.Inventory));
    }

    public boolean ifOnOtherServer(UUID playeUUID, int communicationID) {
        return cache.get(playeUUID).get(communicationID).ifOnOtherServer;
    }

    public String getInventoryOf(UUID playerUUID, int communicationID) {
        return cache.get(playerUUID).get(communicationID).Inventory;
    }

    public void setSLILoadingStatus(UUID playerUUID, int communicationID, boolean status) {
        Map<Integer, SLIStatus> playerCache = cache.get(playerUUID);
        if (playerCache == null) {
            return;
        }
        playerCache.get(communicationID).isLoaded = status;
    }

    public boolean ifSLIExists(UUID playerUUID, int communicationID) {
        Map<Integer, SLIStatus> playerCache = cache.get(playerUUID);
        if (playerCache == null) {
            return false;
        }
        return playerCache.containsKey(communicationID);
    }
}