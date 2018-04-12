import hlt.*;
import java.util.*;

public class MyBot {
    public static void main(final String[] args) {
        final Networking networking = new Networking();
        final GameMap gameMap = networking.initialize("MadTammagochi2");
        
        // We now have 1 full minute to analyse the initial map.
        final String initialMapIntelligence =
                "width: " + gameMap.getWidth() +
                "; height: " + gameMap.getHeight() +
                "; players: " + gameMap.getAllPlayers().size() +
                "; planets: " + gameMap.getAllPlanets().size();
        Log.log(initialMapIntelligence);
        
        final ArrayList<Move> moveList = new ArrayList<>();
        final ArrayList<Planet> assignedPlanets = new ArrayList<>();
        
        for (;;) {
            moveList.clear();
            assignedPlanets.clear();
            networking.updateMap(gameMap);

            //Add a command for each ship in moveList
            for (final Ship ship : gameMap.getMyPlayer().getShips().values()) {
                if (ship.getDockingStatus() != Ship.DockingStatus.Undocked) {
                    continue;
                }
                
                LinkedList<Integer> owners = new LinkedList<>();

                boolean moveCommandSaved = false; //true if we added a command for the current ship

                //If there are empty planets, go to the closest one that doesn't already have a friendly ship going for it
                owners.add(-1);
                LinkedList<Entity> emptyPlanetsByDistance =
                        gameMap.sortedNearbyEntities(ship, 'p', owners);

                if (!emptyPlanetsByDistance.isEmpty()) {
                    for (Entity entity : emptyPlanetsByDistance) {
                        Planet planet = (Planet)entity;

                        Move move = ship.moveAndDock(planet, assignedPlanets, gameMap);

                        if(move != null) {
                            moveList.add(move);
                            assignedPlanets.add(planet);
                            moveCommandSaved = true;
                            break;
                        }
                    }
				}

                //If there are owned planets that can be docked to, go to the closest one that doesn't already have a
                // friendly ship going for it :)
				if (!moveCommandSaved) {
                    owners.clear();
                    owners.add(gameMap.getMyPlayerId());
                    LinkedList<Entity> ownedPlanetsByDistance =
                            gameMap.sortedNearbyEntities(ship, 'p', owners);

                    if (!ownedPlanetsByDistance.isEmpty()) {
                        for (Entity entity : ownedPlanetsByDistance) { //Get closest owned planet that isn't full
                            Planet planet = (Planet) entity;

                            if (planet.isFull())
                                continue;

                            Move move = ship.moveAndDock(planet, assignedPlanets, gameMap);

                            if(move != null) {
                                moveList.add(move);
                                assignedPlanets.add(planet);
                                moveCommandSaved = true;
                                break;
                            }
                        }
                    }
                }

                //If no command was issued yet, attack closest enemy ship
                if (!moveCommandSaved) {
                    owners = gameMap.getAllPlayerIds();
                    owners.remove(gameMap.getMyPlayerId());
                    LinkedList<Entity> enemyShipsByDistance =
                            gameMap.sortedNearbyEntities(ship, 's', owners);

                    Ship closestEnemyShip = (Ship) enemyShipsByDistance.get(0);
                    final ThrustMove newThrustMove = Navigation.navigateShipToEntity(gameMap, ship, closestEnemyShip, Constants.MAX_SPEED);
                    if (newThrustMove != null) {
                        moveList.add(newThrustMove);
                    }
                }
            }

            Networking.sendMoves(moveList);
        }
    }
}