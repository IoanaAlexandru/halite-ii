import hlt.*;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.Map;

public class MyBot {

    public static void main(final String[] args) {
        final Networking networking = new Networking();
        final GameMap gameMap = networking.initialize("smarterTamagocchi");

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
                LinkedList<Planet> planetsByDistance = new LinkedList<>();

                for (double distance : entitiesByDistance.keySet()) {
                    entities = entitiesByDistance.get(distance);
                    for (Entity entity: entities) {
                        if (entity instanceof Planet) {
                            planetsByDistance.add((Planet) entity);
                        }
                    }
                }

                for (final Planet planet : planetsByDistance) {
                    if (planet.isOwned()) {
                        continue;
                    }

                    if (ship.canDock(planet)) {
                        moveList.add(new DockMove(ship, planet));
                        break;
                    }

                    final ThrustMove newThrustMove = Navigation.navigateShipToDock(gameMap, ship, planet, Constants.MAX_SPEED/2);
                    if (newThrustMove != null) {
                        moveList.add(newThrustMove);
                    }

                    break;
                }
            }
            Networking.sendMoves(moveList);
        }
    }
}
