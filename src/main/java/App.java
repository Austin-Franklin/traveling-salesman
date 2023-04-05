import java.io.File;
import java.util.List;
import java.util.ArrayList;

import com.graphhopper.*;
import com.graphhopper.config.*;
import com.graphhopper.util.shapes.GHPoint;

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
        LocationPoint KPNS = new LocationPoint("Pensacola International Airport");
        LocationPoint wahoo = new LocationPoint("Blue Wahoos Stadium, Pensacola");

        ArrayList<LocationPoint> addressList= new ArrayList<LocationPoint>();

        addressList.add(UWF);
        addressList.add(cordovaMall);
        addressList.add(KPNS);
        addressList.add(wahoo);

        //array of indicies for the addressList array
        //we will convert addressList to GHPoints in this order
        int[] orderOfEvaluation= {0,2,1,3}; 

        ArrayList<GHPoint> GHPointList= new ArrayList<GHPoint>();

        for(int i=0;i!=orderOfEvaluation.length;i++){
            GHPointList.add(addressList.get(orderOfEvaluation[i]).getGHPoint());
        }

        GHResponse response = hopper.route(
            new GHRequest(GHPointList)
                .setProfile("car")
        );
        if(response.hasErrors()) {
            List<Throwable> errors = response.getErrors();
            for (Throwable error : errors) {
                System.err.println("An error occurred: " + error.getMessage());
            }
        }
        System.out.print("" + response.getBest().getTime()/1000 + " seconds to go from ");

        for(int i=0;i!=addressList.size();i++){
            System.out.print(addressList.get(orderOfEvaluation[i]).getAddress()+" ");
            if (i<addressList.size()-1) System.out.print("to ");
        }
    }
}
