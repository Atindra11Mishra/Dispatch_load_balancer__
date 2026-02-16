package com.freightfox.dispatch.util;

import lombok.extern.slf4j.Slf4j;

/**
 * HaversineCalculator - Utility for calculating geographic distances
 * 
 * HAVERSINE FORMULA:
 * Calculates shortest distance between two points on Earth's surface
 * (great-circle distance ignoring terrain elevation)
 * 
 * Formula:
 * a = sin²(Δφ/2) + cos φ1 ⋅ cos φ2 ⋅ sin²(Δλ/2)
 * c = 2 ⋅ atan2(√a, √(1−a))
 * d = R ⋅ c
 * 
 * Where:
 * - φ = latitude in radians
 * - λ = longitude in radians
 * - R = Earth's radius (6371 km)
 * - Δφ = difference in latitude
 * - Δλ = difference in longitude
 * 
 * UTILITY CLASS PATTERN:
 * - Private constructor (cannot instantiate)
 * - Static methods only (like Math.sqrt(), Math.abs())
 * - Thread-safe (no instance variables)
 */

@Slf4j  // Lombok annotation - auto-generates: private static final Logger log = ...
public final class HaversineCalculator {
    
    // Constants
    private static final double EARTH_RADIUS_KM = 6371.0;  // Earth's mean radius
    
    /**
     * Private constructor prevents instantiation
     * 
     * Trying to do: new HaversineCalculator() → Compile error
     * Use: HaversineCalculator.calculate(...) instead
     */
    private HaversineCalculator() {
        throw new AssertionError("Utility class - cannot instantiate");
    }
    
    /**
     * Calculate distance between two geographic points using Haversine formula
     * 
     * @param lat1 Starting point latitude (-90 to 90 degrees)
     * @param lon1 Starting point longitude (-180 to 180 degrees)
     * @param lat2 Ending point latitude (-90 to 90 degrees)
     * @param lon2 Ending point longitude (-180 to 180 degrees)
     * @return Distance in kilometers (always >= 0)
     * @throws IllegalArgumentException if coordinates are out of valid range
     * 
     * Example:
     * double distance = HaversineCalculator.calculate(28.6139, 77.2090, 28.5355, 77.3910);
     * // Returns: ~17.8 km (Delhi to Noida)
     */
    public static double calculate(double lat1, double lon1, double lat2, double lon2) {
        
        // Validate coordinates
        validateCoordinates(lat1, lon1, "Starting point");
        validateCoordinates(lat2, lon2, "Ending point");
        
        log.trace("Calculating distance: ({}, {}) → ({}, {})", lat1, lon1, lat2, lon2);
        
        // Convert degrees to radians
        // Radians = Degrees × (π / 180)
        double lat1Rad = Math.toRadians(lat1);
        double lon1Rad = Math.toRadians(lon1);
        double lat2Rad = Math.toRadians(lat2);
        double lon2Rad = Math.toRadians(lon2);
        
        // Calculate differences (deltas)
        double deltaLat = lat2Rad - lat1Rad;  // Δφ
        double deltaLon = lon2Rad - lon1Rad;  // Δλ
        
        // Haversine formula - Step 1: Calculate 'a'
        // a = sin²(Δφ/2) + cos(φ1) ⋅ cos(φ2) ⋅ sin²(Δλ/2)
        double sinDeltaLatDiv2 = Math.sin(deltaLat / 2.0);
        double sinDeltaLonDiv2 = Math.sin(deltaLon / 2.0);
        
        double a = sinDeltaLatDiv2 * sinDeltaLatDiv2 +
                   Math.cos(lat1Rad) * Math.cos(lat2Rad) *
                   sinDeltaLonDiv2 * sinDeltaLonDiv2;
        
        // Haversine formula - Step 2: Calculate 'c' (central angle)
        // c = 2 ⋅ atan2(√a, √(1−a))
        double c = 2.0 * Math.atan2(Math.sqrt(a), Math.sqrt(1.0 - a));
        
        // Haversine formula - Step 3: Calculate distance
        // d = R ⋅ c
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
     * Calculate distance in meters (for higher precision)
     * 
     * @return Distance in meters
     */
    public static double calculateInMeters(double lat1, double lon1, double lat2, double lon2) {
        return calculate(lat1, lon1, lat2, lon2) * 1000.0;
    }
    
    /**
     * Validate geographic coordinates
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
     * Check if two points are within a certain distance threshold
     * Useful for: "Is vehicle within 5 km of order?"
     * 
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