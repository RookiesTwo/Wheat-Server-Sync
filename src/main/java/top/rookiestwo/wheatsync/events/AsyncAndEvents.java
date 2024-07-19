package top.rookiestwo.wheatsync.events;

import net.fabricmc.fabric.api.event.lifecycle.v1.ServerBlockEntityEvents;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerLifecycleEvents;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerTickEvents;
import net.fabricmc.fabric.api.networking.v1.PacketSender;
import net.minecraft.block.BlockState;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.entity.player.PlayerEntity;
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
import top.rookiestwo.wheatsync.database.SLICache;
import top.rookiestwo.wheatsync.screen.SLIScreenHandler;

import java.sql.SQLException;
import java.util.concurrent.CompletableFuture;

public class AsyncAndEvents {
    public static void register() {
        ServerBlockEntityEvents.BLOCK_ENTITY_LOAD.register((BlockEntity entity, ServerWorld world) -> {

            if (entity instanceof StandardLogisticsInterfaceEntity SLIEntity) {
                ChunkPos chunkPos = new ChunkPos(entity.getPos());
                if (!world.isChunkLoaded(chunkPos.toLong())) return;
                if (SLIEntity.getCommunicationID() == 0) return;
                SLIEntity.setInventory(WheatSync.sliCache.getInventoryOf(SLIEntity.getBLOCK_PLACER(), SLIEntity.getCommunicationID()));
                SLIEntity.copyInventoryToSnapshot();
                WheatSync.sliCache.setSLILoadingStatus(SLIEntity.getBLOCK_PLACER(), SLIEntity.getCommunicationID(), true);
            }

        });

        ServerBlockEntityEvents.BLOCK_ENTITY_UNLOAD.register((BlockEntity entity, ServerWorld world) -> {

            if (entity instanceof StandardLogisticsInterfaceEntity SLIEntity) {
                WheatSync.sliCache.setSLILoadingStatus(SLIEntity.getBLOCK_PLACER(), SLIEntity.getCommunicationID(), false);
            }

        });

        ServerLifecycleEvents.SERVER_STARTING.register((server) -> {
            WheatSync.sliCache = new SLICache();
            WheatSync.databaseHelper.loadSLIEntitiesFromDatabaseToCache();
        });

        ServerTickEvents.START_SERVER_TICK.register((server) -> {
            /*CompletableFuture.supplyAsync(()->{

                return true;
            },WheatSync.asyncExecutor);*/
            WheatSync.databaseHelper.loadSLIEntitiesFromDatabaseToCache();
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
        SLIScreenHandler screenHandler = (SLIScreenHandler) player.currentScreenHandler;
        StandardLogisticsInterfaceEntity entity = screenHandler.getSLIEntity();

        if (WheatSync.sliCache.ifSLIExists(entity.getBLOCK_PLACER(), newID)) {
            //将否定信息传递给服务端
            return;
        }

        CompletableFuture.supplyAsync(() -> {
            try {
                Pair<Boolean, Boolean> result = new Pair<>(false, false);
                // 如果数据库内存在此容器
                WheatSync.LOGGER.info("ChangeID Detect");
                if (WheatSync.databaseHelper.ifSLIExists(entity.getBLOCK_PLACER(), newID)) {
                    WheatSync.databaseHelper.getSLIToCache(entity.getBLOCK_PLACER(), newID);
                    result.setLeft(true);
                } else {
                    WheatSync.sliCache.addOrUpdateSLICache(entity.getBLOCK_PLACER(), newID, entity.getInventory(), false);
                    WheatSync.databaseHelper.createSLIRecord(entity.getBLOCK_PLACER(), newID, SLICache.serializeInventory(entity.getInventory()));
                }
                //如果老ID不为0，对老容器的处理
                if (entity.getCommunicationID() != 0) {
                    //如果在其他服务器存在
                    if (WheatSync.sliCache.ifOnOtherServer(entity.getBLOCK_PLACER(), entity.getCommunicationID())) {
                        WheatSync.databaseHelper.updateSLIServerStatus(entity.getBLOCK_PLACER(), entity.getCommunicationID(), false);
                        WheatSync.sliCache.removeSLI(entity.getBLOCK_PLACER(), entity.getCommunicationID());
                    } else {
                        WheatSync.databaseHelper.deleteSLIRecord(entity.getBLOCK_PLACER(), entity.getCommunicationID());
                        result.setRight(true);
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
                if (result.getLeft()) {
                    WheatSync.LOGGER.info("Write To Inventory");
                    entity.setInventory(WheatSync.sliCache.getInventoryOf(entity.getBLOCK_PLACER(), newID));
                }
                //爆金币
                if (result.getRight()) {
                    entity.clear();
                    ItemScatterer.spawn(player.getWorld(), player.getBlockPos(), SLICache.unSerializeInventory(WheatSync.sliCache.getInventoryOf(entity.getBLOCK_PLACER(), entity.getCommunicationID())));
                    WheatSync.sliCache.removeSLI(entity.getBLOCK_PLACER(), entity.getCommunicationID());
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
        CompletableFuture.supplyAsync(() -> {
            if (!world.isClient) {
                if (WheatSync.sliCache.ifOnOtherServer(entity.getBLOCK_PLACER(), entity.getCommunicationID())) {
                    WheatSync.databaseHelper.updateSLIServerStatus(entity.getBLOCK_PLACER(), entity.getCommunicationID(), false);
                    WheatSync.sliCache.setSLILoadingStatus(entity.getBLOCK_PLACER(), entity.getCommunicationID(), false);
                    return true;
                }
                WheatSync.databaseHelper.deleteSLIRecord(entity.getBLOCK_PLACER(), entity.getCommunicationID());
                return false;
            }
            return false;
        }, WheatSync.asyncExecutor).thenAccept((result) -> {
            if (world instanceof ServerWorld) {
                MinecraftServer server = world.getServer();
                server.executeSync(() -> {
                    if (!result) ItemScatterer.spawn(world, pos, entity);
                    block.superOnStateReplaced(state, world, pos, newState, moved);
                    WheatSync.sliCache.removeSLI(entity.getBLOCK_PLACER(), entity.getCommunicationID());
                    world.updateComparators(pos, block);
                });
            }
            //也许需要补充客户端代码，待测试
        });
    }

    public static void runAfterLogic() throws InterruptedException, SQLException {
        CompletableFuture.supplyAsync(() -> {
            WheatSync.databaseHelper.processUpdateInventoryQueue();
            return true;
        }, WheatSync.asyncExecutor);
    }
}
