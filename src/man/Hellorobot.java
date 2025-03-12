/*
 *
 */
package man;

import java.awt.Color;
import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintStream;

import robocode.AdvancedRobot;
import robocode.RobocodeFileOutputStream;
import robocode.ScannedRobotEvent;
import robocode.HitByBulletEvent;

import static robocode.util.Utils.normalRelativeAngle;
import static robocode.util.Utils.normalRelativeAngleDegrees;


/**
 * Hello(World)Robot
 * <p>
 * Prints to a data file.
 * 	Mish-mash of Robocode sample bots.
 *
 * @author Fabio Semedo
 */
public class Hellorobot extends AdvancedRobot {

	public void run() {
		//color of the body, gun, radar, bullet, and scan arc
		setBodyColor(Color.black);
		setGunColor(Color.red);
		setRadarColor(Color.lightGray);
		setBulletColor(Color.red);

		while (true) {
			ahead(100);
			turnRight(180);
		}
	}

	@Override
	public void onScannedRobot(ScannedRobotEvent e){
        double absBearing = getHeading() + e.getHeading();
		double absBearingRadias = getHeadingRadians() + e.getHeadingRadians();

		setTurnRadarRightRadians(robocode.util.Utils.normalRelativeAngle(absBearing - getRadarHeadingRadians()) * 2);
		String str = String.format("""
                        «Testing angle normalisation functions:»
                        absBearing Dg || Rd:
                        %.3f\t%.3f
                        normalRelativeAngle Dg || Rd:
                        %.3f\t%.3f\t\t%.3f|%.3f
						normalRelativeAngleDegrees Dg || Rd:
						%.3f\t%.3f\t\t%.3f|%.3f
                        """,
				absBearing, absBearingRadias,
				normalRelativeAngle(absBearing), normalRelativeAngle(absBearingRadias),
				normalRelativeAngleDegrees(absBearing), normalRelativeAngleDegrees(absBearingRadias));

        PrintStream w = null;
        try {
            w = new PrintStream(new RobocodeFileOutputStream(("stdOut.dat"), true));

            w.println(str);

            // PrintStreams don't throw IOExceptions during prints, they simply set a flag.... so check it here.
            if (w.checkError()) {
                out.println("I could not write the str!");
            }
        } catch (IOException exp) {
            out.println("IOException trying to write: ");
            exp.printStackTrace(out);
        } finally {
            if (w != null) {
                w.close();
            }
        }
    }

	/**
	 * We were hit!  Turn perpendicular to the bullet,
	 * so our seesaw might avoid a future shot.
	 */

	@Override
	public void onHitByBullet(HitByBulletEvent e) {
		turnLeft(90 - e.getBearing());
	}
}												

