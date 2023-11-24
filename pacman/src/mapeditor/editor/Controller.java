package src.mapeditor.editor;

import org.jdom.Document;
import org.jdom.Element;
import org.jdom.input.SAXBuilder;
import org.jdom.output.Format;
import org.jdom.output.XMLOutputter;
import src.Game;
import src.mapeditor.grid.*;
import src.mapeditor.util.GameMap;
import src.mapeditor.util.MapConverter;
import src.mapeditor.util.MapLoader;
import src.mapeditor.util.XMLMapConverter;
import src.utility.GameCallback;
import src.utility.PropertiesLoader;

import javax.swing.*;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import javax.swing.filechooser.FileNameExtensionFilter;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Properties;

/**
 * Controller of the application.
 *
 * @author Marshall Zhang
 * @version 2
 * @since v 1.0
 */
public class Controller implements ActionListener, GUIInformation {
    private static final String TMP_FILE = "tmp.xml";

    /**
     * The model of the map editor.
     */
    private Grid model;

    private Tile selectedTile;
    private Camera camera;

    private List<Tile> tiles;

    private GridView grid;
    private View view;

    private int gridWith = Constants.MAP_WIDTH;
    private int gridHeight = Constants.MAP_HEIGHT;
    public static final String DEFAULT_PROPERTIES_FILE = "properties/test.properties";
    MapConverter mapConverter = new XMLMapConverter();
    MapLoader mapLoader = new MapLoader(mapConverter);

    /**
     * Construct the controller. (No map loaded)
     * Init the empty map
     */
    public Controller() {
        init(Constants.MAP_WIDTH, Constants.MAP_HEIGHT);
    }

    /**
     * Construct the controller. (With map loaded)
     *
     * @param mode 0 - load map from xml file; 1 - load map from folder
     * @param path path of the map
     */
    public Controller(int mode, String path) {
        if (mode == 0) {
            // Load the existing map
            mapLoader.loadMap(path);
            init(Constants.MAP_WIDTH, Constants.MAP_HEIGHT);
            loadSingleFile(path);
        } else if (mode == 1) {
            List<GameMap> maps = mapLoader.loadMaps(path);
            if (maps == null) {
                // Failed game checking
                init(Constants.MAP_WIDTH, Constants.MAP_HEIGHT);
            } else if (maps.size() == 1 && !mapLoader.isPassedLevelCheck()) {
                // Failed level checking
                init(Constants.MAP_WIDTH, Constants.MAP_HEIGHT);
                loadSingleFile(maps.get(0).getMap());
            } else {
                // Passed both game and level checking
                final Properties properties = PropertiesLoader.loadPropertiesFile(DEFAULT_PROPERTIES_FILE);
                GameCallback gameCallback = new GameCallback();
                new Game(gameCallback, properties, maps, this, 0, path);
            }
        }
    }

    /**
     * Initialize the map editor.
     *
     * @param width  width of the map
     * @param height height of the map
     */
    public void init(int width, int height) {
        this.tiles = src.mapeditor.editor.TileManager.getTilesFromFolder("data/");
        this.model = new GridModel(width, height, tiles.get(0).getCharacter());
        this.camera = new GridCamera(model, Constants.GRID_WIDTH,
                Constants.GRID_HEIGHT);

        // Every tile is 30x30 pixels
        grid = new GridView(this, camera, tiles);

        this.view = new View(this, camera, grid, tiles);
    }

    /**
     * Different commands that comes from the view.
     */
    @Override
    public void actionPerformed(ActionEvent e) {
        for (src.mapeditor.editor.Tile t : tiles) {
            if (e.getActionCommand().equals(
                    Character.toString(t.getCharacter()))) {
                selectedTile = t;
                break;
            }
        }
        if (e.getActionCommand().equals("flipGrid")) {
            // view.flipGrid();
        } else if (e.getActionCommand().equals("save")) {
            saveFile();
        } else if (e.getActionCommand().equals("load")) {
            loadFile();
        } else if (e.getActionCommand().equals("update")) {
            updateGrid(gridWith, gridHeight);
        } else if (e.getActionCommand().equals("start_game")) {
            // Save the map as a tmp file
            saveTmpFile();
            // Close the editor
            view.close();
            List<GameMap> maps = new ArrayList<>();
            GameMap gameMap = mapLoader.loadMap(TMP_FILE);
            if (gameMap == null) {
                // Failed level checking
                init(Constants.MAP_WIDTH, Constants.MAP_HEIGHT);
                loadSingleFile(TMP_FILE);
            } else {
                // level checking passed
                maps.add(gameMap);
                // Create a new thread to start the game
                Thread t = new Thread(() -> {
                    final Properties properties = PropertiesLoader.loadPropertiesFile(DEFAULT_PROPERTIES_FILE);
                    GameCallback gameCallback = new GameCallback();
                    new Game(gameCallback, properties, maps, this, 1, TMP_FILE);
                });
                t.start();
            }
        }
    }

    public void updateGrid(int width, int height) {
        view.close();
        // Init the new grid
        init(width, height);
        view.setSize(width, height);
    }

    DocumentListener updateSizeFields = new DocumentListener() {

        public void changedUpdate(DocumentEvent e) {
            gridWith = view.getWidth();
            gridHeight = view.getHeight();
        }

        public void removeUpdate(DocumentEvent e) {
            gridWith = view.getWidth();
            gridHeight = view.getHeight();
        }

        public void insertUpdate(DocumentEvent e) {
            gridWith = view.getWidth();
            gridHeight = view.getHeight();
        }
    };

    private File promptUserForFile(String dialogTitle, FileNameExtensionFilter filter) {
        JFileChooser chooser = new JFileChooser();
        chooser.setFileFilter(filter);
        chooser.setCurrentDirectory(new File(System.getProperty("user.dir")));
        int returnVal = chooser.showOpenDialog(null);
        return returnVal == JFileChooser.APPROVE_OPTION ? chooser.getSelectedFile() : null;
    }

    private Document generateDocumentFromModel() {
        Element level = new Element("level");
        Document doc = new Document(level);

        Element size = new Element("size");
        size.addContent(new Element("width").setText(String.valueOf(model.getWidth())));
        size.addContent(new Element("height").setText(String.valueOf(model.getHeight())));
        doc.getRootElement().addContent(size);

        // Add the tiles
        for (int y = 0; y < model.getHeight(); y++) {
            Element row = new Element("row");
            for (int x = 0; x < model.getWidth(); x++) {
                String type = getTileType(model.getTile(x, y));
                Element e = new Element("cell");
                row.addContent(e.setText(type));
            }
            doc.getRootElement().addContent(row);
        }
        return doc;
    }

    private void saveDocumentToFile(Document doc, File file) throws IOException {
        XMLOutputter xmlOutput = new XMLOutputter();
        xmlOutput.setFormat(Format.getPrettyFormat());
        xmlOutput.output(doc, new FileWriter(file));
    }

    /**
     * Get the tile type from the tile character.
     *
     * @param tileChar tile character
     * @return tile type
     */
    private String getTileType(char tileChar) {
        return switch (tileChar) {
            case 'b' -> "WallTile";
            case 'c' -> "PillTile";
            case 'd' -> "GoldTile";
            case 'e' -> "IceTile";
            case 'f' -> "PacTile";
            case 'g' -> "TrollTile";
            case 'h' -> "TX5Tile";
            case 'i' -> "PortalWhiteTile";
            case 'j' -> "PortalYellowTile";
            case 'k' -> "PortalDarkGoldTile";
            case 'l' -> "PortalDarkGrayTile";
            default -> "PathTile";
        };
    }

    private void saveFile() {
        try {
            File selectedFile = promptUserForFile("Save File",
                    new FileNameExtensionFilter("xml files", "xml"));
            if (selectedFile != null) {
                Document doc = generateDocumentFromModel();
                saveDocumentToFile(doc, selectedFile);
            }
        } catch (IOException e) {
            JOptionPane.showMessageDialog(null, "Error while saving file",
                    "Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    /**
     * Save the map as a tmp file
     */
    public void saveTmpFile() {
        try {
            Document doc = generateDocumentFromModel();
            saveDocumentToFile(doc, new File(TMP_FILE));
        } catch (IOException e) {
            JOptionPane.showMessageDialog(null, "Error while saving file",
                    "Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void updateGridFromDocument(Document document) {
        Element rootNode = document.getRootElement();
        Element sizeElem = rootNode.getChild("size");
        int height = Integer.parseInt(sizeElem.getChildText("height"));
        int width = Integer.parseInt(sizeElem.getChildText("width"));
        updateGrid(width, height);

        // Add the tiles
        List<Element> rows = rootNode.getChildren("row");
        for (int y = 0; y < rows.size(); y++) {
            List<Element> cells = rows.get(y).getChildren("cell");
            for (int x = 0; x < cells.size(); x++) {
                String cellValue = cells.get(x).getText();
                model.setTile(x, y, getTileCharacter(cellValue));
            }
        }
        String mapString = model.getMapAsString();
        grid.redrawGrid();
    }

    /**
     * Returns the character representation of the tile type
     *
     * @param cellValue the tile type
     * @return the character representation of the tile type
     */
    private char getTileCharacter(String cellValue) {
        return switch (cellValue) {
            case "WallTile" -> 'b';
            case "PillTile" -> 'c';
            case "GoldTile" -> 'd';
            case "IceTile" -> 'e';
            case "PacTile" -> 'f';
            case "TrollTile" -> 'g';
            case "TX5Tile" -> 'h';
            case "PortalWhiteTile" -> 'i';
            case "PortalYellowTile" -> 'j';
            case "PortalDarkGoldTile" -> 'k';
            case "PortalDarkGrayTile" -> 'l';
            default -> 'a';
        };
    }

    public void loadFile() {
        try {
            File selectedFile = promptUserForFile("Open File",
                    new FileNameExtensionFilter("xml files", "xml"));
            if (selectedFile != null && selectedFile.canRead()) {
                SAXBuilder builder = new SAXBuilder();
                Document document = builder.build(selectedFile);
                updateGridFromDocument(document);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * Loads a single file from the given file name
     *
     * @param fileName the name of the file to load
     */
    public void loadSingleFile(String fileName) {
        try {
            File selectedFile = new File(fileName);
            if (selectedFile.canRead()) {
                SAXBuilder builder = new SAXBuilder();
                Document document = builder.build(selectedFile);
                updateGridFromDocument(document);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }


    /**
     * {@inheritDoc}
     */
    @Override
    public src.mapeditor.editor.Tile getSelectedTile() {
        return selectedTile;
    }
}
