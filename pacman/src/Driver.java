package src;

import src.mapeditor.editor.Controller;

public class Driver {
    private static final int VALID_ARGS_LENGTH = 1;
    private static final String XML_SUFFIX = ".xml";

    /**
     * Starting point
     *
     * @param args the command line arguments
     */
    public static void main(String[] args) {
        if (args.length == VALID_ARGS_LENGTH) {
            // Check the type of the argument
            if (args[0].endsWith(XML_SUFFIX)) {
                // An existing map file
                new Controller(0, args[0]);
            } else {
                // A map folder
                new Controller(1, args[0]);
            }
        } else if (args.length == 0) {
            // No argument, start the edit mode with no current map
            new Controller();
        } else {
            System.out.println("Invalid command line argument, please try again.");
        }
    }
}
