package xyz.nucleoid.stimuli.mixin.player;

import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.network.packet.s2c.play.ScreenHandlerSlotUpdateS2CPacket;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.util.ActionResult;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import xyz.nucleoid.stimuli.Stimuli;
import xyz.nucleoid.stimuli.event.item.ItemThrowEvent;
import xyz.nucleoid.stimuli.event.player.PlayerRegenerateEvent;

@Mixin(PlayerEntity.class)
public class PlayerEntityMixin {

    @Inject(method = "dropSelectedItem", at = @At("HEAD"), cancellable = true)
    private void dropSelectedItem(boolean dropEntireStack, CallbackInfoReturnable<Boolean> ci) {
        var player = (ServerPlayerEntity) (Object) this;
        if (player.world.isClient) {
            return;
        }

        int slot = player.inventory.selectedSlot;
        var stack = player.inventory.getStack(slot);

        try (var invokers = Stimuli.select().forEntity(player)) {
            var result = invokers.get(ItemThrowEvent.EVENT).onThrowItem(player, slot, stack);
            if (result == ActionResult.FAIL) {
                player.networkHandler.sendPacket(new ScreenHandlerSlotUpdateS2CPacket(-2, slot, stack));
                ci.setReturnValue(false);
            }
        }
    }

    @WrapOperation(method = "tickMovement", at = @At(value = "INVOKE", target = "Lnet/minecraft/entity/player/PlayerEntity;heal(F)V"))
    private void attemptPeacefulRegeneration(PlayerEntity player, float amount, Operation<Boolean> original) {
        if (!(player instanceof ServerPlayerEntity)) {
            original.call(player, amount);
            return;
        }

        try (var invokers = Stimuli.select().forEntity(player)) {
            var result = invokers.get(PlayerRegenerateEvent.EVENT)
                    .onRegenerate((ServerPlayerEntity) player, amount);

            if (result != ActionResult.FAIL) {
                original.call(player, amount);
            }
        }
    }
}
