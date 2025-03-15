package man;

import java.awt.geom.Point2D;

public class Wave {
    Point2D.Double startPoint;  //Co-ordinate of the enemy on the turn they fired.
    long startTime;             //Time (turn) of enemy fire. This is typically current_time - 1
    double bulletSpeed;         //Assumed bullet speed based on energy drop
    double directAngle;         //Direction (radians [-pi,pi)) of thisBot relative to enemy. Enemy's base targeting angle for deciding where to fire.
    int direction;              // 1 or -1. Change in enemy's Firing angle in response to thisBot's movement. 1 Clockwise, -1 anticlockwise.

    // the Wave's constructor parameters must use the enemy's position 1 tick before the time of current radar scan
    // affecting x, y, and startTime
    public Wave(long startTime, Point2D.Double enemyLocation, double enemyAbsBearing, double bulletSpeed, int direction)
    {
        this.startPoint = enemyLocation;
        this.bulletSpeed = bulletSpeed;
        this.startTime = startTime;
        this.directAngle = enemyAbsBearing;
        this.direction = direction;
    }

    public static int calcDirection(double myVelocity, double myHeading, double enemyAbsBearing) {
//      double relativeHeading = robocode.util.Utils.normalRelativeAngleDegrees(myHeading - enemyAbsBearing);
//      relativeHeading = Math.toRadians(relativeHeading);
        if(Math.sin(Math.toRadians(robocode.util.Utils.normalRelativeAngleDegrees(myHeading - enemyAbsBearing))) * myVelocity>= 0)
            return 1;
        return -1;
    }

    public double distanceTraveled(long currentTime){
        return (currentTime-startTime)*bulletSpeed;
    };
}