package top.rookiestwo.wheatsync.events;

import net.fabricmc.fabric.api.event.lifecycle.v1.ServerBlockEntityEvents;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.server.world.ServerWorld;
import top.rookiestwo.wheatsync.WheatSync;
import top.rookiestwo.wheatsync.block.entity.StandardLogisticsInterfaceEntity;
import top.rookiestwo.wheatsync.database.SLIData;

public class WheatSyncChunkIOListener {
    public static void register() {
        ServerBlockEntityEvents.BLOCK_ENTITY_LOAD.register((BlockEntity entity, ServerWorld world) -> {
            if (entity instanceof StandardLogisticsInterfaceEntity) {
                WheatSync.sliCache.addSLI(new SLIData((StandardLogisticsInterfaceEntity) entity));
            }
        });
        ServerBlockEntityEvents.BLOCK_ENTITY_UNLOAD.register((BlockEntity entity, ServerWorld world) -> {
            if (entity instanceof StandardLogisticsInterfaceEntity) {
                WheatSync.sliCache.removeSLI(new SLIData((StandardLogisticsInterfaceEntity) entity));
            }
        });
    }
}
