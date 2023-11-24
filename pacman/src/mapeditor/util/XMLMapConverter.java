package src.mapeditor.util;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Read and convert XML map files
 */
public class XMLMapConverter implements MapConverter {
    /**
     * Convert XML file into GameMap
     *
     * @param filename the name of the file
     * @return GameMap
     */
    @Override
    public GameMap convertMapFile(String filename) {
        // Maps to hold the actors and portals in the game.
        Map<String, List<int[]>> actors = new HashMap<>();
        Map<String, List<int[]>> portals = new HashMap<>();

        try {
            Document document = parseDocument(filename);

            // Process the document to create the map representation.
            StringBuilder sb = processDocument(document, actors, portals);

            return new GameMap(actors, portals, sb.toString());

        } catch (ParserConfigurationException | SAXException | IOException e) {
            throw new RuntimeException(e);
        }
    }

    /**
     * Parse the XML document
     *
     * @param filename the name of the file
     * @return Document
     * @throws ParserConfigurationException the ParserConfigurationException
     * @throws SAXException                 the SAXException
     * @throws IOException                  the IOException
     */
    private Document parseDocument(String filename) throws ParserConfigurationException, SAXException, IOException {
        DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
        DocumentBuilder builder = factory.newDocumentBuilder();
        return builder.parse(new File(filename));
    }

    /**
     * Process the document to create the map representation
     *
     * @param document the document
     * @param actors   the actors
     * @param portals  the portals
     * @return StringBuilder
     */
    private StringBuilder processDocument(Document document, Map<String, List<int[]>> actors, Map<String,
            List<int[]>> portals) {
        Element root = document.getDocumentElement();
        NodeList rows = root.getElementsByTagName("row");
        StringBuilder sb = new StringBuilder();

        for (int i = 0; i < rows.getLength(); i++) {
            Element row = (Element) rows.item(i);

            // Get all cell nodes in the current row.
            NodeList cells = row.getElementsByTagName("cell");

            for (int j = 0; j < cells.getLength(); j++) {
                Element cell = (Element) cells.item(j);
                String cellValue = cell.getTextContent();

                // Determine what character to append to the map representation based on the cell value.
                switch (cellValue) {
                    case "WallTile" -> sb.append("x");
                    case "PillTile" -> {
                        sb.append(".");
                        addActor(actors, "pill", new int[]{j, i});
                    }
                    case "PacTile" -> {
                        addActor(actors, "pacman", new int[]{j, i});
                        sb.append(" ");
                    }
                    case "TX5Tile" -> {
                        addActor(actors, "TX5", new int[]{j, i});
                        sb.append(" ");
                    }
                    case "TrollTile" -> {
                        addActor(actors, "Troll", new int[]{j, i});
                        sb.append(" ");
                    }
                    case "GoldTile" -> {
                        sb.append("g");
                        addActor(actors, "Gold", new int[]{j, i});
                    }
                    case "IceTile" -> sb.append("i");
                    case "PortalWhiteTile" -> {
                        sb.append("1");
                        addActor(portals, "PortalWhiteTile", new int[]{i, j});
                    }
                    case "PortalYellowTile" -> {
                        sb.append("2");
                        addActor(portals, "PortalYellowTile", new int[]{i, j});
                    }
                    case "PortalDarkGoldTile" -> {
                        sb.append("3");
                        addActor(portals, "PortalDarkGoldTile", new int[]{i, j});
                    }
                    case "PortalDarkGrayTile" -> {
                        sb.append("4");
                        addActor(portals, "PortalDarkGrayTile", new int[]{i, j});
                    }
                    default -> sb.append(" ");
                }
            }
        }
        return sb;
    }

    /**
     * Add an actor to the map
     *
     * @param actors    the map of actors
     * @param actorName the name of the actor
     * @param position  the position of the actor
     */
    private void addActor(Map<String, List<int[]>> actors, String actorName, int[] position) {
        if (!actors.containsKey(actorName)) {
            actors.put(actorName, new java.util.ArrayList<>());
        }
        actors.get(actorName).add(position);
    }
}