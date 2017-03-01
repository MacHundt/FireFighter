package model.datatypes;

public class DistanceCalculator {
    private static final double EARTH_RADIUS = 6371.0;
    private static final double LAT_DST_PER_DEG = (EARTH_RADIUS * 2 * Math.PI) / 360;

    // Winkel auf der Orthogonaledurch die beiden Punkte
    public static double orthAngle(final double lat1, final double lng1, final double lat2, final double lng2) {
            double lat1Rad = deg2rad(lat1);
            double lng1Rad = deg2rad(lng1);
            double lat2Rad = deg2rad(lat2);
            double lng2Rad = deg2rad(lng2);
            double res = Math.sin(lat1Rad) * Math.sin(lat2Rad) + Math.cos(lat1Rad) * Math.cos(lat2Rad) * Math.cos(Math.abs(lng2Rad - lng1Rad));
            res = Math.min(Math.abs(res), 1.0);
            return Math.acos(res); 
    }

    // Kurswinkel zwischen den beiden Punkten
    public static double courseAngle(final double lat1, final double lng1, final double lat2, final double lng2) {
            double lat1Rad = deg2rad(lat1);
            double lat2Rad = deg2rad(lat2);
            double delta_lat = lat2 - lat1;
            double delta_lng = lng2 - lng1;
            double orthAngle = orthAngle(lat1, lng1, lat2, lng2);
            double res = (Math.sin(lat2Rad) - Math.sin(lat1Rad) * Math.cos(orthAngle)) / (Math.cos(lat1Rad) * Math.sin(orthAngle));
            res = Math.min(Math.abs(res), 1.0);
            double resDeg = rad2deg(Math.acos(res));
            if (delta_lat < 0)
                    resDeg = delta_lng < 0 ? 180 + resDeg : 180 - resDeg;
            else
                    resDeg = delta_lng < 0 ? 360 - resDeg : resDeg;
            return resDeg;
    }

    // Strecke auf der Orthogonale durch die beiden Punkte
    public static double orthDistance(double lat1, double lng1, double lat2, double lng2) {
            return orthAngle(lat1, lng1, lat2, lng2) * EARTH_RADIUS;
    }
    
    public static double getDistance(double[] fromCoords, double[] toCoords) {
            double a = fromCoords[0];
            double b = fromCoords[1];
            double c = toCoords[0];
            double d = toCoords[1];
            
            return orthDistance(a, b, c, d);
    }

    // Manhattan-Distanz
    public static double manhattanDistance(double lat1, double lng1, double lat2, double lng2) {
            double deltaLat = Math.abs(lat1 - lat2);
            double deltaLng = Math.abs(lng1 - lng2);
            double lngDstPerDeg = LAT_DST_PER_DEG * Math.cos(deg2rad(Math.min(Math.abs(lat1), Math.abs(lat2))));
            double lngDst = lngDstPerDeg * deltaLng;
            double latDst = LAT_DST_PER_DEG * deltaLat;
            return lngDst + latDst;
    }

    public static double deg2rad(Double ang) {
            return deg2rad(ang.doubleValue());
    }

    public static double deg2rad(double ang) {
            return ang * Math.PI / 180;
    }

    public static double rad2deg(double ang) {
            return ang * 180 / Math.PI;
    }
}
