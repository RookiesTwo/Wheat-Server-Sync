package top.rookiestwo.wheatsync.database;

import com.mojang.brigadier.exceptions.CommandSyntaxException;
import net.minecraft.inventory.Inventories;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.nbt.StringNbtReader;
import net.minecraft.util.collection.DefaultedList;
import top.rookiestwo.wheatsync.block.entity.StandardLogisticsInterfaceEntity;
import top.rookiestwo.wheatsync.database.requests.*;

import java.util.HashMap;
import java.util.Map;
import java.util.Queue;
import java.util.UUID;
import java.util.concurrent.ConcurrentLinkedQueue;

public class SLICache {
    private Map<UUID, Map<Integer, StandardLogisticsInterfaceEntity>> cache;

    public static Queue<CreateSLIRequest> createSLIRequestQueue = new ConcurrentLinkedQueue<>();
    public static Queue<UpdateInventoryRequest> updateInventoryRequestQueue = new ConcurrentLinkedQueue<>();
    public static Queue<DeleteSLIRequest> deleteSLIRequestQueue = new ConcurrentLinkedQueue<>();
    public static Queue<ChangeCommunicationIDRequest> changeCommunicationIDRequestQueue = new ConcurrentLinkedQueue<>();
    public static Queue<GetSLIRequest> getSLIRequestQueue = new ConcurrentLinkedQueue<>();

    public SLICache() {
        this.cache = new HashMap<>();
    }

    public void addSLICache(StandardLogisticsInterfaceEntity entity) {
        this.addSLICache(entity.getBLOCK_PLACER(), entity.getCommunicationID(), entity);
    }

    public void addSLICache(UUID playerUUID, int communicationID, StandardLogisticsInterfaceEntity entity) {
        cache.computeIfAbsent(playerUUID, k -> new HashMap<>()).put(communicationID, entity);
    }

    public void removeSLICache(StandardLogisticsInterfaceEntity entity) {
        this.removeSLICache(entity.getBLOCK_PLACER(), entity.getCommunicationID());
    }

    public void removeSLICache(UUID playerUUID, int communicationID) {
        Map<Integer, StandardLogisticsInterfaceEntity> idMap = cache.get(playerUUID);
        if (idMap != null) {
            idMap.remove(communicationID);
            if (idMap.isEmpty()) {
                cache.remove(playerUUID);
            }
        }
    }

    public static void addCreateSLIRequest(CreateSLIRequest request) {
        createSLIRequestQueue.add(request);
    }

    public static void addUpdateInventoryRequest(UpdateInventoryRequest request) {
        updateInventoryRequestQueue.add(request);
    }

    public static void addDeleteSLIRequest(DeleteSLIRequest request) {
        deleteSLIRequestQueue.add(request);
    }

    public void updateCommunicationID(StandardLogisticsInterfaceEntity entity, int newCommunicationID) {
        removeSLICache(entity);
        addSLICache(entity.getBLOCK_PLACER(), newCommunicationID, entity);
    }

    public StandardLogisticsInterfaceEntity getSLI(UUID playerUUID, int communicationID) {
        Map<Integer, StandardLogisticsInterfaceEntity> idMap = cache.get(playerUUID);
        return (idMap != null) ? idMap.get(communicationID) : null;
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

    public void addAllSLIToGetQueue() {
        for (Map<Integer, StandardLogisticsInterfaceEntity> playerCache : cache.values()) {
            for (StandardLogisticsInterfaceEntity entity : playerCache.values()) {
                addGetSLIRequest(new GetSLIRequest(entity));
            }
        }
    }

    public void updateSLIInventory(UUID playerUUID, int communicationID, String inventoryData) throws CommandSyntaxException {
        StandardLogisticsInterfaceEntity sli = getSLI(playerUUID, communicationID);
        if (sli != null) {
            if (!inventoryData.equals(serializeInventory(sli.getInventory()))) {
                sli.setInventory(inventoryData);
            }
        }
    }

    public void addUpdateRequests() {
        for (Map<Integer, StandardLogisticsInterfaceEntity> playerCache : cache.values()) {
            for (StandardLogisticsInterfaceEntity entity : playerCache.values()) {
                if (entity.ifInventoryChanged())
                    addUpdateInventoryRequest(new UpdateInventoryRequest(entity));
            }
        }
    }
}