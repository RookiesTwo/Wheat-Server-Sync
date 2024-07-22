package top.rookiestwo.wheatsync.mixin;

import com.llamalad7.mixinextras.sugar.Local;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.world.WorldSaveHandler;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.ModifyVariable;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import top.rookiestwo.wheatsync.WheatSync;
import top.rookiestwo.wheatsync.events.AsyncAndEvents;

@Mixin(WorldSaveHandler.class)
public class WorldSaveHandlerMixin {

    @ModifyVariable(
            method = "loadPlayerData(Lnet/minecraft/entity/player/PlayerEntity;)Lnet/minecraft/nbt/NbtCompound;",
            at = @At(
                    value = "INVOKE",
                    target = "Lnet/minecraft/entity/player/PlayerEntity;readNbt(Lnet/minecraft/nbt/NbtCompound;)V",
                    shift = At.Shift.BEFORE
            ),
            ordinal = 0
    )
    private NbtCompound modifyNbtCompound$WheatSync(NbtCompound nbt) throws CommandSyntaxException {
        if (!WheatSync.CONFIG.ifEnable) return nbt;
        AsyncAndEvents.onPlayerDataLoad(nbt);
        return nbt;
    }

    @Inject(
            method = "savePlayerData(Lnet/minecraft/entity/player/PlayerEntity;)V",
            at = @At(
                    value = "INVOKE",
                    target = "Ljava/io/File;createTempFile(Ljava/lang/String;Ljava/lang/String;Ljava/io/File;)Ljava/io/File;",
                    shift = At.Shift.BEFORE
            )
    )
    private void onWriteNbt$WheatSync(PlayerEntity player, CallbackInfo ci, @Local NbtCompound nbt) {
        if (!WheatSync.CONFIG.ifEnable) return;
        AsyncAndEvents.onPlayerDataSave(nbt);
    }

}
