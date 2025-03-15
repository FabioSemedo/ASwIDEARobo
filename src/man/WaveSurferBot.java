/* Apollo Tank written by [her] and [I].
 * Credit for the core of the Wave Surfing algorithm: Patrick "Voidious" Cupka at https://robowiki.net/wiki/Wave_Surfing_Tutorial on 11/03/2025, and Vincent Maliko from https://github.com/malikov/robocode/blob/master/WaveSurfer.java on 11/03/2025
 */

package man;

import man.util.Tools;

import robocode.AdvancedRobot;
import robocode.ScannedRobotEvent;
import robocode.HitByBulletEvent;
import robocode.CustomEvent;
import robocode.Condition;

import java.awt.Color;
import java.awt.geom.Point2D;
import java.util.ArrayList;

import static robocode.util.Utils.normalRelativeAngleDegrees;

public class WaveSurferBot extends AdvancedRobot {
    // Battlefield dimensions for wall smoothing
    private static final int WALL_SPACE = 100; // minimum pixels we keep from the wall
    private static final int MIN_WALL_SPACE = 40; // minimum pixels we keep from the wall
    private static final int FIELD_CENTRE_RANGE = 50; // minimum pixels we keep from the wall

    private static final int GUESS_FACTOR_RANGE = 47;
    public static double[] surfStats =new double[GUESS_FACTOR_RANGE];

    public Point2D.Double myLocation;       // Where the enemy last saw us
    public Point2D.Double enemyLocation;    // Where we last saw the enemy

    public ArrayList<Wave> waves;     // List of active waves
    public ArrayList<Integer> surfDirections;   // Directions of past movements, Clockwise 1 or anticlockwise -1

    public ArrayList<Double> waveAbsBearings;   // Absolute bearings of point of fire from past scans

    public static double enemyEnergy = 100.0;   // Enemy's last known energy level

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

        setAdjustGunForRobotTurn(true);
        setAdjustRadarForGunTurn(true);

        while (true){

            turnRadarRight(Double.POSITIVE_INFINITY); // Keep scan if we lose the enemy
        }
    }//end run()

    //Escape walls
    public void onCustomEvent(CustomEvent e) {
        if (e.getCondition().getName().equals("walled")) {
            double range = FIELD_CENTRE_RANGE;
            double waveAngle;
            Point2D.Double centre;
            double distance;
            double angleToCentre;

            //Check for out of bounds
            if( (getBattleFieldHeight() < FIELD_CENTRE_RANGE*2) || (getBattleFieldWidth() < FIELD_CENTRE_RANGE*2)){
                range = Math.min(getBattleFieldHeight(), getBattleFieldWidth())*0.05;
            }

            double x = getBattleFieldWidth()/2 + Math.random()*range*2 - range;
            double y = getBattleFieldHeight()/2 + Math.random()*range*2 - range;

            centre = new Point2D.Double(x,y);

            distance = myLocation.distance(centre);
            angleToCentre = Tools.absoluteBearing(myLocation, centre);

            // Create wavy movement by adjusting turn angle dynamically
            for (int i = 0; i < distance; i += 20) {
                waveAngle = Math.sin(i / 50.0) * 30;
                setTurnRight(angleToCentre + waveAngle); // Slight left/right adjustments
                setAhead(20); // Move in small steps

                //keep scanning to continue the flow of operations
                perfectRadar(Tools.absoluteBearing(myLocation, enemyLocation));

                execute(); // Execute the movement
            }
        }
    }


    public void onScannedRobot(ScannedRobotEvent e) {
        perfectRadar(e.getBearing());
        checkWave(e);
        //surfWaves();

        /*
            Aiming
         */
    }

    public void perfectRadar(double enemyBearing) {
        double absoluteBearing = getHeading() + enemyBearing; // Direction of the enemy relative to this point
        double radarTurn = normalRelativeAngleDegrees(absoluteBearing - getRadarHeading()); // Radar rotation

        // Rotate slightly ahead of the enemy to maintain lock and auto-scan
        if(radarTurn < 0){
            radarTurn = radarTurn - 0.02;
        }else{
            radarTurn = radarTurn + 0.02;
        }

        setTurnRadarRight(radarTurn * 2);
    }

    //Checks if enemy fired and updates Wave ArrayList based on energy drops. Returns
    public boolean checkWave(ScannedRobotEvent e) {
        double enemyAbsBearing = getHeadingRadians() + e.getBearingRadians();
        double bulletPower;
        boolean newWave = false;

/*       ArrayList<Integer> _surfDirections = new ArrayList<Integer>();

        newWave = (bulletPower < 3.01 && bulletPower > 0.09) && (_surfDirections.size() > 2);

        if(newWave){

        }
*/

        // Detect enemy fire
        bulletPower = Math.min(3, enemyEnergy - e.getEnergy());
        if (bulletPower > 0 && bulletPower <= 3) {
            double bulletSpeed = 20 - (3 * bulletPower);
            waves.add(new Wave(getTime() -1 , enemyLocation, enemyAbsBearing, bulletSpeed));
            newWave = true;
        }

        // Move perpendicular to enemy while avoiding walls
        double absoluteBearing = getHeading() + e.getBearing();
        double bearingFromGun = robocode.util.Utils.normalRelativeAngleDegrees(absoluteBearing - getGunHeading());

        // Update last known enemy energy level for the next turn
        enemyEnergy = e.getEnergy();
        // Update last known enemy location for the next turn
        enemyLocation = Tools.project(myLocation, enemyAbsBearing, e.getDistance());
        // Update
        myLocation.setLocation(getX(),getY());
        return newWave;
    }

    @Override
    public void onHitByBullet(HitByBulletEvent e){

    }

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
        while (isNearWall(Tools.project(botLocation, angle, WALL_SPACE))) {
            angle += orientation*0.05;
        }
        return angle;
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

    private boolean isNearCentre(double x, double y, int range) {
        return  ((getBattleFieldWidth()/2 - range) <= x) && (x <= (getBattleFieldWidth()/2 + range)) ||
                ((getBattleFieldHeight()/2 - range) <= y) && (y <= (getBattleFieldHeight()/2 + range));
    }

}
