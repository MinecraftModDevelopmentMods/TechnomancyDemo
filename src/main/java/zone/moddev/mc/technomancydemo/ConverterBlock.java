package zone.moddev.mc.technomancydemo;

import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.core.BlockPos;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.BlockHitResult;
import zone.moddev.mc.skysleylines.api.LeyLinesApi;
import zone.moddev.mc.skysleylines.api.MagicAmount;

final class ConverterBlock extends Block {
    private static final MapCodec<ConverterBlock> CODEC = RecordCodecBuilder.mapCodec(instance -> instance.group(
            propertiesCodec(),
            ResourceLocation.CODEC.fieldOf("currency").forGetter(block -> block.currency)
    ).apply(instance, ConverterBlock::new));
    private static final MagicAmount RAW_PER_CONVERSION = MagicAmount.of(5);

    private final ResourceLocation currency;

    ConverterBlock(Properties properties, ResourceLocation currency) {
        super(properties);
        this.currency = currency;
    }

    @Override protected MapCodec<ConverterBlock> codec() { return CODEC; }

    @Override protected InteractionResult useWithoutItem(BlockState state, Level level, BlockPos pos,
                                                         Player player, BlockHitResult hit) {
        if (!player.getMainHandItem().isEmpty()) return InteractionResult.PASS;
        if (level instanceof ServerLevel server) {
            var power = LeyLinesApi.forLevel(server).power();
            var result = power.convertAt(pos.getX(), pos.getY(), pos.getZ(), currency,
                    RAW_PER_CONVERSION, TechnomancyDemo.consumer(this, player));
            if (result.succeeded()) {
                var receipt = result.receipt().orElseThrow();
                long balance = receipt.after().currency(currency);
                long credited = balance - receipt.before().currency(currency);
                TechnomancyDemo.message(player, "converted", RAW_PER_CONVERSION.toString(), credited, TechnomancyDemo.currencyName(currency), balance);
            } else {
                TechnomancyDemo.failure(player, result.status(), TechnomancyDemo.currencyName(currency), RAW_PER_CONVERSION.toString());
            }
        }
        return InteractionResult.sidedSuccess(level.isClientSide);
    }
}
