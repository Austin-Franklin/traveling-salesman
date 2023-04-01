

import static org.junit.Assert.assertEquals;

import org.junit.Test;

public class LocationTest {
    @Test
    public void UWFLocationTest() {
        LocationPoint UWF = new LocationPoint("University of West Florida, Pensacola");
        assertEquals(UWF.getLatitude(), 30.5479646, 0.0001);
        assertEquals(UWF.getLongitude(), -87.21620152987073, 0.0001);
        assertEquals(UWF.getAddress(), "University of West Florida, Pensacola");
    }

    @Test
    public void CordovaMallLocationTest() {
        LocationPoint cordova = new LocationPoint("Cordova Mall, Pensacola");
        assertEquals(cordova.getLatitude(), 30.4749135, 0.0001);
        assertEquals(cordova.getLongitude(), -87.20803332963806, 0.0001);
        assertEquals(cordova.getAddress(), "Cordova Mall, Pensacola");
    }
}
