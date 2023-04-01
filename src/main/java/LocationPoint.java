

import com.graphhopper.reader.osm.Pair;
import com.graphhopper.util.shapes.GHPoint;

public class LocationPoint {
    private String address;
    private Double latitude;
    private Double longitude;

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
