package top.rookiestwo.wheatsync.events;

import com.mojang.brigadier.exceptions.CommandSyntaxException;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerBlockEntityEvents;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerLifecycleEvents;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerTickEvents;
import net.fabricmc.fabric.api.networking.v1.PacketSender;
import net.minecraft.block.BlockState;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.nbt.StringNbtReader;
import net.minecraft.network.PacketByteBuf;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.network.ServerPlayNetworkHandler;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.ItemScatterer;
import net.minecraft.util.Pair;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.ChunkPos;
import net.minecraft.world.World;
import top.rookiestwo.wheatsync.WheatSync;
import top.rookiestwo.wheatsync.block.StandardLogisticsInterface;
import top.rookiestwo.wheatsync.block.entity.StandardLogisticsInterfaceEntity;
import top.rookiestwo.wheatsync.config.ConfigManager;
import top.rookiestwo.wheatsync.database.DatabaseHelper;
import top.rookiestwo.wheatsync.database.SLICache;
import top.rookiestwo.wheatsync.screen.SLIScreenHandler;

import java.sql.SQLException;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;

public class AsyncAndEvents {
    public static void register() {

        ServerBlockEntityEvents.BLOCK_ENTITY_LOAD.register((BlockEntity entity, ServerWorld world) -> {
            if (!WheatSync.CONFIG.ifEnable) return;
            if (entity instanceof StandardLogisticsInterfaceEntity SLIEntity) {
                ChunkPos chunkPos = new ChunkPos(entity.getPos());
                if (!world.isChunkLoaded(chunkPos.toLong())) return;
                if (SLIEntity.getCommunicationID() == 0) return;
                //SLIEntity.setInventory(WheatSync.sliCache.getInventoryOf(SLIEntity.getBLOCK_PLACER(), SLIEntity.getCommunicationID()));
                //SLIEntity.copyInventoryToSnapshot();
                WheatSync.sliCache.setSLILoadingStatus(SLIEntity.getBLOCK_PLACER(), SLIEntity.getCommunicationID(), true);
            }

        });

        ServerBlockEntityEvents.BLOCK_ENTITY_UNLOAD.register((BlockEntity entity, ServerWorld world) -> {
            if (!WheatSync.CONFIG.ifEnable) return;
            if (entity instanceof StandardLogisticsInterfaceEntity SLIEntity) {
                WheatSync.sliCache.setSLILoadingStatus(SLIEntity.getBLOCK_PLACER(), SLIEntity.getCommunicationID(), false);
            }
        });

        ServerLifecycleEvents.SERVER_STARTING.register((server) -> {
            //配置文件
            WheatSync.CONFIG_MANAGER = new ConfigManager();
            WheatSync.CONFIG = WheatSync.CONFIG_MANAGER.getConfig();
            if (!WheatSync.CONFIG.ifEnable) return;
            //database
            WheatSync.databaseHelper = new DatabaseHelper();
            WheatSync.sliCache = new SLICache();
            WheatSync.databaseHelper.loadSLIEntitiesFromDatabaseToCache();
        });

        ServerTickEvents.START_SERVER_TICK.register((server) -> {
            if (!WheatSync.CONFIG.ifEnable) return;
            CompletableFuture.supplyAsync(() -> {
                WheatSync.databaseHelper.loadSLIEntitiesFromDatabaseToCache();
                return true;
            }, WheatSync.asyncExecutor);
        });


    }

    public static void onReceiveC2SCommunicationIDChangePacket(
            MinecraftServer server,
            PlayerEntity player,
            ServerPlayNetworkHandler handler,
            PacketByteBuf buf,
            PacketSender responseSender
    ) {
        int newID = buf.readInt();
        if (!WheatSync.CONFIG.ifEnable) return;
        SLIScreenHandler screenHandler = (SLIScreenHandler) player.currentScreenHandler;
        StandardLogisticsInterfaceEntity entity = screenHandler.getSLIEntity();

        if (WheatSync.sliCache.ifSLIExists(entity.getBLOCK_PLACER(), newID)) {
            //将否定信息传递给服务端
            return;
        }
        CompletableFuture.supplyAsync(() -> {
            try {
                Pair<Boolean, Boolean> result = new Pair<>(false, false);
                if (entity.getCommunicationID() != 0) {
                    if (WheatSync.databaseHelper.ifSLIExists(entity.getBLOCK_PLACER(), newID)) {
                        // 如果数据库内存在新容器
                        WheatSync.databaseHelper.updateSLIServerStatus(entity.getBLOCK_PLACER(), newID, true);
                        WheatSync.databaseHelper.getSLIToCache(entity.getBLOCK_PLACER(), newID);
                        result.setLeft(true);
                    } else {
                        // 数据库内不存在新容器
                        WheatSync.sliCache.addOrUpdateSLICache(entity.getBLOCK_PLACER(), newID, SLICache.emptyInventory, false);
                        WheatSync.databaseHelper.createSLIRecord(entity.getBLOCK_PLACER(), newID, SLICache.emptyInventory);
                    }
                    //对老容器的处理
                    if (WheatSync.sliCache.ifOnOtherServer(entity.getBLOCK_PLACER(), entity.getCommunicationID())) {
                        //老容器如果在其他服务器存在
                        WheatSync.databaseHelper.updateSLIServerStatus(entity.getBLOCK_PLACER(), entity.getCommunicationID(), false);
                        WheatSync.sliCache.removeSLI(entity.getBLOCK_PLACER(), entity.getCommunicationID());
                    } else {
                        //老容器如果在其他服务器不存在
                        WheatSync.databaseHelper.deleteSLIRecord(entity.getBLOCK_PLACER(), entity.getCommunicationID());
                        WheatSync.sliCache.addOrUpdateSLICache(entity.getBLOCK_PLACER(), entity.getCommunicationID(), SLICache.serializeInventory(entity.getInventory()), false);
                        result.setRight(true);
                    }
                } else {
                    if (WheatSync.databaseHelper.ifSLIExists(entity.getBLOCK_PLACER(), newID)) {
                        // 如果数据库内存在新容器
                        WheatSync.databaseHelper.updateSLIServerStatus(entity.getBLOCK_PLACER(), newID, true);
                        WheatSync.databaseHelper.getSLIToCache(entity.getBLOCK_PLACER(), newID);
                        result.setLeft(true);
                    } else {
                        // 数据库内不存在新容器
                        WheatSync.sliCache.addOrUpdateSLICache(entity.getBLOCK_PLACER(), newID, entity.getInventory(), false);
                        WheatSync.databaseHelper.createSLIRecord(entity.getBLOCK_PLACER(), newID, SLICache.serializeInventory(entity.getInventory()));
                    }
                }

                return result;
            } catch (Exception e) {
                WheatSync.LOGGER.error("Error processing async operation", e);
                return new Pair<>(false, false);
            }
        }, WheatSync.asyncExecutor).thenAccept((result) -> {
            // 将其他服务器存在的容器从缓存写入物品栏
            server.executeSync(() -> {
                //爆金币
                if (entity.getCommunicationID() != 0) {
                    if (result.getRight()) {
                        entity.clear();
                        ItemScatterer.spawn(player.getWorld(), player.getBlockPos(), SLICache.unSerializeInventory(WheatSync.sliCache.getInventoryOf(entity.getBLOCK_PLACER(), entity.getCommunicationID())));
                        WheatSync.sliCache.removeSLI(entity.getBLOCK_PLACER(), entity.getCommunicationID());
                    }
                    if (result.getLeft() || (!result.getLeft() && !result.getRight())) {
                        entity.setInventory(WheatSync.sliCache.getInventoryOf(entity.getBLOCK_PLACER(), newID));
                        entity.copyInventoryToSnapshot();
                    }
                } else {
                    if (result.getLeft()) {
                        ItemScatterer.spawn(player.getWorld(), player.getBlockPos(), entity.getInventory());
                        entity.setInventory(WheatSync.sliCache.getInventoryOf(entity.getBLOCK_PLACER(), newID));
                        entity.copyInventoryToSnapshot();
                    }
                }
                entity.setCommunicationID(newID);
                entity.markDirty();
            });
        }).exceptionally(e -> {
            WheatSync.LOGGER.error("Error in CompletableFuture chain", e);
            return null;
        });
    }

    public static void onSLIBlockDestroyed(BlockState state, World world, BlockPos pos, BlockState newState, boolean moved, StandardLogisticsInterfaceEntity entity, StandardLogisticsInterface block) {
        if (!WheatSync.CONFIG.ifEnable) return;
        CompletableFuture.supplyAsync(() -> {
            if (!world.isClient) {
                if (WheatSync.sliCache.ifOnOtherServer(entity.getBLOCK_PLACER(), entity.getCommunicationID())) {
                    //如果在其他服务器存在此容器
                    WheatSync.databaseHelper.updateSLIServerStatus(entity.getBLOCK_PLACER(), entity.getCommunicationID(), false);
                    WheatSync.sliCache.setSLILoadingStatus(entity.getBLOCK_PLACER(), entity.getCommunicationID(), false);
                    return true;
                }
                //如果其他服务器不存在
                WheatSync.databaseHelper.deleteSLIRecord(entity.getBLOCK_PLACER(), entity.getCommunicationID());
                return false;
            }
            return false;
        }, WheatSync.asyncExecutor).thenAccept((result) -> {
            if (world instanceof ServerWorld) {
                MinecraftServer server = world.getServer();
                server.executeSync(() -> {
                    if (!result)
                        ItemScatterer.spawn(world, pos, SLICache.unSerializeInventory(WheatSync.sliCache.getInventoryOf(entity.getBLOCK_PLACER(), entity.getCommunicationID())));
                    block.superOnStateReplaced(state, world, pos, newState, moved);
                    WheatSync.sliCache.removeSLI(entity.getBLOCK_PLACER(), entity.getCommunicationID());
                    world.updateComparators(pos, block);
                });
            }
            //也许需要补充客户端代码，待测试
        });
    }

    public static void runAfterLogic() throws InterruptedException, SQLException {
        if (!WheatSync.CONFIG.ifEnable) return;
        CompletableFuture.supplyAsync(() -> {
            WheatSync.databaseHelper.processUpdateInventoryQueue();
            return true;
        }, WheatSync.asyncExecutor);
    }

    public static void onPlayerDataSave(NbtCompound nbt) {
        CompletableFuture.supplyAsync(() -> {
            NbtCompound nbt2 = nbt.copy();
            //remove blacklisted elements
            for (int i = 0; i < WheatSync.CONFIG.PlayerSyncBlackList.size(); i++) {
                nbt2.remove(WheatSync.CONFIG.PlayerSyncBlackList.get(i));
            }
            WheatSync.databaseHelper.updatePlayerData(nbt2.getUuid("UUID"), nbt2.toString());
            return true;
        }, WheatSync.asyncExecutor);
    }

    public static void onPlayerDataLoad(NbtCompound nbt) throws CommandSyntaxException {
        UUID playerUUID = nbt.getUuid("UUID");

        if (!WheatSync.databaseHelper.ifPlayerDataExists(playerUUID)) {
            NbtCompound nbt2 = nbt.copy();
            //remove blacklisted elements
            for (int i = 0; i < WheatSync.CONFIG.PlayerSyncBlackList.size(); i++) {
                nbt2.remove(WheatSync.CONFIG.PlayerSyncBlackList.get(i));
            }
            WheatSync.databaseHelper.createPlayerData(playerUUID, nbt2.toString());
        }
        nbt.copyFrom(StringNbtReader.parse(WheatSync.databaseHelper.readPlayerData(playerUUID)));
    }
}
