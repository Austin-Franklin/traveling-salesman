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
        addressList.add(oliveBaptist);
        addressList.add(wahoo);
        addressList.add(civicCenter);
        addressList.add(pier);
        addressList.add(pickens);
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

        LinearCooling coolFn = new LinearCooling(200, .001);
        AnnealTask task = new AnnealTask(hopper, addressList, UWF, 100, coolFn, 0.05);
        
        try {
            
            SpinnerThread spinThread = new SpinnerThread("Calculating Optimal Route");
            spinThread.start();
           
            task.run();

            while(!task.isFinished()){ //This is a jankey, fast solution. I'm sure you know a better one
            int i=0;
            }

            spinThread.stop();

        } catch (Exception e) {
            e.printStackTrace();
        }



        ArrayList<LocationPoint> finalOrder= task.getBestOrder();
      
        System.out.print("\n\n" + task.getBestTime()/1000 + " seconds to go from ");

        for(int i=0;i!=finalOrder.size();i++){
            System.out.print(finalOrder.get(i).getAddress()+" ");
            if (i<addressList.size()-1) System.out.print("to ");
        }
    }
}
