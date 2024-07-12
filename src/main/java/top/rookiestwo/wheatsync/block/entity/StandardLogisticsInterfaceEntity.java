package top.rookiestwo.wheatsync.block.entity;

import net.minecraft.block.BlockState;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.block.entity.BlockEntityType;
import net.minecraft.util.math.BlockPos;

public class StandardLogisticsInterfaceEntity extends BlockEntity {

    public StandardLogisticsInterfaceEntity(BlockEntityType<?> type,BlockPos pos, BlockState state) {
        super(type, pos, state);
    }
}
