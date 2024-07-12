package top.rookiestwo.wheatsync.block.entity;

import net.minecraft.block.BlockState;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.network.listener.ClientPlayPacketListener;
import net.minecraft.network.packet.Packet;
import net.minecraft.util.math.BlockPos;
import top.rookiestwo.wheatsync.WheatServerSyncRegistry;

import java.util.UUID;

public class StandardLogisticsInterfaceEntity extends BlockEntity {

    private String ContainerName="";
    private UUID BLOCK_PLACER;

    public StandardLogisticsInterfaceEntity(BlockPos pos, BlockState state, LivingEntity placer) {
        super(WheatServerSyncRegistry.STANDARD_LOGISTICS_INTERFACE_BLOCK_ENTITY, pos, state);

        if(placer instanceof PlayerEntity){
            BLOCK_PLACER=placer.getUuid();
        } else BLOCK_PLACER = null;
    }

    public StandardLogisticsInterfaceEntity(BlockPos pos, BlockState state) {
        super(WheatServerSyncRegistry.STANDARD_LOGISTICS_INTERFACE_BLOCK_ENTITY, pos, state);
        BLOCK_PLACER=null;
    }

    @Override
    public void writeNbt(NbtCompound nbt){
        if(BLOCK_PLACER!=null){
            nbt.putUuid("PlacerUUID",BLOCK_PLACER);
        }
        nbt.putString("ContainerName",ContainerName);

        super.writeNbt(nbt);
    }

    @Override
    public void readNbt(NbtCompound nbt){
        super.readNbt(nbt);

        if(nbt.containsUuid("PlacerUUID")){
            BLOCK_PLACER=nbt.getUuid("PlacerUUID");
        }
        ContainerName=nbt.getString("ContainerName");
    }
}
