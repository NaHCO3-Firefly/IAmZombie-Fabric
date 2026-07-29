package dev.molang.iamzombieq;

import java.util.function.Function;

import dev.molang.iamzombieq.util.ModIds;
import net.minecraft.core.Direction;
import net.minecraft.core.Registry;
import net.minecraft.core.component.DataComponents;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.Identifier;
import net.minecraft.util.Unit;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.StandingAndWallBlockItem;
import net.minecraft.world.item.equipment.Equippable;
import net.minecraft.world.item.equipment.EquipmentModel;
import net.minecraft.world.food.FoodProperties;

public final class IAmZombieItems {
    public static final BlockItem COFFIN = register(
            "coffin",
            props -> new BlockItem(IAmZombieBlocks.COFFIN, props),
            new Item.Properties().stacksTo(1)
    );

    public static final Item SUPER_ROTTEN_FLESH = register(
            "super_rotten_flesh",
            props -> new Item(props.food(new FoodProperties.Builder()
                    .alwaysEdible()
                    .nutrition(20)
                    .saturationModifier(1.0F)
                    .build())),
            new Item.Properties()
    );

    public static final Item DISGUISE_MASK = register(
            "disguise_mask",
            props -> new Item(props
                    .stacksTo(1)
                    .component(DataComponents.EQUIPPABLE, new Equippable(
                            EquipmentSlot.HEAD,
                            EquipmentModel.HUMANOID_HEAD,
                            Identifier.of(IAmZombieMod.MOD_ID, "disguise_mask"),
                            null, null, null,
                            Unit.INSTANCE,
                            false, false
                    ))
                    .durability(15)),
            new Item.Properties()
    );

    public static final StandingAndWallBlockItem HEROBRINE_HEAD = register(
            "herobrine_head",
            props -> new StandingAndWallBlockItem(
                    IAmZombieBlocks.HEROBRINE_HEAD,
                    IAmZombieBlocks.HEROBRINE_WALL_HEAD,
                    props,
                    Direction.DOWN
            ),
            new Item.Properties().stacksTo(64)
    );

    private IAmZombieItems() {
    }

    private static <T extends Item> T register(String id, Function<Item.Properties, T> factory, Item.Properties props) {
        T item = factory.apply(props);
        return Registry.register(BuiltInRegistries.ITEM, Identifier.of(IAmZombieMod.MOD_ID, id), item);
    }

    public static void register() {
        IAmZombieMod.LOGGER.debug("Registered {} items", 4);
    }
}
