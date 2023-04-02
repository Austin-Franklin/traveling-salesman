

import java.io.*;
import java.net.*;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.graphhopper.reader.osm.Pair;

/**
 * Calls OSM/Nominatim via internet for lat/lon of an address
 */
public class AddressToLatLong {
    /**
     * 
     * @param addressAddress as in<br/>
     * <h5>11000 University Pkwy, Pensacola, FL 32514</h5><br/>
     * or<br/>
     * <h5>University of West Florida, Pensacola</h5>
     * @return <lat, long>
     */
    public static Pair<Double, Double> getFromOSM(String address) {
        try {
            String urlFormattedAddress = URLEncoder.encode(address, "UTF-8");
            URL url = new URL("https://nominatim.openstreetmap.org/search?q=" + urlFormattedAddress + "&format=json&addressdetails=1");
            HttpURLConnection conn = (HttpURLConnection) url.openConnection();
            conn.setRequestMethod("GET");
            conn.setRequestProperty("Accept", "application/json");

            if (conn.getResponseCode() != 200) {
                throw new RuntimeException("Failed: HTTP error code : " + conn.getResponseCode());
            }

            BufferedReader br = new BufferedReader(new InputStreamReader((conn.getInputStream())));
            String output = br.readLine();

            ObjectMapper objectMapper = new ObjectMapper();
            JsonNode jsonNode = objectMapper.readValue(output, JsonNode.class);
            double lat = jsonNode.get(0).get("lat").asDouble();
            double lon = jsonNode.get(0).get("lon").asDouble();
            conn.disconnect();

            return new Pair<Double,Double>(lat, lon);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return new Pair<Double, Double>(0.0, 0.0);
    }
}
