package top.rookiestwo.wheatsync.events;

import net.fabricmc.fabric.api.event.lifecycle.v1.ServerBlockEntityEvents;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.server.world.ServerWorld;
import top.rookiestwo.wheatsync.WheatSync;
import top.rookiestwo.wheatsync.block.entity.StandardLogisticsInterfaceEntity;

public class ChunkIOListener {
    public static void register() {
        ServerBlockEntityEvents.BLOCK_ENTITY_LOAD.register((BlockEntity entity, ServerWorld world) -> {
            if (entity instanceof StandardLogisticsInterfaceEntity) {
                if (((StandardLogisticsInterfaceEntity) entity).getCommunicationID() != 0) {
                    ((StandardLogisticsInterfaceEntity) entity).copyInventoryToSnapshot();
                }
                WheatSync.sliCache.addSLICache((StandardLogisticsInterfaceEntity) entity);
            }
        });

        ServerBlockEntityEvents.BLOCK_ENTITY_UNLOAD.register((BlockEntity entity, ServerWorld world) -> {
            if (entity instanceof StandardLogisticsInterfaceEntity) {
                WheatSync.sliCache.removeSLICache((StandardLogisticsInterfaceEntity) entity);
            }
        });


    }
}
