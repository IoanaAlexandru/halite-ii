import hlt.*;

import java.util.*;

public class MyBot {
	public static void main(final String[] args) {
		final Networking networking = new Networking();
		final GameMap gameMap = networking.initialize("StrategistGocchi");

		// We now have 1 full minute to analyse the initial map.
		final String initialMapIntelligence =
				"width: " + gameMap.getWidth() +
						"; height: " + gameMap.getHeight() +
						"; players: " + gameMap.getAllPlayers().size() +
						"; planets: " + gameMap.getAllPlanets().size();
		Log.log(initialMapIntelligence);

		final ArrayList<Move> moveList = new ArrayList<>();

		for (; ; ) {
			moveList.clear();
			networking.updateMap(gameMap);

			LinkedList<Integer> owners = new LinkedList<>();
			Planet target = gameMap.colonizationTarget();
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
            if (stillAlive >= 2 && networking.getTurn() >= 60) {
                LinkedList<Entity> myOwnedPlanets = gameMap.sortedNearbyEntities(target, 'p', owners);
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
                continue;
            }

            // Survival mode NOT deployed

			//undocked allied ships for target within a radius of 25
			owners.add(gameMap.getMyPlayerId());
			Map<Double, LinkedList<Entity>> alliedShipsByDistance = gameMap.nearbyEntitiesByDistance(target, 's', owners);
			int countUndockedAlliedShips = Entity.countUndockedShipsInRange(alliedShipsByDistance, 25);

			//undocked enemy ships within a radius of 85
			owners = gameMap.getAllPlayerIds();
			owners.remove(gameMap.getMyPlayerId());
			Map<Double, LinkedList<Entity>> enemyShipsByDistance = gameMap.nearbyEntitiesByDistance(target, 's', owners);
			int countUndockedEnemyShips = Entity.countUndockedShipsInRange(enemyShipsByDistance, 85);

			//Add a command for each ship in moveList
			for (final Ship ship : gameMap.getMyPlayer().getShips().values()) {

				//docked ships will undock if enemyCount > allyCount in a radius of 65
				if (ship.getDockingStatus() != Ship.DockingStatus.Undocked) {

					countUndockedAlliedShips = Entity.countUndockedShipsInRange(alliedShipsByDistance, 65);
					countUndockedEnemyShips = Entity.countUndockedShipsInRange(enemyShipsByDistance, 65);

					if (countUndockedAlliedShips < countUndockedEnemyShips) {
						moveList.add(new UndockMove(ship));
						Log.log("undock");
						continue;
					}
				}
				//navigate towards target until you can dock
				if (!ship.canDock(target)) {
					final ThrustMove newThrustMove = Navigation.navigateShipToEntity(gameMap, ship, target, Constants.MAX_SPEED);
					if (newThrustMove != null) {
						moveList.add(newThrustMove);
						Log.log("navigate to planet");
						continue;
					}
				}

				owners = gameMap.getAllPlayerIds();
				owners.add(-1);

				LinkedList<Entity> allEntitiesByDistance = gameMap.sortedNearbyEntities(ship, 'a', owners);

				if (!allEntitiesByDistance.isEmpty()) {
					for (Entity entity : allEntitiesByDistance) {

						//PLANET
						if (entity instanceof Planet) {
							//empty planet / our planet and not full -> move towards it
							if (!((Planet) entity).isOwned() ||
									(entity.getOwner() == gameMap.getMyPlayerId() && !((Planet) entity).isFull())) {

                                if (ship.canDock((Planet) entity)) {
                                    moveList.add(new DockMove(ship, (Planet) entity));
                                    moveCommand = true;
                                    Log.log("dock");
                                    break;
                                } else {
									final ThrustMove newThrustMove = Navigation.navigateShipToEntity(gameMap, ship, entity, Constants.MAX_SPEED);
                                    if (newThrustMove != null) {
                                        moveList.add(newThrustMove);
                                        Log.log("navigate to planet 2?");
                                        break;
                                    }
                                }
							}
						}
						//SHIP
						else {
							//ship not ours -> move towards it
							if (entity.getOwner() != gameMap.getMyPlayerId()) {

								final ThrustMove newThrustMove = Navigation.navigateShipToEntity(gameMap, ship, entity, Constants.MAX_SPEED);
								if (newThrustMove != null) {
									moveList.add(newThrustMove);
									moveCommand = true;
									Log.log("THIS IS SPARTA");
									break;
								}
							}
						}
					}
				}

				if (moveCommand)
					continue;

				//if allyCount > enemyCount, then dock
				if (countUndockedAlliedShips > countUndockedEnemyShips) {
					moveList.add(new DockMove(ship, target));
					Log.log("dock 2?");
					moveCommand = true;
					break;
				} else {
					Log.log("NU.");
				}

			}

			Log.log("ok");

			Networking.sendMoves(moveList);
		}
	}
}