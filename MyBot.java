import hlt.*;

import java.util.*;

public class MyBot {
    public static void main(final String[] args) {
        final Networking networking = new Networking();
        final GameMap gameMap = networking.initialize("Marco Polo");

        // We now have 1 full minute to analyse the initial map.
        final String initialMapIntelligence =
                "width: " + gameMap.getWidth() +
                        "; height: " + gameMap.getHeight() +
                        "; players: " + gameMap.getAllPlayers().size() +
                        "; planets: " + gameMap.getAllPlanets().size();
        Log.log(initialMapIntelligence);

        final ArrayList<Move> moveList = new ArrayList<>();
        final ArrayList<Ship> assignedEnemies = new ArrayList<>();

        for (; ; ) {
            moveList.clear();
            assignedEnemies.clear();
            networking.updateMap(gameMap);

            //Add a command for each ship in moveList
            for (final Ship ship : gameMap.getMyPlayer().getShips().values()) {
                if (ship.getDockingStatus() != Ship.DockingStatus.Undocked) {
                    continue;
                }

                LinkedList<Integer> owners = new LinkedList<>();


                //If no command was issued yet, attack closest enemy ship
                owners = gameMap.getAllPlayerIds();
                owners.remove(gameMap.getMyPlayerId());
                LinkedList<Entity> enemyShipsByDistance =
                        gameMap.sortedNearbyEntities(ship, 's', owners);

                Ship closestEnemyShip = null;
                for (Entity e : enemyShipsByDistance) {
                    if (!assignedEnemies.contains((Ship) e)) {
                        closestEnemyShip = (Ship) e;
                        break;
                    }
                }
                if (closestEnemyShip == null)
                    closestEnemyShip = (Ship) enemyShipsByDistance.get(0);
                final ThrustMove newThrustMove = Navigation.navigateShipToEntity(gameMap, ship, closestEnemyShip, Constants.MAX_SPEED);
                if (newThrustMove != null) {
                    moveList.add(newThrustMove);
                    assignedEnemies.add(closestEnemyShip);

                }
            }

            Networking.sendMoves(moveList);
        }
    }
}