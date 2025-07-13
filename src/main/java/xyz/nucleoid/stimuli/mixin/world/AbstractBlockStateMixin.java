package xyz.nucleoid.stimuli.mixin.world;

import com.llamalad7.mixinextras.injector.WrapWithCondition;
import net.minecraft.block.AbstractBlock;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.ActionResult;
import net.minecraft.util.math.BlockPos;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import xyz.nucleoid.stimuli.Stimuli;
import xyz.nucleoid.stimuli.event.block.BlockRandomTickEvent;

import java.util.Random;

@Mixin(AbstractBlock.AbstractBlockState.class)
public class AbstractBlockStateMixin {
    @WrapWithCondition(method = "randomTick", at = @At(value = "INVOKE", target = "Lnet/minecraft/block/Block;randomTick(Lnet/minecraft/block/BlockState;Lnet/minecraft/server/world/ServerWorld;Lnet/minecraft/util/math/BlockPos;Ljava/util/Random;)V"))
    private boolean applyBlockRandomTickEvent(Block block, BlockState state, ServerWorld world, BlockPos pos, Random random) {
        try (var invokers = Stimuli.select().at(world, pos)) {
            var result = invokers.get(BlockRandomTickEvent.EVENT).onBlockRandomTick(world, pos, state);
            if (result == ActionResult.FAIL) {
                return false;
            }
        }

        return true;
    }
}
