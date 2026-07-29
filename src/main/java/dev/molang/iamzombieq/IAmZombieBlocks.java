package dev.molang.iamzombieq;

import dev.molang.iamzombieq.block.CoffinBlock;
import dev.molang.iamzombieq.block.HerobrineHeadBlock;
import dev.molang.iamzombieq.block.HerobrineWallHeadBlock;
import net.minecraft.core.Registry;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.Identifier;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockBehaviour;

public final class IAmZombieBlocks {
    public static final CoffinBlock COFFIN = register(
            "coffin",
            new CoffinBlock(BlockBehaviour.Properties.ofFullCopy(Blocks.OAK_PLANKS).noOcclusion())
    );

    public static final HerobrineHeadBlock HEROBRINE_HEAD = register(
            "herobrine_head",
            new HerobrineHeadBlock(BlockBehaviour.Properties.ofFullCopy(Blocks.SKELETON_SKULL))
    );

    public static final HerobrineWallHeadBlock HEROBRINE_WALL_HEAD = register(
            "herobrine_wall_head",
            new HerobrineWallHeadBlock(BlockBehaviour.Properties.ofFullCopy(Blocks.SKELETON_WALL_SKULL))
    );

    private IAmZombieBlocks() {
    }

    private static <T extends Block> T register(String id, T block) {
        return Registry.register(BuiltInRegistries.BLOCK, Identifier.fromNamespaceAndPath(IAmZombieMod.MOD_ID, id), block);
    }

    public static void register() {
        // static initializers are triggered by class load; this method forces the class to load
        IAmZombieMod.LOGGER.debug("Registered {} blocks", 3);
    }
}
