package man;
import robocode.*;

import static robocode.util.Utils.normalRelativeAngleDegrees;

public class TheSniper extends AdvancedRobot{

    public void run() {
        //TurnSpeed 10-4" per tick
        //GunTurnSpeed 20" per tick
        //RadarTurnSpeed 45" per tick
        // 360/(45+20+10) ==> 4.8
        // 4.8 * (45,20,10) = (180, 80, 40)
        getTime();
        while(true) {
            turnGunRight(getRadarHeading());
        }
    }

    @Override
    public void onScannedRobot(ScannedRobotEvent enemy){
        clearAllEvents();

        // Calculate exact location of the robot
        double absoluteBearing = getHeading() + enemy.getBearing();
        double bearingFromGun = normalRelativeAngleDegrees(absoluteBearing - getGunHeading());

        // If it's close enough, fire!
        if (Math.abs(bearingFromGun) <= 3) {
            turnGunRight(bearingFromGun);
            // We check gun heat here, because calling fire()
            // uses a turn, which could cause us to lose track
            // of the other robot.
            if (getGunHeat() == 0) {
                fire(Math.min(3 - Math.abs(bearingFromGun), getEnergy() - .1));
            }
        } // otherwise just set the gun to turn.
        // Note:  This will have no effect until we call scan()
        else {
            turnGunRight(bearingFromGun);
        }
        // Generates another scan event if we see a robot.
        // We only need to call this if the gun (and therefore radar)
        // are not turning.  Otherwise, scan is called automatically.
        if (bearingFromGun == 0) {
            scan();
        }
    }
}
