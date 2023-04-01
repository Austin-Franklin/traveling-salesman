

import java.io.*;
import java.net.*;

/**
 * Downloads an OSM XML file of 20km around <location> and stores it right next to wherever this is running
 */
public class OSMDownload {
    public static void osmFile(String location) {
        String query = String.format(
            "[out:xml][timeout:25];" +
            "{{radius=20000}}" +
            "(" +
              "node[\"highway\"][\"maxspeed\"](around:{{radius}},{{geocodeCoords:%s}});" +
              "way[\"highway\"][\"maxspeed\"](around:{{radius}},{{geocodeCoords:%<s}});" +
              "relation[\"highway\"][\"maxspeed\"](around:{{radius}},{{geocodeCoords:%<s}});" +
              "node[\"highway\"=\"residential\"](around:{{radius}},{{geocodeCoords:%<s}});" +
              "way[\"highway\"=\"residential\"](around:{{radius}},{{geocodeCoords:%<s}});" +
              "relation[\"highway\"=\"residential\"](around:{{radius}},{{geocodeCoords:%<s}});" +
              "node[\"tiger:name_type\"](around:{{radius}},{{geocodeCoords:%<s}});" +
              "way[\"tiger:name_type\"](around:{{radius}},{{geocodeCoords:%<s}});" +
              "relation[\"tiger:name_type\"](around:{{radius}},{{geocodeCoords:%<s}});" +
            ");" +
            "(._;>;);" +
            "out body;"
            , location);
            
        try {
            String url = "https://overpass-api.de/api/interpreter?data=" + URLEncoder.encode(query, "UTF-8");
            URLConnection conn = new URL(url).openConnection();
            conn.setRequestProperty("User-Agent", "Mozilla/5.0");
            InputStream is = conn.getInputStream();
            FileOutputStream fos = new FileOutputStream(location + ".osm");
            is.transferTo(fos);
            fos.close();
            is.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
