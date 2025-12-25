package com.luminary.enchants.util;

import java.util.*;
import java.util.concurrent.ThreadLocalRandom;

/**
 * Utility for weighted random selection.
 */
public class WeightedRandom<T> {

    private final List<Entry<T>> entries = new ArrayList<>();
    private double totalWeight = 0;

    public void add(T item, double weight) {
        if (weight > 0) {
            entries.add(new Entry<>(item, weight));
            totalWeight += weight;
        }
    }

    public T select() {
        if (entries.isEmpty()) {
            return null;
        }

        double random = ThreadLocalRandom.current().nextDouble() * totalWeight;
        double cumulative = 0;

        for (Entry<T> entry : entries) {
            cumulative += entry.weight;
            if (random <= cumulative) {
                return entry.item;
            }
        }

        // Fallback (shouldn't happen)
        return entries.get(entries.size() - 1).item;
    }

    public Optional<T> selectOptional() {
        return Optional.ofNullable(select());
    }

    public List<T> selectMultiple(int count) {
        List<T> results = new ArrayList<>();
        for (int i = 0; i < count && !entries.isEmpty(); i++) {
            T selected = select();
            if (selected != null) {
                results.add(selected);
            }
        }
        return results;
    }

    public boolean isEmpty() {
        return entries.isEmpty();
    }

    public int size() {
        return entries.size();
    }

    public void clear() {
        entries.clear();
        totalWeight = 0;
    }

    private static class Entry<T> {
        final T item;
        final double weight;

        Entry(T item, double weight) {
            this.item = item;
            this.weight = weight;
        }
    }

    /**
     * Quick helper to select from a weighted map.
     */
    public static <T> T selectFromMap(Map<T, Double> weightMap) {
        WeightedRandom<T> wr = new WeightedRandom<>();
        weightMap.forEach(wr::add);
        return wr.select();
    }

    /**
     * Simple chance roll.
     */
    public static boolean roll(double chance) {
        return ThreadLocalRandom.current().nextDouble() < chance;
    }

    /**
     * Random int in range (inclusive).
     */
    public static int randomInt(int min, int max) {
        if (min >= max) return min;
        return ThreadLocalRandom.current().nextInt(min, max + 1);
    }

    /**
     * Random double in range.
     */
    public static double randomDouble(double min, double max) {
        if (min >= max) return min;
        return ThreadLocalRandom.current().nextDouble(min, max);
    }
}
