package dev.molang.iamzombieq.api.event;

import dev.molang.iamzombieq.rules.food.FoodRule;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.item.ItemStack;
import org.jetbrains.annotations.NotNull;

/**
 * Observer event fired AFTER a zombie player's food has been handled (design §5.a; the design's
 * {@code ZombieEatedEvent}), carrying the eaten item and the applied {@link FoodRule}. Not cancellable; the eaten
 * stack is an immutable snapshot.
 *
 * <p>Part of the STABLE public API surface (semver 1.x).
 */
public final class ZombieAteEvent implements ZombieEvent {

    private final ServerPlayer player;
    private final ItemStack eaten;
    private final FoodRule rule;

    public ZombieAteEvent(@NotNull ServerPlayer player, @NotNull ItemStack eaten, @NotNull FoodRule rule) {
        this.player = player;
        this.eaten = eaten.copy();
        this.rule = rule;
    }

    @NotNull
    public ServerPlayer player() {
        return player;
    }

    /** An immutable snapshot copy of the eaten stack. */
    @NotNull
    public ItemStack eaten() {
        return eaten.copy();
    }

    @NotNull
    public FoodRule rule() {
        return rule;
    }
}
