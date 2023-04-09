import com.graphhopper.reader.osm.Pair;
import com.graphhopper.util.shapes.GHPoint;

/**
 * Holds address info and converts to lat/long coords through OSM,
 * <p>
 * Extends GHPoint
 * @see AddressToLatLong
 * @see com.graphhopper.util.shapes.GHPoint
 */
public class LocationPoint extends GHPoint{
    private String address;

    /**
     * 
     * @param address Address as in<br/>
     * <h5>11000 University Pkwy, Pensacola, FL 32514</h5><br/>
     * or<br/>
     * <h5>University of West Florida, Pensacola</h5>
     * @see AddressToLatLong
     */
    public LocationPoint(String address) {
        this.address = address;
        Pair<Double, Double> latLongPair = AddressToLatLong.getFromOSM(address);
        this.lat = latLongPair.first;
        this.lon = latLongPair.second;
    }

    public String getAddress() {
        return address;
    }
}
