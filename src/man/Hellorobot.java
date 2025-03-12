/*
 * Copyright (c) 2001-2023 Mathew A. Nelson and Robocode contributors
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * https://robocode.sourceforge.io/license/epl-v10.html
 */
package man;

import java.awt.Color;
// import java.io.BufferedWriter;
// import java.io.FileWriter;
// import java.io.IOException;

import robocode.*;
import static robocode.util.Utils.normalRelativeAngle;
import static robocode.util.Utils.normalRelativeAngleDegrees;


/**
 * MyFirstRobot - a sample robot by Mathew Nelson.
 * <p>
 * Moves in a seesaw motion, and spins the gun around at each end.
 *
 * @author Mathew A. Nelson (original)
 */
public class Hellorobot extends AdvancedRobot {
	/**
	 * MyFirstRobot's run method - Seesaw
	 */
	public void run() {
		//color of the body, gun, radar, bullet, and scan arc
		setBodyColor(Color.black);
		setGunColor(Color.red);
		setRadarColor(Color.lightGray);
		setBulletColor(Color.red);

		while (true) {
			turnGunRight(90);
		}
	}

	/*
	 * Fire when we see a robot
	 */
	public void onScannedRobotEvent(ScannedRobotEvent e){
        double absBearing = getHeading() + e.getHeading();
		double absBearingRadias = getHeadingRadians() + e.getHeadingRadians();

		setTurnRadarRightRadians(robocode.util.Utils.normalRelativeAngle(absBearing - getRadarHeadingRadians()) * 2);
		String str = String.format("""
                        «Testing angle normalisation functions:»
                        absBearing:\t\t%.3f || %.3f
                        normalRelativeAngle:  \t\t%.3f || %.3f
                        normalRelativeAngleDg:\t\t%.3f || %.3f
                        """,
				absBearing, absBearingRadias,
				normalRelativeAngle(absBearing), normalRelativeAngle(absBearingRadias),
				normalRelativeAngleDegrees(absBearing), normalRelativeAngleDegrees(absBearingRadias));

		out.println("Getting closer."+100.0);
		out.println(str);
    }

	/**
	 * We were hit!  Turn perpendicular to the bullet,
	 * so our seesaw might avoid a future shot.
	 */
	public void onHitByBullet(HitByBulletEvent e) {
		turnLeft(90 - e.getBearing());
	}
}												

