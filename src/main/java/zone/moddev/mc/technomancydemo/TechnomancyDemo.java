package zone.moddev.mc.technomancydemo;

import java.util.List;
import java.util.Locale;
import java.util.Optional;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.CreativeModeTab;
import net.minecraft.world.item.CreativeModeTabs;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.SoundType;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.fml.common.Mod;
import net.neoforged.fml.event.lifecycle.FMLCommonSetupEvent;
import net.neoforged.neoforge.event.BuildCreativeModeTabContentsEvent;
import net.neoforged.neoforge.registries.DeferredBlock;
import net.neoforged.neoforge.registries.DeferredItem;
import net.neoforged.neoforge.registries.DeferredRegister;
import zone.moddev.mc.skysleylines.api.LeyLinesApi;
import zone.moddev.mc.skysleylines.api.MagicAmount;
import zone.moddev.mc.skysleylines.api.Paradigm;
import zone.moddev.mc.skysleylines.api.PowerRequest;
import zone.moddev.mc.skysleylines.api.PowerResult;

@Mod(TechnomancyDemo.MOD_ID)
public final class TechnomancyDemo {
    public static final String MOD_ID = "technomancy_demo";
    private static final ResourceLocation AETHER = id("aether");
    private static final ResourceLocation ELECTRIC = id("electric");

    private static final DeferredRegister.Blocks BLOCKS = DeferredRegister.createBlocks(MOD_ID);
    private static final DeferredRegister.Items ITEMS = DeferredRegister.createItems(MOD_ID);
    private static final DeferredRegister<CreativeModeTab> TABS = DeferredRegister.create(Registries.CREATIVE_MODE_TAB, MOD_ID);

    private static final DeferredBlock<ConverterBlock> AETHER_CONVERTER = BLOCKS.register("aether_converter",
            () -> new ConverterBlock(machineProperties(), AETHER));
    private static final DeferredBlock<LampBlock> AETHER_LAMP = BLOCKS.register("aether_lamp",
            () -> new LampBlock(lampProperties(), AETHER, 1000));
    private static final DeferredBlock<ConverterBlock> ELECTRIC_CONVERTER = BLOCKS.register("electric_converter",
            () -> new ConverterBlock(machineProperties(), ELECTRIC));
    private static final DeferredBlock<LampBlock> ELECTRIC_LAMP = BLOCKS.register("electric_lamp",
            () -> new LampBlock(lampProperties(), ELECTRIC, 10000));

    private static final DeferredItem<BlockItem> AETHER_CONVERTER_ITEM = ITEMS.registerSimpleBlockItem(AETHER_CONVERTER);
    private static final DeferredItem<BlockItem> AETHER_LAMP_ITEM = ITEMS.registerSimpleBlockItem(AETHER_LAMP);
    private static final DeferredItem<BlockItem> ELECTRIC_CONVERTER_ITEM = ITEMS.registerSimpleBlockItem(ELECTRIC_CONVERTER);
    private static final DeferredItem<BlockItem> ELECTRIC_LAMP_ITEM = ITEMS.registerSimpleBlockItem(ELECTRIC_LAMP);

    static {
        TABS.register("machines", () -> CreativeModeTab.builder()
                .title(Component.translatable("itemGroup.technomancy_demo.machines"))
                .icon(() -> new ItemStack(AETHER_CONVERTER_ITEM.get()))
                .displayItems((parameters, output) -> {
                    output.accept(AETHER_CONVERTER_ITEM);
                    output.accept(AETHER_LAMP_ITEM);
                    output.accept(ELECTRIC_CONVERTER_ITEM);
                    output.accept(ELECTRIC_LAMP_ITEM);
                })
                .build());
    }

    public TechnomancyDemo(IEventBus bus) {
        BLOCKS.register(bus);
        ITEMS.register(bus);
        TABS.register(bus);
        bus.addListener(TechnomancyDemo::commonSetup);
        bus.addListener(TechnomancyDemo::functionalBlocks);
    }

    private static void commonSetup(FMLCommonSetupEvent event) {
        event.enqueueWork(() -> LeyLinesApi.paradigms().register(new Paradigm<>(id("technomancy"), null,
                List.of(new Paradigm.Currency(AETHER, () -> MagicAmount.of(1000)),
                        new Paradigm.Currency(ELECTRIC, () -> MagicAmount.of(10000))))));
    }

    private static void functionalBlocks(BuildCreativeModeTabContentsEvent event) {
        if (event.getTabKey() == CreativeModeTabs.FUNCTIONAL_BLOCKS) {
            event.accept(AETHER_CONVERTER_ITEM);
            event.accept(AETHER_LAMP_ITEM);
        }
    }

    private static BlockBehaviour.Properties machineProperties() {
        return BlockBehaviour.Properties.of().sound(SoundType.COPPER).strength(3, 6).requiresCorrectToolForDrops();
    }

    private static BlockBehaviour.Properties lampProperties() {
        return machineProperties().lightLevel(state -> state.getValue(LampBlock.LIT) ? 15 : 0);
    }

    private static ResourceLocation id(String path) {
        return ResourceLocation.fromNamespaceAndPath(MOD_ID, path);
    }

    static PowerRequest.Consumer consumer(Block machine, Player player) {
        return new PowerRequest.Consumer(BuiltInRegistries.BLOCK.getKey(machine), Optional.of(player.getUUID()));
    }

    static Component currencyName(ResourceLocation currency) {
        return Component.translatable(currency.toLanguageKey("currency"));
    }

    static void message(Player player, String key, Object... arguments) {
        player.displayClientMessage(Component.translatable("message." + MOD_ID + "." + key, arguments), true);
    }

    static void failure(Player player, PowerResult.Status status, Component currencyName, Object amount) {
        message(player, "failure." + status.name().toLowerCase(Locale.ROOT), currencyName, amount);
    }
}
