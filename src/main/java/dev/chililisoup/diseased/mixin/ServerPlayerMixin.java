package dev.chililisoup.diseased.mixin;

import dev.chililisoup.diseased.item.SlotBlocker;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.level.GameRules;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(ServerPlayer.class)
public abstract class ServerPlayerMixin {
    @Inject(method = "restoreFrom", at = @At(value = "INVOKE", target = "Lnet/minecraft/server/level/ServerPlayer;setHealth(F)V", ordinal = 1))
    private void restoreBlockers(ServerPlayer oldPlayer, boolean bl, CallbackInfo ci) {
        ServerPlayer thisPlayer = (ServerPlayer) (Object) this;
        if (thisPlayer.level().getGameRules().getBoolean(GameRules.RULE_KEEPINVENTORY)) return;

        Inventory inventory = oldPlayer.getInventory();
        inventory.clearOrCountMatchingItems(
                itemStack -> !(itemStack.getItem() instanceof SlotBlocker),
                -1,
                oldPlayer.inventoryMenu.getCraftSlots()
        );

        thisPlayer.getInventory().replaceWith(inventory);
    }
}
