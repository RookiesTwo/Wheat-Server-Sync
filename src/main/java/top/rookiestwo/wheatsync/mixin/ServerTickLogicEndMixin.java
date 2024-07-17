package top.rookiestwo.wheatsync.mixin;

import net.minecraft.server.MinecraftServer;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import top.rookiestwo.wheatsync.events.TickEndEvent;

import java.sql.SQLException;

@Mixin(MinecraftServer.class)
public class ServerTickLogicEndMixin {

    @Inject(method = "runServer",
            at = @At(
                    value = "INVOKE",
                    target = "Lnet/minecraft/server/MinecraftServer;tick(Ljava/util/function/BooleanSupplier;)V",
                    shift = At.Shift.AFTER
            )
    )
    private void onRunTasksTillTickEnd$WheatSync(CallbackInfo ci) throws InterruptedException, SQLException {
        TickEndEvent.runAfterLogic();
    }
}