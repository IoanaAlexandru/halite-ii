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

            LinkedList<Integer> owners = new LinkedList<>();
//            Planet target = gameMap.colonizationTarget();
            boolean moveCommand = true;

            // Checks survival strategy conditions
            boolean survivalMode = true;
            List<Player> players = gameMap.getAllPlayers();
            int stillAlive = 0;  // number of players that still have ships
            for (Player p : players) {
                if (p.getShips().size() > 0)
                    stillAlive++;
            }

            owners.add(gameMap.getMyPlayerId());
            if (stillAlive >= 2 && networking.getTurn() >= 10) {
                LinkedList<Entity> myOwnedPlanets = gameMap.sortedNearbyEntities(gameMap.getMyPlayer().getShips().get(0), 'p', owners);
                for (Entity e : myOwnedPlanets) {
                    //undocked allied ships for target within a radius of 50
                    owners.add(gameMap.getMyPlayerId());
                    Map<Double, LinkedList<Entity>> alliedShipsByDistance = gameMap.nearbyEntitiesByDistance(e, 's', owners);
                    int countUndockedAlliedShips = Entity.countUndockedShipsInRange(alliedShipsByDistance, 50);

                    //undocked enemy ships within a radius of 50
                    owners = gameMap.getAllPlayerIds();
                    owners.remove(gameMap.getMyPlayerId());
                    Map<Double, LinkedList<Entity>> enemyShipsByDistance = gameMap.nearbyEntitiesByDistance(e, 's', owners);
                    int countUndockedEnemyShips = Entity.countUndockedShipsInRange(enemyShipsByDistance, 50);

                    if (countUndockedEnemyShips - countUndockedAlliedShips < 3) {
                        survivalMode = false;
                    }
                }
            } else {
                survivalMode = false;
            }

            if (survivalMode) {
                for (final Ship ship : gameMap.getMyPlayer().getShips().values()) {
                    Position closestCorner =  gameMap.getCorners().get(0);
                    for (Position c : gameMap.getCorners()) {
                        if (ship.getDistanceTo(c) < ship.getDistanceTo(closestCorner))
                            closestCorner = c;
                    }

                    Entity e = new Entity(-1, 1, closestCorner.getXPos(), closestCorner.getYPos(), 1, 1);
                    final ThrustMove newThrustMove = Navigation.navigateShipToEntity(gameMap, ship, e, Constants.MAX_SPEED);
                    if (newThrustMove != null) {
                        moveList.add(newThrustMove);
                        Log.log("navigate to planet");
                    }
                }
            }  else {

                //Add a command for each ship in moveList
                for (final Ship ship : gameMap.getMyPlayer().getShips().values()) {
                    if (ship.getDockingStatus() != Ship.DockingStatus.Undocked) {
                        continue;
                    }

                    owners.clear();


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
            }

            Log.log(Integer.toString(moveList.size()));
            Networking.sendMoves(moveList);
        }
    }
}