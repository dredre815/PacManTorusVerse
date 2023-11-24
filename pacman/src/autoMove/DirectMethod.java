package src.autoMove;

import ch.aplu.jgamegrid.Location;
import src.Game;
import src.PacActor;

import java.awt.*;
import java.util.List;
import java.util.*;

/**
 * Using A* algorithm to find the shortest path to the closest item.
 */
public class DirectMethod implements MovingMethod {
    private static final int turnAngle = 90;

    /**
     * Enum to represent the different directions.
     */
    enum Direction {
        UP(-1, 0),
        DOWN(1, 0),
        LEFT(0, -1),
        RIGHT(0, 1);

        int dx, dy;

        Direction(int dx, int dy) {
            this.dx = dx;
            this.dy = dy;
        }

        public static Direction[] directions = {UP, DOWN, LEFT, RIGHT};
    }

    /**
     * Using the result of the A* algorithm to build the path.
     *
     * @param pacman The PacMan.
     * @param game   The current game.
     * @return The path.
     */
    @Override
    public Location getNext(PacActor pacman, Game game) {
        // Use A* algorithm to find the shortest path
        List<Location> path = aStarSearch(pacman, game);
        Location nextLocation;

        // Set the next location if there is a path
        if (path.size() > 1) {
            nextLocation = path.get(1);
        } else {
            nextLocation = pacman.getLocation();
        }

        // Decide the next location
        return decideNextLocation(pacman, nextLocation);
    }

    /**
     * Check if the location is a valid location.
     *
     * @param pacman       The PacMan.
     * @param nextLocation The location to check.
     * @return The next location if it is valid. Otherwise, try other directions.
     */
    private Location decideNextLocation(PacActor pacman, Location nextLocation) {
        double oldDirection = pacman.getDirection();

        // Get the direction to the closest item
        Location.CompassDirection compassDir =
                pacman.getLocation().get4CompassDirectionTo(nextLocation);
        Location next = pacman.getLocation().getNeighbourLocation(compassDir);

        // Set the direction to the closest item
        pacman.setDirection(compassDir);

        // Check if the pacman can move to the next location
        if (pacman.isVisited(next) || !pacman.canMove(next)) {
            Random randomiser = pacman.getRandom();
            int sign = randomiser.nextDouble() < 0.5 ? 1 : -1;
            pacman.setDirection(oldDirection);
            pacman.turn(sign * turnAngle);
            next = pacman.getNextMoveLocation();
            if (!pacman.canMove(next)) {
                // If the pacman cannot move to the next location, try other directions
                next = tryOtherDirections(pacman, oldDirection, sign);
            }
        }
        return next;
    }

    /**
     * Trying other directions if the pacman cannot move to the next location.
     *
     * @param pacman       The PacMan.
     * @param oldDirection The old direction.
     * @param sign         The sign of the turn angle.
     * @return The next location if it is valid. Otherwise, return the current location.
     */
    private Location tryOtherDirections(PacActor pacman, double oldDirection, int sign) {
        Location next;
        pacman.setDirection(oldDirection);
        next = pacman.getNextMoveLocation();
        // If the pacman cannot move to the next location, try other directions
        if (!pacman.canMove(next)) {
            pacman.setDirection(oldDirection);
            pacman.turn(-sign * turnAngle);
            next = pacman.getNextMoveLocation();
            if (!pacman.canMove(next)) {
                pacman.setDirection(oldDirection);
                pacman.turn(turnAngle + turnAngle);
                next = pacman.getNextMoveLocation();
            }
        }
        return next;
    }

    /**
     * A* algorithm to find the shortest path to the closest item.
     *
     * @param pacman The PacMan.
     * @param game   The current game.
     * @return The list of locations in the path.
     */
    private List<Location> aStarSearch(PacActor pacman, Game game) {
        // Using priority queue to get the node with the lowest cost
        PriorityQueue<Node> openList = new PriorityQueue<>();
        Set<Node> closedList = new HashSet<>();

        Node startNode = new Node(pacman.getLocation());
        openList.add(startNode);

        // Loop until the open list is empty
        while (!openList.isEmpty()) {
            Node currentNode = openList.poll();
            closedList.add(currentNode);

            // If we reach the goal, build and return the path
            if (goalTest(currentNode, game)) {
                return buildPath(currentNode);
            }

            // Get neighbors
            List<Node> neighbors = getNeighbors(currentNode, pacman, game);

            for (Node neighbor : neighbors) {
                if (closedList.contains(neighbor)) {
                    continue;
                }

                Node existingNode = findNode(neighbor, openList);
                if (existingNode == null) {
                    openList.add(neighbor);
                } else {
                    // If the neighbor is in the open list, check if the new path is better
                    // If it's better, update the cost and the path
                    if (neighbor.g < existingNode.g) {
                        existingNode.g = neighbor.g;
                        existingNode.parent = neighbor.parent;
                        openList.remove(existingNode);
                        openList.add(existingNode);
                    }
                }
            }
        }

        // No path found
        return new ArrayList<>();
    }

    class Node implements Comparable<Node> {
        Location location;
        Node parent;
        int g;

        public Node(Location location) {
            this.location = location;
        }

        @Override
        public int compareTo(Node other) {
            return Integer.compare(this.g + heuristic(this.location, other.location),
                    other.g + heuristic(other.location, this.location));
        }

        @Override
        public boolean equals(Object obj) {
            if (this == obj) return true;
            if (obj == null || getClass() != obj.getClass()) return false;
            Node node = (Node) obj;
            return location.equals(node.location);
        }

        @Override
        public int hashCode() {
            return Objects.hash(location);
        }
    }

    /**
     * Check if the node contains the item.
     *
     * @param node The current node.
     * @param game The current game.
     * @return True if the node contains the item. Otherwise, false.
     */
    private boolean goalTest(Node node, Game game) {
        // Check if the node is a pill or gold
        List<Location> pillAndItemLocations = game.getPillAndItemLocations();
        return pillAndItemLocations.contains(node.location);
    }

    /**
     * Find the neighbor with the lowest cost.
     *
     * @param node   The current node.
     * @param pacman The PacMan.
     * @param game   The current game.
     * @return The neighbor.
     */
    private List<Node> getNeighbors(Node node, PacActor pacman, Game game) {
        List<Node> neighbors = new ArrayList<>();

        // Get the neighbors in all directions
        for (Direction dir : Direction.directions) {
            Location neighborLoc = new Location(node.location.x + dir.dx, node.location.y + dir.dy);
            if (pacman.canMove(neighborLoc)) {
                Node neighbor = new Node(neighborLoc);
                neighbor.g = node.g + 1;
                neighbor.parent = node;
                neighbors.add(neighbor);
            }
        }

        // Check if current location is a portal
        if (pacman.getBackground().getColor(node.location).equals(Color.black)) {
            // If it is, add the other end of the portal to the neighbors
            Location portalOtherEnd = getOtherEndOfPortal(node.location, game);
            if (portalOtherEnd != null) {
                Node neighbor = new Node(portalOtherEnd);
                neighbor.g = node.g + 1;
                neighbor.parent = node;
                neighbors.add(neighbor);
            }
        }

        return neighbors;
    }

    /**
     * Find the location of the other end of the portal.
     *
     * @param portal The location of the portal.
     * @param game   The current game.
     * @return The location of the other end of the portal.
     */
    private Location getOtherEndOfPortal(Location portal, Game game) {
        Map<String, java.util.List<int[]>> portals = game.getCurrentMap().getPortals();

        // Loop through all the portals
        for (Map.Entry<String, java.util.List<int[]>> entry : portals.entrySet()) {
            List<int[]> locs = entry.getValue();
            if (portal.getX() == locs.get(0)[1] && portal.getY() == locs.get(0)[0]) {
                // This is the first end of the portal, return the second end
                return new Location(locs.get(1)[1], locs.get(1)[0]);
            } else if (portal.getX() == locs.get(1)[1] && portal.getY() == locs.get(1)[0]) {
                // This is the second end of the portal, return the first end
                return new Location(locs.get(0)[1], locs.get(0)[0]);
            }
        }

        // No portal found
        return null;
    }

    /**
     * Calculate the heuristic cost.
     *
     * @param current The current location.
     * @param target  The target location.
     * @return The heuristic cost.
     */
    private int heuristic(Location current, Location target) {
        // Manhattan distance to the target
        return Math.abs(current.x - target.x) + Math.abs(current.y - target.y);
    }

    /**
     * Build the path from the start node to the current node.
     *
     * @param node The current node.
     * @return The path.
     */
    private List<Location> buildPath(Node node) {
        List<Location> path = new ArrayList<>();
        Node currentNode = node;
        while (currentNode != null) {
            path.add(0, currentNode.location);
            currentNode = currentNode.parent;
        }

        return path;
    }

    /**
     * Find the node in the open list.
     *
     * @param node     The current node.
     * @param openList The open list.
     * @return The node if it is in the open list. Otherwise, return null.
     */
    private Node findNode(Node node, PriorityQueue<Node> openList) {
        for (Node n : openList) {
            if (n.equals(node)) {
                return n;
            }
        }
        return null;
    }
}
