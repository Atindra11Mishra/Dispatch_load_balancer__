package com.freightfox.dispatch.util;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

import static org.assertj.core.api.Assertions.*;


@DisplayName("HaversineCalculator - Distance Calculation Tests")
class HaversineCalculatorTest {
    
    private static final double DELTA = 0.1; 
    
    @Test
    @DisplayName("Should calculate distance from Delhi to Noida (~17-18 km)")
    void testDelhiToNoidaDistance() {
        
        double delhiLat = 28.6139;    
        double delhiLon = 77.2090;
        double noidaLat = 28.5355;    
        double noidaLon = 77.3910;
        
        double distance = HaversineCalculator.calculate(
            delhiLat, delhiLon, 
            noidaLat, noidaLon
        );
        
        
        assertThat(distance)
            .as("Delhi to Noida distance")
            .isCloseTo(19.8, within(DELTA));

        
        assertThat(distance).isGreaterThan(19.0);
        assertThat(distance).isLessThan(21.0);
    }
    
    @Test
    @DisplayName("Should return 0 km for same location")
    void testSamePointDistance() {
        
        double lat = 28.6139;
        double lon = 77.2090;
        
        double distance = HaversineCalculator.calculate(lat, lon, lat, lon);
        
        assertThat(distance)
            .as("Same location distance")
            .isEqualTo(0.0);
    }
    
    @Test
    @DisplayName("Should calculate distance with negative coordinates (southern hemisphere)")
    void testNegativeCoordinates() {
        
        double sydneyLat = -33.8688;
        double sydneyLon = 151.2093;
        double jakartaLat = -6.2088;
        double jakartaLon = 106.8456;
        
        double distance = HaversineCalculator.calculate(
            sydneyLat, sydneyLon,
            jakartaLat, jakartaLon
        );
        
        
        assertThat(distance)
            .as("Sydney to Jakarta distance")
            .isCloseTo(5496.0, within(100.0));
    }
    
    @Test
    @DisplayName("Should be symmetric (A→B = B→A)")
    void testSymmetry() {
        
        double lat1 = 28.6139, lon1 = 77.2090;
        double lat2 = 28.5355, lon2 = 77.3910;
        
        double distanceAtoB = HaversineCalculator.calculate(lat1, lon1, lat2, lon2);
        double distanceBtoA = HaversineCalculator.calculate(lat2, lon2, lat1, lon1);
        
        
        assertThat(distanceAtoB)
            .as("Distance symmetry")
            .isEqualTo(distanceBtoA);
    }
    
    @Test
    @DisplayName("Should calculate very short distances accurately (<1 km)")
    void testShortDistance() {
        
        double lat1 = 28.6139, lon1 = 77.2090;
        double lat2 = 28.6229, lon2 = 77.2090;  
        
        double distance = HaversineCalculator.calculate(lat1, lon1, lat2, lon2);
        
        
        assertThat(distance)
            .as("Short distance accuracy")
            .isBetween(0.9, 1.1);
    }
    
    @Test
    @DisplayName("Should calculate long distances (>1000 km)")
    void testLongDistance() {
        
        double delhiLat = 28.6139, delhiLon = 77.2090;
        double bangaloreLat = 12.9716, bangaloreLon = 77.5946;
        
        double distance = HaversineCalculator.calculate(
            delhiLat, delhiLon,
            bangaloreLat, bangaloreLon
        );
        
        // THEN: Distance should be approximately 1740 km
        assertThat(distance)
            .as("Long distance calculation")
            .isCloseTo(1740.0, within(20.0));
    }
    
    
    @ParameterizedTest
    @DisplayName("Should reject invalid latitude values")
    @CsvSource({
        "91.0, 77.0, 28.6, 77.2",      
        "-91.0, 77.0, 28.6, 77.2",     
        "28.6, 77.0, 100.0, 77.2",     
        "28.6, 77.0, -100.0, 77.2"     
    })
    void testInvalidLatitude(double lat1, double lon1, double lat2, double lon2) {
        
        
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
        "28.6, 181.0, 28.5, 77.2",     
        "28.6, -181.0, 28.5, 77.2",    
        "28.6, 77.0, 28.5, 200.0",     
        "28.6, 77.0, 28.5, -200.0"     
    })
    void testInvalidLongitude(double lat1, double lon1, double lat2, double lon2) {
        
        
        assertThatThrownBy(() -> 
            HaversineCalculator.calculate(lat1, lon1, lat2, lon2)
        )
            .isInstanceOf(IllegalArgumentException.class)
            .hasMessageContaining("longitude")
            .hasMessageContaining("out of range");
    }
    
    
    @Test
    @DisplayName("Should format distance with km suffix")
    void testFormattedDistance() {
        
        double lat1 = 28.6139, lon1 = 77.2090;
        double lat2 = 28.5355, lon2 = 77.3910;
        
        String formatted = HaversineCalculator.calculateFormatted(lat1, lon1, lat2, lon2);
        
        
        assertThat(formatted)
            .as("Formatted distance string")
            .matches("\\d+\\.\\d{2} km")
            .startsWith("19.");
    }
    
    @Test
    @DisplayName("Should calculate distance in meters")
    void testDistanceInMeters() {
        
        double lat1 = 28.6139, lon1 = 77.2090;
        double lat2 = 28.5355, lon2 = 77.3910;
        
        double distanceMeters = HaversineCalculator.calculateInMeters(lat1, lon1, lat2, lon2);
        
        
        assertThat(distanceMeters)
            .as("Distance in meters")
            .isCloseTo(19800.0, within(100.0));
    }
    
    @Test
    @DisplayName("Should check if points are within distance threshold")
    void testIsWithinDistance() {
        
        double lat1 = 28.6139, lon1 = 77.2090;
        double lat2 = 28.5355, lon2 = 77.3910;
        
        
        assertThat(HaversineCalculator.isWithinDistance(lat1, lon1, lat2, lon2, 20.0))
            .as("Should be within 20 km")
            .isTrue();
        
        assertThat(HaversineCalculator.isWithinDistance(lat1, lon1, lat2, lon2, 10.0))
            .as("Should NOT be within 10 km")
            .isFalse();
    }
    
    
    
    @Test
    @DisplayName("Should handle equator crossing")
    void testEquatorCrossing() {
        
        double singaporeLat = 1.3521, singaporeLon = 103.8198;
        double jakartaLat = -6.2088, jakartaLon = 106.8456;
        
        double distance = HaversineCalculator.calculate(
            singaporeLat, singaporeLon,
            jakartaLat, jakartaLon
        );
        
        
        assertThat(distance)
            .as("Equator crossing distance")
            .isCloseTo(890.0, within(20.0));
    }
    
    @Test
    @DisplayName("Should handle prime meridian crossing")
    void testPrimeMeridianCrossing() {
        
        double londonLat = 51.5074, londonLon = -0.1278;
        double parisLat = 48.8566, parisLon = 2.3522;
        
        
        double distance = HaversineCalculator.calculate(
            londonLat, londonLon,
            parisLat, parisLon
        );
        
        
        assertThat(distance)
            .as("Prime meridian crossing distance")
            .isCloseTo(344.0, within(10.0));
    }
}