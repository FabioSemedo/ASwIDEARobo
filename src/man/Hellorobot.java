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

public class Hellorobot extends AdvancedRobot {
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

        setAdjustRadarForGunTurn(true);
        setAdjustGunForRobotTurn(true);


		while (true) {
			setAhead(100);
			setTurnRight(180);
            if(!found){
                turnRadarRight(45);
            }
		}
	}

	@Override
	public void onScannedRobot(ScannedRobotEvent e){
        found = true;
        double absoluteBearing = getHeading() + e.getBearing(); // Direction of the enemy relative to this point
        enemyLocation =Tools.project(getX(), getY(), absoluteBearing, e.getDistance());
        if(enemySpeed==-1) enemySpeed = e.getVelocity();
        perfectRadar(e);
        setFire(1);
    }

    public void perfectRadar(ScannedRobotEvent e) {
        double absoluteBearing;                 // Direction of the enemy relative to our location
        Point2D.Double projectedEnemyLocation;  // Estimated enemy location in the next tick
        double radarRotation;                   // Estimated radar rotation for next scan
        Point2D.Double eLocation;               // Enemy's co-ordinates

        absoluteBearing = getHeading() + e.getBearing();
        eLocation = Tools.project(getX(), getY(), absoluteBearing, e.getDistance());

        projectedEnemyLocation = Tools.project(eLocation, normalRelativeAngleDegrees(e.getHeading()), e.getVelocity());

        radarRotation = normalRelativeAngle(getRadarHeading()) - Math.atan( (projectedEnemyLocation.x - getX()) /(projectedEnemyLocation.y - getY()));

        // Choosing minimum rotation (right vs left)
        if(radarRotation==0){
            scan();
        }else if(radarRotation > Math.PI){
            radarRotation = Math.PI - radarRotation;
        }
        setTurnRadarRightRadians(radarRotation);
    }

    @Override
    public void onBulletHit(BulletHitEvent e){

    }

    public void printToFile(ScannedRobotEvent e){
        double absBearing = getHeading() + e.getHeading();
		double absBearingRadians = getHeadingRadians() + e.getHeadingRadians();

        setTurnRadarRightRadians(robocode.util.Utils.normalRelativeAngle(absBearing - getRadarHeadingRadians()) * 2);
        String str = String.format("""
                        «Testing angle normalisation functions:»
                        absBearing Dg || Rd:
                        %.3f\t%.3f
                        normalRelativeAngle Dg || Rd:
                        %.3f\t%.3f
                        normalRelativeAngleDegrees Dg || Rd:
                        %.3f\t%.3f
                        """,
                absBearing, absBearingRadians,
                normalRelativeAngle(absBearing), normalRelativeAngle(absBearingRadians),
                normalRelativeAngleDegrees(absBearing), normalRelativeAngleDegrees(absBearingRadians));

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

