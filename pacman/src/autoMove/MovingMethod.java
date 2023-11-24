package src.autoMove;

import ch.aplu.jgamegrid.Location;
import src.Game;
import src.PacActor;

/**
 * Interface for moving methods.
 * Gets the next most appropriate location for the PacMan to move to.
 */
public interface MovingMethod {
    Location getNext(PacActor pacman, Game game);
}
