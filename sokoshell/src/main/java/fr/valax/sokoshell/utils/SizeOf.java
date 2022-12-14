package fr.valax.sokoshell.utils;

import fr.valax.sokoshell.solver.State;
import org.openjdk.jol.info.ClassLayout;

import java.util.HashMap;
import java.util.Set;

public class SizeOf {

    private static boolean initialized = false;

    private static ClassLayout HASH_MAP_LAYOUT;
    private static ClassLayout HASH_MAP_NODE_LAYOUT;
    private static ClassLayout STATE_LAYOUT;
    private static ClassLayout INT_ARRAY_LAYOUT;

    public static void initialize() {
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
        INT_ARRAY_LAYOUT = ClassLayout.parseClass(int[].class);

        initialized = true;
    }

    public static long approxSizeOf(Set<State> map, int nCrate) {
        // hash map size can be neglected
        return map.size() * (
                HASH_MAP_NODE_LAYOUT.instanceSize() +
                        STATE_LAYOUT.instanceSize() +
                        INT_ARRAY_LAYOUT.instanceSize() +
                        (long) Integer.BYTES * nCrate);
    }

    public static long approxSizeOf2(Set<State> map, int nCrate) {
        // hash map size can be neglected
        return map.size() * (32 + 32 + 16 + (long) Integer.BYTES * nCrate);
    }

    public static ClassLayout getHashMapLayout() {
        return HASH_MAP_LAYOUT;
    }

    public static ClassLayout getHashMapNodeLayout() {
        return HASH_MAP_NODE_LAYOUT;
    }

    public static ClassLayout getIntArrayLayout() {
        return INT_ARRAY_LAYOUT;
    }

    public static ClassLayout getStateLayout() {
        return STATE_LAYOUT;
    }
}
