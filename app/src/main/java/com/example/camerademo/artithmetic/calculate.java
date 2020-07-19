package com.example.camerademo.artithmetic;

public class calculate {

    /**
     * Given the current direction, current location, and a target location, calculate the Bearing
     * towards the target point.
     *
     * The return value is the angle input to turn() that would point the turtle in the direction of
     * the target point (targetX,targetY), given that the turtle is already at the point
     * (currentX,currentY) and is facing at angle currentBearing. The angle must be expressed in
     * degrees, where 0 <= angle < 360.
     *
     * HINT: look at http://en.wikipedia.org/wiki/Atan2 and Java's math libraries
     *
     * @param currentBearing current direction as clockwise from north
     * @param currentX current location x-coordinate
     * @param currentY current location y-coordinate
     * @param targetX target point x-coordinate
     * @param targetY target point y-coordinate
     * @return adjustment to Bearing (right turn amount) to get to target point,
     *         must be 0 <= angle < 360
     */

    public static double calculateBearingToPoint(double currentBearing, int currentX, int currentY, int targetX, int targetY) {
//        throw new RuntimeException("implement me!");
        double angle;
        int x = targetX - currentX;
        int y = targetY - currentY;
        angle = Math.atan2(x, y)*180.0/ Math.PI-currentBearing;
        return angle < 0? angle + 360 : angle;
    }

    public static double calculateBearingToPoint(double currentBearing, double currentX, double currentY,double targetX, double targetY) {
        //throw new RuntimeException("implement me!");
        double angle;
        double x = targetX - currentX;
        double y = targetY - currentY;
        angle = Math.atan2(x, y)*180.0/ Math.PI-currentBearing;
        return angle < 0? angle + 360 : angle;
    }

}
