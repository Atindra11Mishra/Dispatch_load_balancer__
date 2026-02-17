package com.freightfox.dispatch.util;

import lombok.extern.slf4j.Slf4j;


@Slf4j  // Lombok annotation - auto-generates: private static final Logger log = ...
public final class HaversineCalculator {
    
    // Constants
    private static final double EARTH_RADIUS_KM = 6371.0;  // Earth's mean radius
    
   
    private HaversineCalculator() {
        throw new AssertionError("Utility class - cannot instantiate");
    }
    
    /**
     * 
     * 
     * @param lat1 Starting point latitude 
     * @param lon1 Starting point longitude 
     * @param lat2 Ending point latitude 
     * @param lon2 
     * @return Distance in kilometers (always >= 0)
     * @throws IllegalArgumentException 
     * 
     
     */
    public static double calculate(double lat1, double lon1, double lat2, double lon2) {
        
        // Validate coordinates
        validateCoordinates(lat1, lon1, "Starting point");
        validateCoordinates(lat2, lon2, "Ending point");
        
        log.trace("Calculating distance: ({}, {}) → ({}, {})", lat1, lon1, lat2, lon2);
        
    
        double lat1Rad = Math.toRadians(lat1);
        double lon1Rad = Math.toRadians(lon1);
        double lat2Rad = Math.toRadians(lat2);
        double lon2Rad = Math.toRadians(lon2);
        
       
        double deltaLat = lat2Rad - lat1Rad;  // Δφ
        double deltaLon = lon2Rad - lon1Rad;  // Δλ
        
        // Haversine formula - Step 1: Calculate 'a'
        // a = sin²(Δφ/2) + cos(φ1) ⋅ cos(φ2) ⋅ sin²(Δλ/2)
        double sinDeltaLatDiv2 = Math.sin(deltaLat / 2.0);
        double sinDeltaLonDiv2 = Math.sin(deltaLon / 2.0);
        
        double a = sinDeltaLatDiv2 * sinDeltaLatDiv2 +
                   Math.cos(lat1Rad) * Math.cos(lat2Rad) *
                   sinDeltaLonDiv2 * sinDeltaLonDiv2;
        
      
        double c = 2.0 * Math.atan2(Math.sqrt(a), Math.sqrt(1.0 - a));
        
        
        double distance = EARTH_RADIUS_KM * c;
        
        log.trace("Calculated distance: {} km", String.format("%.2f", distance));
        
        return distance;
    }
    
    /**
     * Calculate distance and format as string with "km" suffix
     * 
     * @param lat1 Starting point latitude
     * @param lon1 Starting point longitude
     * @param lat2 Ending point latitude
     * @param lon2 Ending point longitude
     * @return Formatted distance string (e.g., "17.85 km")
     */
    public static String calculateFormatted(double lat1, double lon1, double lat2, double lon2) {
        double distance = calculate(lat1, lon1, lat2, lon2);
        return String.format("%.2f km", distance);
    }
    
    /**
    
     * 
     * @return Distance in meters
     */
    public static double calculateInMeters(double lat1, double lon1, double lat2, double lon2) {
        return calculate(lat1, lon1, lat2, lon2) * 1000.0;
    }
    
    /**
     
     * 
     * @param lat Latitude to validate
     * @param lon Longitude to validate
     * @param label Label for error message
     * @throws IllegalArgumentException if coordinates are invalid
     */
    private static void validateCoordinates(double lat, double lon, String label) {
        if (lat < -90.0 || lat > 90.0) {
            String message = String.format(
                "%s latitude %.4f is out of range [-90, 90]", 
                label, 
                lat
            );
            log.error(message);
            throw new IllegalArgumentException(message);
        }
        
        if (lon < -180.0 || lon > 180.0) {
            String message = String.format(
                "%s longitude %.4f is out of range [-180, 180]", 
                label, 
                lon
            );
            log.error(message);
            throw new IllegalArgumentException(message);
        }
    }
    
    /**
     
     * @param lat1 Point 1 latitude
     * @param lon1 Point 1 longitude
     * @param lat2 Point 2 latitude
     * @param lon2 Point 2 longitude
     * @param thresholdKm Maximum distance in kilometers
     * @return true if distance <= threshold
     */
    public static boolean isWithinDistance(
            double lat1, double lon1, 
            double lat2, double lon2, 
            double thresholdKm) {
        
        double distance = calculate(lat1, lon1, lat2, lon2);
        return distance <= thresholdKm;
    }
}