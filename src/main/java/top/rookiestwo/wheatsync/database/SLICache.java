package top.rookiestwo.wheatsync.database;

import com.mojang.brigadier.exceptions.CommandSyntaxException;
import net.minecraft.inventory.Inventories;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.nbt.StringNbtReader;
import net.minecraft.util.Pair;
import net.minecraft.util.collection.DefaultedList;
import top.rookiestwo.wheatsync.block.entity.StandardLogisticsInterfaceEntity;
import top.rookiestwo.wheatsync.database.requests.*;

import java.util.HashMap;
import java.util.Map;
import java.util.Queue;
import java.util.UUID;
import java.util.concurrent.ConcurrentLinkedQueue;

public class SLICache {
    private Map<UUID, Map<Integer, Pair<StandardLogisticsInterfaceEntity, String>>> cache;

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
        cache.computeIfAbsent(playerUUID, k -> new HashMap<>()).put(communicationID, new Pair<>(entity, serializeInventory(entity.getInventory())));
    }

    public void removeSLICache(StandardLogisticsInterfaceEntity entity) {
        this.removeSLICache(entity.getBLOCK_PLACER(), entity.getCommunicationID());
    }

    public void removeSLICache(UUID playerUUID, int communicationID) {
        Map<Integer, Pair<StandardLogisticsInterfaceEntity, String>> idMap = cache.get(playerUUID);
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

    public static void addChangeCommunicationIDRequest(ChangeCommunicationIDRequest request) {
        changeCommunicationIDRequestQueue.add(request);
    }

    public static void addGetSLIRequest(GetSLIRequest request) {
        getSLIRequestQueue.add(request);
    }

    public void updateCommunicationID(StandardLogisticsInterfaceEntity entity, int newCommunicationID) {
        removeSLICache(entity);
        addSLICache(entity.getBLOCK_PLACER(), newCommunicationID, entity);
    }

    public StandardLogisticsInterfaceEntity getSLI(UUID playerUUID, int communicationID) {
        Map<Integer, Pair<StandardLogisticsInterfaceEntity, String>> idMap = cache.get(playerUUID);
        if (idMap != null) {
            return idMap.get(communicationID).getLeft();
        }
        return null;
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

    public void addAllCachedEntityToGetQueue() {
        for (Map<Integer, Pair<StandardLogisticsInterfaceEntity, String>> playerCache : cache.values()) {
            for (Map.Entry<Integer, Pair<StandardLogisticsInterfaceEntity, String>> entry : playerCache.entrySet()) {
                StandardLogisticsInterfaceEntity entity = entry.getValue().getLeft();
                addGetSLIRequest(new GetSLIRequest(entity));
            }
        }
    }

    public void updateSLIInventoryCache(UUID playerUUID, int communicationID, String inventoryData) throws CommandSyntaxException {
        Map<Integer, Pair<StandardLogisticsInterfaceEntity, String>> playerCache = cache.get(playerUUID);
        if (playerCache != null) {
            Pair<StandardLogisticsInterfaceEntity, String> sliPair = playerCache.get(communicationID);
            if (sliPair != null) {
                playerCache.put(communicationID, new Pair<>(sliPair.getLeft(), inventoryData));
            }
        }
    }

    public void freshInventoryCacheToEntities() {
        for (Map<Integer, Pair<StandardLogisticsInterfaceEntity, String>> playerCache : cache.values()) {
            for (Map.Entry<Integer, Pair<StandardLogisticsInterfaceEntity, String>> entry : playerCache.entrySet()) {
                entry.getValue().getLeft().setInventory(entry.getValue().getRight());
            }
        }
    }

    public void addUpdateRequests() {
        for (Map<Integer, Pair<StandardLogisticsInterfaceEntity, String>> pairs : cache.values()) {
            for (Pair<StandardLogisticsInterfaceEntity, String> pair : pairs.values()) {
                if (pair.getLeft().ifInventoryChanged())
                    addUpdateInventoryRequest(new UpdateInventoryRequest(pair.getLeft()));
            }
        }
    }
}