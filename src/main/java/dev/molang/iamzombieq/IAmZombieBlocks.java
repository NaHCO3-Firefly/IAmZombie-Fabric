package dev.molang.iamzombieq;

import dev.molang.iamzombieq.block.CoffinBlock;
import dev.molang.iamzombieq.block.HerobrineHeadBlock;
import dev.molang.iamzombieq.block.HerobrineWallHeadBlock;
import net.minecraft.core.Registry;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.Identifier;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockBehaviour;

public final class IAmZombieBlocks {
    public static CoffinBlock COFFIN;
    public static HerobrineHeadBlock HEROBRINE_HEAD;
    public static HerobrineWallHeadBlock HEROBRINE_WALL_HEAD;

    private IAmZombieBlocks() {}

    public static void register() {
        COFFIN = reg("coffin",
                new CoffinBlock(props("coffin").strength(2.0f, 3.0f).noOcclusion()));
        // HEROBRINE_HEAD and HEROBRINE_WALL_HEAD temporarily disabled
        // They require custom block entity registration in MC 26.2
        //HEROBRINE_HEAD = reg("herobrine_head",
        //        new HerobrineHeadBlock(props("herobrine_head").strength(1.0f)));
        //HEROBRINE_WALL_HEAD = reg("herobrine_wall_head",
        //        new HerobrineWallHeadBlock(props("herobrine_wall_head").strength(1.0f)));
        IAmZombieMod.LOGGER.debug("Registered blocks");
    }

    private static BlockBehaviour.Properties props(String name) {
        Identifier id = Identifier.fromNamespaceAndPath(IAmZombieMod.MOD_ID, name);
        return BlockBehaviour.Properties.of()
                .setId(ResourceKey.create(Registries.BLOCK, id));
    }

    private static <T extends Block> T reg(String name, T block) {
        return Registry.register(BuiltInRegistries.BLOCK,
                Identifier.fromNamespaceAndPath(IAmZombieMod.MOD_ID, name), block);
    }
}
