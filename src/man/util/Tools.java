package man.util;

import java.awt.geom.Point2D;

public class Tools {
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