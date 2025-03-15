/* Apollo Tank written by Maria A. and Fábio S.
 *
 * The movement of this bot was adapted from the wave surfing algorithm, due credit to:
 *   Patrick "Voidious" Cupka, Wave Surfing Tutorial, at https://robowiki.net/wiki/Wave_Surfing_Tutorial on 11/03/2025,
 *   Vincent Maliko, code of the Wave Surfing Tutorial by "Voidious", at https://github.com/malikov/robocode/blob/master/WaveSurfer.java on 11/03/2025,
 * Additional help:
 *   "Kawigi" ,GuessFactor Targeting Tutorial , at https://robowiki.net/wiki/GuessFactor_Targeting_Tutorial
 *   ChatGPT , code and learning assistant, https://chatgpt.com/.
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
    //space for GuessFactor targeting indexes
    private static final int GUESS_FACTOR_RANGE = 47;
    public static double[] surfStats =new double[GUESS_FACTOR_RANGE];

    // Battlefield dimensions for wall smoothing
    private static final int WALL_STICK = 150; // minimum pixels we keep from the wall
    private static final int MIN_WALL_SPACE = 72; // minimum pixels we keep from the wall.
    private static final int FIELD_CENTRE_RANGE = 50; // minimum pixels we keep from the wall

    public Point2D.Double myLocation;       // Where the enemy last saw us
    public Point2D.Double enemyLocation;    // Where we last saw the enemy

    public ArrayList<Wave> waves;     // List of active waves
    public ArrayList<Integer> surfDirections;   // Directions of past movements, Clockwise 1 or anticlockwise -1

    public ArrayList<Double> waveAbsBearings;   // Absolute bearings at of point of fire from past scans

    public static double enemyEnergy = 100.0;   // Enemy's last known energy level

    public void run() {
        setBodyColor(Color.black);
        setGunColor(Color.red);
        setRadarColor(Color.black);
        setBulletColor(Color.red);

        myLocation = new Point2D.Double(getX(), getY());
        waves = new ArrayList<Wave>();
        surfDirections = new ArrayList<Integer>();

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

    //Escape walls if we spawn there
    public void onCustomEvent(CustomEvent e) {
        if (e.getCondition().getName().equals("walled")) {
            double range = FIELD_CENTRE_RANGE;
            double waveAngle;
            Point2D.Double centre;
            double distance;
            double angleToCentre;

            //Check for out of bounds
            if( (getBattleFieldHeight() < FIELD_CENTRE_RANGE*2)
                || (getBattleFieldWidth() < FIELD_CENTRE_RANGE*2)
            ){
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
                perfectRadar(Tools.absoluteBearing(new Point2D.Double(getX(), getY()), enemyLocation));

                execute(); // Execute the movement
            }
        }
    }


    public void onScannedRobot(ScannedRobotEvent e) {
        perfectRadar(e.getBearing());
        waveHandler(e);
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

        //Making the radar over-scan tends to keep it from losing the enemy
        setTurnRadarRight(radarTurn * 2);
    }

    //Checks if enemy fired and updates Wave ArrayList based on energy drops.
    public void waveHandler(ScannedRobotEvent e) {
        double enemyAbsBearing = getHeading() + e.getBearing();
        double bulletPower;

/*       ArrayList<Integer> _surfDirections = new ArrayList<Integer>();

        newWave = (bulletPower < 3.01 && bulletPower > 0.09) && (_surfDirections.size() > 2);

        if(newWave){

        }
*/

        // Detect enemy fire
        bulletPower = Math.min(3, enemyEnergy - e.getEnergy());
        if (bulletPower > 0.85 && bulletPower <= 3) {
            int direction = Wave.calcDirection(getVelocity(), getHeading(), enemyAbsBearing);
            double bulletSpeed = 20 - (3 * bulletPower);

            waves.add(new Wave(getTime() -1 , enemyLocation, enemyAbsBearing, bulletSpeed, direction));
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

        updateWaves();
        //goWaveSurf();
    }

    public void updateWaves() {
        for (int i = 0; i < waves.size(); i++) {
            Wave enemyWave = waves.get(i);

            // Remove waves that have passed (with tolerance)
            if (enemyWave.distanceTraveled(getTime()) > myLocation.distance(enemyWave.startPoint) + 50) {
                waves.remove(i);
                i--;
            }
        }
    }

    public void goWaveSurf(){

    }

    @Override
    public void onHitByBullet(HitByBulletEvent e){
        waveHit(e);
    }

    public void waveHit(HitByBulletEvent e){
        double blX = e.getBullet().getX();
        double blY = e.getBullet().getY();
        double blRelativeHeading = normalRelativeAngleDegrees(e.getBullet().getHeading());
        double blSpeed = e.getBullet().getVelocity();
        Wave hitWave = null;

        for(Wave w : waves){
            // check if the speeds and timing match some wave (with a little tolerance)
            if( (Math.abs(w.distanceTraveled(getTime()) - w.startPoint.distance(myLocation)) <= 10) //Compare distances myLocation-to-waveStartPoint and waveDistanceTraveled
                && (Math.abs(w.bulletSpeed - blSpeed) <= 0.1) // compare speed of the bullet that hit us to the waveBulletSpeed
            ){
                hitWave = w;
            }
        }

        // If we found a matching wave, update the stats.
        if (hitWave != null) {
            Point2D.Double hitLocation = new Point2D.Double(getX(), getY());

            // Calculate the offset angle and normalise it:
            // This is the difference between our Bearing at the wave's fireTime and our Bearing at the time of the hit; Relative to waveStartPoint.
            // Normalised the angle to the range [-180, 180].
            double normalizedOffset = normalRelativeAngleDegrees(Tools.absoluteBearing(hitWave.startPoint, hitLocation) - hitWave.directAngle);

            // Divide by the maximum escape angle (which depends on bullet velocity) and multiply by (-1 or 1) the wave's direction.
            double guessFactor = normalizedOffset / maxEscapeAngle(hitWave.bulletSpeed) * hitWave.direction;

            // Map the guess factor from [-1, 1] to a bin index (our bins are the elements of the surfStats defined as Guess_Factor_Range)
            int index = (int) limit(0, (guessFactor * ((GUESS_FACTOR_RANGE - 1) / 2.0)) + ((GUESS_FACTOR_RANGE - 1) / 2.0), GUESS_FACTOR_RANGE - 1);

            // Increment the stat for this guess factor.
            surfStats[index]++;

            // Remove the wave so it isn't used again.
            waves.remove(hitWave);
        }
    }

    public static double limit(double min, double value, double max) {
        if(value < min){
            return min;
        }else if(value > max){
            return max;
        }
        return value;
    }

    //Method taken from the Robowiki Wave Surfing Turorial
    public static double maxEscapeAngle(double velocity) {
        return Math.sin(8.0/velocity);
    }

    //Method taken and adapted from the Robowiki Wave Surfing Turorial
    // botLocation - our co-ordinates
    // angle - try to find an angle that will keep us away from a wall
    // orientation - (1 or -1), informs if angle turns right (1) or angle turns left (-1)
    public double wallSmoothing(Point2D.Double botLocation, double angle, int orientation) {
        while (isNearWall(Tools.project(botLocation, angle, WALL_STICK), WALL_STICK)) {
            angle += orientation*0.05;
        }
        return angle;
    }

    private boolean isNearWall(double x, double y, double minSpace) {
        return ((x < minSpace) || (x > (getBattleFieldWidth() - minSpace)) ||
                (y < minSpace) || (y > getBattleFieldHeight() - minSpace) );
    }
    private boolean isNearWall(Point2D.Double point, double minSpace) {
        return isNearWall(point.x, point.y, minSpace);
    }

}
