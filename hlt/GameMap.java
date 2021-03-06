package hlt;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;
import java.util.Collections;
import java.util.LinkedList;
import java.util.Collection;

public class GameMap {
    private final int width, height;
    private final int playerId;
    private final List<Player> players;
    private final List<Player> playersUnmodifiable;
    private final Map<Integer, Planet> planets;
    private final List<Ship> allShips;
    private final List<Ship> allShipsUnmodifiable;

    // used only during parsing to reduce memory allocations
    private final List<Ship> currentShips = new ArrayList<>();

    public GameMap(final int width, final int height, final int playerId) {
        this.width = width;
        this.height = height;
        this.playerId = playerId;
        players = new ArrayList<>(Constants.MAX_PLAYERS);
        playersUnmodifiable = Collections.unmodifiableList(players);
        planets = new TreeMap<>();
        allShips = new ArrayList<>();
        allShipsUnmodifiable = Collections.unmodifiableList(allShips);
    }

    public int getHeight() {
        return height;
    }

    public int getWidth() {
        return width;
    }

    public int getMyPlayerId() {
        return playerId;
    }

    public List<Player> getAllPlayers() {
        return playersUnmodifiable;
    }

    public LinkedList<Integer> getAllPlayerIds() {
        List<Player> players = getAllPlayers();
        LinkedList<Integer> playerIds = new LinkedList<>();

        for (Player player : players)
            playerIds.add(player.getId());

        return playerIds;
    }

    public Player getMyPlayer() {
        return getAllPlayers().get(getMyPlayerId());
    }

    public LinkedList<Position> getCorners() {
        LinkedList<Position> corners = new LinkedList<>();
        corners.add(new Position(0, 0));
        corners.add(new Position(0, width - 1));
        corners.add(new Position(height - 1, 0));
        corners.add(new Position(height - 1, width - 1));
        return corners;
    }

    public Ship getShip(final int playerId, final int entityId) throws IndexOutOfBoundsException {
        return players.get(playerId).getShip(entityId);
    }

    public Planet getPlanet(final int entityId) {
        return planets.get(entityId);
    }

    public Map<Integer, Planet> getAllPlanets() {
        return planets;
    }

    public List<Ship> getAllShips() {
        return allShipsUnmodifiable;
    }

    public ArrayList<Entity> objectsBetween(Position start, Position target) {
        final ArrayList<Entity> entitiesFound = new ArrayList<>();

        addEntitiesBetween(entitiesFound, start, target, planets.values());
        addEntitiesBetween(entitiesFound, start, target, allShips);

        return entitiesFound;
    }

    private static void addEntitiesBetween(final List<Entity> entitiesFound,
                                           final Position start, final Position target,
                                           final Collection<? extends Entity> entitiesToCheck) {

        for (final Entity entity : entitiesToCheck) {
            if (entity.equals(start) || entity.equals(target)) {
                continue;
            }
            if (Collision.segmentCircleIntersect(start, target, entity, Constants.FORECAST_FUDGE_FACTOR)) {
                entitiesFound.add(entity);
            }
        }
    }

    /**
     *
     * @param entity reference position
     * @param entityType 'p' for planets, 's' for ships, 'a' for all
     * @param owners list of IDs (can contain -1 for empty planets)
     * @return list of relevant entities on the map, sorted by nearest to farthest from the reference position
     */
   public Map<Double, LinkedList<Entity>> nearbyEntitiesByDistance(final Entity entity, char entityType, LinkedList<Integer> owners) {
		final Map<Double, LinkedList<Entity>> entityByDistance = new TreeMap<>();
		double distance;

		if (entityType == 'p' || entityType == 'a')
            for (final Planet planet : planets.values()) {

                if (planet.equals(entity) || !owners.contains(planet.getOwner())) {
                    continue;
                }

                distance = entity.getDistanceTo(planet);

                if (entityByDistance.get(distance) == null)
                    entityByDistance.put(distance, new LinkedList<Entity>());
                entityByDistance.get(distance).add(planet);

                // if there are more planets at the same distance, sort them by radius
                if (entityByDistance.get(distance).size() > 1)
                    Collections.sort(entityByDistance.get(distance));
            }

        if (entityType == 's' || entityType == 'a')
		for (final Ship ship : allShips) {
			if (ship.equals(entity) || !owners.contains(ship.getOwner())) {
				continue;
			}

			distance = entity.getDistanceTo(ship);

			if (entityByDistance.get(distance) == null)
				entityByDistance.put(distance, new LinkedList<Entity>());
			entityByDistance.get(distance).add(ship);
		}

		return entityByDistance;
	}

	public LinkedList<Entity> sortedNearbyEntities(final Entity entity, char entityType, LinkedList<Integer> owners) {
       LinkedList<Entity> entities = new LinkedList<>();
       Map<Double, LinkedList<Entity>> entitiesByDistance = nearbyEntitiesByDistance(entity, entityType, owners);

       for (double dist : entitiesByDistance.keySet()) {
           for (Entity ent : entitiesByDistance.get(dist)) {
               entities.add(ent);
           }
       }

       return entities;
    }

    public GameMap updateMap(final Metadata mapMetadata) {
        final int numberOfPlayers = MetadataParser.parsePlayerNum(mapMetadata);

        players.clear();
        planets.clear();
        allShips.clear();

        // update players info
        for (int i = 0; i < numberOfPlayers; ++i) {
            currentShips.clear();
            final Map<Integer, Ship> currentPlayerShips = new TreeMap<>();
            final int playerId = MetadataParser.parsePlayerId(mapMetadata);

            final Player currentPlayer = new Player(playerId, currentPlayerShips);
            MetadataParser.populateShipList(currentShips, playerId, mapMetadata);
            allShips.addAll(currentShips);

            for (final Ship ship : currentShips) {
                currentPlayerShips.put(ship.getId(), ship);
            }
            players.add(currentPlayer);
        }

        final int numberOfPlanets = Integer.parseInt(mapMetadata.pop());

        for (int i = 0; i < numberOfPlanets; ++i) {
            final List<Integer> dockedShips = new ArrayList<>();
            final Planet planet = MetadataParser.newPlanetFromMetadata(dockedShips, mapMetadata);
            planets.put(planet.getId(), planet);
        }

        if (!mapMetadata.isEmpty()) {
            throw new IllegalStateException("Failed to parse data from Halite game engine. Please contact maintainers.");
        }

        return this;
    }
}