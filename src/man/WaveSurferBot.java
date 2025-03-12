/** Apollo Tank written by [her] and [I].
 * 
 * Credit for the core of the Wave Surfing algorithm: Patrick "Voidious" Cupka at https://robowiki.net/wiki/Wave_Surfing_Tutorial on 11/03/2025, and Vincent Maliko from https://github.com/malikov/robocode/blob/master/WaveSurfer.java on 11/03/2025
 */

package man;

import robocode.AdvancedRobot;
import robocode.Condition;
import robocode.CustomEvent;
import robocode.ScannedRobotEvent;

import java.awt.Color;
import java.awt.geom.Point2D;
import java.util.ArrayList;

public class WaveSurferBot extends AdvancedRobot {
    private static final int GUESS_FACTOR_RANGE = 47;
    public static double[] surfStats =new double[GUESS_FACTOR_RANGE];

    public Point2D.Double myLocation;
    public Point2D.Double enemyLocation;

    public ArrayList<Wave> waves;     // List of active waves
    public ArrayList<Integer> surfDirections;   // Directions of past movements

    public ArrayList<Double> waveAbsBearings;   // Absolute bearings of point of fire from past scans

    public static double enemyEnergy = 100.0;   // Enemy's last known energy level
    // Battlefield dimensions for wall smoothing
    private static final int WALL_SPACE = 100; // minimum pixels we keep from the wall
    private static final int MIN_WALL_SPACE = 40; // minimum pixels we keep from the wall

    public void run() {
        setBodyColor(Color.black);
        setGunColor(Color.red);
        setRadarColor(Color.black);
        setBulletColor(Color.red);
        myLocation = new Point2D.Double(getX(), getY());

        //Too close to a wall
        addCustomEvent(new Condition("walled") {
            public boolean test() {
                return isNearWall(getX(), getY(), MIN_WALL_SPACE);
            }
        });

        while (true) {
            turnGunRight(360); // Keep scanning
        }
    }

    public void onCustomEvent(CustomEvent e) {
        Point2D.Double centre;
        if (e.getCondition().getName().equals("walled")){
            centre = new Point2D.Double(
                    getBattleFieldWidth()/2 + Math.random()*100 - 99,
                    getBattleFieldHeight()/2 + Math.random()*100 - 99);
            setAhead(myLocation.distance(centre));
            setTurnRight(absoluteBearing(myLocation,centre));
        }
    }

    public void onScannedRobot(ScannedRobotEvent e) {
        checkWave(e);
        surfWaves();

        /*
            Aiming
         */
    }

    //Checks and updates Wave ArrayList based on energy drops. Returns
    public boolean checkWave(ScannedRobotEvent e) {
        double enemyAbsBearing = getHeadingRadians() + e.getBearingRadians();
        double bulletPower = enemyEnergy - e.getEnergy();
        boolean newWave = (bulletPower < 3.01 && bulletPower > 0.09
                && _surfDirections.size() > 2)

        if(newWave){

        }

        // Adjust radar to keep tracking the enemy
        setTurnRadarRight(robocode.util.Utils.normalRelativeAngleDegrees(enemyAbsBearing - getRadarHeadingRadians()) * 2);

        // Detect enemy fire
        bulletPower = Math.min(3, enemyEnergy - e.getEnergy());
        enemyEnergy = e.getEnergy();
        if (bulletPower > 0 && bulletPower <= 3) {
            double bulletSpeed = 20 - (3 * bulletPower);
            waves.add(new Wave(getTime() -1 , enemyLocation, enemyAbsBearing, bulletSpeed));
        }

        // Move perpendicular to enemy while avoiding walls
        double absoluteBearing = getHeading() + e.getBearing();
        double bearingFromGun = robocode.util.Utils.normalRelativeAngleDegrees(absoluteBearing - getGunHeading());

        enemyLocation = project(myLocation, enemyAbsBearing, e.getDistance());
        myLocation.setLocation(getX(),getY());
    }

    @Override
    public void onBulletHitEvent()

    public void moveSafely(ScannedRobotEvent e) {
        double angle = e.getBearing() + 90; // Perpendicular movement
        if (Math.random() > 0.5) angle *= -1; // Randomly flip direction

        double moveDistance = 100;
        double newX = getX() + Math.sin(Math.toRadians(getHeading() + angle)) * moveDistance;
        double newY = getY() + Math.cos(Math.toRadians(getHeading() + angle)) * moveDistance;

        // Check if this move would hit a wall

        if (isNearWall(newX, newY)) {
            if(newX < 50){ // LeftWall
                angle = 0;
            }

            else if(newX > getWidth()-50){ //RightWall
                angle = getHeading() ;
            }

            else if(newY < 50){// BottomWall
                angle = getHeading() ;
            }

            else if(newY > getHeading()-50){ // TopWall
                angle += 45; // Adjust to slide along the wall
            }
        }

        setTurnRight(angle);
        setAhead(moveDistance);
    }

    //Method taken and adapted from the Robowiki Wave Surfing Turorial
    public double wallSmoothing(Point2D.Double botLocation, double angle, int orientation) {
        while (isNearWall(project(botLocation, angle, WALL_SPACE))) {
            angle += orientation*0.05;
        }
        return angle;
    }

    //Method taken from the Robowiki Wave Surfing Turorial
    public static Point2D.Double project(Point2D.Double sourceLocation,
                                         double angle, double length) {
        return new Point2D.Double(sourceLocation.x + Math.sin(angle) * length,
                sourceLocation.y + Math.cos(angle) * length);
    }

    //Method taken from Robowiki Wave Surfing Turorial
    //Returns the direction, in  radians,  of a target point relative to a starting point
    public static double absoluteBearing(Point2D.Double source, Point2D.Double target) {
        return Math.atan2(target.x - source.x, target.y - source.y);
    }

    public static double absoluteBearing(double sourceX, double sourceY, double targetX, double targetY) {
        return Math.atan2(targetX - sourceX, targetY - sourceY);
    }


    private boolean isNearWall(double x, double y, int minSpace) {
        return ((x < minSpace) || (x > (getBattleFieldWidth() - minSpace)) ||
                (y < minSpace) || (y > getBattleFieldHeight() - minSpace) );
    }
    private boolean isNearWall(double x, double y) {
        return isNearWall(x, y, WALL_SPACE);
    }
    private boolean isNearWall(Point2D.Double point) {
        return isNearWall(point.x, point.y);
    }

    public static double directAngleDg(Point2D.Double origin, Point2D.Double destination){
        //returns the bearing of a point relative to a given origin point
        return Math.toDegrees(Math.atan2(destination.x - origin.x, destination.y - origin.y));
    }

    public static Point2D.Double calcPoint(Point2D.Double point, double relativeBearing, double distance){
        //Note that trigonometry in Robocode has an offset of 90º anticlockwise
        //This results in us having x:sin and y:cos instead of x:cos and y:sin
        return new Point2D.Double(
                point.x + Math.sin( Math.toRadians(relativeBearing ) )*distance,
                point.y + Math.cos( Math.toRadians(relativeBearing ) )*distance
        );
    }

}
