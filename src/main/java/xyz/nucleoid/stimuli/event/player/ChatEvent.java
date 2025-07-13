package xyz.nucleoid.stimuli.event.player;

import net.minecraft.network.MessageType;
import net.minecraft.text.Text;
import net.minecraft.util.ActionResult;
import xyz.nucleoid.stimuli.event.StimulusEvent;
import net.minecraft.util.Util;
import net.minecraft.server.PlayerManager;

import java.util.UUID;

/**
 * Called when a message gets sent in chat. Message uses its final formatting
 * Check {@link MessageType} to check whether it's a chat message or a system message
 * Additionally you can check if {@param senderId} has {@link Util#NIL_UUID} or {@link PlayerManager#getPlayer(UUID)}
 *
 * <p>Upon return:
 * <ul>
 * <li>{@link ActionResult#SUCCESS} cancels further processing and allows the message to be sent.
 * <li>{@link ActionResult#FAIL} cancels further processing and the message being sent.
 * <li>{@link ActionResult#PASS} moves on to the next listener.
 * </ul>
 *
 * @see ReplaceChatEvent to modify a chat message
 */
public interface ChatEvent {
    StimulusEvent<ChatEvent> EVENT = StimulusEvent.create(ChatEvent.class, ctx -> (message, messageType, senderId) -> {
        try {
            for (var listener : ctx.getListeners()) {
                var result = listener.onSendMessage(message, messageType, senderId);
                if (result != ActionResult.PASS) {
                    return result;
                }
            }
        } catch (Throwable t) {
            ctx.handleException(t);
        }
        return ActionResult.PASS;
    });

    ActionResult onSendMessage(Text message, MessageType messageType, UUID senderId);
}
