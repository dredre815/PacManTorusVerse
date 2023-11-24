// Monster.java
// Used for PacMan
package src;

import ch.aplu.jgamegrid.*;

import java.awt.Color;
import java.util.*;

public class Monster extends Actor {
    private src.Game game;
    private src.MonsterType type;
    private ArrayList<Location> visitedList = new ArrayList<>();
    private final int listLength = 10;
    private boolean stopMoving = false;
    private int seed = 0;
    private Random randomiser = new Random(0);

    public Monster(Game game, MonsterType type) {
        super("sprites/" + type.getImageName());
        this.game = game;
        this.type = type;
    }

    public void stopMoving(int seconds) {
        this.stopMoving = true;
        Timer timer = new Timer(); // Instantiate Timer Object
        int SECOND_TO_MILLISECONDS = 1000;
        final Monster monster = this;
        timer.schedule(new TimerTask() {
            @Override
            public void run() {
                monster.stopMoving = false;
            }
        }, seconds * SECOND_TO_MILLISECONDS);
    }

    public void setSeed(int seed) {
        this.seed = seed;
        randomiser.setSeed(seed);
    }

    public void setStopMoving(boolean stopMoving) {
        this.stopMoving = stopMoving;
    }

    public void act() {
        if (stopMoving) {
            return;
        }
        walkApproach();
        setHorzMirror(!(getDirection() > 150) || !(getDirection() < 210));
    }

    private void walkApproach() {
        Location pacLocation = game.pacActor.getLocation();
        double oldDirection = getDirection();
        Location locationNow = this.getLocation();
        // If the monster is on a portal, move it to the other portal.
        if (onPortal(locationNow)) {
            movePortal(locationNow);
        }

        // Walking approach:
        // TX5: Determine direction to pacActor and try to move in that direction. Otherwise, random walk.
        // Troll: Random walk.
        Location.CompassDirection compassDir =
                getLocation().get4CompassDirectionTo(pacLocation);
        Location next = getLocation().getNeighbourLocation(compassDir);
        setDirection(compassDir);
        if (type == src.MonsterType.TX5 &&
                !isVisited(next) && canMove(next)) {
                setLocation(next);
        } else {
            // Random walk
            int sign = randomiser.nextDouble() < 0.5 ? 1 : -1;
            setDirection(oldDirection);
            turn(sign * 90);  // Try to turn left/right
            next = getNextMoveLocation();
            if (canMove(next)) {
                setLocation(next);
            } else {
                setDirection(oldDirection);
                next = getNextMoveLocation();
                if (canMove(next)) // Try to move forward
                {
                    setLocation(next);
                } else {
                    setDirection(oldDirection);
                    turn(-sign * 90);  // Try to turn right/left
                    next = getNextMoveLocation();
                    if (canMove(next)) {
                        setLocation(next);
                    } else {
                        setDirection(oldDirection);
                        turn(180);  // Turn backward
                        next = getNextMoveLocation();
                        setLocation(next);
                    }
                }
            }
        }
        game.getGameCallback().monsterLocationChanged(this);
        addVisitedList(next);
    }

    public MonsterType getType() {
        return type;
    }

    private void addVisitedList(Location location) {
        visitedList.add(location);
        if (visitedList.size() == listLength)
            visitedList.remove(0);
    }

    private boolean isVisited(Location location) {
        for (Location loc : visitedList)
            if (loc.equals(location))
                return true;
        return false;
    }

    private boolean canMove(Location location) {
        Color c = getBackground().getColor(location);
        return !c.equals(Color.gray) && location.getX() < game.getNumHorzCells()
                && location.getX() >= 0 && location.getY() < game.getNumVertCells() && location.getY() >= 0;
    }

    /**
     * Check if the monster is on a portal.
     * @param location The location of the monster.
     * @return True if the monster is on a portal, false otherwise.
     */
    private boolean onPortal(Location location) {
        Color c = getBackground().getColor(location);
        return c.equals(Color.black);
    }

    /**
     * Move the monster to the other portal.
     * @param location The location of the monster.
     */
    private void movePortal(Location location) {
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
