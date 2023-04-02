

import java.io.*;
import java.net.*;

/**
 * Downloads an OSM XML file of 20km around <location> and stores it right next to wherever this is running
 */
public class OSMDownload {
    public static void osmFile(String location) {
        // String query = String.format(
        //     "[out:xml][timeout:25];\n" +
        //     "{{radius=20000}}\n" +
        //     "(\n" +
        //       "node[\"highway\"][\"maxspeed\"](around:{{radius}},{{geocodeCoords:%s}});\n" +
        //       "way[\"highway\"][\"maxspeed\"](around:{{radius}},{{geocodeCoords:%<s}});\n" +
        //       "relation[\"highway\"][\"maxspeed\"](around:{{radius}},{{geocodeCoords:%<s}});\n" +
        //       "node[\"highway\"=\"residential\"](around:{{radius}},{{geocodeCoords:%<s}});\n" +
        //       "way[\"highway\"=\"residential\"](around:{{radius}},{{geocodeCoords:%<s}});\n" +
        //       "relation[\"highway\"=\"residential\"](around:{{radius}},{{geocodeCoords:%<s}});\n" +
        //       "node[\"tiger:name_type\"](around:{{radius}},{{geocodeCoords:%<s}});\n" +
        //       "way[\"tiger:name_type\"](around:{{radius}},{{geocodeCoords:%<s}});\n" +
        //       "relation[\"tiger:name_type\"](around:{{radius}},{{geocodeCoords:%<s}});\n" +
        //     ");\n" +
        //     "(._;>;);\n" +
        //     "out body;"
        //     , location);
        String query = """
            [out:xml][timeout:25];(node["highway"]["maxspeed"](around:20000,30.421309,-87.2169149);way["highway"]["maxspeed"](around:20000,30.421309,-87.2169149);relation["highway"]["maxspeed"](around:20000,30.421309,-87.2169149);node["highway"="residential"](around:20000,30.421309,-87.2169149);way["highway"="residential"](around:20000,30.421309,-87.2169149);relation["highway"="residential"](around:20000,30.421309,-87.2169149);node["tiger:name_type"](around:20000,30.421309,-87.2169149);way["tiger:name_type"](around:20000,30.421309,-87.2169149);relation["tiger:name_type"](around:20000,30.421309,-87.2169149););(._;>;);out body;
                """;
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
