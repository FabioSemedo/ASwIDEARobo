package man;

import java.awt.geom.Point2D;

public class Wave {
    Point2D.Double startPoint;
    long startTime;
    double bulletSpeed;

    // the Wave's constructor parameters must be adjusted for the enemy's position 2 ticks before the current radar scan
    // affecting x, y, and startTime
    public Wave(long startTime, double enemyX, double enemyY, double enemyAbsBearing, double bulletSpeed)
    {
        //co-ordinates of enemy shot
        this.startPoint = new Point2D.Double(enemyX, enemyY);
        //bulletspeed based on enemy energy drop
        this.bulletSpeed = bulletSpeed;
        //time of enemy shot
        this.startTime = startTime;
        //enemy velocity
    }

    public double getCurrentRadius(long currentTime)
    {
        return (double) ((currentTime - startTime) * bulletSpeed);
    }

    public double bulletSpeed(double energy){
        if(energy>3) return 0.0;

        return 20 - (3.0 * energy);
    }
}