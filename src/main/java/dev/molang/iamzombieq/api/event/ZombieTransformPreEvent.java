package dev.molang.iamzombieq.api.event;

import dev.molang.iamzombieq.rules.core.ZombieForm;
import net.minecraft.server.level.ServerPlayer;
import org.jetbrains.annotations.NotNull;

/**
 * Cancellable event fired BEFORE a player's form is actively changed (design §5.a), e.g. inside
 * {@code IZombiePlayer.transformToForm}. Cancel it to veto the transform.
 *
 * <p>Fields are an immutable snapshot of the transform's {@code from}/{@code to} forms.
 *
 * <p>Part of the STABLE public API surface (semver 1.x).
 */
public final class ZombieTransformPreEvent implements ZombieEvent, Cancellable {

    private final ServerPlayer player;
    private final ZombieForm from;
    private final ZombieForm to;
    private boolean canceled;

    public ZombieTransformPreEvent(@NotNull ServerPlayer player, @NotNull ZombieForm from, @NotNull ZombieForm to) {
        this.player = player;
        this.from = from;
        this.to = to;
    }

    @NotNull
    public ServerPlayer player() {
        return player;
    }

    @NotNull
    public ZombieForm from() {
        return from;
    }

    @NotNull
    public ZombieForm to() {
        return to;
    }

    @Override
    public boolean isCanceled() {
        return canceled;
    }

    @Override
    public void setCanceled(boolean canceled) {
        this.canceled = canceled;
    }
}
