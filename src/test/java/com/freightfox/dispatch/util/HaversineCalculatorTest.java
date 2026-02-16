package com.freightfox.dispatch.util;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

import static org.assertj.core.api.Assertions.*;

/**
 * Unit tests for HaversineCalculator
 * 
 * TEST STRUCTURE (Given-When-Then):
 * - GIVEN: Setup test data (arrange)
 * - WHEN: Execute method under test (act)
 * - THEN: Verify results (assert)
 * 
 * ASSERTJ vs JUnit Assertions:
 * JUnit:  assertEquals(expected, actual)
 * AssertJ: assertThat(actual).isEqualTo(expected)
 * 
 * AssertJ benefits:
 * - Fluent API (more readable)
 * - Better error messages
 * - Auto-completion friendly
 */
@DisplayName("HaversineCalculator - Distance Calculation Tests")
class HaversineCalculatorTest {
    
    private static final double DELTA = 0.1;  // 100 meters tolerance
    
    // ========================================================================
    // DISTANCE CALCULATION TESTS
    // ========================================================================
    
    @Test
    @DisplayName("Should calculate distance from Delhi to Noida (~17-18 km)")
    void testDelhiToNoidaDistance() {
        // GIVEN: Coordinates for Delhi and Noida
        double delhiLat = 28.6139;    // Connaught Place, Delhi
        double delhiLon = 77.2090;
        double noidaLat = 28.5355;    // Sector 18, Noida
        double noidaLon = 77.3910;
        
        // WHEN: Calculate distance
        double distance = HaversineCalculator.calculate(
            delhiLat, delhiLon, 
            noidaLat, noidaLon
        );
        
        // THEN: Distance should be approximately 19.8 km
        assertThat(distance)
            .as("Delhi to Noida distance")
            .isCloseTo(19.8, within(DELTA));

        // Alternative assertions
        assertThat(distance).isGreaterThan(19.0);
        assertThat(distance).isLessThan(21.0);
    }
    
    @Test
    @DisplayName("Should return 0 km for same location")
    void testSamePointDistance() {
        // GIVEN: Same coordinates
        double lat = 28.6139;
        double lon = 77.2090;
        
        // WHEN: Calculate distance to same point
        double distance = HaversineCalculator.calculate(lat, lon, lat, lon);
        
        // THEN: Distance should be 0
        assertThat(distance)
            .as("Same location distance")
            .isEqualTo(0.0);
    }
    
    @Test
    @DisplayName("Should calculate distance with negative coordinates (southern hemisphere)")
    void testNegativeCoordinates() {
        // GIVEN: Sydney (negative latitude) and Jakarta (negative latitude)
        double sydneyLat = -33.8688;
        double sydneyLon = 151.2093;
        double jakartaLat = -6.2088;
        double jakartaLon = 106.8456;
        
        // WHEN: Calculate distance
        double distance = HaversineCalculator.calculate(
            sydneyLat, sydneyLon,
            jakartaLat, jakartaLon
        );
        
        // THEN: Distance should be approximately 5,496 km
        assertThat(distance)
            .as("Sydney to Jakarta distance")
            .isCloseTo(5496.0, within(100.0));
    }
    
    @Test
    @DisplayName("Should be symmetric (A→B = B→A)")
    void testSymmetry() {
        // GIVEN: Two different points
        double lat1 = 28.6139, lon1 = 77.2090;
        double lat2 = 28.5355, lon2 = 77.3910;
        
        // WHEN: Calculate distance both ways
        double distanceAtoB = HaversineCalculator.calculate(lat1, lon1, lat2, lon2);
        double distanceBtoA = HaversineCalculator.calculate(lat2, lon2, lat1, lon1);
        
        // THEN: Distances should be equal
        assertThat(distanceAtoB)
            .as("Distance symmetry")
            .isEqualTo(distanceBtoA);
    }
    
    @Test
    @DisplayName("Should calculate very short distances accurately (<1 km)")
    void testShortDistance() {
        // GIVEN: Two nearby points (~1 km apart)
        double lat1 = 28.6139, lon1 = 77.2090;
        double lat2 = 28.6229, lon2 = 77.2090;  // ~1 km north
        
        // WHEN: Calculate distance
        double distance = HaversineCalculator.calculate(lat1, lon1, lat2, lon2);
        
        // THEN: Distance should be approximately 1 km
        assertThat(distance)
            .as("Short distance accuracy")
            .isBetween(0.9, 1.1);
    }
    
    @Test
    @DisplayName("Should calculate long distances (>1000 km)")
    void testLongDistance() {
        // GIVEN: Delhi and Bangalore (~1740 km)
        double delhiLat = 28.6139, delhiLon = 77.2090;
        double bangaloreLat = 12.9716, bangaloreLon = 77.5946;
        
        // WHEN: Calculate distance
        double distance = HaversineCalculator.calculate(
            delhiLat, delhiLon,
            bangaloreLat, bangaloreLon
        );
        
        // THEN: Distance should be approximately 1740 km
        assertThat(distance)
            .as("Long distance calculation")
            .isCloseTo(1740.0, within(20.0));
    }
    
    // ========================================================================
    // VALIDATION TESTS (Error Cases)
    // ========================================================================
    
    @ParameterizedTest
    @DisplayName("Should reject invalid latitude values")
    @CsvSource({
        "91.0, 77.0, 28.6, 77.2",      // lat1 too high
        "-91.0, 77.0, 28.6, 77.2",     // lat1 too low
        "28.6, 77.0, 100.0, 77.2",     // lat2 too high
        "28.6, 77.0, -100.0, 77.2"     // lat2 too low
    })
    void testInvalidLatitude(double lat1, double lon1, double lat2, double lon2) {
        // GIVEN: Invalid latitude values
        
        // WHEN & THEN: Should throw IllegalArgumentException
        assertThatThrownBy(() -> 
            HaversineCalculator.calculate(lat1, lon1, lat2, lon2)
        )
            .isInstanceOf(IllegalArgumentException.class)
            .hasMessageContaining("latitude")
            .hasMessageContaining("out of range");
    }
    
    @ParameterizedTest
    @DisplayName("Should reject invalid longitude values")
    @CsvSource({
        "28.6, 181.0, 28.5, 77.2",     // lon1 too high
        "28.6, -181.0, 28.5, 77.2",    // lon1 too low
        "28.6, 77.0, 28.5, 200.0",     // lon2 too high
        "28.6, 77.0, 28.5, -200.0"     // lon2 too low
    })
    void testInvalidLongitude(double lat1, double lon1, double lat2, double lon2) {
        // GIVEN: Invalid longitude values
        
        // WHEN & THEN: Should throw IllegalArgumentException
        assertThatThrownBy(() -> 
            HaversineCalculator.calculate(lat1, lon1, lat2, lon2)
        )
            .isInstanceOf(IllegalArgumentException.class)
            .hasMessageContaining("longitude")
            .hasMessageContaining("out of range");
    }
    
    // ========================================================================
    // HELPER METHOD TESTS
    // ========================================================================
    
    @Test
    @DisplayName("Should format distance with km suffix")
    void testFormattedDistance() {
        // GIVEN: Two points
        double lat1 = 28.6139, lon1 = 77.2090;
        double lat2 = 28.5355, lon2 = 77.3910;
        
        // WHEN: Calculate formatted distance
        String formatted = HaversineCalculator.calculateFormatted(lat1, lon1, lat2, lon2);
        
        // THEN: Should match pattern "XX.XX km"
        assertThat(formatted)
            .as("Formatted distance string")
            .matches("\\d+\\.\\d{2} km")
            .startsWith("19.");
    }
    
    @Test
    @DisplayName("Should calculate distance in meters")
    void testDistanceInMeters() {
        // GIVEN: Two points ~17.8 km apart
        double lat1 = 28.6139, lon1 = 77.2090;
        double lat2 = 28.5355, lon2 = 77.3910;
        
        // WHEN: Calculate distance in meters
        double distanceMeters = HaversineCalculator.calculateInMeters(lat1, lon1, lat2, lon2);
        
        // THEN: Should be approximately 19,800 meters
        assertThat(distanceMeters)
            .as("Distance in meters")
            .isCloseTo(19800.0, within(100.0));
    }
    
    @Test
    @DisplayName("Should check if points are within distance threshold")
    void testIsWithinDistance() {
        // GIVEN: Two points ~17.8 km apart
        double lat1 = 28.6139, lon1 = 77.2090;
        double lat2 = 28.5355, lon2 = 77.3910;
        
        // WHEN & THEN: Check various thresholds
        assertThat(HaversineCalculator.isWithinDistance(lat1, lon1, lat2, lon2, 20.0))
            .as("Should be within 20 km")
            .isTrue();
        
        assertThat(HaversineCalculator.isWithinDistance(lat1, lon1, lat2, lon2, 10.0))
            .as("Should NOT be within 10 km")
            .isFalse();
    }
    
    // ========================================================================
    // EDGE CASES
    // ========================================================================
    
    @Test
    @DisplayName("Should handle equator crossing")
    void testEquatorCrossing() {
        // GIVEN: Singapore (north) and Jakarta (south of equator)
        double singaporeLat = 1.3521, singaporeLon = 103.8198;
        double jakartaLat = -6.2088, jakartaLon = 106.8456;
        
        // WHEN: Calculate distance
        double distance = HaversineCalculator.calculate(
            singaporeLat, singaporeLon,
            jakartaLat, jakartaLon
        );
        
        // THEN: Distance should be approximately 890 km
        assertThat(distance)
            .as("Equator crossing distance")
            .isCloseTo(890.0, within(20.0));
    }
    
    @Test
    @DisplayName("Should handle prime meridian crossing")
    void testPrimeMeridianCrossing() {
        // GIVEN: London (west) and Paris (east of prime meridian)
        double londonLat = 51.5074, londonLon = -0.1278;
        double parisLat = 48.8566, parisLon = 2.3522;
        
        // WHEN: Calculate distance
        double distance = HaversineCalculator.calculate(
            londonLat, londonLon,
            parisLat, parisLon
        );
        
        // THEN: Distance should be approximately 344 km
        assertThat(distance)
            .as("Prime meridian crossing distance")
            .isCloseTo(344.0, within(10.0));
    }
}