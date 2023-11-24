package src.mapeditor.util;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

/**
 * The context class ]for the strategy pattern
 * Load map file or folder
 */
public class MapLoader {
    private final MapConverter mapConverter;
    private boolean passedLevelCheck;

    /**
     * Constructor
     *
     * @param mapConverter map converter
     */
    public MapLoader(MapConverter mapConverter) {
        this.mapConverter = mapConverter;
        this.passedLevelCheck = true;
    }

    /**
     * Load a single map
     *
     * @param arg map file path
     * @return GameMap
     */
    public GameMap loadMap(String arg) {
        File file = new File(arg);
        if (file.isFile()) {
            GameMap map = mapConverter.convertMapFile(file.getPath());
            // check level
            boolean isValidMap = LevelChecker.levelCheck(arg, map);
            if (!isValidMap) return null;
            return map;
        }
        return null;
    }

    /**
     * Load map folder
     *
     * @param arg map folder path
     * @return List<GameMap>
     */
    public List<GameMap> loadMaps(String arg) {
        List<GameMap> maps = new ArrayList<>();

        // check folder first
        List<File> validMaps = GameChecker.gameCheck(arg);
        if (validMaps == null) {
            return null;
        }
        for (File file : validMaps) {
            if (file.isFile()) {
                GameMap map = mapConverter.convertMapFile(file.getPath());
                // check level
                boolean isValidMap = LevelChecker.levelCheck(file.getPath(), map);
                if (!isValidMap) {
                    this.passedLevelCheck = false;
                    GameMap invalidMap = new GameMap(file.getPath());
                    return List.of(invalidMap);
                }
                maps.add(map);
            }
        }

        this.passedLevelCheck = true;
        return maps;
    }

    public boolean isPassedLevelCheck() {
        return this.passedLevelCheck;
    }
}