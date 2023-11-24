package src.mapeditor.util;

/**
 * The interface using Strategy Pattern
 * Read and convert different map files based on format
 */
public interface MapConverter {
    GameMap convertMapFile(String filename);
}
