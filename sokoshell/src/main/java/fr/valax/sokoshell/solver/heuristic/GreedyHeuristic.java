package fr.valax.sokoshell.solver.heuristic;

import fr.valax.sokoshell.solver.Map;
import fr.valax.sokoshell.solver.State;
import fr.valax.sokoshell.solver.TileInfo;

import java.util.*;

/**
 * According to <a href="http://sokobano.de/wiki/index.php?title=Solver#Greedy_approach">this article</a>
 */
public class GreedyHeuristic extends AbstractHeuristic {

    private List<CrateToTarget> crateToTargets;


    public GreedyHeuristic(Map map) {
        super(map);
    }

    public List<CrateToTarget> getCrateToTargets() {
        return crateToTargets;
    }

    @Override
    public int compute(State s) {

        crateToTargets = new ArrayList<>();
        for (int crateIndex : s.cratesIndices()) {
            mergeCrateDistances(crateIndex);
        }

        List<CrateToTarget> newCrateToTargets = new ArrayList<>();
        Set<Integer> matchedCrates = new HashSet<>();
        Set<Integer> matchedTargets = new HashSet<>();

        List<Integer> remainingCrateIndices = new LinkedList<>();
        for (int i : s.cratesIndices()) {
            remainingCrateIndices.add(i);
        }

        for (CrateToTarget ctt : crateToTargets) {
            if (matchedCrates.contains(ctt.crateIndex) || matchedTargets.contains(ctt.target.index())) {
                continue;
            }
            matchedCrates.add(ctt.crateIndex);
            matchedTargets.add(ctt.target.index());
            remainingCrateIndices.remove(Integer.valueOf(ctt.crateIndex));
            newCrateToTargets.add(new CrateToTarget(ctt));
        }
        crateToTargets = newCrateToTargets;

        for (int crateIndex : remainingCrateIndices) {
            crateToTargets.add(new CrateToTarget(crateIndex, map.getAt(crateIndex).getNearestTarget()));
        }

        int heuristic = 0;
        for (CrateToTarget ctt : crateToTargets) {
            heuristic += ctt.target().distance();
        }
        return heuristic;
    }

    private void mergeCrateDistances(int crateIndex) {
        final TileInfo.TargetRemoteness[] crateTargets = map.getAt(crateIndex).getTargets();
        if (crateToTargets.isEmpty()) {
            for (TileInfo.TargetRemoteness target : crateTargets) {
                crateToTargets.add(new CrateToTarget(crateIndex, target));
            }
        } else {
            int i = 0;
            for (int j = 0; j < crateToTargets.size() && i < crateTargets.length; j++) {
                if (crateTargets[i].compareTo(crateToTargets.get(j).target()) <= 0) {
                    crateToTargets.add(j, new CrateToTarget(crateIndex, crateTargets[i]));
                    i++;
                }
            }
            for (int k = i; k < crateTargets.length; k++) {
                crateToTargets.add(new CrateToTarget(crateIndex, crateTargets[k]));
            }
        }
    }

    @Override
    public String toString() {
        return "GreedyHeuristic[" + crateToTargets.size() + " ctt: " + crateToTargets.toString() + "]";
    }

    record CrateToTarget(int crateIndex, TileInfo.TargetRemoteness target) implements Comparable<CrateToTarget> {

        public CrateToTarget(CrateToTarget other) {
            this(other.crateIndex, other.target);//new TileInfo.TargetRemoteness(other.target));
        }

        @Override
        public int compareTo(CrateToTarget other) {
            return this.target.compareTo(other.target);
        }

        @Override
        public String toString() {
            return "CTT[d=" + target.distance() + ", " + crateIndex + " -> " + target.index() + "]";
        }
    }
}
