package fr.valax.sokoshell.solver.heuristic;

import fr.valax.sokoshell.solver.Map;
import fr.valax.sokoshell.solver.State;
import fr.valax.sokoshell.solver.TileInfo;

import java.util.ArrayList;
import java.util.List;

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
        computeCratesToTargetArray(s);
        return 0;
    }

    private void computeCratesToTargetArray(State s) {
        crateToTargets = new ArrayList<>();
        for (int i : s.cratesIndices()) {
            mergeCrateDistances(i);
        }
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
        return crateToTargets.toString();
    }

    record CrateToTarget(int crateIndex, TileInfo.TargetRemoteness target) implements Comparable<CrateToTarget> {

        @Override
        public int compareTo(CrateToTarget other) {
            return this.target.compareTo(other.target);
        }

        @Override
        public String toString() {
            return "CTT[d=" + target.distance() + ", i=" + crateIndex + "]";
        }
    }
}
