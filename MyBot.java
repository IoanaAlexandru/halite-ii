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
        final ArrayList<Planet> nextPlanets = new ArrayList<>();
        
        for (;;) {
            moveList.clear();
            nextPlanets.clear();
            networking.updateMap(gameMap);
            
            for (final Ship ship : gameMap.getMyPlayer().getShips().values()) {
                if (ship.getDockingStatus() != Ship.DockingStatus.Undocked) {
                    continue;
                }
                
                LinkedList<Integer> owners = new LinkedList<>();

                boolean moveCommandSaved = false;

                owners.add(-1);
                LinkedList<Entity> emptyPlanetsByDistance =
                        gameMap.sortedNearbyEntities(ship, 'p', owners);

                if (!emptyPlanetsByDistance.isEmpty()) {
                    for (Entity entity : emptyPlanetsByDistance) {
                        Planet planet = (Planet)entity;

                        if (ship.canDock(planet)) {
                            moveList.add(new DockMove(ship, planet));
                            moveCommandSaved = true;
                            break;
                        } else if (!nextPlanets.contains(planet) || !planet.isFull()) {
                            final ThrustMove newThrustMove = Navigation.navigateShipToDock(gameMap, ship, planet, Constants.MAX_SPEED);
                            if (newThrustMove != null) {
                                moveList.add(newThrustMove);
                                nextPlanets.add(planet);
                                moveCommandSaved = true;
                                break;
                            }
                        }
                    }
				}

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

                            if (ship.canDock(planet)) {
                                moveList.add(new DockMove(ship, planet));
                                moveCommandSaved = true;
                                break;
                            } else if (!nextPlanets.contains(planet)) {
                                final ThrustMove newThrustMove = Navigation.navigateShipToDock(gameMap, ship, planet, Constants.MAX_SPEED);
                                if (newThrustMove != null) {
                                    moveList.add(newThrustMove);
                                    nextPlanets.add(planet);
                                    moveCommandSaved = true;
                                    break;
                                }
                            }
                        }
                    }
                }

                if (!moveCommandSaved) {
                    owners = gameMap.getAllPlayerIds();
                    owners.remove(gameMap.getMyPlayerId());
                    Log.log(owners.toString());
                    LinkedList<Entity> enemyShipsByDistance =
                            gameMap.sortedNearbyEntities(ship, 's', owners);

                    Ship closestEnemyShip = (Ship) enemyShipsByDistance.get(0);
                    final ThrustMove newThrustMove = Navigation.navigateShipTowardsTarget(gameMap, ship,
                            closestEnemyShip.getClosestPoint(ship), Constants.MAX_SPEED, true,
                            Constants.MAX_NAVIGATION_CORRECTIONS, Math.PI/180.0);
                    if (newThrustMove != null) {
                        moveList.add(newThrustMove);
                    }
                }
            }
            Networking.sendMoves(moveList);
        }
    }
}