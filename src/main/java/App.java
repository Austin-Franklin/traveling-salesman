import java.io.File;
import java.util.List;
import java.util.Random;
import java.util.ArrayList;

import com.graphhopper.*;   
import com.graphhopper.config.*;
import com.graphhopper.util.shapes.GHPoint;

import cooling_functions.LinearCooling;

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
        LocationPoint oliveBaptist = new LocationPoint("Olive Baptist Church, Pensacola");
        LocationPoint wahoo = new LocationPoint("Blue Wahoos Stadium, Pensacola");
        LocationPoint civicCenter = new LocationPoint("Pensacola Bay Center, Pensacola");
        LocationPoint pier = new LocationPoint("Pensacola Beach Pier");
        LocationPoint pickens = new LocationPoint("Fort Pickens, Florida");
        LocationPoint fair = new LocationPoint("Pensacola Interstate Fair, Pensacola");
        

        ArrayList<LocationPoint> addressList= new ArrayList<LocationPoint>();

        addressList.add(cordovaMall);
        addressList.add(KPNS);
        addressList.add(pickens);
        addressList.add(oliveBaptist);
        addressList.add(wahoo);
        addressList.add(civicCenter);
        addressList.add(pier);
        addressList.add(fair);

/* 
// generates GHPoints within our dataset, 
//I think it causes out of bounds errors because the points are not necessarily on a way

        final double maxLat=-86.947796;
        final double minLat=-87.436965;
        final double latSpan=maxLat-minLat;

        final double maxLon=30.643156;
        final double minLon=30.318335;
        final double lonSpan=maxLon-minLon;

        Random rnd = new Random();


        ArrayList<LocationPoint> pointList= new ArrayList<LocationPoint>(); 

        for(int i=0;i!=100;i++){
            pointList.add(new LocationPoint(
                minLat+(rnd.nextDouble()*latSpan),
                minLon+(rnd.nextDouble()*lonSpan)));
        }

*/

        LinearCooling coolFn = new LinearCooling(4000, 3.5);
        AnnealTask task = new AnnealTask(hopper, addressList, UWF, 1000, coolFn, 1e10);
        AnnealTaskManager taskMan = new AnnealTaskManager(task, 7, 10);
        //Thread thread = new Thread(taskMan);

        try {
        
            taskMan.run();
            taskMan.stop();

        } catch (Exception e) {
            e.printStackTrace();
        }

        ArrayList<LocationPoint> bestOrder= taskMan.getBestOrder();
        ArrayList<GHPoint> finalOrder= new ArrayList<GHPoint>();

        for(int i=0;i<taskMan.getBestOrder().size();i++){
            finalOrder.add(taskMan.getBestOrder().get(i));
        }

        GHResponse response = hopper.route(
            new GHRequest(finalOrder)
                .setProfile("car")
        );

        if(response.hasErrors()) {
            List<Throwable> errors = response.getErrors();
            for (Throwable error : errors) {
                System.err.println("An error occurred: " + error.getMessage());
            }
        }


        System.out.print("\n\n" + response.getBest().getTime()/60000 + " minutes to go from ");


        for(int i=0;i!=bestOrder.size();i++){
            if(bestOrder.get(i).getAddress().contains(",")){
            
                System.out.print(bestOrder.get(i).getAddress().subSequence(0, 
                bestOrder.get(i).getAddress().indexOf(",")));
                if (i<bestOrder.size()-1) System.out.print(", to ");
                
            }
            else{
                System.out.print(bestOrder.get(i).getAddress());
                if (i<bestOrder.size()-1) System.out.print(", to ");
            }
        }


    }
}
