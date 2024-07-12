package top.rookiestwo.wheatsync;

import net.fabricmc.fabric.api.item.v1.FabricItemSettings;
import net.fabricmc.fabric.api.object.builder.v1.block.FabricBlockSettings;
import net.fabricmc.fabric.api.object.builder.v1.block.entity.FabricBlockEntityTypeBuilder;
import net.minecraft.block.Block;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.block.entity.BlockEntityType;
import net.minecraft.item.BlockItem;
import net.minecraft.item.Item;
import net.minecraft.registry.Registries;
import net.minecraft.registry.Registry;
import net.minecraft.util.Identifier;
import top.rookiestwo.wheatsync.block.StandardLogisticsInterface;
import top.rookiestwo.wheatsync.block.entity.StandardLogisticsInterfaceEntity;

public class WheatServerSyncRegistry {
    public static final Block STANDARD_LOGISTICS_INTERFACE_BLOCK = new Block(FabricBlockSettings.create().strength(4.0f));

    //Do all registering here
    public static void Register(){
        Registry.register(Registries.BLOCK, new Identifier("wheatsync", "standard_logistics_interface_block"), STANDARD_LOGISTICS_INTERFACE_BLOCK);
        Registry.register(Registries.ITEM, new Identifier("wheatsync", "standard_logistics_interface_block"), new BlockItem(STANDARD_LOGISTICS_INTERFACE_BLOCK,new Item.Settings()));
    }
}
