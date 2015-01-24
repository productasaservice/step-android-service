package com.discover.step.helper;

import com.discover.step.model.StepPoint;

import java.util.List;

/**
 * Created by Geri on 2015.01.24..
 */
public class GeoHelper {

    public static StepPoint getCenteredStepPoint(List<StepPoint> points) {
        if (points.size() == 1) {
            return points.get(0);
        }

        double x = 0;
        double y = 0;
        double z = 0;

        for (StepPoint sp : points) {
            double latitude = sp.latitude * Math.PI / 180;
            double longitude = sp.longitude * Math.PI / 180;

            x += Math.cos(latitude) * Math.cos(longitude);
            y += Math.cos(latitude) * Math.sin(longitude);
            z += Math.sin(latitude);
        }

        int total = points.size();

        x = x / total;
        y = y / total;
        z = z / total;

        double centralLongitude = Math.atan2(y,x);
        double centralSquareRoot = Math.sqrt(x*x + y*y);
        double centralLatitude = Math.atan2(z,centralSquareRoot);

        StepPoint temp = points.get(0);

        StepPoint point = new StepPoint();
        point.latitude = centralLatitude * 180 / Math.PI;
        point.longitude = centralLongitude * 180 / Math.PI;
        point.isDrawnPoint = temp.isDrawnPoint;
        point.color = temp.color;
        point.user_social_id = temp.user_social_id;
        point.description = temp.description;

        return point;
    }

}
