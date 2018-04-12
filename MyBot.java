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
		final ArrayList<Planet> assignedPlanets = new ArrayList<>();

		for (; ; ) {
			moveList.clear();
			assignedPlanets.clear();
			networking.updateMap(gameMap);

			//Add a command for each ship in moveList
			for (final Ship ship : gameMap.getMyPlayer().getShips().values()) {

				if (ship.getDockingStatus() != Ship.DockingStatus.Undocked) {
					continue;
				}

				LinkedList<Integer> owners = new LinkedList<>();

				//get a list with all entities by distance - planets and ships
				LinkedList<Integer> playersId = gameMap.getAllPlayerIds();
				if (!playersId.isEmpty()) {
					for (Integer id : playersId) {
						owners.add(id);
					}
				}
				owners.add(-1);
				owners.add(gameMap.getMyPlayerId());

				LinkedList<Entity> allEntitiesByDistance = gameMap.sortedNearbyEntities(ship, 'a', owners);

				if (!allEntitiesByDistance.isEmpty()) {
					for (Entity entity : allEntitiesByDistance) {

						//PLANET
						if (entity instanceof Planet) {
							//empty planet / our planet and not full -> move towards it
							if (!((Planet) entity).isOwned() ||
									(entity.getOwner() == gameMap.getMyPlayerId() && !((Planet) entity).isFull())) {

								Move move = ship.moveAndDock((Planet) entity, assignedPlanets, gameMap);

								if (move != null) {
									moveList.add(move);
									assignedPlanets.add((Planet) entity);
									break;
								}
							}
						}
						//SHIP
						else {
							//ship not ours -> move towards it
							if (entity.getId() != gameMap.getMyPlayerId()) {

								final ThrustMove newThrustMove = Navigation.navigateShipToEntity(gameMap, ship, entity, Constants.MAX_SPEED);
								if (newThrustMove != null) {
									moveList.add(newThrustMove);
									break;
								}
							}
						}
					}
				}

//				boolean moveCommandSaved = false; //true if we added a command for the current ship
//
//				//If there are empty planets, go to the closest one that doesn't already have a friendly ship going for it
//				owners.add(-1);
//				LinkedList<Entity> emptyPlanetsByDistance =
//						gameMap.sortedNearbyEntities(ship, 'p', owners);
//
//				if (!emptyPlanetsByDistance.isEmpty()) {
//					for (Entity entity : emptyPlanetsByDistance) {
//						Planet planet = (Planet) entity;
//
//						Move move = ship.moveAndDock(planet, assignedPlanets, gameMap);
//
//						if (move != null) {
//							moveList.add(move);
//							assignedPlanets.add(planet);
//							moveCommandSaved = true;
//							break;
//						}
//					}
//				}
//
//				//If there are owned planets that can be docked to, go to the closest one that doesn't already have a
//				// friendly ship going for it
//				if (!moveCommandSaved) {
//					owners.clear();
//					owners.add(gameMap.getMyPlayerId());
//					LinkedList<Entity> ownedPlanetsByDistance =
//							gameMap.sortedNearbyEntities(ship, 'p', owners);
//
//					if (!ownedPlanetsByDistance.isEmpty()) {
//						for (Entity entity : ownedPlanetsByDistance) { //Get closest owned planet that isn't full
//							Planet planet = (Planet) entity;
//
//							if (planet.isFull())
//								continue;
//
//							Move move = ship.moveAndDock(planet, assignedPlanets, gameMap);
//
//							if (move != null) {
//								moveList.add(move);
//								assignedPlanets.add(planet);
//								moveCommandSaved = true;
//								break;
//							}
//						}
//					}
//				}
//
//				//If no command was issued yet, for each nearby enemy ship we either attack or defense
//				if (!moveCommandSaved) {
//
//					//get nearby enemy ships by distance
//					owners = gameMap.getAllPlayerIds();
//					owners.remove(gameMap.getMyPlayerId());
//					LinkedList<Entity> enemyShipsByDistance =
//							gameMap.sortedNearbyEntities(ship, 's', owners);
//
//
//					//get my closest ships
//					owners.add(gameMap.getMyPlayerId());
//					LinkedList<Entity> myShipsByDistance =
//							gameMap.sortedNearbyEntities(ship, 's', owners);
//
//					//get my closest Docked ship
//					Ship myDockedShipsByDistance;
//					for (Ship ship : myShipsByDistance) {
//						if (ship.getDockingStatus() == Ship.DockingStatus.Docked) {
//							myDockedShipsByDistance = ship;
//							break;
//						}
//					}
//
//					for (Ship currentShip : enemyShipsByDistance) {
//
//						//DEFENSE : for Undocked enemy ships
//						if (currentShip.getDockingStatus() == Ship.DockingStatus.Undocked) {
//
//							//we check if it is nearby one of our Docked ship and defend it if only we can reach it in time
//							int x = myDockedShipsByDistance.getXPos();
//							int y = myDockedShipsByDistance.getYPos();
//
//							if (currentShip.getDistanceTo(new Position(x, y)) <= 7 * myDockedShipsByDistance.getDockingProgress()){
//
//								//if there is more than 1 enemy ship, we gather
//								if (enemyShipsByDistance.size() > 1){
//
//								}
//
//								//else we move alone towards it
//								else
//								{
//									final ThrustMove newThrustMove = Navigation.navigateShipToEntity(gameMap, ship, currentShip, Constants.MAX_SPEED);
//									if (newThrustMove != null) {
//									moveList.add(newThrustMove);
//									}
//								}
//							}
//						}
//						//ATTACK : for Docked enemy ships
//						//we count Undocked enemy ships nearby it and if
//						//there is no one, we attack it alone, else we gather and attack
//
//
//
//					}
////                    Ship closestEnemyShip = (Ship) enemyShipsByDistance.get(0);
////                    final ThrustMove newThrustMove = Navigation.navigateShipToEntity(gameMap, ship, closestEnemyShip, Constants.MAX_SPEED);
////                    if (newThrustMove != null) {
////                        moveList.add(newThrustMove);
////                    }
//
//				}
			}

			Networking.sendMoves(moveList);
		}
	}
}