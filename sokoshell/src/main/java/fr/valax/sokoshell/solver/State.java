package fr.valax.sokoshell.solver;

import java.util.Arrays;
import java.util.Random;

/**
 * A state represents an arrangement of the crates in the map and the location of the player.
 *
 * @implNote <strong>DO NOT MODIFY THE ARRAY AFTER THE INITIALIZATION. THE HASH WON'T BE RECALCULATED</strong>
 * @author darth-mole
 * @author PoulpoGaz
 */
public class State {

    // http://sokobano.de/wiki/index.php?title=Solver#Hash_Function
    // https://en.wikipedia.org/wiki/Zobrist_hashing
    protected static int[][] zobristValues;

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



    protected final int playerPos;
    protected final int[] cratesIndices;
    protected final int hash;
    protected final State parent;

    public State(int playerPos, int[] cratesIndices, int hash, State parent) {
        this.playerPos = playerPos;
        this.cratesIndices = cratesIndices;
        this.hash = hash;
        this.parent = parent;
    }

    /**
     * Creates a child of the state.
     * It uses property of XOR to compute efficiently the hash of the child state
     * @param newPlayerPos the new player position
     * @param crateToMove the index of the crate to move
     * @param crateDestination the new position of the crate to move
     * @return the child state
     */
    public State child(int newPlayerPos, int crateToMove, int crateDestination) {
        int[] newCrates = this.cratesIndices().clone();
        int hash = this.hash ^ zobristValues[this.playerPos][0] ^ zobristValues[newPlayerPos][0] // 'moves' the player in the hash
                ^ zobristValues[newCrates[crateToMove]][1] ^ zobristValues[crateDestination][1]; // 'moves' the crate in the hash
        newCrates[crateToMove] = crateDestination;

        return new State(newPlayerPos, newCrates, hash, this);
    }

    public State(int playerPos, int[] cratesIndices, State parent) {
        this(playerPos, cratesIndices, hashCode(playerPos, cratesIndices), parent);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        State state = (State) o;

        if (playerPos != state.playerPos) return false;
        return equals(cratesIndices, state.cratesIndices);
    }

    /**
     * Returns true if all elements of array1 are included in array2 and vice-versa.
     * However, because there is no duplicate and the two array have the same length,
     * it is only necessary to check if array1 is included in array2.
     *
     * @param array1 the first array
     * @param array2 the second array
     * @return true if all elements are included in the second one
     */
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

    public static int hashCode(int playerPos, int[] cratesIndices) {
        int hash = zobristValues[playerPos][0];

        for (int crate : cratesIndices) {
            hash ^= zobristValues[crate][1];
        }

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

    /**
     * The index of the case of the map on which the player is.
     */
    public int playerPos() {
        return playerPos;
    }

    /**
     * The index of the cases of the map on which the crates are.
     */
    public int[] cratesIndices() {
        return cratesIndices;
    }

    public int hash() {
        return hash;
    }

    /**
     * The state in which the map was before coming to this state.
     */
    public State parent() {
        return parent;
    }

}
