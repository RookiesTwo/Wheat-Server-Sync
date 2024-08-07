package top.rookiestwo.wheatsync.block.entity;

import net.fabricmc.fabric.api.screenhandler.v1.ExtendedScreenHandlerFactory;
import net.minecraft.block.BlockState;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.player.PlayerInventory;
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
import top.rookiestwo.wheatsync.database.SLICache;
import top.rookiestwo.wheatsync.screen.SLIScreenHandler;

import java.util.UUID;

public class StandardLogisticsInterfaceEntity extends BlockEntity implements ExtendedScreenHandlerFactory, LogisticsInterfaceInventory {

    public static final short inventorySize = 27;
    private DefaultedList<ItemStack> inventory = DefaultedList.ofSize(inventorySize, ItemStack.EMPTY);
    private DefaultedList<ItemStack> inventorySnapshot = DefaultedList.ofSize(inventorySize, ItemStack.EMPTY);


    private int CommunicationID = 0;
    private UUID BLOCK_PLACER;
    private String BLOCK_PLACER_ID;

    public StandardLogisticsInterfaceEntity(BlockPos pos, BlockState state, LivingEntity placer) {
        super(WheatSyncRegistry.STANDARD_LOGISTICS_INTERFACE_BLOCK_ENTITY, pos, state);

        if (placer instanceof PlayerEntity player) {
            BLOCK_PLACER = player.getUuid();
            BLOCK_PLACER_ID = player.getName().getString();
        } else BLOCK_PLACER = null;
    }

    public StandardLogisticsInterfaceEntity(BlockPos pos, BlockState state) {
        super(WheatSyncRegistry.STANDARD_LOGISTICS_INTERFACE_BLOCK_ENTITY, pos, state);
        BLOCK_PLACER = null;
    }

    public void setCommunicationID(int communicationID) {
        CommunicationID = communicationID;
        this.writeNbt(new NbtCompound());
    }

    public DefaultedList<ItemStack> getInventory() {
        return inventory;
    }

    public int getCommunicationID() {
        return CommunicationID;
    }

    public UUID getBLOCK_PLACER() {
        return BLOCK_PLACER;
    }

    public String getBLOCK_PLACER_ID() {
        return BLOCK_PLACER_ID;
    }

    public void setInventory(String inventory) {
        this.inventory = SLICache.unSerializeInventory(inventory);
        this.markDirty();
    }

    public void setBLOCK_PLACER(UUID BLOCK_PLACER) {
        this.BLOCK_PLACER = BLOCK_PLACER;
    }

    public void setBLOCK_PLACER_ID(String BLOCK_PLACER_ID) {
        this.BLOCK_PLACER_ID = BLOCK_PLACER_ID;
    }

    public boolean ifInventoryChanged() {
        if (getCommunicationID() == 0) return false;
        // 比较当前物品栏与快照
        for (int i = 0; i < inventory.size(); i++) {
            ItemStack currentStack = inventory.get(i);
            ItemStack snapshotStack = inventorySnapshot.get(i);
            if (!ItemStack.areEqual(currentStack, snapshotStack)) {
                copyInventoryToSnapshot();
                return true;
            }
        }
        return false;
    }

    public void copyInventoryToSnapshot() {
        for (int i = 0; i < inventory.size(); i++) {
            inventorySnapshot.set(i, inventory.get(i).copy());
        }
    }

    @Nullable
    @Override
    public ScreenHandler createMenu(int syncId, PlayerInventory playerInventory, PlayerEntity player) {
        return new SLIScreenHandler(syncId, playerInventory, this, this);
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
        //Inventories.readNbt(nbt, this.inventory);

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

    @Override
    public void writeNbt(NbtCompound nbt) {
        if (BLOCK_PLACER != null) {
            nbt.putUuid("PlacerUUID", BLOCK_PLACER);
            nbt.putString("PlacerID", BLOCK_PLACER_ID);
        }
        nbt.putInt("CommunicationID", CommunicationID);
        //Inventories.writeNbt(nbt, this.inventory);


        super.writeNbt(nbt);
    }


}
