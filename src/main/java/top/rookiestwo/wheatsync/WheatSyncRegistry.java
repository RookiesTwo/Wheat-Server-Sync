package top.rookiestwo.wheatsync;

import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import net.fabricmc.fabric.api.object.builder.v1.block.FabricBlockSettings;
import net.fabricmc.fabric.api.object.builder.v1.block.entity.FabricBlockEntityTypeBuilder;
import net.fabricmc.fabric.api.screenhandler.v1.ScreenHandlerRegistry;
import net.minecraft.block.Block;
import net.minecraft.block.entity.BlockEntityType;
import net.minecraft.item.BlockItem;
import net.minecraft.item.Item;
import net.minecraft.registry.Registries;
import net.minecraft.registry.Registry;
import net.minecraft.screen.ScreenHandlerType;
import net.minecraft.util.Identifier;
import top.rookiestwo.wheatsync.block.StandardLogisticsInterface;
import top.rookiestwo.wheatsync.block.entity.StandardLogisticsInterfaceEntity;
import top.rookiestwo.wheatsync.database.DataBaseIOManager;
import top.rookiestwo.wheatsync.database.requests.ChangeCommunicationIDRequest;
import top.rookiestwo.wheatsync.database.requests.CreateSLIRequest;
import top.rookiestwo.wheatsync.screen.SLIScreenHandler;

public class WheatSyncRegistry {

    private static final String MOD_ID = WheatSync.MOD_ID;

    public static final Identifier SLI = new Identifier(MOD_ID, "standard_logistics_interface_block");
    //blocks
    public static Block STANDARD_LOGISTICS_INTERFACE_BLOCK;

    //block entities
    public static BlockEntityType<StandardLogisticsInterfaceEntity> STANDARD_LOGISTICS_INTERFACE_BLOCK_ENTITY;

    //screen handlers
    public static ScreenHandlerType<SLIScreenHandler> SLI_SCREEN_HANDLER;
    //block items
    public static BlockItem STANDARD_LOGISTICS_INTERFACE_BLOCK_ITEM;

    //register
    public static void registerBlocks(){
        STANDARD_LOGISTICS_INTERFACE_BLOCK = Registry.register(
                Registries.BLOCK,
                SLI,
                new StandardLogisticsInterface(FabricBlockSettings.copyOf(net.minecraft.block.Blocks.CHEST))
        );
        STANDARD_LOGISTICS_INTERFACE_BLOCK_ITEM = Registry.register(
                Registries.ITEM,
                SLI,
                new BlockItem(STANDARD_LOGISTICS_INTERFACE_BLOCK, new Item.Settings())
        );
    }

    public static void registerBlockEntities(){
        STANDARD_LOGISTICS_INTERFACE_BLOCK_ENTITY = Registry.register(
                Registries.BLOCK_ENTITY_TYPE,
                SLI,
                FabricBlockEntityTypeBuilder.create(StandardLogisticsInterfaceEntity::new, STANDARD_LOGISTICS_INTERFACE_BLOCK).build(null)
        );
    }

    public static void registerScreenHandler() {
        SLI_SCREEN_HANDLER = ScreenHandlerRegistry.registerExtended(SLI, SLIScreenHandler::new);
    }

    public static void registerServerPacketReceiver() {
        ServerPlayNetworking.registerGlobalReceiver(new Identifier(WheatSync.MOD_ID, "set_communication_id"), (server, player, handler, buf, responseSender) -> {
            int newID = buf.readInt();
            server.execute(() -> {
                SLIScreenHandler screenHandler = (SLIScreenHandler) player.currentScreenHandler;
                int oldID = screenHandler.getSLIEntity().getCommunicationID();
                screenHandler.getSLIEntity().setCommunicationID(newID);
                WheatSync.LOGGER.info("Communication ID set to " + newID);
                if (oldID == 0) {
                    DataBaseIOManager.addCreateSLIRequest(new CreateSLIRequest(screenHandler.getSLIEntity()));
                    WheatSync.sliCache.addSLICache(screenHandler.getSLIEntity());
                    screenHandler.getSLIEntity().markDirty();
                }
                if (oldID != 0) {
                    DataBaseIOManager.addChangeCommunicationIDRequest(new ChangeCommunicationIDRequest(screenHandler.getSLIEntity(), newID));
                    WheatSync.sliCache.updateCommunicationID(screenHandler.getSLIEntity(), newID);
                    screenHandler.getSLIEntity().markDirty();
                }
            });
        });
    }

    public static void registerAll() {
        registerBlocks();
        registerBlockEntities();
        registerScreenHandler();
    }
}
