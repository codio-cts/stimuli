package xyz.nucleoid.stimuli.mixin.player;

import net.minecraft.network.MessageType;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.PlayerManager;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.text.Text;
import net.minecraft.util.ActionResult;
import net.minecraft.util.Util;
import org.jetbrains.annotations.Nullable;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.ModifyArg;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import xyz.nucleoid.stimuli.Stimuli;
import xyz.nucleoid.stimuli.event.player.ChatEvent;
import xyz.nucleoid.stimuli.event.player.ReplaceChatEvent;

import java.util.UUID;

@Mixin(PlayerManager.class)
public abstract class PlayerManagerMixin {
    @Shadow public abstract MinecraftServer getServer();

    @Inject(method = "broadcastChatMessage", at = @At("HEAD"), cancellable = true)
    public void broadcastChatMessage(Text message, MessageType type, UUID senderId, CallbackInfo ci) {
        if (this.handleChatMessage(message, type, senderId)) {
            ci.cancel();
        }
    }

    @ModifyArg(method = "broadcastChatMessage", at = @At(value = "INVOKE", target = "Lnet/minecraft/network/packet/s2c/play/GameMessageS2CPacket;<init>(Lnet/minecraft/text/Text;Lnet/minecraft/network/MessageType;Ljava/util/UUID;)V"))
    public Text broadcastChatMessageToPlayers(Text message, MessageType type, UUID senderId) {
        try (var invokers = Stimuli.select().forGlobal(this.getServer())) {
            return invokers.get(ReplaceChatEvent.EVENT).replaceChatMessage(message, type, senderId);
        }
    }

    @Unique
    private boolean handleChatMessage(Text message, MessageType type, UUID senderId) {
        /*
        if (type != MessageType.CHAT || senderId == Util.NIL_UUID) {
            return false;
        }

        var sender = this.getPlayer(senderId);
        if (sender == null) {
            return false;
        }

         */

        try (var invokers = Stimuli.select().forGlobal(this.getServer())) {
            var result = invokers.get(ChatEvent.EVENT).onSendMessage(message, type, senderId);
            return result == ActionResult.FAIL;
        }
    }
}
