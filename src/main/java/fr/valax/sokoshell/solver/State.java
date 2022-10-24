package fr.valax.sokoshell.solver;

import java.util.Arrays;
import java.util.Random;

/**
 * <strong>DO NOT MODIFY THE ARRAY AFTER THE INITIALIZATION. THE HASH WON'T BE RECALCULATED</strong>
 *
 * @author darth-mole
 * @author PoulpoGaz
 */
public record State(int playerPos, int[] cratesIndices, int hash, State parent) {

    // http://sokobano.de/wiki/index.php?title=Solver#Hash_Function
    // https://en.wikipedia.org/wiki/Zobrist_hashing
    private static int[][] zobristValues;

    /**
     * @param minSize minSize is the number of tile in the map
     */
    public static void initZobristValues(int minSize) {
        int i;
        if (zobristValues == null) {
            i = 0;
            zobristValues = new int[minSize][2];
        } else if (zobristValues.length < minSize) {
            i = zobristValues.length;
            zobristValues = Arrays.copyOf(zobristValues, minSize);
        } else {
            i = zobristValues.length;
        }

        Random random = new Random();
        for (; i < zobristValues.length; i++) {
            if (zobristValues[i] == null) {
                zobristValues[i] = new int[2];
            }

            zobristValues[i][0] = random.nextInt();
            zobristValues[i][1] = random.nextInt();
        }
    }

    /**
     * This function creates a child of a state.
     * It uses property of XOR to compute efficiently the hash of the child state
     * @param parent the parent state
     * @param newPlayerPos the new player position
     * @param crateToMove the index of the crate to move
     * @param crateDestination the new position of the crate to move
     * @return the child state
     */
    public static State child(State parent, int newPlayerPos, int crateToMove, int crateDestination) {
        int[] newCrates = parent.cratesIndices().clone();

        int hash = parent.hash ^ zobristValues[parent.playerPos][0] ^ zobristValues[newPlayerPos][0] // 'moves' the player in the hash
                ^ zobristValues[newCrates[crateToMove]][1] ^ zobristValues[crateDestination][1]; // 'moves' the crate in the hash

        newCrates[crateToMove] = crateDestination;

        return new State(newPlayerPos, newCrates, hash, parent);
    }


    public State(int playerPos, int[] cratesIndices, State parent) {
        this(playerPos, cratesIndices, hashCode(playerPos, cratesIndices), parent);
    }

    public static int hashCode(int playerPos, int[] cratesIndices) {
        int hash = zobristValues[playerPos][0];

        for (int crate : cratesIndices) {
            hash ^= zobristValues[crate][1];
        }

        return hash;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        State state = (State) o;

        if (playerPos != state.playerPos) return false;
        return equals(cratesIndices, state.cratesIndices);
    }

    private boolean equals(int[] array1, int[] array2) {
        for (int a : array1) {
            if (!contains(a, array2)) {
                return false;
            }
        }

        return true;
    }

    private boolean contains(int a, int[] array) {
        for (int b : array) {
            if (a == b) {
                return true;
            }
        }

        return false;
    }

    /**
     * Compute the approximate size of this object. It is deduced from the size
     * of an integer (4 bytes. fixed for all jvm), the minimal size of an object in java (16 bytes on
     * 64 bits architectures) and the maximal size of a pointer (8 bytes)
     * @return the approximate size of this object
     */
    public static int approxSize(int nCrate) {
        // object header: mark and class
        // only valid for hotspot
        return 8 + 8 +
                // arrays are also object! +4 to count the array length
                8 + 8 + 4 +
                // playerPos   hash
                  (1 +         1 +     nCrate) * Integer.BYTES
                // the pointer to the next state
                + Long.BYTES;
    }

    /**
     * Compute the approximate size of this object but the user supplied the size of a state and an int array
     * @param stateSize the size of a {@code State} in bytes
     * @param arraySize the size of a {@code int[]} in bytes
     * @return the approximate size of this object
     */
    public static int approxSize(int stateSize, int arraySize, int nCrate) {
        return stateSize + arraySize + nCrate * Integer.BYTES;
    }

    @Override
    public int hashCode() {
        return hash;
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append("Player: ").append(playerPos).append(", Crates: [");

        for (int i = 0; i < cratesIndices.length; i++) {
            int crate = cratesIndices[i];
            sb.append(crate);

            if (i + 1 < cratesIndices.length) {
                sb.append("; ");
            }
        }

        sb.append("], hash: ").append(hash);

        return sb.toString();
    }
}
