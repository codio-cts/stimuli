package xyz.nucleoid.stimuli.mixin.player;

import com.llamalad7.mixinextras.sugar.Local;
import net.minecraft.entity.Entity;
import net.minecraft.network.packet.c2s.play.ClickWindowC2SPacket;
import net.minecraft.network.packet.c2s.play.HandSwingC2SPacket;
import net.minecraft.network.packet.c2s.play.PlayerActionC2SPacket;
import net.minecraft.network.packet.c2s.play.SpectatorTeleportC2SPacket;
import net.minecraft.server.network.ServerPlayNetworkHandler;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.ActionResult;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import xyz.nucleoid.stimuli.Stimuli;
import xyz.nucleoid.stimuli.event.player.*;

@Mixin(ServerPlayNetworkHandler.class)
public class ServerPlayNetworkHandlerMixin {
    @Shadow
    public ServerPlayerEntity player;

    @Inject(method = "onHandSwing", at = @At("HEAD"))
    private void onHandSwing(HandSwingC2SPacket packet, CallbackInfo ci) {
        var hand = packet.getHand();
        try (var invokers = Stimuli.select().forEntity(this.player)) {
            invokers.get(PlayerSwingHandEvent.EVENT).onSwingHand(this.player, hand);
        }
    }

    @Inject(method = "onClickWindow", at = @At(value = "INVOKE", target = "Lnet/minecraft/screen/ScreenHandler;onSlotClick(IILnet/minecraft/screen/slot/SlotActionType;Lnet/minecraft/entity/player/PlayerEntity;)Lnet/minecraft/item/ItemStack;"), cancellable = true)
    private void onInventoryAction(ClickWindowC2SPacket packet, CallbackInfo ci) {
        try (var invokers = Stimuli.select().forEntity(this.player)) {
            var result = invokers.get(PlayerInventoryActionEvent.EVENT).onInventoryAction(this.player, packet.getSlot(), packet.getActionType(), packet.getClickData());
            if (result == ActionResult.FAIL) {
                ci.cancel();
            }
        }
    }

    @Inject(method = "onPlayerAction", at = @At(value = "INVOKE", target = "Lnet/minecraft/server/network/ServerPlayerEntity;getStackInHand(Lnet/minecraft/util/Hand;)Lnet/minecraft/item/ItemStack;", ordinal = 0, shift = At.Shift.BEFORE), cancellable = true)
    private void onSwapWithOffhand(PlayerActionC2SPacket packet, CallbackInfo ci) {
        try (var invokers = Stimuli.select().forEntity(this.player)) {
            var result = invokers.get(PlayerSwapWithOffhandEvent.EVENT).onSwapWithOffhand(this.player);
            if (result == ActionResult.FAIL) {
                ci.cancel();
            }
        }
    }

    @Inject(method = "executeCommand", at = @At("HEAD"), cancellable = true)
    private void onCommandExecution(String input, CallbackInfo ci) {
        try (var invokers = Stimuli.select().forEntity(this.player)) {
            var result = invokers.get(PlayerCommandEvent.EVENT).onPlayerCommand(this.player, input);
            if (result == ActionResult.FAIL) {
                ci.cancel();
            }
        }
    }

    @Inject(method = "onSpectatorTeleport", at = @At(value = "INVOKE", target = "Lnet/minecraft/server/network/ServerPlayerEntity;teleport(Lnet/minecraft/server/world/ServerWorld;DDDFF)V"), cancellable = true)
    private void onSpectatorTeleport(SpectatorTeleportC2SPacket packet, CallbackInfo ci, @Local ServerWorld serverWorld, @Local Entity entity) {
        try (var invokers = Stimuli.select().forEntity(player)) {
            var result = invokers.get(PlayerSpectateEntityEvent.EVENT).onSpectateEntity(player, entity);
            if (result == ActionResult.FAIL) {
                ci.cancel();
            }
        }
    }

}
