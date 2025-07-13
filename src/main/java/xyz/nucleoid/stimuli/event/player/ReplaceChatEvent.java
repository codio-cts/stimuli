package xyz.nucleoid.stimuli.event.player;

import net.minecraft.network.MessageType;
import net.minecraft.text.Text;
import xyz.nucleoid.stimuli.event.StimulusEvent;
import net.minecraft.util.Util;
import net.minecraft.server.PlayerManager;

import java.util.UUID;

/**
 * Called when a message gets sent in chat. This event can be used to modify the message before it gets sent to the players
 * Check {@link MessageType} to check whether it's a chat message or a system message
 * Additionally you can check if {@param senderId} has {@link Util#NIL_UUID} or {@link PlayerManager#getPlayer(UUID)}
 *
 * @see ChatEvent to cancel chat events
 */
public interface ReplaceChatEvent {
    StimulusEvent<ReplaceChatEvent> EVENT = StimulusEvent.create(ReplaceChatEvent.class, ctx -> (message, messageType, senderId) -> {
        try {
            for (var listener : ctx.getListeners()) {
                return listener.replaceChatMessage(message, messageType, senderId);
            }
        } catch (Throwable t) {
            ctx.handleException(t);
        }
        return message;
    });

    Text replaceChatMessage(Text message, MessageType messageType, UUID senderId);
}
