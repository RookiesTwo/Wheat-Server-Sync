package top.rookiestwo.wheatsync.block;

import net.minecraft.block.Block;
import net.minecraft.block.BlockEntityProvider;
import net.minecraft.block.BlockState;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.block.entity.BlockEntityType;
import net.minecraft.entity.LivingEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import org.jetbrains.annotations.Nullable;
import top.rookiestwo.wheatsync.WheatServerSync;
import top.rookiestwo.wheatsync.block.entity.StandardLogisticsInterfaceEntity;

public class StandardLogisticsInterface extends Block implements BlockEntityProvider {

    private LivingEntity blockPlacer = null;

    public StandardLogisticsInterface(Settings settings) {
        super(settings);
    }

    @Override
    public BlockEntity createBlockEntity(BlockPos pos, BlockState state) {
        return new StandardLogisticsInterfaceEntity(pos,state,blockPlacer);
    }

    @Override
    public void onPlaced(World world, BlockPos pos, BlockState state, @Nullable LivingEntity placer, ItemStack itemStack) {
        blockPlacer=placer;
    }

}
