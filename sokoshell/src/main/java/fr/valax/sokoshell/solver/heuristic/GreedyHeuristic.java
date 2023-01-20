package fr.valax.sokoshell.solver.heuristic;

import fr.valax.sokoshell.solver.Board;
import fr.valax.sokoshell.solver.State;
import fr.valax.sokoshell.solver.TileInfo;

/**
 * According to <a href="http://sokobano.de/wiki/index.php?title=Solver#Greedy_approach">this article</a>
 */
public class GreedyHeuristic extends AbstractHeuristic {

    MinHeap cttHeap;

    public GreedyHeuristic(Board board) {
        super(board);
        final int n = board.getTargetCount();
        cttHeap = new MinHeap(n * n);
    }

    @Override
    public int compute(State s) {

        int heuristic = 0;

        cttHeap.clear();
        board.getMarkSystem().unmarkAll();

        for (int crateIndex : s.cratesIndices()) {
            mergeCrateTargets(crateIndex);
        }

        while (!cttHeap.isEmpty()) {
            final CrateToTarget ctt = cttHeap.pop();

            if (board.getAt(ctt.crateIndex).isMarked()
             || board.getAt(ctt.target.index()).isMarked()) {
                continue;
            }
            board.getAt(ctt.crateIndex).mark();
            board.getAt(ctt.target.index()).mark();

            //System.out.printf("%s; ", ctt);
            heuristic += ctt.target.distance();
        }
        //System.out.print(" -- ");

        for (int crateIndex : s.cratesIndices()) {
            final TileInfo curCrate = board.getAt(crateIndex);
            if (!curCrate.isMarked()) {
                //System.out.printf("%d; ", curCrate.getNearestTarget().distance());
                heuristic += curCrate.getNearestTarget().distance();
            }
        }
        //System.out.println();

        return heuristic;
    }

    public void mergeCrateTargets(int crateIndex) {
        final TileInfo.TargetRemoteness[] crateTargets = board.getAt(crateIndex).getTargets();
        for (final TileInfo.TargetRemoteness curTarget : crateTargets) {
            if (board.getAt(curTarget.index()).isCrateOnTarget()) {
                continue;
            }
            cttHeap.add(crateIndex, curTarget);
        }
    }

    @Override
    public String toString() {
        return "GreedyHeuristic[" + cttHeap.size() + " ctt: " + cttHeap.toString() + "]";
    }

    static class CrateToTarget implements Comparable<CrateToTarget> {

        private int crateIndex;
        private TileInfo.TargetRemoteness target;

        CrateToTarget() {
            set(-1, null);
        }

        CrateToTarget(int crateIndex, TileInfo.TargetRemoteness target) {
            set(crateIndex, target);
        }

        @Override
        public int compareTo(CrateToTarget other) {
            return this.target.compareTo(other.target);
        }

        @Override
        public String toString() {
            return "CTT[d=" + target.distance() + ", " + crateIndex + " -> " + target.index() + "]";
        }

        public void set(int crateIndex, TileInfo.TargetRemoteness target) {
            this.crateIndex = crateIndex;
            this.target = target;
        }

        public int crateIndex() {
            return crateIndex;
        }

        public TileInfo.TargetRemoteness target() {
            return target;
        }
    }

    private static class MinHeap extends fr.valax.sokoshell.solver.collections.MinHeap<CrateToTarget> {

        public MinHeap(int capacity) {
            super(capacity);
            for (int i = 0; i < capacity; i++) {
                nodes.get(i).setContent(new CrateToTarget());
            }
        }

        public void add(int crateIndex, TileInfo.TargetRemoteness target) {
            nodes.get(currentSize).content().set(crateIndex, target);
            nodes.get(currentSize).setPriority(target.distance());
            moveNodeUp(currentSize);
            currentSize++;
        }

        @Override
        public String toString() {
            return nodes.toString();
        }
    }
}
