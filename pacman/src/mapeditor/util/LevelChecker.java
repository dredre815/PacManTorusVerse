package src.mapeditor.util;

import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.*;

/**
 * Static class for checking the validity of the level (single map)
 */
public class LevelChecker {
    private static final String logOutput = "Log.txt";

    /**
     * Check if the level is valid
     *
     * @param level level
     * @return true if the level is valid
     */
    public static boolean levelCheck(String level, GameMap gameMap) {
        PrintWriter logWriter = null;

        try {
            logWriter = new PrintWriter(new FileWriter(logOutput));
        } catch (IOException e) {
            e.printStackTrace();
        }

        boolean check1 = onePacman(level, gameMap, logWriter);
        boolean check2 = twoTiles(level, gameMap, logWriter);
        boolean check3 = checkPG(level, gameMap, logWriter);
        boolean check4 = isAccessible(level, gameMap, check1, logWriter);

        if (logWriter != null) {
            logWriter.close();
        }

        return check1 && check2 && check3 && check4;
    }

    /**
     * Check 1
     * Check if there is exactly one starting point for PacMan
     *
     * @param level   level
     * @param gameMap game map
     * @return true if there is exactly one starting point for PacMan
     */
    public static boolean onePacman(String level, GameMap gameMap, PrintWriter logWriter) {
        Map<String, List<int[]>> actors = gameMap.getActors();
        // Check if there is no starting point for PacMan
        if (!actors.containsKey("pacman")) {
            if (logWriter != null) {
                logWriter.println("Level " + level + " - no start for PacMan");
            }
            return false;
        }
        // Check if there is more than one starting point for PacMan
        List<int[]> pacmansList = actors.get("pacman");
        if (pacmansList.size() != 1) {
            StringBuilder startPointListString = new StringBuilder();
            for (int[] coords : pacmansList) {
                startPointListString.append("(").append(coords[1] + 1).append(",").append(coords[0] + 1).append("); ");
            }
            // Remove last "; " from the stringBuilder
            startPointListString.setLength(startPointListString.length() - 2);
            if (logWriter != null) {
                logWriter.println("Level " + level + " - more than one start for Pacman: " + startPointListString);
            }
            return false;
        }
        return true;
    }

    /**
     * Check 2
     * Check if there are exactly two tiles for each portal appearing on the map
     *
     * @param level   level
     * @param gameMap game map
     * @return true if there are exactly two tiles for each portal appearing on the map
     */
    public static boolean twoTiles(String level, GameMap gameMap, PrintWriter logWriter) {
        Map<String, List<int[]>> portals = gameMap.getPortals();
        for (Map.Entry<String, List<int[]>> entry : portals.entrySet()) {
            String key = entry.getKey();
            List<int[]> value = entry.getValue();

            // Check if there are exactly two tiles for each portal appearing on the map
            if (key.equals("PortalDarkGoldTile") || key.equals("PortalDarkGrayTile")
                    || key.equals("PortalWhiteTile") || key.equals("PortalYellowTile")) {
                if (value.size() != 2) {
                    StringBuilder portalCoordinatesString = new StringBuilder();
                    for (int[] coords : value) {
                        portalCoordinatesString.append("(").append(coords[1] + 1).append(",")
                                .append(coords[0] + 1).append(")" + "; ");
                    }

                    // Remove the last "; " from the stringBuilder
                    portalCoordinatesString.setLength(portalCoordinatesString.length() - 2);

                    if (logWriter != null) {
                        logWriter.println("Level " + level + " - portal " + key
                                + " count is not 2: " + portalCoordinatesString);
                    }
                    return false;
                }
            }
        }
        return true;
    }

    /**
     * Check 3
     * Check if there are at least totally two pills and golds
     *
     * @param level   level
     * @param gameMap game map
     * @return true if there are at least totally two pills and golds
     */
    public static boolean checkPG(String level, GameMap gameMap, PrintWriter logWriter) {
        Map<String, List<int[]>> actors = gameMap.getActors();
        int numPills = 0;
        int numGolds = 0;
        for (Map.Entry<String, List<int[]>> entry : actors.entrySet()) {
            String key = entry.getKey();

            if (key.equals("pill")) {
                numPills += entry.getValue().size();
            } else if (key.equals("Gold")) {
                numGolds += entry.getValue().size();
            }
        }

        // Check if there are less than 2 pills or golds
        if (numPills < 2 || numGolds < 2) {
            if (logWriter != null) {
                logWriter.println("Level " + level + " - less than 2 Gold and Pill");
            }
            return false;
        } else {
            return true;
        }
    }

    /**
     * Check 4
     * Check if all golds and pills are accessible for PacMan
     *
     * @param gameMap game map
     * @return true if all elements are accessible
     */
    public static boolean isAccessible(String level, GameMap gameMap, boolean check1, PrintWriter logWriter) {
        List<String> goldPositions = new ArrayList<>();
        List<String> pillPositions = new ArrayList<>();
        String grid = gameMap.getMap();

        // If the check1 is false, all golds and pills are not accessible
        if (!check1) {
            for (int i = 0; i < grid.length(); i++) {
                if (grid.charAt(i) == 'g') {
                    goldPositions.add("(" + ((i % 20) + 1) + "," + ((i / 20) + 1) + ")");
                } else if (grid.charAt(i) == '.') {
                    pillPositions.add("(" + ((i % 20) + 1) + "," + ((i / 20) + 1) + ")");
                }
            }
        } else {
            int row = gameMap.getActors().get("pacman").get(0)[1];
            int col = gameMap.getActors().get("pacman").get(0)[0];
            Map<String, List<int[]>> portals = gameMap.getPortals();

            char[][] grid2D = new char[11][20];
            for (int i = 0; i < 11; i++) {
                for (int j = 0; j < 20; j++) {
                    grid2D[i][j] = grid.charAt(i * 20 + j);
                }
            }

            Queue<Integer[]> queue = new LinkedList<>();
            boolean[][] visited = new boolean[11][20];
            visited[row][col] = true;
            int count = 0;

            for (int i = 0; i < grid.length(); i++) {
                if (grid.charAt(i) == 'g' || grid.charAt(i) == '.') {
                    count++;
                }
            }

            queue.add(new Integer[]{row, col});

            // BFS
            while (!queue.isEmpty()) {
                Integer[] curr = queue.poll();
                int r = curr[0];
                int c = curr[1];

                if (grid2D[r][c] == 'g' || grid2D[r][c] == '.') {
                    count--;
                } else if (grid2D[r][c] == 'g') {
                    goldPositions.add("(" + (c + 1) + "," + (r + 1) + ")");
                } else if (grid2D[r][c] == '.') {
                    pillPositions.add("(" + (c + 1) + "," + (r + 1) + ")");
                }

                if (count == 0) {
                    return true;
                }

                // Check if the current tile is a portal
                for (List<int[]> portalLocs : portals.values()) {
                    for (int[] loc : portalLocs) {
                        if (r == loc[0] && c == loc[1]) {
                            for (int[] otherLoc : portalLocs) {
                                if (otherLoc != loc && !visited[otherLoc[0]][otherLoc[1]]) {
                                    queue.add(new Integer[]{otherLoc[0], otherLoc[1]});
                                    visited[otherLoc[0]][otherLoc[1]] = true;
                                }
                            }
                        }
                    }
                }

                int[][] dirs = {{-1, 0}, {1, 0}, {0, -1}, {0, 1}};
                for (int[] dir : dirs) {
                    int newRow = r + dir[0];
                    int newCol = c + dir[1];

                    if (newRow >= 0 && newRow < 11 && newCol >= 0 && newCol < 20
                            && grid2D[newRow][newCol] != 'x' && !visited[newRow][newCol]) {
                        visited[newRow][newCol] = true;
                        queue.add(new Integer[]{newRow, newCol});
                    }
                }
            }
        }

        if (!goldPositions.isEmpty()) {
            if (logWriter != null) {
                logWriter.println("Level " + level + " – Gold not accessible: " +
                        String.join("; ", goldPositions));
            }
        }

        if (!pillPositions.isEmpty()) {
            if (logWriter != null) {
                logWriter.println("Level " + level + " – Pill not accessible: " +
                        String.join("; ", pillPositions));
            }
        }

        return false;
    }
}

