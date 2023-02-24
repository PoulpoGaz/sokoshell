package fr.valax.sokoshell.utils;

import fr.valax.sokoshell.solver.State;
import fr.valax.sokoshell.solver.WeightedState;
import org.openjdk.jol.info.ClassLayout;

import java.util.HashMap;
import java.util.List;
import java.util.Set;

public class SizeOf {

    private static boolean initialized = false;

    private static ClassLayout HASH_MAP_LAYOUT;
    private static ClassLayout HASH_MAP_NODE_LAYOUT;
    private static ClassLayout STATE_LAYOUT;
    private static ClassLayout WEIGHTED_STATE_LAYOUT;
    private static ClassLayout INT_ARRAY_LAYOUT;

    public static synchronized void initialize() {
        if (initialized) {
            return;
        }

        HASH_MAP_LAYOUT = ClassLayout.parseClass(HashMap.class);

        try {
            HASH_MAP_NODE_LAYOUT = ClassLayout.parseClass(Class.forName("java.util.HashMap$Node"));
        } catch (ClassNotFoundException e) {
            throw new RuntimeException(e);
        }

        STATE_LAYOUT = ClassLayout.parseClass(State.class);
        WEIGHTED_STATE_LAYOUT = ClassLayout.parseClass(WeightedState.class);
        INT_ARRAY_LAYOUT = ClassLayout.parseClass(int[].class);

        initialized = true;
    }

    public static long approxSizeOfAccurate(Set<?> set, long contentSize) {
        return set.size() * (HASH_MAP_NODE_LAYOUT.instanceSize() + contentSize);
    }

    public static long approxSizeOf(Set<?> set, long contentSize) {
        return set.size() * (HASH_MAP_NODE_LAYOUT.instanceSize() + contentSize);
    }

    public static ClassLayout getHashMapLayout() {
        return HASH_MAP_LAYOUT;
    }

    public static ClassLayout getHashMapNodeLayout() {
        return HASH_MAP_NODE_LAYOUT;
    }

    public static ClassLayout getStateLayout() {
        return STATE_LAYOUT;
    }

    public static ClassLayout getWeightedStateLayout() {
        return WEIGHTED_STATE_LAYOUT;
    }

    public static ClassLayout getIntArrayLayout() {
        return INT_ARRAY_LAYOUT;
    }
}
