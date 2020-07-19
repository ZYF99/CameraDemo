package com.example.camerademo.artithmetic;


import com.example.camerademo.LatLngM;

import java.util.ArrayList;
import java.util.List;

import static com.example.camerademo.artithmetic.calculate.calculateBearingToPoint;

public class convexHullBorder {
    /**
     * Given a set of points, compute the convex hull, the smallest convex set that contains all the points
     * in a set of input points. The gift-wrapping algorithm is one simple approach to this problem, and
     * there are other algorithms too.
     *
     * @param points a set of points with xCoords and yCoords. It might be empty, contain only 1 point, two points or more.
     * @return minimal subset of the input points that form the vertices of the perimeter of the convex hull
     */
    public static List<LatLngM> convexHull(List<LatLngM> points) {
//        throw new RuntimeException("implement me!");
        List<LatLngM> shellPoint = new ArrayList<>();
        LatLngM minPoint = null;
        double nowBearing;
        double nextBearing;
        double preBearing;
        double nextLength;
        LatLngM nowPoint;
        LatLngM nextPoint = null;
//    	Iterator<LatLngM> it = points.iterator();
        if (!points.isEmpty()) {
            //元素小于3个时，必是凸包直接返回
            if (points.size() <= 3)
                return points;

            //求最左下元素
            for (LatLngM point : points) {
                if (minPoint == null) {
                    minPoint = point;
                    continue;
                }
                if (minPoint.getDzwdValue() > point.getDzwdValue())
                    minPoint = point;
                else if (minPoint.getDzwdValue() == point.getDzwdValue()) {
                    if (point.getDzjdValue() < minPoint.getDzjdValue())
                        minPoint = point;
                }
            }

            shellPoint.add(minPoint); //最左下元素定时凸包元素，加入集合
            nowPoint = minPoint;
            preBearing = 0; //之前凸包元素指向最近凸包元素的角度（相对与y轴顺时针）
            while (true) {
                nextBearing = 360;
                nextLength = Double.MAX_VALUE;
                for (LatLngM point : points) {
                    if (point.equals(nowPoint))
                        continue;
                    nowBearing = calculateBearingToPoint(preBearing, nowPoint.getDzwdValue(), nowPoint.getDzjdValue(), point.getDzwdValue(), point.getDzjdValue());
                    if (nextBearing == nowBearing) {
                        if (nextLength < (Math.pow(point.getDzwdValue() - nowPoint.getDzwdValue(), 2) + Math.pow(point.getDzwdValue() - nowPoint.getDzwdValue(), 2))) {
                            nextLength = Math.pow(point.getDzwdValue() - nowPoint.getDzwdValue(), 2) + Math.pow(point.getDzwdValue() - nowPoint.getDzjdValue(), 2);
                            nextPoint = point;
                        }
                    } else if (nextBearing > nowBearing) {
                        nextLength = Math.pow(point.getDzwdValue() - nowPoint.getDzwdValue(), 2) + Math.pow(point.getDzjdValue() - nowPoint.getDzjdValue(), 2);
                        nextBearing = nowBearing;
                        nextPoint = point;
                    }
                }
                if (minPoint.equals(nextPoint)) {
                    break;
                }
                nowPoint = nextPoint;
                preBearing += nextBearing;
                shellPoint.add(nextPoint);
            }

        }
        return shellPoint;
    }

}
