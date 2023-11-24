// PacGrid.java
package src;

import ch.aplu.jgamegrid.*;

public class PacManGameGrid {
    private int nbHorzCells;
    private int nbVertCells;
    private int[][] mazeArray;
    private String maze;

    public PacManGameGrid(int nbHorzCells, int nbVertCells, String map) {
        this.nbHorzCells = nbHorzCells;
        this.nbVertCells = nbVertCells;
        mazeArray = new int[nbVertCells][nbHorzCells];
        maze = map;

        // Convert the maze string to a 2D array
        for (int i = 0; i < nbVertCells; i++) {
            for (int k = 0; k < nbHorzCells; k++) {
                int value = toInt(maze.charAt(nbHorzCells * i + k));
                mazeArray[i][k] = value;
            }
        }
    }

    public int getCell(Location location) {
        return mazeArray[location.y][location.x];
    }

    private int toInt(char c) {
        if (c == 'x')
            return 0;
        if (c == '.')
            return 1;
        if (c == ' ')
            return 2;
        if (c == 'g')
            return 3;
        if (c == 'i')
            return 4;
        if (c == '1')
            return 5;
        if (c == '2')
            return 6;
        if (c == '3')
            return 7;
        if (c == '4')
            return 8;
        return -1;
    }
}
