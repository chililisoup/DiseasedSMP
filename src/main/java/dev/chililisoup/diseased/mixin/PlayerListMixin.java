package dev.chililisoup.diseased.mixin;

import dev.chililisoup.diseased.reg.ModItems;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.server.players.PlayerList;
import net.minecraft.util.ProblemReporter;
import net.minecraft.world.level.storage.ValueInput;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.util.Optional;

@Mixin(PlayerList.class)
public abstract class PlayerListMixin {
    @Inject(method = "load", at = @At("RETURN"))
    private void setConstantItems(ServerPlayer serverPlayer, ProblemReporter problemReporter, CallbackInfoReturnable<Optional<ValueInput>> cir) {
        serverPlayer.getInventory().setItem(39, ModItems.SLOT_BLOCKER.getDefaultInstance());
    }
}
