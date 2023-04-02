import java.io.File;
import java.util.List;

import com.graphhopper.*;
import com.graphhopper.config.*;

public class App {
    public static void main(String[] args) {

        //check if graph file exists, if not, download it
        File osmFile = new File("Pensacola.osm");
        if (!osmFile.exists()) {
            OSMDownload.osmFile("Pensacola");
        }

        //graphhopper setup
        GraphHopper hopper = new GraphHopper();
        hopper.setOSMFile("Pensacola.osm");
        hopper.setGraphHopperLocation("GH_folder");
        hopper.setProfiles(
            new Profile("car").setVehicle("car").setWeighting("fastest")
        );
        hopper.getCHPreparationHandler().setCHProfiles(
            new CHProfile("car")
        );
        hopper.getLMPreparationHandler().setLMProfiles(
            new LMProfile("car")
        );
        hopper.importOrLoad();

        LocationPoint UWF = new LocationPoint("University of West Florida, Pensacola");
        LocationPoint cordovaMall = new LocationPoint("Cordova Mall, Pensacola");
        GHResponse response = hopper.route(
            new GHRequest(UWF.getGHPoint(), cordovaMall.getGHPoint())
                .setProfile("car")
        );
        if(response.hasErrors()) {
            List<Throwable> errors = response.getErrors();
            for (Throwable error : errors) {
                System.err.println("An error occurred: " + error.getMessage());
            }
        }
        System.out.println("" + response.getBest().getTime()/1000 + " seconds to go from UWF to Cordova Mall");
    }
}
