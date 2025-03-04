package man;
import robocode.*;

import static robocode.util.Utils.normalRelativeAngleDegrees;

public class Hellorobot extends AdvancedRobot {
    public void run() {
		int count = 0;
		double gunTurnAmt;
		String trackName;

        while (true) {

            //sample.SpinBot shows us that curved movement is better at dodging
            // Tell the game that when we take move,
			// we'll also want to turn right... a lot.
			setTurnRight(10000);
			// Limit our speed to 5
			setMaxVelocity(5);
			// Start moving (and turning)
			ahead(10000);
			// Repeat.

            //sample.Tracker LockOn
            // turn the Gun (looks for enemy)
			turnGunRight(10);
			// Keep track of how long we've been looking
			count++;
			// If we haven't seen our target for 2 turns, look left
			if (count > 2) {
				gunTurnAmt = -10;
			}
			// If we still haven't seen our target for 5 turns, look right
			if (count > 5) {
				gunTurnAmt = 10;
			}
			// If we *still* haven't seen our target after 10 turns, find another target
			if (count > 11) {
				trackName = null;
			}
        }
    }

	@Override
    public void onScannedRobot(ScannedRobotEvent e) {
        //sample.Fire 
        //If the other robot is close by, and we have plenty of life,
		// fire hard!
		e.getBearing();




		if (e.getDistance() < 50 && getEnergy() > 50) {
			fire(3);
		} // otherwise, fire 1.
		else if(e.getDistance() > 50) {
			fire(1);
		}
        //sample.RamFire
        else{
            if (e.getEnergy() > 10) {
                fire(2);
            } else if (e.getEnergy() > 4) {
                fire(1);
            } else if (e.getEnergy() > 2) {
                fire(.5);
            } else if (e.getEnergy() > .4) {
                fire(.1);
            }
        }
		// Call scan again, before we turn the gun
		scan();
        //sample.TrackFire
        // Calculate exact location of the robot
		double absoluteBearing = getHeading() + e.getBearing();
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

/*
 * sample.Fire:
 * 	 * onHitByBullet:  Turn perpendicular to the bullet, and move a bit.

 * public void onHitByBullet(HitByBulletEvent e) {
		turnRight(normalRelativeAngleDegrees(90 - (getHeading() - e.getHeading())));

		ahead(dist);
		dist *= -1;
		scan();
	}
	* onHitRobot:  Aim at it.  Fire Hard!
	public void onHitRobot(HitRobotEvent e) {
		double turnGunAmt = normalRelativeAngleDegrees(e.getBearing() + getHeading() - getGunHeading());

		turnGunRight(turnGunAmt);
		fire(3);
	}

    * sample.SpinBot
	 * onHitRobot:  If it's our fault, we'll stop turning and moving,
	 * so we need to turn again to keep spinning.
    if (e.getBearing() > -10 && e.getBearing() < 10) {
			fire(3);
		}
		if (e.isMyFault()) {
			turnRight(10);
		}
 * 
 */