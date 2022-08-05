package fr.valax.sokoshell.solver;

import java.util.Arrays;

/**
 * @author darth-mole
 * @author PoulpoGaz
 */

/*
 * TODO: replace int[] with Set<Integer>
 * because int[] {1, 3} and int[] {3, 1} are wrongly identified as not equals
 */
public record State(int playerPos, int[] cratesIndices, State parent) {

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        State state = (State) o;

        if (playerPos != state.playerPos) return false;
        return equals(cratesIndices, state.cratesIndices);
    }

    @Override
    public int hashCode() {
        return playerPos * hashCode(cratesIndices);
    }

    private int hashCode(int[] array) {
        int prod = 1;
        for (int a : array) {
            prod *= (a + 1); // only positive value allowed
        }
        return prod;
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
}
