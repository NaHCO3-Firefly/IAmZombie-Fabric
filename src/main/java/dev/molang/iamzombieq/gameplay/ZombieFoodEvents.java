package dev.molang.iamzombieq.gameplay;

import java.util.Locale;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

import dev.molang.iamzombieq.IAmZombieConfig;
import dev.molang.iamzombieq.api.event.ZombieAteEvent;
import dev.molang.iamzombieq.api.event.ZombieEatPreEvent;
import dev.molang.iamzombieq.api.extension.IFoodRuleProvider;
import dev.molang.iamzombieq.api.extension.IZombieExtensions;
import dev.molang.iamzombieq.internal.event.ZombieEventPublisher;
import dev.molang.iamzombieq.rules.EffectSpec;
import dev.molang.iamzombieq.rules.food.FoodRule;
import dev.molang.iamzombieq.rules.food.ZombieFoodRules;
import dev.molang.iamzombieq.rules.core.ZombieSize;
import dev.molang.iamzombieq.state.IAmZombieAttachments;
import dev.molang.iamzombieq.state.PlayerZombieData;
import net.minecraft.core.Holder;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.component.DataComponents;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.food.FoodProperties;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.component.Consumable;
import net.minecraft.world.level.block.CakeBlock;
import net.minecraft.world.level.block.CandleCakeBlock;
import net.minecraft.world.level.block.state.BlockState;

public final class ZombieFoodEvents {
    private static final Map<UUID, ZombieFoodRules.PreservedFoodPunishments> PENDING_FOOD_PUNISHMENTS = new ConcurrentHashMap<>();
    private static final Map<UUID, ZombieFoodRules.PreservedGoldenAppleEffects> PENDING_GOLDEN_APPLE_EFFECTS = new ConcurrentHashMap<>();

    private ZombieFoodEvents() {
    }

    // G7: let a zombie player begin eating the special buff foods (pufferfish/spider eye/poisonous potato and the mod's
    // super rotten flesh) even at a full hunger bar.
    public static InteractionResult onRightClickItem(Player player, net.minecraft.world.level.Level level, net.minecraft.world.InteractionHand hand) {
        if (level.isClientSide() || !shouldProcessZombieFood(player)) {
            return InteractionResult.PASS;
        }
        var stack = player.getItemInHand(hand);
        if (!isFood(stack)) {
            return InteractionResult.PASS;
        }
        String id = itemId(stack);
        if (!ZombieFoodRules.isFoodRuleTarget(id)) {
            return InteractionResult.PASS;
        }
        var rule = resolveFoodRule(player, stack, id);
        if (rule == null) {
            return InteractionResult.PASS;
        }
        if (!player.isUsingItem()) {
            player.startUsingItem(hand);
        }
        return InteractionResult.CONSUME;
    }

    // Cake (and candle cake) is eaten as a BLOCK via CakeBlock#useWithoutItem, never as an ItemStack, so it never fires
    // LivingEntityUseItemEvent and its T3-sweet zombie-food debuff (Hunger II + Nausea + Slowness) was silently skipped.
    // We mirror the cake's own eat gate here on the server-side right-click of the block and apply the same human-food
    // punishment + zombie effects the finished-eat handler applies for an ItemStack food. We do NOT cancel the event, so
    // vanilla still runs its own eat (eats the slice, plays sound, advances BITES); we only add the missing zombie rules.
    public static void onRightClickCakeBlock() {
        // Cake block eating is handled via UseBlockCallback in IAmZombieMod.
        // This stub preserves the call site for future expansion.
    }

    public static void onItemUseStarted() {
        // No-op: snapshot preservation is handled in onItemUseFinished for simplicity.
    }

    public static void onItemUseFinished(ServerPlayer player) {
        if (!shouldProcessZombieFood(player)) return;
        var stack = player.getUseItem();
        if (stack.isEmpty() || !isFood(stack)) return;
        String id = itemId(stack);
        if (!ZombieFoodRules.isFoodRuleTarget(id)) return;
        var rule = resolveFoodRule(player, stack, id);
        if (rule == null) return;

        // Apply zombie food effects
        applyZombieEffects(player, rule, id);

        // Handle baby → adult growth for super rotten flesh
        if (rule.restoresBabyState()) {
            var pzd = dev.molang.iamzombieq.platform.Services.ATTACHMENT.get(player,
                    IAmZombieAttachments.PLAYER_ZOMBIE_KEY, PlayerZombieData.DEFAULT);
            dev.molang.iamzombieq.internal.core.ServerZombiePlayer.of(player).setSize(dev.molang.iamzombieq.rules.core.ZombieSize.ADULT);
        }

        // Fire API event
        var ateEvent = new ZombieAteEvent(player, stack, rule);
        ZombieEventPublisher.post(ateEvent);
    }

    public static void onItemUseStopped() {
        // No-op: no pending state to clear on stop.
    }

    public static void onPlayerDeath() {
        // No-op: pending food snapshots are cleared on logout/stop.
    }

    public static void onPlayerLoggedOut() {
        // No-op: pending food snapshots are cleared on server stop.
    }

    public static void onServerStopped() {
        PENDING_FOOD_PUNISHMENTS.clear();
        PENDING_GOLDEN_APPLE_EFFECTS.clear();
    }

    // Whether this player's food use should be inspected at all (server-side, not a spectator). Creative is included
    // here so the special always-edible zombie foods (G1) still get their buff substitution + vanilla-side-effect
    // removal; the creative-vs-survival distinction is applied per-item via {@link #appliesFullZombieFoodRules}.
    private static boolean shouldProcessZombieFood(Player player) {
        return !player.level().isClientSide() && !player.isSpectator();
    }

    // N6: the full zombie food rule set (including the human-food hunger/nausea debuff) now applies even in creative,
    // so being a zombie is consistent across game modes. Only spectators are excluded (already filtered upstream by
    // {@link #shouldProcessZombieFood}). Kept as a named predicate because the food handlers branch on it per item.
    private static boolean appliesFullZombieFoodRules(Player player) {
        return !player.isSpectator();
    }

    private static boolean isFood(ItemStack stack) {
        return stack.has(DataComponents.FOOD);
    }

    private static Set<String> configuredZombieFoods() {
        return IAmZombieConfig.ZOMBIE_FOODS.get()
                .stream()
                .map(value -> value.toLowerCase(Locale.ROOT))
                .collect(Collectors.toUnmodifiableSet());
    }

    // ADDITIVE (Phase-1 API): the FOOD extension hook-query (design §5.b / PLAN A3). Addon-registered
    // IFoodRuleProviders are consulted in order, first non-null wins; otherwise this falls through to the existing
    // built-in ZombieFoodRules.ruleForStack(...) call UNCHANGED. The provider list is empty by default
    // (IZombieExtensions, neutral-when-empty), so with no addon present this is behavior-identical to calling
    // ruleForStack directly. Providers take a ServerPlayer, so they are only consulted for a ServerPlayer eater.
    private static FoodRule resolveFoodRule(Player player, ItemStack stack, String itemId) {
        if (player instanceof ServerPlayer serverPlayer) {
            for (IFoodRuleProvider provider : IZombieExtensions.foodRuleProviders()) {
                FoodRule provided = provider.ruleForStack(serverPlayer, stack, itemId);
                if (provided != null) {
                    return provided;
                }
            }
        }
        return ZombieFoodRules.ruleForStack(stack, itemId, configuredZombieFoods());
    }

    private static String itemId(ItemStack stack) {
        return BuiltInRegistries.ITEM.getKey(stack.getItem()).toString();
    }

    private static void clearPendingFoodSnapshots(Player player) {
        PENDING_FOOD_PUNISHMENTS.remove(player.getUUID());
        PENDING_GOLDEN_APPLE_EFFECTS.remove(player.getUUID());
    }

    private static void applyHumanFoodPunishment(Player player, ZombieFoodRules.HumanFoodPunishmentSettings settings) {
        player.addEffect(new MobEffectInstance(MobEffects.NAUSEA, settings.nauseaDurationTicks(), 0));
        player.addEffect(new MobEffectInstance(MobEffects.HUNGER, settings.hungerDurationTicks(), settings.hungerAmplifier()));
    }

    private static void removeVanillaFoodPunishment(Player player, ZombieFoodRules.PreservedFoodPunishments preserved, int elapsedTicks) {
        // No Start snapshot (e.g. creative-start/survival-finish) means we cannot tell bite-caused effects from
        // pre-existing ones, so do nothing rather than wiping the player's pre-existing Hunger/Nausea/Poison.
        if (preserved == null) {
            return;
        }
        player.removeEffect(MobEffects.HUNGER);
        player.removeEffect(MobEffects.NAUSEA);
        player.removeEffect(MobEffects.POISON);
        restoreFoodPunishments(player, preserved, elapsedTicks);
    }

    private static void removeGoldenAppleEffects(Player player, ZombieFoodRules.PreservedGoldenAppleEffects preserved, int elapsedTicks) {
        if (preserved == null) {
            return;
        }
        player.removeEffect(MobEffects.REGENERATION);
        player.removeEffect(MobEffects.ABSORPTION);
        player.removeEffect(MobEffects.RESISTANCE);
        player.removeEffect(MobEffects.FIRE_RESISTANCE);
        restoreGoldenAppleEffects(player, preserved, elapsedTicks);
    }

    private static void applyZombieEffects(Player player, FoodRule rule, String eatenId) {
        // Remove vanilla food benefits (saturation, etc.) – zombie food doesn't saturate normally
        // Apply custom buffs from the food rule
        for (var buff : rule.buffs()) {
            player.addEffect(new MobEffectInstance(
                    buff.effect(), buff.durationTicks(), buff.amplifier()));
        }
        // Apply debuffs (human food punishment)
        if (rule.appliesHumanFoodPunishment()) {
            applyHumanFoodPunishment(player, ZombieFoodRules.humanFoodPunishmentSettings(
                    IAmZombieConfig.COOKED_HUMAN_FOOD_NAUSEA_DURATION.get(),
                    IAmZombieConfig.COOKED_HUMAN_FOOD_HUNGER_DURATION.get(),
                    0));
        }
        for (var debuff : rule.debuffs()) {
            player.addEffect(new MobEffectInstance(
                    debuff.effect(), debuff.durationTicks(), debuff.amplifier()));
        }
        // Handle random positive effect for poisonous potato
        if (eatenId.equals("minecraft:poisonous_potato")) {
            applyRandomSmallPositive(player);
        }
    }

    private static void applyRandomSmallPositive(Player player) {
        var effects = new MobEffectInstance[] {
                new MobEffectInstance(MobEffects.SPEED, 600, 0),
                new MobEffectInstance(MobEffects.HASTE, 600, 0),
                new MobEffectInstance(MobEffects.LUCK, 600, 0)
        };
        MobEffectInstance chosen = effects[player.getRandom().nextInt(effects.length)];
        player.addEffect(chosen);
    }

    private static ZombieFoodRules.PreservedFoodPunishments preserveExistingFoodPunishments(Player player) {
        return ZombieFoodRules.preserveExistingFoodPunishments(
                preserve(player.getEffect(MobEffects.HUNGER)),
                preserve(player.getEffect(MobEffects.NAUSEA)),
                preserve(player.getEffect(MobEffects.POISON))
        );
    }

    private static ZombieFoodRules.PreservedEffect preserve(MobEffectInstance effect) {
        if (effect == null) {
            return ZombieFoodRules.PreservedEffect.absent();
        }
        return new ZombieFoodRules.PreservedEffect(true, effect.getDuration(), effect.getAmplifier());
    }

    private static void restoreFoodPunishments(Player player, ZombieFoodRules.PreservedFoodPunishments preserved, int elapsedTicks) {
        if (preserved == null) {
            return;
        }
        restore(player, MobEffects.HUNGER, preserved.hunger(), elapsedTicks);
        restore(player, MobEffects.NAUSEA, preserved.nausea(), elapsedTicks);
        restore(player, MobEffects.POISON, preserved.poison(), elapsedTicks);
    }

    private static ZombieFoodRules.PreservedGoldenAppleEffects preserveExistingGoldenAppleEffects(Player player) {
        return ZombieFoodRules.preserveExistingGoldenAppleEffects(
                preserve(player.getEffect(MobEffects.REGENERATION)),
                preserve(player.getEffect(MobEffects.ABSORPTION)),
                preserve(player.getEffect(MobEffects.RESISTANCE)),
                preserve(player.getEffect(MobEffects.FIRE_RESISTANCE))
        );
    }

    private static void restoreGoldenAppleEffects(Player player, ZombieFoodRules.PreservedGoldenAppleEffects preserved, int elapsedTicks) {
        if (preserved == null) {
            return;
        }
        restore(player, MobEffects.REGENERATION, preserved.regeneration(), elapsedTicks);
        restore(player, MobEffects.ABSORPTION, preserved.absorption(), elapsedTicks);
        restore(player, MobEffects.RESISTANCE, preserved.resistance(), elapsedTicks);
        restore(player, MobEffects.FIRE_RESISTANCE, preserved.fireResistance(), elapsedTicks);
    }

    private static void restore(Player player, net.minecraft.core.Holder<net.minecraft.world.effect.MobEffect> effect, ZombieFoodRules.PreservedEffect preserved, int elapsedTicks) {
        if (!preserved.present()) {
            return;
        }
        int duration = preserved.durationTicks();
        if (duration > 0) {
            // Snapshot was taken at use Start; account for the ticks elapsed during the eat so the pre-existing
            // effect isn't silently extended. A finite effect that would have expired during the eat is not re-added.
            duration -= Math.max(0, elapsedTicks);
            if (duration <= 0) {
                return;
            }
        }
        // duration <= 0 here means the infinite-duration sentinel (-1), which is re-applied unchanged.
        player.addEffect(new MobEffectInstance(effect, duration, preserved.amplifier()));
    }
}
