package top.rookiestwo.wheatsync.screen;

import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.inventory.Inventory;
import net.minecraft.inventory.SimpleInventory;
import net.minecraft.item.ItemStack;
import net.minecraft.network.PacketByteBuf;
import net.minecraft.screen.ScreenHandler;
import net.minecraft.screen.slot.Slot;
import net.minecraft.server.network.ServerPlayerEntity;
import top.rookiestwo.wheatsync.WheatSync;
import top.rookiestwo.wheatsync.WheatSyncRegistry;
import top.rookiestwo.wheatsync.block.entity.StandardLogisticsInterfaceEntity;
import top.rookiestwo.wheatsync.database.SLICache;

import java.util.UUID;


//SLI is short of StandardLogisticsInterface.
public class SLIScreenHandler extends ScreenHandler {

    public static final int SLOT_COUNT = 27;
    private final Inventory inventory;

    private int communicationID;
    private UUID placerUUID;
    private String placerID;
    private StandardLogisticsInterfaceEntity SLIEntity;

    public SLIScreenHandler(int syncId, PlayerInventory playerInventory, PacketByteBuf buf) {
        this(syncId, playerInventory, new SimpleInventory(SLOT_COUNT));
        communicationID = buf.readInt();
        placerUUID = buf.readUuid();
        placerID = buf.readString();
    }

    public SLIScreenHandler(int syncId, PlayerInventory playerInventory, Inventory inventory) {
        this(syncId, playerInventory, new SimpleInventory(SLOT_COUNT), null);
    }

    public SLIScreenHandler(int syncId, PlayerInventory playerInventory, Inventory inventory, StandardLogisticsInterfaceEntity SLIEntity) {
        super(WheatSyncRegistry.SLI_SCREEN_HANDLER, syncId);
        checkSize(inventory, SLOT_COUNT);

        this.SLIEntity = SLIEntity;
        this.inventory = inventory;

        inventory.onOpen(playerInventory.player);
        int m;//height
        int l;//width
        //SLI inventory
        for (m = 0; m < 3; ++m) {
            for (l = 0; l < 9; ++l) {
                this.addSlot(new Slot(inventory, l + m * 9, 8 + l * 18, 18 + m * 18));
            }
        }
        //The player inventory
        for (m = 0; m < 3; ++m) {
            for (l = 0; l < 9; ++l) {
                this.addSlot(new Slot(playerInventory, l + m * 9 + 9, 8 + l * 18, 84 + m * 18));
            }
        }
        //The player Hotbar
        for (m = 0; m < 9; ++m) {
            this.addSlot(new Slot(playerInventory, m, 8 + m * 18, 142));
        }
        communicationID = 0;
        placerUUID = UUID.randomUUID();
        placerID = "default";
    }

    public boolean setNewCommunicationID(int newID) {
        if (newID == communicationID || newID == 0) return false;
        communicationID = newID;
        return true;
    }

    public int getCommunicationID() {
        return communicationID;
    }

    public UUID getPlacerUUID() {
        return placerUUID;
    }

    public String getPlacerID() {
        return placerID;
    }

    public StandardLogisticsInterfaceEntity getSLIEntity() {
        return SLIEntity;
    }

    @Override
    public void onClosed(PlayerEntity player) {
        if (player instanceof ServerPlayerEntity) {
            if (SLIEntity != null) {
                if (SLIEntity.ifInventoryChanged()) {
                    WheatSync.sliCache.updateSLIInventory(SLIEntity.getBLOCK_PLACER(), SLIEntity.getCommunicationID(), SLICache.serializeInventory(SLIEntity.getInventory()));
                }
                if (SLIEntity.getCommunicationID() != 0) SLIEntity.clear();
            }
        }
        super.onClosed(player);
    }

    @Override
    public boolean canUse(PlayerEntity player) {
        return true;
    }

    @Override
    public ItemStack quickMove(PlayerEntity player, int invSlot) {
        ItemStack newStack = ItemStack.EMPTY;
        Slot slot = this.slots.get(invSlot);
        if (slot != null && slot.hasStack()) {
            ItemStack originalStack = slot.getStack();
            newStack = originalStack.copy();
            if (invSlot < this.inventory.size()) {
                if (!this.insertItem(originalStack, this.inventory.size(), this.slots.size(), true)) {
                    return ItemStack.EMPTY;
                }
            } else if (!this.insertItem(originalStack, 0, this.inventory.size(), false)) {
                return ItemStack.EMPTY;
            }
            if (originalStack.isEmpty()) {
                slot.setStack(ItemStack.EMPTY);
            } else {
                slot.markDirty();
            }
        }
        return newStack;
    }
}
