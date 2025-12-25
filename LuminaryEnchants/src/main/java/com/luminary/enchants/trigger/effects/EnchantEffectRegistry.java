package com.luminary.enchants.trigger.effects;

import com.luminary.enchants.LuminaryEnchants;
import com.luminary.enchants.trigger.effects.impl.*;

import java.util.HashMap;
import java.util.Map;

/**
 * Registry for all enchant effect implementations.
 */
public class EnchantEffectRegistry {

    private final LuminaryEnchants plugin;
    private final Map<String, EnchantEffect> effects = new HashMap<>();

    public EnchantEffectRegistry(LuminaryEnchants plugin) {
        this.plugin = plugin;
        registerEffects();
    }

    private void registerEffects() {
        // Register all enchant effect implementations
        register(new KeyfinderEffect(plugin));
        register(new EnhancerEffect(plugin));
        register(new PiggyBankEffect(plugin));
        register(new ThorEffect(plugin));
        register(new TowerEffect(plugin));
        register(new FreezeEffect(plugin));
        register(new SecondHandEffect(plugin));
        register(new PoisonEffect(plugin));
        register(new SonicEffect(plugin));
        register(new FlameEffect(plugin));
        register(new SlimeEffect(plugin));
        register(new QuicksandEffect(plugin));
        register(new LaserEffect(plugin));
        register(new DemonEffect(plugin));
        register(new AngelEffect(plugin));
        register(new GoldenEffect(plugin));
        register(new PauseEffect(plugin));
        register(new RewindEffect(plugin));
        register(new RainbowEffect(plugin));
        register(new OverclockEffect(plugin));
        register(new StormCloudEffect(plugin));
        register(new BlackholeEffect(plugin));
        register(new GhostriderEffect(plugin));
        register(new MirrorEffect(plugin));
        register(new FlashEffect(plugin));
        register(new OverloadEffect(plugin));
        register(new MeteorShowerEffect(plugin));
    }

    public void register(EnchantEffect effect) {
        effects.put(effect.getEnchantId(), effect);
    }

    public EnchantEffect getEffect(String enchantId) {
        return effects.get(enchantId);
    }

    public boolean hasEffect(String enchantId) {
        return effects.containsKey(enchantId);
    }
}
