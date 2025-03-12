package man;

import robocode.util.Utils;

public class SandBox {
    public static void main(String[] args) {
        // Example angles in radians
        double[] angles = {
                0.0,                  // 0 radians
                Math.PI / 2,          // 90 degrees
                Math.PI,              // 180 degrees
                3 * Math.PI / 2,      // 270 degrees
                2 * Math.PI,          // 360 degrees (should normalize to 0)
                -Math.PI / 2,         // -90 degrees
                -Math.PI,             // -180 degrees
                -3 * Math.PI / 2,     // -270 degrees
                -2 * Math.PI,         // -360 degrees (should normalize to 0)
                5 * Math.PI / 2,      // 450 degrees (should normalize to π/2)
                -5 * Math.PI / 2      // -450 degrees (should normalize to -π/2)
        };

        // Print normalized angles
        for (double angle : angles) {
            double normalizedAngle = Utils.normalRelativeAngle(angle);
            System.out.printf("Original: %8.4f radians -> Normalized: %8.4f radians%n", angle, normalizedAngle);
        }
    }
}
