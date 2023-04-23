import java.io.File;
import java.util.ArrayList;

import com.graphhopper.*;
import com.graphhopper.config.*;

import cooling_functions.*;

public class App {
    public static void main(String[] args) {

        // check if graph file exists, if not, download it
        File osmFile = new File("Pensacola.osm");
        if (!osmFile.exists()) {
            OSMDownload.osmFile("Pensacola");
        }

        // graphhopper setup
        GraphHopper hopper = new GraphHopper();
        hopper.setOSMFile("Pensacola.osm");
        hopper.setGraphHopperLocation("GH_folder");
        hopper.setProfiles(
                new Profile("car").setVehicle("car").setWeighting("fastest"));
        hopper.getCHPreparationHandler().setCHProfiles(
                new CHProfile("car"));
        hopper.getLMPreparationHandler().setLMProfiles(
                new LMProfile("car"));
        hopper.importOrLoad();

        //runs a small menu function to get input from user
        ArrayList<LocationPoint> addressList = Menu.run();

        //how many times do the mutations iterate
        int totalAnnealRuns = 1000 + addressList.size() * 30;

        //how many times do crossovers occur
        int totalCrossovers = 10 + addressList.size() * 3;
        
        //ranges from totalAnnealRuns (tAR) to 1 over the course of the annealing, aka 1 = tAR * exp(-coolingFactor * tAR), rearrange to get ln(tAR) / tAR
        CoolingFunction coolingFunction = new ExponentialCooling(totalAnnealRuns, Math.log(totalCrossovers) / totalAnnealRuns);
        
        //a 75% chance of taking a longer route at the start due to boltzmann factor (50% wasnt cutting it sometimes)
        //0.75 = exp(-deltaE/kT), rearrange for k it is deltaE / (ln(4/3) * T)
        //initial deltaE would be around 1e6 milliseconds per edge, which is nodes - 1 but since start and end node are the same and occur once in addressList, it is just addressList.size()
        double boltzmannFactor = 1e6 * addressList.size() / (Math.log(4.0/3.0) * totalAnnealRuns);

        AnnealTaskOverallDriver driver = new AnnealTaskOverallDriver(
                hopper,
                addressList,
                coolingFunction,
                boltzmannFactor)
                .setTotalAnneals(totalAnnealRuns)
                .setCrossoverActions(totalCrossovers)
                .setTotalConcurrentThreads(16)
                .setCrossoverNum(40);

        driver.start();

        ArrayList<LocationPoint> bestOrder = driver.getBestPath();

        System.out.print("\n\n" + String.format("%.1f", driver.getBestTimeInMillis() / 60000) + " minutes to go from ");
        bestOrder.forEach(loc -> 
            System.out.print(
                loc.getAddress().split(", ")[0]
                + ", to "));
        System.out.println(bestOrder.get(0).getAddress().split(", ")[0]);

    }
}
