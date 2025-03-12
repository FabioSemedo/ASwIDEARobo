package man;

import java.awt.geom.Point2D;

public class EnemyWave extends Wave{
    Point2D.Double fireLocation;
    long fireTime;
    double bulletVelocity, directAngle, distanceTraveled;
    int direction;

    public EnemyWave() {

    }
}