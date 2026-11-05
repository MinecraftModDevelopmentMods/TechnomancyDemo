package zone.moddev.mc.technomancydemo;

import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.core.BlockPos;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.RandomSource;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.block.state.properties.BooleanProperty;
import net.minecraft.world.phys.BlockHitResult;
import zone.moddev.mc.skysleylines.api.LeyLinesApi;

final class LampBlock extends Block {
    private static final MapCodec<LampBlock> CODEC = RecordCodecBuilder.mapCodec(instance -> instance.group(
            propertiesCodec(),
            ResourceLocation.CODEC.fieldOf("currency").forGetter(block -> block.currency),
            Codec.LONG.fieldOf("pulse_cost").forGetter(block -> block.pulseCost)
    ).apply(instance, LampBlock::new));
    static final BooleanProperty LIT = BlockStateProperties.LIT;
    private static final int PULSE_TICKS = 100;
    private final ResourceLocation currency;
    private final long pulseCost;

    LampBlock(Properties properties, ResourceLocation currency, long pulseCost) {
        super(properties);
        this.currency = currency;
        this.pulseCost = pulseCost;
        registerDefaultState(stateDefinition.any().setValue(LIT, false));
    }

    @Override protected MapCodec<LampBlock> codec() { return CODEC; }

    @Override protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) {
        builder.add(LIT);
    }

    @Override protected InteractionResult useWithoutItem(BlockState state, Level level, BlockPos pos,
                                                         Player player, BlockHitResult hit) {
        if (!player.getMainHandItem().isEmpty()) return InteractionResult.PASS;
        if (level instanceof ServerLevel server) {
            // A scheduled tick can survive breaking and replacing a lamp at this position.
            if (state.getValue(LIT) || server.getBlockTicks().hasScheduledTick(pos, this)) {
                TechnomancyDemo.message(player, "lamp_busy");
                return InteractionResult.CONSUME;
            }
            var power = LeyLinesApi.forLevel(server).power();
            var result = power.spendAt(pos.getX(), pos.getY(), pos.getZ(), currency,
                    pulseCost, TechnomancyDemo.consumer(this, player));
            if (result.succeeded()) {
                if (server.getBlockState(pos).is(this)) {
                    server.scheduleTick(pos, this, PULSE_TICKS);
                    server.setBlock(pos, state.setValue(LIT, true), Block.UPDATE_ALL);
                }
                long balance = result.receipt().orElseThrow().after().currency(currency);
                TechnomancyDemo.message(player, "lamp_lit", pulseCost, TechnomancyDemo.currencyName(currency), balance);
            } else {
                TechnomancyDemo.failure(player, result.status(), TechnomancyDemo.currencyName(currency), pulseCost);
            }
        }
        return InteractionResult.sidedSuccess(level.isClientSide);
    }

    @Override protected void tick(BlockState state, ServerLevel level, BlockPos pos, RandomSource random) {
        if (state.getValue(LIT)) level.setBlock(pos, state.setValue(LIT, false), Block.UPDATE_ALL);
    }
}
