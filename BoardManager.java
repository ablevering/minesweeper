package board;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.Random;

public class BoardManager {

    private int[][] board;
    private int width;
    private int height;
    private Random r = new Random();
    private HashSet<Integer> mineSet = new HashSet<Integer>();
    HashSet<Integer> clears = new HashSet<Integer>();

    public BoardManager(int mineCount, int location, int width, int height) {
        this.width = width;
        this.height = height;
        board = new int[width][height];
        ArrayList<Integer> adjacents = getAdjacents(location);

        // Place the mines, avoiding the location that was clicked.
        while (mineCount > 0) {
            int x = r.nextInt(width);
            int y = r.nextInt(height);
            int l = x + (y * height);
            if (!adjacents.contains(l) && board[x][y] != -1) {
                board[x][y] = -1;
                mineCount--;
            }
        }

        adjustAdjacentCounts();
    }

    /**
     * Returns a list of the adjacent tiles to a location.
     * 
     * @param location The location that was clicked.
     * @return An ArrayList<Integer> containing all the adjacent tiles of the
     *         clicked location.
     */
    public ArrayList<Integer> getAdjacents(int location) {
        int x = location % width;
        int y = location / height;
        ArrayList<Integer> adjacents = new ArrayList<Integer>();
        adjacents.add(x + (y * height));
        if (y + 1 < height)
            adjacents.add(x + ((y + 1) * height));
        if (y - 1 >= 0)
            adjacents.add(x + ((y - 1) * height));
        if (x + 1 < width) {
            adjacents.add((x + 1) + (y * height));
            if (y + 1 < height)
                adjacents.add((x + 1) + ((y + 1) * height));
            if (y - 1 >= 0)
                adjacents.add((x + 1) + ((y - 1) * height));
        }
        if (x - 1 >= 0) {
            adjacents.add((x - 1) + (y * height));
            if (y + 1 < height)
                adjacents.add((x - 1) + ((y + 1) * height));
            if (y - 1 >= 0)
                adjacents.add((x - 1) + ((y - 1) * height));
        }
        return adjacents;
    }

    /**
     * Iterates through the entire board, increasing the adjacent values whenever a
     * mine is found.
     */
    private void adjustAdjacentCounts() {
        for (int i = 0; i < width; i++) {
            for (int j = 0; j < height; j++) {
                if (board[i][j] < 0) {
                    mineSet.add(i + j * height);
                    // All adjacent non-mines need to be added to.
                    if (i + 1 < width) {
                        increment(i + 1, j);
                        if (j + 1 < height)
                            increment(i + 1, j + 1);
                        if (j - 1 >= 0)
                            increment(i + 1, j - 1);
                    }
                    if (i - 1 >= 0) {
                        increment(i - 1, j);
                        if (j + 1 < height)
                            increment(i - 1, j + 1);
                        if (j - 1 >= 0)
                            increment(i - 1, j - 1);
                    }
                    if (j + 1 < height)
                        increment(i, j + 1);
                    if (j - 1 >= 0)
                        increment(i, j - 1);
                }
            }
        }
    }

    private void increment(int i, int j) {
        // Check to make sure that a mine is not being added to.
        if (board[i][j] >= 0)
            board[i][j]++;
    }

    /**
     * Returns the mineCount of tile at location.
     * 
     * @param location the integer location of the tile
     * @return the adjacent mineCount of the tile at location.
     */
    public int revealTile(int location) {
        int x = location % width;
        int y = location / height;
        return board[x][y];
    }

    /**
     * Clears away the board when a zero is clicked. An implementation of the
     * flood-fill/cascade algorithm.
     * 
     * @param x
     * @param y
     */
    public void getAdjacentZeroes(int x, int y) {
        if (x >= 0 && x < width && y >= 0 && y < height && board[x][y] == 0 && !clears.contains(getLocation(x, y))) {
            clears.add(getLocation(x, y));
            getAdjacentZeroes(x + 1, y);
            getAdjacentZeroes(x - 1, y);
            getAdjacentZeroes(x, y + 1);
            getAdjacentZeroes(x, y - 1);
            getAdjacentZeroes(x + 1, y + 1);
            getAdjacentZeroes(x + 1, y - 1);
            getAdjacentZeroes(x - 1, y + 1);
            getAdjacentZeroes(x - 1, y - 1);
        } else if (x >= 0 && x < width && y >= 0 && y < height && board[x][y] > 0
                && !clears.contains(getLocation(x, y))) {
            // A number greater than zero was found, so the recursion should not continue
            clears.add(getLocation(x, y));
        }
    }

    public HashSet<Integer> getClears() {
        return clears;
    }

    public HashSet<Integer> getMineSet() {
        return mineSet;
    }

    private int getLocation(int x, int y) {
        return x + (y * height);
    }
}
