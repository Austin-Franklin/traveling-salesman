import java.io.File;
import java.util.List;
import java.util.ArrayList;
import java.util.Scanner;

import com.graphhopper.*;   
import com.graphhopper.config.*;
import com.graphhopper.util.DistanceCalcEarth;

import cooling_functions.*;

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

        ArrayList<LocationPoint> addressList = new  ArrayList<>();
        String origin=null;

        //input starting/ending point
        while(origin==null){
           origin=getNextAddress("Please enter the origin of the loop:");
        }
        addressList.add(new LocationPoint(origin));

        //input addresses
        boolean finishedWithInputs=false;

        while(!finishedWithInputs){

            String nextAddress=getNextAddress("Please enter the next address, or \"n\" when you are finished:");
            if(nextAddress==null){
                finishedWithInputs=true; //unnecessary tbh
            }
            else{
                addressList.add(new LocationPoint(nextAddress));
            }

            if(finishedWithInputs && addressList.size()<2){
                finishedWithInputs=false;
                System.out.println("ERROR, you must enter a destination");
            }
        }


        AnnealTaskOverallDriver driver = new AnnealTaskOverallDriver(
            hopper,
            addressList,
            new ExponentialCooling(1000, 1),
            1e7)
            .setTotalConcurrentThreads(16)
            .setCrossoverNum(40);
        
        driver.start();

        ArrayList<LocationPoint> bestOrder = driver.getBestPath();

        // GHResponse response = hopper.route(
        //     new GHRequest(finalOrder)
        //         .setProfile("car")
        // );

        // if(response.hasErrors()) {
        //     List<Throwable> errors = response.getErrors();
        //     for (Throwable error : errors) {
        //         System.err.println("An error occurred: " + error.getMessage());
        //     }
        // }


        System.out.print("\n\n" + driver.getBestTimeInMillis()/60000 + " minutes to go from ");
        bestOrder.forEach(loc -> System.out.print(
            loc.getAddress().split(", ")[0]
            + ", to "
        ));
        System.out.println(bestOrder.get(0).getAddress().split(", ")[0]);


    }

    
    //returns a resolvable address to construct a LocationPoint, 
    //or null if the user is finished giving addresses and enters a "n"
    public static String getNextAddress(String prompt){

        Scanner in=new Scanner(System.in);
        String input="";
        boolean done=false;

        while(!done){

            try{
                System.out.print(prompt);
                input=in.nextLine();

                if(input.equals("n")){
                    return null;
                }
                
                LocationPoint temp = new LocationPoint(input);
                if(temp.getLat()==0){
                    System.out.println("ERROR, ADDRESS NOT RESOLVABLE");
                }
                else if(checkResolvable(new LocationPoint(input))){
                    done=true;
                }
                else{
                    System.out.println("ERROR, ADDRESS OUTSIDE OF MAXIMUM RANGE");
                }

            }
            catch(Exception e){

            }

        }
        
        return input;

    }

    //verifies that a given LocationPoint appears within our dataset
    public static boolean checkResolvable(LocationPoint point){

        DistanceCalcEarth sphereCalc= new DistanceCalcEarth();
        
        //these hardcoded values are the lat and lon we chose as the center of our 
        //query when downloading the Pensacola.OSM file
        if(sphereCalc.calcDist(30.421309,-87.2169149,point.lat,point.lon)>19750){
            return false;
        } 

        return true;
    }

}
