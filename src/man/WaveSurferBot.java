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
        while (true) {
            turnGunRight(360); // Keep scanning
        }
    }

    public void onScannedRobot(ScannedRobotEvent e) {
        /*
        double enemyDistance = e.getDistance();
        enemyAbsoluteBearing = getHeadingRadians() + Math.toRadians(e.getBearing());

        // Detect enemy fire
        double bulletPower = Math.min(3, getEnergy() - e.getEnergy());
        if (bulletPower > 0 && bulletPower <= 3) {
            double bulletSpeed = 20 - (3 * bulletPower);
            waves.add(new Wave(getX(), getY(), enemyAbsoluteBearing, bulletSpeed, getTime()));
        }

        // Move perpendicular to enemy while avoiding walls


        double absoluteBearing = getHeading() + e.getBearing();
        double bearingFromGun = robocode.util.Utils.normalRelativeAngleDegrees(absoluteBearing - getGunHeading());

        if(getGunHeat()==0)
            fire(Math.min(3 - Math.abs(bearingFromGun), getEnergy() - .1));
//*/
        double absoluteBearing = getHeading() + e.getBearing();
        double bearingFromGun = normalRelativeAngleDegrees(absoluteBearing - getGunHeading());

        // If it's close enough, fire!
        if (Math.abs(bearingFromGun) <= 3) {
            turnGunRight(bearingFromGun);
            // We check gun heat here, because calling fire()
            // uses a turn, which could cause us to lose track
            // of the other robot.
            if (getGunHeat() == 0) {
                fire(Math.min(3 - Math.abs(bearingFromGun), getEnergy() - .1));
            }
        } // otherwise just set the gun to turn.
        // Note:  This will have no effect until we call scan()
        else {
            turnGunRight(bearingFromGun);
        }
        // Generates another scan event if we see a robot.
        // We only need to call this if the gun (and therefore radar)
        // are not turning.  Otherwise, scan is called automatically.
        moveSafely(e);

        if (bearingFromGun == 0) {
            scan();
        }
    }

    private void moveSafely(ScannedRobotEvent e) {
        double angle = e.getBearing() + 90; // Perpendicular movement
        if (Math.random() > 0.5) angle *= -1; // Randomly flip direction

        double moveDistance = 100;
        double newX = getX() + Math.sin(Math.toRadians(getHeading() + angle)) * moveDistance;
        double newY = getY() + Math.cos(Math.toRadians(getHeading() + angle)) * moveDistance;

        // Check if this move would hit a wall
        if (isNearWall(newX, newY)) {
            if(newX < 50){ // LeftWall
                angle = 0;
            }

            else if(newX > getWidth()-50){ //RightWall
                angle = getHeading() ;
            }

            else if(newY < 50){// BottomWall
                angle = getHeading() ;
            }

            else if(newY > getHeading()-50){ // TopWall
                angle += 45; // Adjust to slide along the wall
            }
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
