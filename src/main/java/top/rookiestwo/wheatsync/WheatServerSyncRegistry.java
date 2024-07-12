package top.rookiestwo.wheatsync;

import net.fabricmc.fabric.api.object.builder.v1.block.FabricBlockSettings;
import net.fabricmc.fabric.api.object.builder.v1.block.entity.FabricBlockEntityTypeBuilder;
import net.minecraft.block.Block;
import net.minecraft.block.entity.BlockEntityType;
import net.minecraft.item.BlockItem;
import net.minecraft.item.Item;
import net.minecraft.registry.Registries;
import net.minecraft.registry.Registry;
import net.minecraft.util.Identifier;
import top.rookiestwo.wheatsync.block.entity.StandardLogisticsInterfaceEntity;

public class WheatServerSyncRegistry {
    //blocks
    public static Block STANDARD_LOGISTICS_INTERFACE_BLOCK = new Block(FabricBlockSettings.create().strength(4.0f));
    private static final String MOD_ID =WheatServerSync.MOD_ID;

    //block entities
    public static BlockEntityType<StandardLogisticsInterfaceEntity> STANDARD_LOGISTICS_INTERFACE_BLOCK_ENTITY;

    //Do all registering here
    public static void registerBlocks(){
        Registry.register(Registries.BLOCK, new Identifier(MOD_ID, "standard_logistics_interface_block"), STANDARD_LOGISTICS_INTERFACE_BLOCK);
        Registry.register(Registries.ITEM, new Identifier(MOD_ID, "standard_logistics_interface_block"), new BlockItem(STANDARD_LOGISTICS_INTERFACE_BLOCK,new Item.Settings()));
    }

    public static void registerBlockEntities(){
        STANDARD_LOGISTICS_INTERFACE_BLOCK_ENTITY = Registry.register(
                Registries.BLOCK_ENTITY_TYPE,
                new Identifier(MOD_ID,"standard_logistics_interface_block_entity"),
                FabricBlockEntityTypeBuilder.create(StandardLogisticsInterfaceEntity::new,STANDARD_LOGISTICS_INTERFACE_BLOCK).build()
        );
    }

}
