package fr.valax.sokoshell.solver;

import java.util.Arrays;

/**
 * @author darth-mole
 * @author PoulpoGaz
 */
public record State(int playerPos, int[] cratesIndices, State parent) {

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        State state = (State) o;

        if (playerPos != state.playerPos) return false;
        return Arrays.equals(cratesIndices, state.cratesIndices);
    }

    @Override
    public int hashCode() {
        int result = playerPos;
        result = 31 * result + Arrays.hashCode(cratesIndices);
        return result;
    }
}