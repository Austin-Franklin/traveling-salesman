import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Locale;
import java.util.Scanner;

import com.graphhopper.*;
import com.graphhopper.config.*;
import com.graphhopper.util.*;
import com.graphhopper.util.shapes.GHPoint;

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
        Scanner scnr = new Scanner(System.in);
        ArrayList<LocationPoint> addressList = Menu.run(scnr);

        //how many times do the mutations iterate
        int totalAnnealRuns = 100 + addressList.size() * 20;

        //how many times do crossovers occur
        int totalCrossovers = 2 + addressList.size() * 2;
        
        //ranges from totalAnnealRuns (tAR) to 1 over the course of the annealing, aka 1 = tAR * exp(-coolingFactor * tAR), rearrange to get ln(tAR) / tAR
        CoolingFunction coolingFunction = new ExponentialCooling(1000, Math.log(1000 / 100) / totalAnnealRuns);
        
                //get initial path time
                @SuppressWarnings("unchecked")
                ArrayList<GHPoint> initialPath = (ArrayList<GHPoint>) (ArrayList<? extends GHPoint>) addressList;
                long initialTime = hopper.route(
                    new GHRequest(initialPath)
                    .setProfile("car")
                    .setAlgorithm(Parameters.Algorithms.DIJKSTRA_BI) //should be slow at start, but speed up after a while
                    .setLocale(Locale.US)
                ).getBest().getTime();
        
        //a 75% chance of taking a longer route at the start due to boltzmann factor (50% wasnt cutting it sometimes)
        //0.75 = exp(-E/kT), rearrange for k it is E / (ln(4/3) * T)
        //initial dE would be around 1e6 milliseconds per edge, which is nodes - 1 but since start and end node are the same and occur once in addressList, it is just addressList.size()
        //tuned for each initial path
        double boltzmannFactor = initialTime / (Math.log(1.0 / 0.75) * coolingFunction.getTemp());

        //create thing that does all the calculations
        AnnealTaskOverallDriver driver = new AnnealTaskOverallDriver(
                hopper,
                addressList,
                coolingFunction,
                boltzmannFactor)
                .setTotalAnneals(totalAnnealRuns)
                .setCrossoverActions(totalCrossovers)
                .setTotalConcurrentThreads(16)
                .setCrossoverNum((int) Math.ceil(Math.sqrt(2.0 * addressList.size()))); //scale by sqrt(2) * sqrt(path size)

        //does the actual calculations
        driver.start();

        ArrayList<LocationPoint> bestOrder = driver.getBestPath();

        //print the solution
        System.out.print("\n\n" + String.format("%.1f", driver.getBestTimeInMillis() / 60000) + " minutes to go from ");
        bestOrder.forEach(loc -> 
            System.out.print(
                loc.getAddress().split(", ")[0]
                + ", to "));
        System.out.println(bestOrder.get(0).getAddress().split(", ")[0]);

        //saving solution
        System.out.print("Do you want to save the results? Y/N: ");
        while (true) {
            if (!scnr.hasNext()) {
                try {
                    scnr.wait(1000);
                } catch (InterruptedException e) {}
                continue;
            }
            String input = scnr.nextLine();
            if (input.toLowerCase().equals("y") || input.toLowerCase().equals("n")) {
                try {
                    FileWriter fw = new FileWriter("path.txt");
                    fw.write(String.format("%.1f min\n", driver.getBestTimeInMillis() / 60000));
                    bestOrder.forEach(node -> {
                        try {
                            fw.write(node.getAddress() + "\n");
                        } catch (IOException e) {}
                    });
                    fw.close();
                } catch (Exception e) {
                    e.printStackTrace();
                    System.out.println("Error cannot write output.");
                    break;
                }
                System.out.println("Path and time available in 'path.txt'");
                break;
            }
            System.out.println("Not a valid option.");
        }

        scnr.close();
    }
}
