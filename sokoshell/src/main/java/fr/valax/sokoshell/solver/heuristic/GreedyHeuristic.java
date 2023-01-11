package fr.valax.sokoshell.solver.heuristic;

import fr.valax.sokoshell.solver.Map;
import fr.valax.sokoshell.solver.State;
import fr.valax.sokoshell.solver.TileInfo;

import java.util.*;

/**
 * According to <a href="http://sokobano.de/wiki/index.php?title=Solver#Greedy_approach">this article</a>
 */
public class GreedyHeuristic extends AbstractHeuristic {

    private List<CrateToTarget> crateToTargetsList;


    public GreedyHeuristic(Map map) {
        super(map);
    }

    public List<CrateToTarget> getCrateToTargetsList() {
        return crateToTargetsList;
    }

    @Override
    public int compute(State s) {

        int heuristic = 0;

        crateToTargetsList = new ArrayList<>();
        List<Integer> remainingIndices = new LinkedList<>();
        for (int crateIndex : s.cratesIndices()) {
            mergeCrateDistances(crateIndex);
            remainingIndices.add(crateIndex);
        }

        for (CrateToTarget ctt : crateToTargetsList) {
            if (map.getAt(ctt.crateIndex).isMarked()
             || map.getAt(ctt.target.index()).isMarked()) {
                continue;
            }
            map.getAt(ctt.crateIndex).mark();
            map.getAt(ctt.target.index()).mark();
            heuristic += ctt.target.distance();
            remainingIndices.remove(Integer.valueOf(ctt.crateIndex));
        }

        for (int crateIndex : remainingIndices) {
            heuristic += map.getAt(crateIndex).getNearestTarget().distance();
        }
        return heuristic;
    }

    /* <old_code> kept for performance comparison */

    public int compute_old(State s) {

        crateToTargetsList = new ArrayList<>();
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

        for (CrateToTarget ctt : crateToTargetsList) {
            if (matchedCrates.contains(ctt.crateIndex) || matchedTargets.contains(ctt.target.index())) {
                continue;
            }
            matchedCrates.add(ctt.crateIndex);
            matchedTargets.add(ctt.target.index());
            remainingCrateIndices.remove(Integer.valueOf(ctt.crateIndex));
            newCrateToTargets.add(new CrateToTarget(ctt));
        }
        crateToTargetsList = newCrateToTargets;

        for (int crateIndex : remainingCrateIndices) {
            crateToTargetsList.add(new CrateToTarget(crateIndex, map.getAt(crateIndex).getNearestTarget()));
        }

        int heuristic = 0;
        for (CrateToTarget ctt : crateToTargetsList) {
            heuristic += ctt.target().distance();
        }
        return heuristic;
    }
    private void mergeCrateDistances(int crateIndex) {
        final TileInfo.TargetRemoteness[] crateTargets = map.getAt(crateIndex).getTargets();
        if (crateToTargetsList.isEmpty()) {
            for (TileInfo.TargetRemoteness target : crateTargets) {
                addToList(crateIndex, target);
            }
        } else {
            int i = 0;
            for (int j = 0; j < crateToTargetsList.size() && i < crateTargets.length; j++) {
                if (crateTargets[i].compareTo(crateToTargetsList.get(j).target()) <= 0) {
                    addToList(crateIndex, crateTargets[i]);
                    i++;
                }
            }
            for (int k = i; k < crateTargets.length; k++) {
                addToList(crateIndex, crateTargets[k]);
            }
        }
    }

    private void addToList(int crateIndex, TileInfo.TargetRemoteness target) {
        crateToTargetsList.add(new CrateToTarget(crateIndex, target));
        map.getAt(crateIndex).unmark();
        map.getAt(target.index()).unmark();
    }

    /* </old_code> */

    @Override
    public String toString() {
        return "GreedyHeuristic[" + crateToTargetsList.size() + " ctt: " + crateToTargetsList.toString() + "]";
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
