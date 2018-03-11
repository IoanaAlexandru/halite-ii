import hlt.*;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.Map;

public class MyBot {

    public static void main(final String[] args) {
        final Networking networking = new Networking();
        final GameMap gameMap = networking.initialize("aggressiveTamagocchi");

        // We now have 1 full minute to analyse the initial map.
        final String initialMapIntelligence =
                "width: " + gameMap.getWidth() +
                "; height: " + gameMap.getHeight() +
                "; players: " + gameMap.getAllPlayers().size() +
                "; planets: " + gameMap.getAllPlanets().size();
        Log.log(initialMapIntelligence);

        final ArrayList<Move> moveList = new ArrayList<>();
        for (;;) {
            moveList.clear();
            networking.updateMap(gameMap);

            for (final Ship ship : gameMap.getMyPlayer().getShips().values()) {
                if (ship.getDockingStatus() != Ship.DockingStatus.Undocked) {
                    continue;
                }

                Map<Double, LinkedList<Entity>> entitiesByDistance = gameMap.nearbyEntitiesByDistance(ship);
                LinkedList<Entity> entities;
                LinkedList<Planet> emptyPlanetsByDistance = new LinkedList<>();
                LinkedList<Planet> ownedFreePlanetsByDistance = new LinkedList<>();
                LinkedList<Ship> enemyShipsByDistance = new LinkedList<>();

                for (double distance : entitiesByDistance.keySet()) {
                    entities = entitiesByDistance.get(distance);
                    for (Entity entity: entities) {
                        if (entity instanceof Planet) {
                            if (!((Planet) entity).isOwned()) {
                                emptyPlanetsByDistance.add((Planet) entity);
                            }
                            if (entity.getOwner() == gameMap.getMyPlayerId() && !((Planet) entity).isFull()) {
                                ownedFreePlanetsByDistance.add((Planet) entity);
                            }
                        }

                        if (entity instanceof Ship && (entity.getOwner() != gameMap.getMyPlayerId())) {
                            enemyShipsByDistance.add((Ship) entity);
                        }
                    }
                }

                if (!emptyPlanetsByDistance.isEmpty()) {
                    Planet closestPlanet = emptyPlanetsByDistance.get(0);

                    if (ship.canDock(closestPlanet)) {
                        moveList.add(new DockMove(ship, closestPlanet));
                        break;
                    }

                    final ThrustMove newThrustMove = Navigation.navigateShipToDock(gameMap, ship, closestPlanet, Constants.MAX_SPEED);
                    if (newThrustMove != null) {
                        moveList.add(newThrustMove);
                    }
                } else if (!ownedFreePlanetsByDistance.isEmpty()) {
                    Planet closestPlanet = ownedFreePlanetsByDistance.get(0);

                    if (ship.canDock(closestPlanet)) {
                        moveList.add(new DockMove(ship, closestPlanet));
                        break;
                    }

                    final ThrustMove newThrustMove = Navigation.navigateShipToDock(gameMap, ship, closestPlanet, Constants.MAX_SPEED);
                    if (newThrustMove != null) {
                        moveList.add(newThrustMove);
                    }
                } else if (!enemyShipsByDistance.isEmpty()) {
                    Ship closestEnemyShip = enemyShipsByDistance.get(0);

                    final ThrustMove newThrustMove = Navigation.navigateShipToDock(gameMap, ship, closestEnemyShip, Constants.MAX_SPEED);
                    if (newThrustMove != null) {
                        moveList.add(newThrustMove);
                    }
                }
            }
            Networking.sendMoves(moveList);
        }
    }
}
