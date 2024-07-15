package top.rookiestwo.wheatsync.block.entity;

import net.fabricmc.fabric.api.screenhandler.v1.ExtendedScreenHandlerFactory;
import net.minecraft.block.BlockState;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.inventory.Inventories;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.network.PacketByteBuf;
import net.minecraft.screen.ScreenHandler;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.text.Text;
import net.minecraft.util.collection.DefaultedList;
import net.minecraft.util.math.BlockPos;
import org.jetbrains.annotations.Nullable;
import top.rookiestwo.wheatsync.WheatSyncRegistry;
import top.rookiestwo.wheatsync.api.LogisticsInterfaceInventory;
import top.rookiestwo.wheatsync.screen.SLIScreenHandler;

import java.util.UUID;

public class StandardLogisticsInterfaceEntity extends BlockEntity implements ExtendedScreenHandlerFactory, LogisticsInterfaceInventory {

    private final DefaultedList<ItemStack> inventory = DefaultedList.ofSize(5, ItemStack.EMPTY);
    private final short inventorySize = 5;

    private int CommunicationID = 0;
    private UUID BLOCK_PLACER;
    private String BLOCK_PLACER_ID;

    public StandardLogisticsInterfaceEntity(BlockPos pos, BlockState state, LivingEntity placer) {
        super(WheatSyncRegistry.STANDARD_LOGISTICS_INTERFACE_BLOCK_ENTITY, pos, state);

        if (placer instanceof PlayerEntity) {
            BLOCK_PLACER = placer.getUuid();
            BLOCK_PLACER_ID = placer.getName().getString();
        } else BLOCK_PLACER = null;
    }

    public StandardLogisticsInterfaceEntity(BlockPos pos, BlockState state) {
        super(WheatSyncRegistry.STANDARD_LOGISTICS_INTERFACE_BLOCK_ENTITY, pos, state);
        BLOCK_PLACER = null;
    }

    @Nullable
    @Override
    public ScreenHandler createMenu(int syncId, PlayerInventory playerInventory, PlayerEntity player) {
        // 因为我们的类实现 Inventory，所以将*这个*提供给 ScreenHandler
        // 一开始只有服务器拥有物品栏，然后在 ScreenHandler 中同步给客户端
        return new SLIScreenHandler(syncId, playerInventory, this);
    }

    @Override
    public Text getDisplayName() {
        if (CommunicationID == 0) {
            return Text.translatable("block.wheatsync.unnamed_standard_logistics_interface");
        }
        return Text.of(Text.translatable(getCachedState().getBlock().getTranslationKey()).getString() + CommunicationID);
    }

    @Override
    public DefaultedList<ItemStack> getItems() {
        return inventory;
    }

    @Override
    public void readNbt(NbtCompound nbt) {
        super.readNbt(nbt);

        if (nbt.containsUuid("PlacerUUID")) {
            BLOCK_PLACER = nbt.getUuid("PlacerUUID");
            BLOCK_PLACER_ID = nbt.getString("PlacerID");
        }
        CommunicationID = nbt.getInt("CommunicationID");
        Inventories.readNbt(nbt, this.inventory);
    }

    @Override
    public void writeNbt(NbtCompound nbt) {
        if (BLOCK_PLACER != null) {
            nbt.putUuid("PlacerUUID", BLOCK_PLACER);
            nbt.putString("PlacerID", BLOCK_PLACER_ID);
        }
        nbt.putInt("CommunicationID", CommunicationID);
        Inventories.writeNbt(nbt, this.inventory);

        super.writeNbt(nbt);
    }

    @Override
    public void writeScreenOpeningData(ServerPlayerEntity player, PacketByteBuf buf) {
        buf.writeInt(CommunicationID);
        if (BLOCK_PLACER != null) {
            buf.writeUuid(BLOCK_PLACER);
            buf.writeString(BLOCK_PLACER_ID);
        } else {
            buf.writeUuid(UUID.randomUUID());
            buf.writeString("unknown player");
        }

    }
}
