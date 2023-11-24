package src.mapeditor.util;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Static class to check if the game map folder is valid.
 */
public class GameChecker {
    private static final String logOutput = "Log.txt";

    /**
     * Check if the list of files in the folder is valid
     *
     * @param folderName folder name
     * @return valid maps
     */
    public static List<File> gameCheck(String folderName) {

        File folder = new File(folderName);

        // Use a HashMap to store level with corresponding files
        Map<Integer, List<File>> levelMaps = new HashMap<>();

        // Check if the folder is valid
        if (!folder.isDirectory()) {
            return null;
        }

        // Check if the folder is empty
        if (Objects.requireNonNull(folder.listFiles()).length == 0) {
            return null;
        }

        File[] files = folder.listFiles();
        List<File> validMaps = new ArrayList<>();

        PrintWriter logWriter = null;

        try {
            logWriter = new PrintWriter(new FileWriter(logOutput));
        } catch (IOException e) {
            e.printStackTrace();
        }

        assert files != null;
        // Check if the files are valid
        for (File file : files) {
            Pattern pattern = Pattern.compile("^(\\d+).*\\.xml");
            Matcher matcher = pattern.matcher(file.getName());
            if (matcher.find()) {
                int level = Integer.parseInt(matcher.group(1));
                levelMaps.putIfAbsent(level, new ArrayList<>());
                levelMaps.get(level).add(file);
                validMaps.add(file);
            }
        }

        // Check if there are multiple maps at same level
        for (Map.Entry<Integer, List<File>> entry : levelMaps.entrySet()) {
            if (entry.getValue().size() > 1 && logWriter != null) {
                logWriter.print("Game " + folderName + " - " +
                        "multiple maps at same level: ");
                for (File file : entry.getValue()) {
                    logWriter.print(file.getName() + "; ");
                }
                logWriter.println();
                logWriter.close();
                return null;
            }
        }

        // Check if there are no valid maps
        if (validMaps.size() == 0 && logWriter != null) {
            logWriter.println("Game " + folderName + " - " + "no maps found");
            logWriter.close();
            return null;
        }

        // sort the levels
        validMaps.sort(Comparator.comparing(File::getName));

        if (logWriter != null) {
            logWriter.close();
        }

        return validMaps;
    }
}
