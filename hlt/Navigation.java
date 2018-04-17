package hlt;

public class Navigation {

    /**
     *
     * @param gameMap
     * @param ship
     * @param target
     * @param maxThrust
     * @return
     */
    public static ThrustMove navigateShipToEntity(
            final GameMap gameMap,
            final Ship ship,
            final Entity target,
            final int maxThrust)
    {
        final int maxCorrections = Constants.MAX_NAVIGATION_CORRECTIONS;
        final boolean avoidObstacles = true;
        final double angularStepRad = Math.PI/180.0;
        final Position targetPos = ship.getClosestPoint(target);

        return navigateShipTowardsTarget(gameMap, ship, targetPos, maxThrust, avoidObstacles, maxCorrections, angularStepRad);
    }

    /**
     *
     * @param gameMap
     * @param ship
     * @param unit accuracy = 0.1, equal to maxThrust
     * @return
     */
/*    public static boolean[][] createHitMap (
            final GameMap gameMap,
            final Ship ship,
            final double unit)
    {
        boolean[][] hitMap = new boolean[width / unit][height / unit];
        List<Ship> allShips = gameMap.getAllShips();
        List<Planet> allPlanets = gameMap.getAllPlanets();
        for (Ship ship : allShips) {
            double leftSide = ship.getXPos() - ship.getRadius();
            while (true) {
                double fPart = leftSide % 1;
                if (fPart == 0)
                    break;
                else
                    leftSide -= 0.1;
            }
            double rightSide = ship.getXPos() + ship.getRadius();
            while (true) {
                double fPart = rightSide % 1;
                if (fPart == 0)
                    break;
                else
                    rightSide += 0.1;
            }
            double upSide = ship.getYPos() + ship.getRadius();
            while (true) {
                double fPart = upSide % 1;
                if (fPart == 0)
                    break;
                else
                    upSide += 0.1;
            }
            double downSide = ship.getYPos() - ship.getRadius();
            while (true) {
                double fPart = downSide % 1;
                if (fPart == 0)
                    break;
                else
                    downSide -= 0.1;
            }

            for (double i = leftSide; i <= rightSide; i += unit) {
                for (double j = upSide; j <= downSide; j += unit) {
                    hitMap[i][j] = false;
                }
            }
        }
    }
*/
    /**
     *
     * @param gameMap
     * @param ship
     * @param targetPos
     * @param maxThrust
     * @param avoidObstacles
     * @param maxCorrections
     * @param angularStepRad
     * @return
     */
    public static ThrustMove navigateShipTowardsTarget(
            final GameMap gameMap,
            final Ship ship,
            final Position targetPos,
            final int maxThrust,
            final boolean avoidObstacles,
            final int maxCorrections,
            final double angularStepRad)
    {
        if (maxCorrections <= 0) {
            return null;
        }

        final double distance = ship.getDistanceTo(targetPos);
        final double angleRad = ship.orientTowardsInRad(targetPos);

        if (avoidObstacles && !gameMap.objectsBetween(ship, targetPos).isEmpty()) {
            final double newTargetDx = Math.cos(angleRad + angularStepRad) * distance;
            final double newTargetDy = Math.sin(angleRad + angularStepRad) * distance;
            final Position newTarget = new Position(ship.getXPos() + newTargetDx, ship.getYPos() + newTargetDy);

            return navigateShipTowardsTarget(gameMap, ship, newTarget, maxThrust, true, (maxCorrections-1), angularStepRad);
        }

        final int thrust;
        if (distance < maxThrust) {
            // Do not round up, since overshooting might cause collision.
            thrust = (int) distance;
        }
        else {
            thrust = maxThrust;
        }

        final int angleDeg = Util.angleRadToDegClipped(angleRad);

        return new ThrustMove(ship, angleDeg, thrust);
    }
}
