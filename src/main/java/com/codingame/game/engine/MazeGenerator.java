package com.codingame.game.engine;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Queue;
import java.util.Random;
import java.util.concurrent.ConcurrentLinkedDeque;
import java.util.stream.Collectors;

public class MazeGenerator {
    class Cell {
        public int x;
        public int y;
        public int id;
        public int cellType = Constants.TYPE_OBSTACLE;
        public ArrayList<Cell> neighbors = new ArrayList<>();
        public ArrayList<Cell> neighbors8 = new ArrayList<>();

        public Cell(int x, int y) {
            this.x = x;
            this.y = y;
            id = x * Constants.MAZE_HEIGHT + y;
        }

        private final int[] dx = {0, 1, 0, -1, 1, 1, -1, -1};
        private final int[] dy = {1, 0, -1, 0, 1, -1, 1, -1};

        public void initNeighbors(Cell[][] grid) {
            for (int dir = 0; dir < dx.length; dir++) {
                int x = this.x + dx[dir];
                int y = this.y + dy[dir];
                if (x < 0 || x >= Constants.MAZE_WIDTH || y < 0 || y >= Constants.MAZE_HEIGHT) continue;
                if (dir < 4) neighbors.add(grid[x][y]);
                neighbors8.add(grid[x][y]);
            }
        }

        public ArrayList<Cell> expandCandidates() {
            ArrayList<Cell> result = new ArrayList<>();
            for (Cell cell : neighbors) {
                if (cell.cellType != Constants.TYPE_OBSTACLE) continue;
                if (cell.neighbors.stream().filter(c -> c.cellType == Constants.TYPE_FLOOR).count() != 1) continue;
                Cell touching = cell.neighbors.stream().filter(c -> c.cellType == Constants.TYPE_FLOOR).collect(Collectors.toCollection(ArrayList::new)).get(0);
                if (cell.neighbors8.stream().filter(c -> c.cellType == Constants.TYPE_FLOOR && c != touching && !touching.neighbors.contains(c)).count() > 0) continue;
                result.add(cell);
            }
            return result;
        }

        public int[] BFS() {
            int[] result = new int[Constants.MAZE_WIDTH * Constants.MAZE_HEIGHT];
            Arrays.fill(result, result.length);
            Queue<Cell> queue = new ConcurrentLinkedDeque<>();
            queue.add(this);
            result[this.id] = 0;
            while (queue.size() > 0) {
                Cell c = queue.poll();
                for (Cell n : c.neighbors) {
                    if (result[n.id] < result.length || n.cellType == Constants.TYPE_OBSTACLE) continue;
                    queue.add(n);
                    result[n.id] = 1 + result[c.id];
                }
            }
            return result;
        }

        public int viewDist(Cell cell) {
            int dx = Math.abs(this.x - cell.x);
            int dy = Math.abs(this.y - cell.y);
            return Math.max(dx, dy);
        }

        @Override
        public String toString() {
            return x + "/" + y;
        }
    }

    private Cell[][] grid;
    public String[] generateMaze(Random random) {
        ArrayList<Cell> cells = generateMazeLayout(random);

        ArrayList<Cell> floor = cells.stream().filter(c -> c.cellType == Constants.TYPE_FLOOR).collect(Collectors.toCollection(ArrayList::new));
        Cell start = floor.get(0);
        Cell end = floor.get(0);
        while (start.BFS()[end.id] < Constants.MAZE_WIDTH) {
            start = floor.get(random.nextInt(floor.size()));
            end = floor.get(random.nextInt(floor.size()));
        }
        floor.remove(start);
        floor.remove(end);
        start.cellType = Constants.TYPE_HERO;
        end.cellType = Constants.TYPE_EXIT;

        int treasureCount = 5 + random.nextInt(5);
        for (int i = 0; i < treasureCount; i++) {
            Cell treasure = floor.get(random.nextInt(floor.size()));
            for (int retry = 0; retry < 10; retry++) { // more likely to place treasure at dead end
                if (treasure.neighbors.stream().filter(c -> c.cellType != Constants.TYPE_OBSTACLE).count() == 1) break;
                treasure = floor.get(random.nextInt(floor.size()));
            }
            treasure.cellType = Constants.TYPE_TREASURE;
            floor.remove(treasure);
        }
        placeItem(floor, random, start, 0, Constants.TYPE_MONSTER0, 5 + random.nextInt(11)); // BOX
        placeItem(floor, random, start, 0, Constants.TYPE_POTION, 4 + random.nextInt(4));
        placeItem(floor, random, start, 0, Constants.TYPE_CHARGES1, random.nextInt(5));
        placeItem(floor, random, start, 0, Constants.TYPE_CHARGES2, random.nextInt(5));
        placeItem(floor, random, start, 0, Constants.TYPE_CHARGES3, random.nextInt(5));

        int monsterCount = 30 + random.nextInt(21);
        double skeletonProb = 0.1 + 0.4 * random.nextDouble();
        double gargoyleProb = 0.1 + 0.2 * random.nextDouble() + skeletonProb;
        double orcProb = 0.05 + 0.1 * random.nextDouble() + gargoyleProb;
        double vampireProb = 0.2 + 0.5 * random.nextDouble() + orcProb;
        double[] probs = {skeletonProb, gargoyleProb, orcProb, vampireProb};
        int[] types = {Constants.TYPE_MONSTER1, Constants.TYPE_MONSTER2, Constants.TYPE_MONSTER3, Constants.TYPE_MONSTER4};
        for (int i = 0; i < monsterCount; i++) {
            double d = random.nextDouble() * probs[3];
            int index = 0;
            while (d > probs[index]) index++;
            placeItem(floor, random, start, 2, types[index], 1);
        }

        String[] result = new String[Constants.MAZE_HEIGHT];
        for (int y = 0; y < Constants.MAZE_HEIGHT; y++) {
            String line = "";
            for (int x = 0; x < Constants.MAZE_WIDTH; x++) {
                for (String key : Constants.MAZE_CHARS_TYPES.keySet()) {
                    if (Constants.MAZE_CHARS_TYPES.get(key) == grid[x][y].cellType) line += key;
                }
            }
            result[y] = line;
        }

        System.out.println(Arrays.toString(result));

        return result;
    }

    private void placeItem(ArrayList<Cell> floor, Random random, Cell start, int distToStart, int item, int count) {
        ArrayList<Cell> candidates = new ArrayList<>(floor);
        for (int i = 0; i < count; i++) {
            if (candidates.size() == 0) return;
            Cell cell = floor.get(random.nextInt(floor.size()));
            candidates.remove(cell);
            if (cell.viewDist(start) <= distToStart) { // don't place enemies next to starting position
                i--;
                continue;
            }
            floor.remove(cell);
            cell.cellType = item;
        }
    }

    private ArrayList<Cell> generateMazeLayout(Random random) {
        grid = new Cell[Constants.MAZE_WIDTH][Constants.MAZE_HEIGHT];
        ArrayList<Cell> cells = new ArrayList<>();
        for (int x = 0; x < Constants.MAZE_WIDTH; x++) {
            for (int y = 0; y < Constants.MAZE_HEIGHT; y++) {
                grid[x][y] = new Cell(x, y);
                cells.add(grid[x][y]);
            }
        }
        for (Cell cell : cells) {
            cell.initNeighbors(grid);
        }

        grid[random.nextInt(Constants.MAZE_WIDTH)][random.nextInt(Constants.MAZE_HEIGHT)].cellType = Constants.TYPE_FLOOR;
        while (true) {
            ArrayList<Cell> toExpand = new ArrayList<>();
            for (Cell cell : cells) {
                if (cell.cellType == Constants.TYPE_FLOOR && cell.expandCandidates().size() > 0) toExpand.add(cell);
            }

            if (toExpand.size() == 0) break;
            Cell exp = toExpand.get(random.nextInt(toExpand.size()));
            while (exp.expandCandidates().size() > 0) {
                ArrayList<Cell> next = exp.expandCandidates();
                exp = next.get(random.nextInt(next.size()));
                exp.cellType = Constants.TYPE_FLOOR;
            }
        }

        int closers = random.nextInt(10) + 12;
        ArrayList<Cell> closerCells = new ArrayList<>();
        for (int close = 0; close < closers; close++) {
            ArrayList<Cell> cands = new ArrayList<>();
            int penalty = random.nextInt(5); // avoid close edges
            for (Cell cell : cells) {
                if (cell.cellType != Constants.TYPE_OBSTACLE) continue;
                ArrayList<Cell> toConnect = cell.neighbors.stream().filter(n -> n.cellType == Constants.TYPE_FLOOR).collect(Collectors.toCollection(ArrayList::new));
                if (toConnect.size() < 2) continue;
                int dist = 0;
                for (int i = 0; i < toConnect.size(); i++) {
                    int[] dists = toConnect.get(i).BFS();
                    for (int j = i + 1; j < toConnect.size(); j++) {
                        dist = Math.max(dist, dists[toConnect.get(j).id]);
                    }
                }
                for (int i = 0; i < dist - penalty; i++) cands.add(cell); // add multiple times for weighted probability
            }
            if (cands.size() == 0) break;
            closerCells.add(cands.get(random.nextInt(cands.size())));
        }
        for (Cell cell : closerCells) {
            if (random.nextInt(2) == 0) cell.cellType = Constants.TYPE_FLOOR;
        }

        return cells;
    }
}
