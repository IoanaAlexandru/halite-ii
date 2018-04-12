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


			LinkedList<Integer> owners = new LinkedList<>();
			Planet target = null;
			boolean moveCommand = false;

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
						break;
					}
					continue;
				}
				//navigate towards target until you can dock
				if (!ship.canDock(target)) {
					final ThrustMove newThrustMove = Navigation.navigateShipToEntity(gameMap, ship, target, Constants.MAX_SPEED);
					if (newThrustMove != null) {
						moveList.add(newThrustMove);
						break;
					}
				}
				//if allyCount > enemyCount, then dock
				if (countUndockedAlliedShips > countUndockedEnemyShips) {
					moveList.add(new DockMove(ship, target));
					moveCommand = true;
					break;
				} else {

				}

			}

			Networking.sendMoves(moveList);
		}
	}
}