package top.rookiestwo.wheatsync.events;

import net.fabricmc.fabric.api.event.lifecycle.v1.ServerBlockEntityEvents;
import net.fabricmc.fabric.api.networking.v1.PacketSender;
import net.minecraft.block.BlockState;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.network.PacketByteBuf;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.network.ServerPlayNetworkHandler;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.ItemScatterer;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import top.rookiestwo.wheatsync.WheatSync;
import top.rookiestwo.wheatsync.block.StandardLogisticsInterface;
import top.rookiestwo.wheatsync.block.entity.StandardLogisticsInterfaceEntity;
import top.rookiestwo.wheatsync.database.SLICache;
import top.rookiestwo.wheatsync.screen.SLIScreenHandler;

import java.util.concurrent.CompletableFuture;

public class AsyncEvents {
    public static void register() {
        ServerBlockEntityEvents.BLOCK_ENTITY_LOAD.register((BlockEntity entity, ServerWorld world) -> {

            if (entity instanceof StandardLogisticsInterfaceEntity SLIEntity) {
                if (SLIEntity.getCommunicationID() == 0) {
                    return;
                }
                SLIEntity.setInventory(WheatSync.sliCache.getInventoryOf(SLIEntity.getBLOCK_PLACER(), SLIEntity.getCommunicationID()));
                WheatSync.sliCache.setSLILoadingStatus(SLIEntity.getBLOCK_PLACER(), SLIEntity.getCommunicationID(), true);
            }

        });

        ServerBlockEntityEvents.BLOCK_ENTITY_UNLOAD.register((BlockEntity entity, ServerWorld world) -> {
            if (entity instanceof StandardLogisticsInterfaceEntity SLIEntity) {
                WheatSync.sliCache.setSLILoadingStatus(SLIEntity.getBLOCK_PLACER(), SLIEntity.getCommunicationID(), false);
            }
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
            //如果数据库内存在此容器
            if (WheatSync.databaseHelper.ifSLIExists(entity.getBLOCK_PLACER(), newID)) {
                WheatSync.databaseHelper.getSLIToCache(entity.getBLOCK_PLACER(), newID);
                return true;
            }
            //如果数据库内不存在
            WheatSync.sliCache.addOrUpdateSLICache(entity.getBLOCK_PLACER(), newID, entity.getInventory(), false);
            WheatSync.databaseHelper.createSLIRecord(entity.getBLOCK_PLACER(), newID, SLICache.serializeInventory(entity.getInventory()));
            return false;
        }, WheatSync.asyncExecutor).thenAccept((result) -> {
            //将其他服务器存在的容器从缓存写入物品栏
            if (result) {
                server.executeSync(() -> {
                    entity.setInventory(WheatSync.sliCache.getInventoryOf(entity.getBLOCK_PLACER(), newID));
                });
            }
            server.executeSync(() -> {
                entity.setCommunicationID(newID);
            });
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
                    WheatSync.sliCache.removeSLI(entity.getBLOCK_PLACER(), entity.getCommunicationID());
                    if (!result) ItemScatterer.spawn(world, pos, entity);
                    block.superOnStateReplaced(state, world, pos, newState, moved);
                    world.updateComparators(pos, block);
                });
            }
            //也许需要补充客户端代码，待测试
        });
    }
}
