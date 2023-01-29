package fr.valax.sokoshell.solver.heuristic;

import fr.valax.sokoshell.solver.State;
import fr.valax.sokoshell.solver.board.Board;
import fr.valax.sokoshell.solver.board.tiles.TileInfo;

/**
 * According to <a href="http://sokobano.de/wiki/index.php?title=Solver#Greedy_approach">this article</a>
 */
public class GreedyHeuristic extends AbstractHeuristic {

    private final LinkedList list;

    public GreedyHeuristic(Board board) {
        super(board);
        final int n = board.getTargetCount();

        list = new LinkedList(n);
    }

    @Override
    public int compute(State s) {
        int heuristic = 0;

        board.getMarkSystem().unmarkAll();

        int n = 0;
        for (int crate : s.cratesIndices()) {
            TileInfo tile = board.getAt(crate);

            if (tile.isCrateOnTarget()) {
                tile.mark();
            } else {
                list.add(tile);

                n++;
            }
        }


        for (int i = 0; i < n; i++) {
            Node minNode = list.getHead();
            TileInfo.TargetRemoteness minDist = minNode.getNearestNotAttributedTarget();

            Node node = minNode.nextNode();
            while (node != null) {
                TileInfo.TargetRemoteness nearest = node.getNearestNotAttributedTarget();

                if (nearest.distance() < minDist.distance()) {
                    minNode = node;
                    minDist = nearest;
                }

                node = node.nextNode();
            }

            board.getAt(minDist.index()).mark();
            minNode.getCrate().mark();
            heuristic += minDist.distance();

            minNode.remove();
        }

        return heuristic;
    }

    private static class LinkedList {

        private final Node[] nodeCache;
        private int size = 0;

        private Node head;

        public LinkedList(int size) {
            nodeCache = new Node[size];

            for (int i = 0; i < size; i++) {
                nodeCache[i] = new Node(this);
            }
        }

        public void add(TileInfo crate) {
            Node newHead = nodeCache[size];
            newHead.set(crate);

            if (head != null) {
                newHead.next = head;
                head.previous = newHead;
            }
            head = newHead;

            size++;
        }

        public void remove(Node node) {
            if (node == head) {
                head = node.next;

                if (head != null) {
                    head.previous = null;
                }
            } else {
                node.previous.next = node.next;

                if (node.next != null) {
                    node.next.previous = node.previous;
                }
            }

            size--;
        }

        public Node getHead() {
            return head;
        }
    }

    private static class Node {

        private final LinkedList list;
        private TileInfo crate;

        private Node previous;
        private Node next;

        /**
         * Index in crate's target remoteness
         */
        private int index = 0;

        public Node(LinkedList list) {
            this.list = list;
        }

        public void set(TileInfo tile) {
            crate = tile;
            index = 0;
        }

        public void remove() {
            list.remove(this);
        }

        public Node nextNode() {
            return next;
        }

        public TileInfo getCrate() {
            return crate;
        }

        public TileInfo.TargetRemoteness getNearestNotAttributedTarget() {
            TileInfo.TargetRemoteness[] remoteness = crate.getTargets();

            Board b = crate.getBoard();
            while (b.getAt(remoteness[index].index()).isMarked()) {
                index++;
            }

            return remoteness[index];
        }
    }
}
