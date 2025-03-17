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

import static robocode.util.Utils.normalRelativeAngle;
import static robocode.util.Utils.normalRelativeAngleDegrees;


public class WaveSurferBot extends AdvancedRobot {
    //space for GuessFactor targeting indexes
    private static final int GUESS_FACTOR_RANGE = 47;
    public static double[] surfStats =new double[GUESS_FACTOR_RANGE];

    // Battlefield dimensions for wall smoothing
    private static final int WALL_STICK = 150; // minimum pixels we keep from the wall
    private static final int FIELD_CENTRE_RANGE = 50; // minimum pixels we keep from the wall

    public Point2D.Double myLocation;       // Where the enemy last saw us
    public Point2D.Double enemyLocation;    // Where we last saw the enemy

    public ArrayList<Wave> waves;     // List of active waves
    public ArrayList<Integer> surfDirections;   // Directions of past movements, Clockwise 1 or anticlockwise -1

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
                return isNearWall(getX(), getY(), WALL_STICK);
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
                setAhead(WALL_STICK); // Move in small steps

                //keep scanning to continue the flow of operations
                perfectRadar(Math.toDegrees(Tools.absoluteBearing(new Point2D.Double(getX(), getY()), enemyLocation)));
            }
        }
    }


    public void onScannedRobot(ScannedRobotEvent e) {
        perfectRadar(e.getBearing());
        waveHandler(e);
        goWaveSurf();

        /*
            Aiming
         */
    }

    //Continuous enemy scan
    public void perfectRadar(double enemyBearing) {
        double absoluteBearing = getHeading() + enemyBearing; // Direction of the enemy relative to this point
        double radarTurn = normalRelativeAngleDegrees(absoluteBearing - getRadarHeading()); // Radar rotation

        // Rotate slightly ahead of the enemy to maintain lock and auto-scan
        if(radarTurn < 0){
            radarTurn = radarTurn - 0.02;
        }else{
            radarTurn = radarTurn + 0.02;
        }

        //Making the radar over-shoot tends to keep it from losing the enemy
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
        if (bulletPower >= 0.1 && bulletPower <= 3) {
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
        Wave nextWave = getClosestSurfableWave();
        if (nextWave == null) return;

        // Check danger for both directions
        double dangerLeft = checkDanger(nextWave, -1);
        double dangerRight = checkDanger(nextWave, 1);

        // Choose the safer direction
        double goAngleRd = Tools.absoluteBearing(nextWave.startPoint, myLocation);
        if (dangerLeft < dangerRight) {
            goAngleRd = wallSmoothing(myLocation, goAngleRd - (Math.PI / 2), -1);
        } else {
            goAngleRd = wallSmoothing(myLocation, goAngleRd + (Math.PI / 2), 1);
        }

        // Move in the chosen direction
        setBackAsFront(this, goAngleRd);
    }

    //Returns the wave that will arrive at our location the soonest
    public Wave getClosestSurfableWave(){
        //Start at the max possible time (longest path and slowest bullet)
        double shortestTime; // The closest arrival time of a wave to our location
//        shortestTime <-- Math.pow(getBattleFieldHeight(), 2) + Math.pow(getBattleFieldWidth(),2) ;
//        shortestTime <-- Math.sqrt(shortestTime); //Pythagoras for diagonal of the battlefield
//        shortestTime <-- shortestTime/(11); //Divided by the speed of bullet of power 3
        shortestTime = Math.sqrt(Math.pow(getBattleFieldHeight(),2) + Math.pow(getBattleFieldWidth(),2))/11;

        Wave wave = null; // incoming wave
        double arrivalTime;

        for( Wave ew : waves ){
            //In how many ticks this wave will reach us
            arrivalTime = myLocation.distance(ew.startPoint)/ew.bulletSpeed - (getTime() - ew.startTime);

            // >1 implies we have more than 1 tick to react
            if(arrivalTime > 1 && arrivalTime < shortestTime){
                wave = ew;
                shortestTime = arrivalTime;
            }
        }

        return wave;
    }

    @Override
    public void onHitByBullet(HitByBulletEvent e){
        myLocation.setLocation(getX(), getY()); //enemy knows where we are
        waveHit(e.getBullet().getVelocity());
    }

    //We were shot.
    public void waveHit(double blSpeed){
        Wave hitWave = null;
        Point2D.Double thisLocation = new Point2D.Double(getX(), getY()); //Not dependent on global variables

        for(Wave w : waves){
            // check if the speed and timing match some wave (with a little tolerance)
            if( (Math.abs(w.distanceTraveled(getTime()) - w.startPoint.distance(thisLocation)) <= 10) //Compare distances myLocation-to-waveStartPoint and waveDistanceTraveled
                && (Math.abs(w.bulletSpeed - blSpeed) <= 0.1) // compare speed of the bullet that hit us to the waveBulletSpeed
            ){
                hitWave = w;
            }
        }

        // If we found a matching wave, update the stats.
        if (hitWave != null) {

            // Map the guess factor from [-1, 1] to a bin index (our bins are the elements of the surfStats defined as Guess_Factor_Range)
            // Increment the stat for this guess factor.
            surfStats[getFactorIndex(hitWave, thisLocation)]++;

            // Remove the wave so it isn't used again.
            waves.remove(hitWave);
        }
    }

    //maxEscapeAngle = Math.sin(8.0/bulletSpeed)

    //Method based on wallSmoothing from the Surfing Tutorial
    // angle - try to find an angle that will keep us away from a wall
    // orientation - (1 or -1), informs if angle turns right (1) or angle turns left (-1)
    public double wallSmoothing(Point2D.Double currectLocation, double angleRd, int orientation) {
        //decide if we need to turn to avoid walls and by how much
        while (isNearWall(Tools.project(currectLocation, angleRd, WALL_STICK), WALL_STICK + 0.1)) {
            angleRd = angleRd + orientation*0.05;
        }
        return angleRd;
    }

    private boolean isNearWall(double x, double y, double minSpace) {
        return ((x < minSpace) || (x > (getBattleFieldWidth() - minSpace)) ||
                (y < minSpace) || (y > getBattleFieldHeight() - minSpace) );
    }
    private boolean isNearWall(Point2D.Double point, double minSpace) {
        return isNearWall(point.x, point.y, minSpace);
    }

    //Tells us how often we are shot based on the past
    public double checkDanger(Wave wave, int direction) {
        return surfStats[ getFactorIndex(wave, estimateFuturePosition(wave, direction)) ];
    }

    //Tells us what bearing (normalised to the guess factor range [-1, 1]) a point in a wave corresponds to.
    //This index is in [0 , GUESS FACTOR RANGE-1]
    public static int getFactorIndex(Wave ew, Point2D.Double targetLocation) {
        double offsetAngle = (Tools.absoluteBearing(ew.startPoint, targetLocation) - ew.directAngle);
        double factor = ew.direction * normalRelativeAngleDegrees(offsetAngle) / (Math.sin(8.0 / (ew.bulletSpeed)));
        int index = (int) (0.5 * (GUESS_FACTOR_RANGE - 1) * (1.0 + factor));

        if (index < 0) {
            return 0;
        }else if(index > GUESS_FACTOR_RANGE - 1) {
            return GUESS_FACTOR_RANGE - 1;
        }

        return  index;
    }

    //Estimates the future position of the bot based on game physics.
    public Point2D.Double estimateFuturePosition(Wave wave, int direction) {
        Point2D.Double futurePosition = new Point2D.Double(getX(), getY());

        //For the sake of the math, radians were used.
        double speed = getVelocity();
        double heading = getHeadingRadians();
        double turnLimit;
        double moveAngle;
        double movementDirection;
        double waveDistance;
        double perpendicularAngle;

        int timeStep = 0;
        boolean reached = false;

        while(!reached && timeStep < 500){
            waveDistance = wave.startPoint.distance(futurePosition);
            perpendicularAngle = Math.PI / 2 * Math.min(1, waveDistance / (20 * wave.bulletSpeed));

            // Calculate movement angle considering walls
            moveAngle = wallSmoothing(futurePosition, Tools.absoluteBearing(wave.startPoint, futurePosition) + (direction * perpendicularAngle), direction);
            moveAngle = moveAngle - heading; //Adjust for current heading
            movementDirection = 1;

            // Correct for downward movement
            if (Math.cos(moveAngle) < 0) {
                moveAngle = moveAngle + Math.PI;
                movementDirection = -1;
            }

            moveAngle = normalRelativeAngle(moveAngle);
            // Limit turning rate per tick according to the Surfing tutorial
            turnLimit = Math.PI / 720.0 * (40.0 - 3.0 * Math.abs(speed));

            if(moveAngle < -turnLimit) moveAngle = -turnLimit;
            if(moveAngle > turnLimit) moveAngle = turnLimit;
            if(-0.01 < moveAngle && moveAngle < 0.01) moveAngle = 0.01; //struggles with 0


            heading = normalRelativeAngle(heading + moveAngle);

            // Adjust speed based on direction
            if(speed * movementDirection < 0){
                speed = speed * (1 + 2*movementDirection); //deceleration = 2
            }else{
                speed = speed * (1 + movementDirection); //acceleration = 1
            }

            //ensure speed is within the correct bounds
            if(speed < -8) speed = -8; //max reverse speed
            if(speed > 8) speed = 8; //max forward speed

            // Compute the next position
            futurePosition = Tools.project(futurePosition, heading, speed);
            timeStep++;

            // Check if the wave reaches the predicted position
            if (futurePosition.distance(wave.startPoint) < wave.bulletSpeed * (1 + timeStep + getTime() - wave.startTime) ) {
                reached = true;
            }
        }//end while

        return futurePosition;
    }

    //Decide if it is better to move forward or backwards to reach a given angle
    //Method from Surfing Tutorial
    public static void setBackAsFront(AdvancedRobot robot, double goAngleRd) {
        double angle = normalRelativeAngle(goAngleRd - robot.getHeadingRadians());
        if (Math.abs(angle) <= (Math.PI /2)) {
            //North
            robot.setAhead(100);

            //NorthWest
            if (angle < 0) robot.setTurnLeft(-1*angle);
            //NorthEast
            else robot.setTurnRight(angle);

        } else {
            //South
            robot.setBack(100);
            //SouthWest
            if (angle < 0) robot.setTurnRight(Math.PI + angle);
            //SouthEast
            else robot.setTurnLeft(Math.PI - angle);

        }
    }
}
