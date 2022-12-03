package fr.valax.sokoshell.solver;

/**
 * A simple derivation of State with a weight, i.e. something to rank the states.
 * Used for instance by {@link AStarSolver}
 */
public class WeightedState extends State {

    /**
     * The cost the come to this state.
     */
    public int cost = 0;

    /**
     * The heuristic between this state and a solution.
     */
    public int heuristic = 0;

    public WeightedState(int playerPos, int[] cratesIndices, int hash, State parent, int cost, int heuristic) {
        super(playerPos, cratesIndices, hash, parent);
        this.cost = cost;
        this.heuristic = heuristic;
    }

    /**
     * The state weight, which is the sum of its cost and its heuristic.
     */
    public int weight() {
        return cost + heuristic;
    }
}
