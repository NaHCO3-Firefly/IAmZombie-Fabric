package dev.molang.iamzombieq;

import java.util.function.Function;

import dev.molang.iamzombieq.util.ModIds;
import net.minecraft.core.Registry;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.Identifier;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.StandingAndWallBlockItem;
import net.minecraft.world.food.FoodProperties;

public final class IAmZombieItems {
    public static BlockItem COFFIN;
    public static Item SUPER_ROTTEN_FLESH;
    public static Item DISGUISE_MASK;
    public static StandingAndWallBlockItem HEROBRINE_HEAD;

    private IAmZombieItems() {}

    private static <T extends Item> T reg(String name, T item) {
        return Registry.register(BuiltInRegistries.ITEM,
                Identifier.fromNamespaceAndPath(IAmZombieMod.MOD_ID, name), item);
    }

    private static Item.Properties itemProps(String name) {
        Identifier id = Identifier.fromNamespaceAndPath(IAmZombieMod.MOD_ID, name);
        return new Item.Properties()
                .setId(ResourceKey.create(Registries.ITEM, id));
    }

    public static void register() {
        COFFIN = reg("coffin", new BlockItem(IAmZombieBlocks.COFFIN,
                itemProps("coffin").stacksTo(1)));
        SUPER_ROTTEN_FLESH = reg("super_rotten_flesh", new Item(
                itemProps("super_rotten_flesh").food(new FoodProperties.Builder()
                        .alwaysEdible().nutrition(20).saturationModifier(1.0F).build())));
        DISGUISE_MASK = reg("disguise_mask", new Item(
                itemProps("disguise_mask").stacksTo(1).durability(15)));
        //HEROBRINE_HEAD = reg("herobrine_head", new StandingAndWallBlockItem(
        //        IAmZombieBlocks.HEROBRINE_HEAD,
        //        IAmZombieBlocks.HEROBRINE_WALL_HEAD,
        //        net.minecraft.core.Direction.DOWN,
        //        itemProps("herobrine_head").stacksTo(64)));
        IAmZombieMod.LOGGER.debug("Registered items");
    }
}
