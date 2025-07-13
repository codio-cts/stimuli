package xyz.nucleoid.stimuli.mixin.player;

import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.screen.ScreenHandler;
import net.minecraft.screen.slot.Slot;
import net.minecraft.screen.slot.SlotActionType;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.util.ActionResult;
import net.minecraft.util.collection.DefaultedList;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import xyz.nucleoid.stimuli.Stimuli;
import xyz.nucleoid.stimuli.event.item.ItemThrowEvent;

import java.util.List;

@Mixin(ScreenHandler.class)
public class ScreenHandlerMixin {
    @Shadow @Final public List<Slot> slots;

    @Inject(method = "method_30010", at = @At("HEAD"), cancellable = true)
    private void onSlotAction(int slot, int button, SlotActionType type, PlayerEntity player, CallbackInfoReturnable<ItemStack> cir) {
        if (player.getEntityWorld().isClient) {
            return;
        }

        if (type == SlotActionType.THROW || type == SlotActionType.PICKUP) {
            ItemStack stack = null;
            if (type == SlotActionType.PICKUP && slot == -999) {
                stack = player.inventory.getCursorStack();
            } else if (type == SlotActionType.THROW && slot >= 0 && slot < this.slots.size()) {
                stack = this.slots.get(slot).getStack();
            }

            if (stack != null) {
                if (this.shouldBlockThrowingItems(player, slot, stack)) {
                    player.inventory.setCursorStack(stack);
                    //ci.cancel();
                    cir.setReturnValue(stack);
                }
            }
        }
    }

    @Inject(method = "close", at = @At("HEAD"))
    private void onClosed(PlayerEntity player, CallbackInfo ci) {
        var cursor = player.inventory.getCursorStack();
        if (cursor.isEmpty()) {
            return;
        }

        if (this.shouldBlockThrowingItems(player, -999, cursor)) {
            if (player.inventory.insertStack(cursor)) {
                player.inventory.setCursorStack(ItemStack.EMPTY);
            }
        }
    }

    @Unique
    private boolean shouldBlockThrowingItems(PlayerEntity player, int slot, ItemStack stack) {
        if (player instanceof ServerPlayerEntity serverPlayer) {
            try (var invokers = Stimuli.select().forEntity(player)) {
                return invokers.get(ItemThrowEvent.EVENT)
                        .onThrowItem(serverPlayer, slot, stack) == ActionResult.FAIL;
            }
        }

        return false;
    }
}
