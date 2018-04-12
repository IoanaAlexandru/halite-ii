package hlt;

import java.util.LinkedList;
import java.util.Map;

public class Entity extends Position implements Comparable<Entity> {

	private final int owner;
	private final int id;
	private final int health;
	private final double radius;

	public Entity(final int owner, final int id, final double xPos, final double yPos, final int health, final double radius) {
		super(xPos, yPos);
		this.owner = owner;
		this.id = id;
		this.health = health;
		this.radius = radius;
	}

	public int getOwner() {
		return owner;
	}

	public int getId() {
		return id;
	}

	public int getHealth() {
		return health;
	}

	public double getRadius() {
		return radius;
	}

	@Override
	public String toString() {
		return "Entity[" +
				super.toString() +
				", owner=" + owner +
				", id=" + id +
				", health=" + health +
				", radius=" + radius +
				"]";
	}

	//sort in descending order by radius
	@Override
	public int compareTo(Entity o) {

		return (int) (o.radius - this.radius);
	}

	public static int countUndockedShipsInRange(Map<Double, LinkedList<Entity>> shipsByDistance, int range) {
		int count = 0;

		for (double dist : shipsByDistance.keySet()) {
			for (Entity e : shipsByDistance.get(dist)) {
				if (((Ship) e).getDockingStatus() == Ship.DockingStatus.Undocked) {
					if (dist <= range) {
						count++;
					} else {
						break;
					}
				}
			}
		}
		return count;
	}
}
