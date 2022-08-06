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
    private static int[] zobristValues;

    /**
     * @param minSize minSize is the number of tile in the map
     */
    public static void initZobristValues(int minSize) {
        int i;
        if (zobristValues == null) {
            i = 0;
            zobristValues = new int[minSize];
        } else if (zobristValues.length < minSize) {
            i = zobristValues.length;
            zobristValues = Arrays.copyOf(zobristValues, minSize);
        } else {
            i = zobristValues.length;
        }

        Random random = new Random();
        for (; i < zobristValues.length; i++) {
            zobristValues[i] = random.nextInt();
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

        int hash = parent.hash ^ zobristValues[parent.playerPos] ^ zobristValues[newPlayerPos] // 'moves' the player in the hash
                ^ zobristValues[newCrates[crateToMove]] ^ zobristValues[crateDestination]; // 'moves' the crate in the hash

        newCrates[crateToMove] = crateDestination;

        return new State(newPlayerPos, newCrates, hash, parent);
    }


    public State(int playerPos, int[] cratesIndices, State parent) {
        this(playerPos, cratesIndices, hashCode(playerPos, cratesIndices), parent);
    }

    public static int hashCode(int playerPos, int[] cratesIndices) {
        int hash = zobristValues[playerPos];

        for (int crate : cratesIndices) {
            hash ^= zobristValues[crate];
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


    @Override
    public int hashCode() {
        return hash;
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append("Player: ").append(playerPos).append(" Crates: [");

        for (int i = 0; i < cratesIndices.length; i++) {
            int crate = cratesIndices[i];
            sb.append(crate);

            if (i + 1 < cratesIndices.length) {
                sb.append("; ");
            }
        }

        sb.append("] hash: ").append(hash);

        return sb.toString();
    }
}
