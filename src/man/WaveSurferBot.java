package man;

import robocode.*;
import java.awt.*;
import java.awt.geom.Point2D;
import java.util.ArrayList;

import static robocode.util.Utils.normalRelativeAngleDegrees;

public class WaveSurferBot extends AdvancedRobot {
    private ArrayList<Wave> waves = new ArrayList<>();
    private double enemyAbsoluteBearing;

    public void run() {
        setAdjustGunForRobotTurn(true);
        setAdjustRadarForGunTurn(true);

        while (true) {
            setTurnRadarRight(360); // Keep scanning
            execute();
        }
    }

    public void onScannedRobot(ScannedRobotEvent e) {
        double enemyDistance = e.getDistance();
        enemyAbsoluteBearing = getHeadingRadians() + Math.toRadians(e.getBearing());

        // Detect enemy fire
        double bulletPower = Math.min(3, getEnergy() - e.getEnergy());
        if (bulletPower > 0 && bulletPower <= 3) {
            double bulletSpeed = 20 - (3 * bulletPower);
            waves.add(new Wave(getX(), getY(), enemyAbsoluteBearing, bulletSpeed, getTime()));
        }

        // Move perpendicular to enemy while avoiding walls
        moveSafely(e);
        double absoluteBearing = getHeading() + e.getBearing();
        double bearingFromGun = robocode.util.Utils.normalRelativeAngleDegrees(absoluteBearing - getGunHeading());

        if(getGunHeat()==0)
            fire(Math.min(3 - Math.abs(bearingFromGun), getEnergy() - .1));
    }

    private void moveSafely(ScannedRobotEvent e) {
        double angle = e.getBearing() + 90; // Perpendicular movement
        if (Math.random() > 0.5) angle *= -1; // Randomly flip direction

        double moveDistance = 100;
        double newX = getX() + Math.sin(Math.toRadians(getHeading() + angle)) * moveDistance;
        double newY = getY() + Math.cos(Math.toRadians(getHeading() + angle)) * moveDistance;

        // Check if this move would hit a wall
        if (isNearWall(newX, newY)) {
            angle += 45; // Adjust to slide along the wall
        }

        setTurnRight(angle);
        setAhead(moveDistance);
    }

    private boolean isNearWall(double x, double y) {
        double buffer = 50; // How close before we avoid walls
        return (x < buffer || x > getBattleFieldWidth() - buffer ||
                y < buffer || y > getBattleFieldHeight() - buffer);
    }

    public void onPaint(Graphics2D g) {
        g.setColor(Color.RED);
        for (Wave wave : waves) {
            int radius = wave.getCurrentRadius(getTime());
            g.drawOval((int) (wave.x - radius), (int) (wave.y - radius), radius * 2, radius * 2);
        }
    }

    private class Wave {
        double x, y;
        double bulletSpeed;
        long creationTime;

        public Wave(double x, double y, double absoluteBearing, double bulletSpeed, long creationTime) {
            this.x = x + Math.sin(absoluteBearing) * 10;
            this.y = y + Math.cos(absoluteBearing) * 10;
            this.bulletSpeed = bulletSpeed;
            this.creationTime = creationTime;
        }

        public int getCurrentRadius(long currentTime) {
            return (int) ((currentTime - creationTime) * bulletSpeed);
        }
    }
}
