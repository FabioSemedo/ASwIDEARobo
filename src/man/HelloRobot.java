/*
 *
 */
package man;

import java.awt.Color;
import java.awt.geom.Point2D;
import java.io.IOException;
import java.io.PrintStream;

import robocode.*;

import static robocode.util.Utils.normalRelativeAngle;
import static robocode.util.Utils.normalRelativeAngleDegrees;

class Tools {
    //Note that trigonometry in Robocode has an offset of 90º anticlockwise
    //This results in us having x:sin and y:cos instead of x:cos and y:sin
    //Method taken from the Robowiki Wave Surfing Tutorial
    public static Point2D.Double project(Point2D.Double sourceLocation,
                                         double angle, double length) {
        return new Point2D.Double(sourceLocation.x + Math.sin(angle) * length,
                sourceLocation.y + Math.cos(angle) * length);
    }
    public static Point2D.Double project(double x, double y, double angle, double length) {
        return new Point2D.Double(x + Math.sin(angle) * length, y + Math.cos(angle) * length);
    }

    //Returns the bearing (radians) of a point relative to a given origin point
    public static double directAngleDg(Point2D.Double origin, Point2D.Double destination){
        return Math.toDegrees(Math.atan2(destination.x - origin.x, destination.y - origin.y));
    }
    //Returns the bearing (radians) of a point relative to a given origin point
    public static double directAngle(Point2D.Double origin, Point2D.Double destination){
        return (Math.atan2(destination.x - origin.x, destination.y - origin.y));
    }

    //Method taken from Robowiki Wave Surfing Turorial
    //Returns the direction, in  radians,  of a target point relative to a starting point
    public static double absoluteBearing(Point2D.Double source, Point2D.Double target) {
        return Math.atan2(target.x - source.x, target.y - source.y);
    }

    public static double absoluteBearing(double sourceX, double sourceY, double targetX, double targetY) {
        return Math.atan2(targetX - sourceX, targetY - sourceY);
    }
    
}

/**
 * Hello(World)Robot
 * <p>
 * Prints to a data file.
 * Started as Mish-mash of Robocode sample bots.
 * But continues to grow stronger.
 *
 * @author Fabio Semedo
 */

public class HelloRobot extends AdvancedRobot {
    public Point2D.Double enemyLocation;
    public double enemyAbsBearing = 0;
    double enemySpeed = -1;
    public Point2D.Double myLocation;
    boolean found = false;

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
        found = true;
        // absoluteBearing represents the direction from our bot to the enemy.
        // It is calculated by adding the bot’s current heading to the bearing of the enemy relative to our bot.
        // This gives us the absolute direction of the enemy in the battlefield.
        double absoluteBearing = getHeading() + e.getBearing(); // Direction of the enemy relative to this point
        enemyLocation =Tools.project(getX(), getY(), absoluteBearing, e.getDistance());
        if(enemySpeed==-1) enemySpeed = e.getVelocity();
        perfectRadar(e);
    }

    public void perfectRadar(ScannedRobotEvent e) {
        double absoluteBearing = getHeading() + e.getBearing(); // Direction of the enemy relative to this point
        double radarTurn = normalRelativeAngleDegrees(absoluteBearing - getRadarHeading()); // Radar rotation

        // Rotate slightly ahead of the enemy to maintain lock and auto-scan
        if(radarTurn < 0){
            radarTurn += -0.02;
        }else{
            radarTurn += 0.02;
        }

//        setTurnRadarRight(radarTurn * 2);
        setTurnGunRight(radarTurn * 2);
    }
}												

