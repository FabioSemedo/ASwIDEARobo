package man;

import java.awt.Color;

import robocode.*;

import static robocode.util.Utils.normalRelativeAngleDegrees;
/*
 * Hello(World)Robot
 * Prints to a data file.
 * Started as Mish-mash of Robocode sample bots.
 * But continues to grow stronger.
 *
 * @author Fabs
 */

public class HelloRobot extends AdvancedRobot {

    public void run() {
		//color of the body, gun, radar, bullet, and scan arc
		setBodyColor(Color.black);
		setGunColor(Color.red);
		setRadarColor(Color.lightGray);
		setBulletColor(Color.red);

        setAdjustGunForRobotTurn(true);


		while (true) {
            //Note that we are probably never going to come back to the run() function once we scan an enemy
			turnGunRight(360*360);
		}
	}

	@Override
	public void onScannedRobot(ScannedRobotEvent e){
        // absoluteBearing represents the direction from our bot to the enemy.
        // It is calculated by adding the bot’s current heading to the bearing of the enemy relative to our bot.
        // This gives us the absolute direction of the enemy in the battlefield.
        perfectRadar(e);
    }

    public void perfectRadar(ScannedRobotEvent e) {
        double absoluteBearing = getHeading() + e.getBearing(); // Direction of the enemy relative to this point
        double radarTurn = normalRelativeAngleDegrees(absoluteBearing - getRadarHeading()); // Radar rotation

        // Rotate slightly ahead of the enemy to maintain lock and auto-scan
        if(radarTurn < 0){
            radarTurn = radarTurn - 0.02;
        }else{
            radarTurn = radarTurn + 0.02;
        }

//        setTurnRadarRight(radarTurn * 2);
        setTurnGunRight(radarTurn * 2);
    }
}												

