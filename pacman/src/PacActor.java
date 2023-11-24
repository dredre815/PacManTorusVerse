// PacActor.java
// Used for PacMan
package src;

import ch.aplu.jgamegrid.Actor;
import ch.aplu.jgamegrid.GGKeyRepeatListener;
import ch.aplu.jgamegrid.Location;
import src.autoMove.DirectMethod;
import src.autoMove.MovingMethod;

import java.awt.*;
import java.awt.event.KeyEvent;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Random;

/**
 * A PacActor is an Actor that can move and eat pills
 */
public class PacActor extends Actor implements GGKeyRepeatListener {
    private static final int nbSprites = 4;
    private int idSprite = 0;
    private int nbPills = 0;
    private int score = 0;
    private src.Game game;
    private ArrayList<Location> visitedList = new ArrayList<>();
    private final int listLength = 100;
    private int seed;
    private Random randomiser = new Random();

    public Random getRandom() {
        return this.randomiser;
    }

    public MovingMethod movingMethod;

    public PacActor(src.Game game) {
        super(true, "sprites/pacpix.gif", nbSprites);  // Rotatable
        this.game = game;
        movingMethod = new DirectMethod();
    }

    private boolean isAuto = false;

    public void setAuto(boolean auto) {
        isAuto = auto;
    }


    public void setSeed(int seed) {
        this.seed = seed;
        randomiser.setSeed(seed);
    }

    public void keyRepeated(int keyCode) {
        if (isAuto) {
            return;
        }
        if (isRemoved())  // Already removed
            return;
        Location next = null;
        switch (keyCode) {
            case KeyEvent.VK_LEFT -> {
                next = getLocation().getNeighbourLocation(Location.WEST);
                setDirection(Location.WEST);
            }
            case KeyEvent.VK_UP -> {
                next = getLocation().getNeighbourLocation(Location.NORTH);
                setDirection(Location.NORTH);
            }
            case KeyEvent.VK_RIGHT -> {
                next = getLocation().getNeighbourLocation(Location.EAST);
                setDirection(Location.EAST);
            }
            case KeyEvent.VK_DOWN -> {
                next = getLocation().getNeighbourLocation(Location.SOUTH);
                setDirection(Location.SOUTH);
            }
        }
        if (next != null && canMove(next)) {
            setLocation(next);
            onPortal(next);
            eatPill(next);
        }
    }

    public void act() {
        show(idSprite);
        idSprite++;
        if (idSprite == nbSprites)
            idSprite = 0;

        // Auto move
        if (isAuto) {
            Location next = movingMethod.getNext(this, game);
            setLocation(next);
            onPortal(next);
            eatPill(next);
            addVisitedList(next);
        }
        this.game.getGameCallback().pacManLocationChanged(getLocation(), score, nbPills);
    }

    private void addVisitedList(Location location) {
        visitedList.add(location);
        if (visitedList.size() == listLength)
            visitedList.remove(0);
    }

    public boolean isVisited(Location location) {
        for (Location loc : visitedList)
            if (loc.equals(location))
                return true;
        return false;
    }

    /**
     * Check if the PacMan can move to the location
     *
     * @param location the location to check
     * @return true if the PacMan can move to the location
     */
    public boolean canMove(Location location) {
        Color c = getBackground().getColor(location);
        return !c.equals(Color.gray) && location.getX() < game.getNumHorzCells()
                && location.getX() >= 0 && location.getY() < game.getNumVertCells() && location.getY() >= 0;
    }

    public int getNbPills() {
        return nbPills;
    }

    /**
     * Check if the PacMan is on a portal and move it to the other portal
     *
     * @param location the location of the PacMan
     */
    private void onPortal(Location location) {
        Color c = getBackground().getColor(location);
        if (c.equals(Color.black)) {
            Map<String, java.util.List<int[]>> portals = game.getCurrentMap().getPortals();
            for (Map.Entry<String, java.util.List<int[]>> entry : portals.entrySet()) {
                List<int[]> locs = entry.getValue();
                if (location.getX() == locs.get(0)[1] && location.getY() == locs.get(0)[0]) {
                    // Set the location to the second loc
                    setLocation(new Location(locs.get(1)[1], locs.get(1)[0]));
                } else if (location.getX() == locs.get(1)[1] && location.getY() == locs.get(1)[0]) {
                    // Set the location to the first loc
                    setLocation(new Location(locs.get(0)[1], locs.get(0)[0]));
                }
            }
        }
    }

    private void eatPill(Location location) {
        Color c = getBackground().getColor(location);
        if (c.equals(Color.white)) {
            nbPills++;
            score++;
            getBackground().fillCell(location, Color.lightGray);
            game.getGameCallback().pacManEatPillsAndItems(location, "pills");
        } else if (c.equals(Color.yellow)) {
            nbPills++;
            score += 5;
            getBackground().fillCell(location, Color.lightGray);
            game.getGameCallback().pacManEatPillsAndItems(location, "gold");
            game.removeItem("gold", location);
        } else if (c.equals(Color.blue)) {
            getBackground().fillCell(location, Color.lightGray);
            game.getGameCallback().pacManEatPillsAndItems(location, "ice");
            game.removeItem("ice", location);
        }
        String title = "[PacMan in the Multiverse] Current score: " + score;
        gameGrid.setTitle(title);
    }
}
