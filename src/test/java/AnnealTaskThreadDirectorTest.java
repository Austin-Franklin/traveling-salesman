import static org.junit.Assert.assertEquals;

import java.util.*;
import java.util.concurrent.Executors;
import java.util.concurrent.ExecutorService;

import org.junit.Test;

import com.graphhopper.GraphHopper;
import com.graphhopper.config.*;

import cooling_functions.ExponentialCooling;

public class AnnealTaskThreadDirectorTest {
    @Test
    public void test() {

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
        

        ArrayList<LocationPoint> addressList = new  ArrayList<>(Arrays.asList(UWF, cordovaMall, KPNS, oliveBaptist, wahoo, civicCenter, pier, pickens, fair));
        
        AnnealTask task = new AnnealTask(hopper, addressList, 100, new ExponentialCooling(1000, 10), 1);

        ExecutorService executor = Executors.newFixedThreadPool(16);
        AnnealTaskThreadDirector td = new AnnealTaskThreadDirector(task, executor, 5);

        td.run();
        try {
            td.stop();
        } catch (InterruptedException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }

        ArrayList<LocationPoint> newPath = td.getBestRuns(1).get(0).getBestOrder();
        assertEquals(addressList.size(), newPath.size());
    }
}
