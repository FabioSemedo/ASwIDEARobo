package man;

import java.awt.geom.Point2D;

public class Wave {
    Point2D.Double startPoint;  //Co-ordinate of the enemy on the turn they fired.
    long startTime;             //Time (turn) of enemy fire. This is typically current_time - 1
    double bulletSpeed;         //Assumed bullet speed based on energy drop
    double directAngle;         //Direction (radians [-pi,pi)) of thisBot relative to enemy.
    int direction;              //Change in enemy's Firing angle relative to thisBot's movement. 1 Clockwise, -1 anticlockwise.

    // the Wave's constructor parameters must be adjusted for the enemy's position 2 ticks before the current radar scan
    // affecting x, y, and startTime
    public Wave(long startTime, Point2D.Double enemyLocation, double enemyAbsBearing, double bulletSpeed)
    {
        this.startPoint = enemyLocation;
        this.bulletSpeed = bulletSpeed;
        this.startTime = startTime;
        this.directAngle = enemyAbsBearing;
    }

    public double distanceTraveled(long currentTime){
        return (currentTime-startTime)*bulletSpeed;
    };

    public double getCurrentRadius(long currentTime)
    {
        return (double) ((currentTime - startTime) * bulletSpeed);
    }

    public double bulletSpeed(double energy){
        if(energy>3) return 0.0;

        return 20 - (3.0 * energy);
    }
}