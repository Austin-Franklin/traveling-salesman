import com.graphhopper.reader.osm.Pair;
import com.graphhopper.util.shapes.GHPoint;

/**
 * Holds address info and converts to lat/long coords through OSM
 * @see AdressToLatLong
 */
public class LocationPoint {
    private String address;
    private Double latitude;
    private Double longitude;

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
        this.latitude = latLongPair.first;
        this.longitude = latLongPair.second;
    }

    public String getAddress() {
        return address;
    }

    public Double getLatitude() {
        return latitude;
    }

    public Double getLongitude() {
        return longitude;
    }

    public GHPoint getGHPoint() {
        return new GHPoint(latitude, longitude);
    }
}
