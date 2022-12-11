package fr.valax.sokoshell.solver;

/**
 * A simple derivation of State with a weight, i.e. something to rank the states.
 * Used for instance by {@link AStarSolver}
 */
public class WeightedState extends State {

    private int cost = 0;

    private int heuristic = 0;

    public WeightedState(int playerPos, int[] cratesIndices, int hash, State parent, int cost, int heuristic) {
        super(playerPos, cratesIndices, hash, parent);
        this.setCost(cost);
        this.setHeuristic(heuristic);
    }

    public WeightedState(State state, int cost, int heuristic) {
        this(state.playerPos(), state.cratesIndices(), state.hash(), state.parent(), cost, heuristic);
    }

    /**
     * The state weight, which is the sum of its cost and its heuristic.
     */
    public int weight() {
        return cost() + heuristic();
    }

    /**
     * <strong>This function does NOT compute the heuristic of the child state.</strong>
     * Use {@link WeightedState#setHeuristic(int)} to set it after calling this method.
     */
    public WeightedState child(int newPlayerPos, int crateToMove, int crateDestination) {
        return new WeightedState(super.child(newPlayerPos, crateToMove, crateDestination),
                                 cost(), 0);
    }

    /**
     * The cost the come to this state.
     */
    public int cost() {
        return cost;
    }

    public void setCost(int cost) {
        this.cost = cost;
    }

    /**
     * The heuristic between this state and a solution.
     */
    public int heuristic() {
        return heuristic;
    }

    public void setHeuristic(int heuristic) {
        this.heuristic = heuristic;
    }
}
