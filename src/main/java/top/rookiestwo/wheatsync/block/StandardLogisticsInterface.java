package top.rookiestwo.wheatsync.block;

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
import net.minecraft.util.hit.BlockHitResult;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import org.jetbrains.annotations.Nullable;
import top.rookiestwo.wheatsync.WheatSync;
import top.rookiestwo.wheatsync.block.entity.StandardLogisticsInterfaceEntity;
import top.rookiestwo.wheatsync.events.AsyncAndEvents;

public class StandardLogisticsInterface extends BlockWithEntity {

    public StandardLogisticsInterface(Settings settings) {
        super(settings);
    }

    @Override
    public BlockEntity createBlockEntity(BlockPos pos, BlockState state) {
        return new StandardLogisticsInterfaceEntity(pos, state, null);
    }

    @Override
    public BlockRenderType getRenderType(BlockState state) {
        return BlockRenderType.MODEL;
    }

    @Override
    public ActionResult onUse(BlockState state, World world, BlockPos pos, PlayerEntity player, Hand hand, BlockHitResult hit) {
        if (!world.isClient) {
            if (world.getBlockEntity(pos) instanceof StandardLogisticsInterfaceEntity SLIEntity) {
                SLIEntity.setInventory(WheatSync.sliCache.getInventoryOf(SLIEntity.getBLOCK_PLACER(), SLIEntity.getCommunicationID()));
                SLIEntity.copyInventoryToSnapshot();
                NamedScreenHandlerFactory screenHandlerFactory = state.createScreenHandlerFactory(world, pos);
                if (screenHandlerFactory != null) {
                    player.openHandledScreen(screenHandlerFactory);
                }
            }
        }
        return ActionResult.SUCCESS;
    }

    @Override
    public void onStateReplaced(BlockState state, World world, BlockPos pos, BlockState newState, boolean moved) {
        if (state.getBlock() != newState.getBlock()) {
            BlockEntity blockEntity = world.getBlockEntity(pos);
            if (blockEntity instanceof StandardLogisticsInterfaceEntity SLIEntity) {
                AsyncAndEvents.onSLIBlockDestroyed(state, world, pos, newState, moved, SLIEntity, this);
            }
        }
    }

    public void superOnStateReplaced(BlockState state, World world, BlockPos pos, BlockState newState, boolean moved) {
        super.onStateReplaced(state, world, pos, newState, moved);
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
        if (!world.isClient) {
            BlockEntity entity = world.getBlockEntity(pos);
            if (entity instanceof StandardLogisticsInterfaceEntity SLIEntity) {
                if (placer instanceof PlayerEntity player) {
                    SLIEntity.setBLOCK_PLACER(player.getUuid());
                    SLIEntity.setBLOCK_PLACER_ID(player.getName().getString());
                }
            }
        }
    }

}
