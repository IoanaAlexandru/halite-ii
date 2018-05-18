package hlt;

import java.util.Collections;
import java.util.List;

public class Planet extends Entity {

    private final int remainingProduction;
    private final int currentProduction;
    private final int dockingSpots;
    private final List<Integer> dockedShips;

    public Planet(final int owner, final int id, final double xPos, final double yPos, final int health,
                  final double radius, final int dockingSpots, final int currentProduction,
                  final int remainingProduction, final List<Integer> dockedShips) {

        super(owner, id, xPos, yPos, health, radius);

        this.dockingSpots = dockingSpots;
        this.currentProduction = currentProduction;
        this.remainingProduction = remainingProduction;
        this.dockedShips = Collections.unmodifiableList(dockedShips);
    }

    public int getRemainingProduction() {
        return remainingProduction;
    }

    public int getCurrentProduction() {
        return currentProduction;
    }

    public int getDockingSpots() {
        return dockingSpots;
    }

    public List<Integer> getDockedShips() {
        return dockedShips;
    }

    public boolean isFull() {
        return dockedShips.size() == dockingSpots;
    }

    public boolean isOwned() {
        return getOwner() != -1;
    }

    public int getScore(GameMap gameMap, double distance) {
        int score = 0;

        // 6 points per docking spot
        score += 6 * this.dockingSpots;

        // The farthest from center, the less points
        score -= (int) this.getDistanceTo(gameMap.getCenter()) / 2;

        // The farthest from ship, the less points
        if (distance <= 75)
            score += 3 * this.dockingSpots;
        if (distance <= 50)
            score += 3 * this.dockingSpots;
        if (distance <= 25)
            score += 3 * this.dockingSpots;
        score -= (int) distance;

        return score;
    }

    @Override
    public String toString() {
        return "Planet[" +
                super.toString() +
                ", remainingProduction=" + remainingProduction +
                ", currentProduction=" + currentProduction +
                ", dockingSpots=" + dockingSpots +
                ", dockedShips=" + dockedShips +
                "]";
    }
}
