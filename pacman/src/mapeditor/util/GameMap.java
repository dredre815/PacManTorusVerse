package src.mapeditor.util;

import java.util.List;
import java.util.Map;

/**
 * GameMap class to store the map information
 */
public class GameMap {

    private final static int DEFAULT_NBHORZCELLS = 20;
    private final static int DEFAULT_NBVERTCELLS = 11;

    private int nbHorzCells;
    private int nbVertCells;
    private Map<String, List<int[]>> actors;
    private Map<String, List<int[]>> portals;
    private String map;

    /**
     * Constructor
     *
     * @param actors  actors in the map
     * @param portals portals in the map
     * @param map     map in string
     */
    public GameMap(Map<String, List<int[]>> actors, Map<String, List<int[]>> portals, String map) {
        this.nbHorzCells = DEFAULT_NBHORZCELLS;
        this.nbVertCells = DEFAULT_NBVERTCELLS;
        this.actors = actors;
        this.portals = portals;
        this.map = map;
    }

    public GameMap(String map) {
        this.map = map;
    }

    public Map<String, List<int[]>> getActors() {
        return actors;
    }

    public String getMap() {
        return map;
    }

    public Map<String, List<int[]>> getPortals() {
        return portals;
    }
}