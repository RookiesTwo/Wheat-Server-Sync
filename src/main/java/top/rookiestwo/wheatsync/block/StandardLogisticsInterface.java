package top.rookiestwo.wheatsync.block;

import net.minecraft.block.BlockEntityProvider;
import net.minecraft.block.BlockRenderType;
import net.minecraft.block.BlockState;
import net.minecraft.block.BlockWithEntity;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.screen.NamedScreenHandlerFactory;
import net.minecraft.screen.ScreenHandler;
import net.minecraft.util.ActionResult;
import net.minecraft.util.Hand;
import net.minecraft.util.ItemScatterer;
import net.minecraft.util.hit.BlockHitResult;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import org.jetbrains.annotations.Nullable;
import top.rookiestwo.wheatsync.WheatServerSync;
import top.rookiestwo.wheatsync.block.entity.StandardLogisticsInterfaceEntity;

public class StandardLogisticsInterface extends BlockWithEntity implements BlockEntityProvider {

    private LivingEntity blockPlacer = null;

    public StandardLogisticsInterface(Settings settings) {
        super(settings);
    }

    @Override
    public BlockEntity createBlockEntity(BlockPos pos, BlockState state) {
        return new StandardLogisticsInterfaceEntity(pos,state,blockPlacer);
    }

    @Override
    public BlockRenderType getRenderType(BlockState state) {
        return BlockRenderType.MODEL;
    }

    @Override
    public ActionResult onUse(BlockState state, World world, BlockPos pos, PlayerEntity player, Hand hand, BlockHitResult hit) {
        if (!world.isClient) {
            // 这里会调用 BlockWithEntity 的 createScreenHandlerFactory 方法，会将返回的方块实体强转为
            // 一个 namedScreenHandlerFactory。如果你的方块没有继承 BlockWithEntity，那就需要单独实现 createScreenHandlerFactory。
            NamedScreenHandlerFactory screenHandlerFactory = state.createScreenHandlerFactory(world, pos);
            WheatServerSync.LOGGER.info("SLI Used!");
            if (screenHandlerFactory != null) {
                // 这个调用会让服务器请求客户端开启合适的 Screenhandler
                player.openHandledScreen(screenHandlerFactory);
            }
        }
        return ActionResult.SUCCESS;
    }

    // 这个方法能让方块破坏时物品全部掉落
    @Override
    public void onStateReplaced(BlockState state, World world, BlockPos pos, BlockState newState, boolean moved) {
        if (state.getBlock() != newState.getBlock()) {
            BlockEntity blockEntity = world.getBlockEntity(pos);
            if (blockEntity instanceof StandardLogisticsInterfaceEntity) {
                ItemScatterer.spawn(world, pos, (StandardLogisticsInterfaceEntity) blockEntity);
                // 更新比较器
                world.updateComparators(pos, this);
            }
            super.onStateReplaced(state, world, pos, newState, moved);
        }
    }

    @Override
    public boolean hasComparatorOutput(BlockState state) {
        return true;
    }

    @Override
    public int getComparatorOutput(BlockState state, World world, BlockPos pos) {
        return ScreenHandler.calculateComparatorOutput(world.getBlockEntity(pos));
    }

    @Override
    public void onPlaced(World world, BlockPos pos, BlockState state, @Nullable LivingEntity placer, ItemStack itemStack) {
        WheatServerSync.LOGGER.info("SLI Placed!");
        blockPlacer=placer;
    }

}
