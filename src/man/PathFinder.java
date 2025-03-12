package man;

import robocode.AdvancedRobot;
import robocode.ScannedRobotEvent;
import static robocode.util.Utils.normalRelativeAngle;

import java.awt.geom.Point2D;
import java.awt.geom.Rectangle2D;
import java.util.ArrayList;

public class PathFinder extends AdvancedRobot {
    public static int BINS = 47; // Number of bins for GuessFactor stats
    public static double[] _surfStats = new double[BINS]; // Stats for each bin
    public Point2D.Double _myLocation; // Your bot's location
    public Point2D.Double _enemyLocation; // Enemy bot's location

    public ArrayList<EnemyWave> _enemyWaves; // List of active waves
    public ArrayList<Integer> _surfDirections; // Directions of past movements
    public ArrayList<Double> _surfAbsBearings; // Absolute bearings of past scans

    public static double _oppEnergy = 100.0; // Enemy's last known energy

    // Battlefield dimensions for wall smoothing
    public static Rectangle2D.Double _fieldRect = new Rectangle2D.Double(18, 18, 764, 564);
    public static double WALL_STICK = 160; // Distance to maintain from walls

    public void run() {
        _enemyWaves = new ArrayList<>();
        _surfDirections = new ArrayList<>();
        _surfAbsBearings = new ArrayList<>();

        setAdjustGunForRobotTurn(true);
        setAdjustRadarForGunTurn(true);

        // Continuous radar scanning
        while (true) {
            turnRadarRightRadians(Double.POSITIVE_INFINITY);
        }
    }

    public void onScannedRobot(ScannedRobotEvent e) {
        _myLocation = new Point2D.Double(getX(), getY());

        double lateralVelocity = getVelocity() * Math.sin(e.getBearingRadians());
        double absBearing = e.getBearingRadians() + getHeadingRadians();

        // Update radar to keep tracking the enemy
        setTurnRadarRightRadians(robocode.util.Utils.normalRelativeAngle(absBearing - getRadarHeadingRadians()) * 2);

        // Record movement direction and absolute bearing
        _surfDirections.add(0, (lateralVelocity >= 0) ? 1 : -1);
        _surfAbsBearings.add(0, absBearing + Math.PI);

        // Detect bullet firing by energy drop
        double bulletPower = _oppEnergy - e.getEnergy();
        /*
        if (bulletPower < 3.01 && bulletPower > 0.09 && _surfDirections.size() > 2) {
            Wave ew = new Wave(
                    getTime() - 1,// the turn before this scan
                    bulletVelocity()
            getTime() - 1;
            bulletVelocity(bulletPower);
            bulletVelocity(bulletPower);
             _surfDirections.get(2);
            _surfAbsBearings.get(2);
            (Point2D.Double) _enemyLocation.clone();
            );

            _enemyWaves.add(ew);
        }

        _oppEnergy = e.getEnergy();

        // Update enemy location
        _enemyLocation = project(_myLocation, absBearing, e.getDistance());

        updateWaves();
        doSurfing();
         */
    }
/*
    public void updateWaves() {
        for (int x = 0; x < _enemyWaves.size(); x++) {
            EnemyWave ew = _enemyWaves.get(x);
            ew.distanceTraveled = (getTime() - ew.fireTime) * ew.bulletVelocity;

            // Remove waves that have passed
            if (ew.distanceTraveled > _myLocation.distance(ew.fireLocation) + 50) {
                _enemyWaves.remove(x);
                x--;
            }
        }
    }

    public void doSurfing() {
        EnemyWave surfWave = getClosestSurfableWave();
        if (surfWave == null) return;

        // Check danger for both directions
        double dangerLeft = checkDanger(surfWave, -1);
        double dangerRight = checkDanger(surfWave, 1);

        // Choose the safer direction
        double goAngle = absoluteBearing(surfWave.fireLocation, _myLocation);
        if (dangerLeft < dangerRight) {
            goAngle = wallSmoothing(_myLocation, goAngle - (Math.PI / 2), -1);
        } else {
            goAngle = wallSmoothing(_myLocation, goAngle + (Math.PI / 2), 1);
        }

        // Move in the chosen direction
        setBackAsFront(this, goAngle);
    }

    public double wallSmoothing(Point2D.Double botLocation, double angle, int orientation) {
        while (!_fieldRect.contains(project(botLocation, angle, WALL_STICK))) {
            angle += orientation * 0.05;
        }
        return angle;
    }

    public static Point2D.Double project(Point2D.Double sourceLocation, double angle, double length) {
        return new Point2D.Double(
                sourceLocation.x + Math.sin(angle) * length,
                sourceLocation.y + Math.cos(angle) * length
        );
    }

    public static double absoluteBearing(Point2D.Double source, Point2D.Double target) {
        return Math.atan2(target.x - source.x, target.y - source.y);
    }

    public static double bulletVelocity(double power) {
        return 20.0 - 3.0 * power;
    }

 */
}