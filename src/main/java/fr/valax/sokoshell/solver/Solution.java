package fr.valax.sokoshell.solver;

import java.util.Collections;
import java.util.List;

public class Solution {

    private final List<State> states;

    public Solution(List<State> states) {
        this.states = Collections.unmodifiableList(states);
    }

    public List<State> getStates() {
        return states;
    }
}
