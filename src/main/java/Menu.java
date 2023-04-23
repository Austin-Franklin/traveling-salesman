import java.util.ArrayList;
import java.util.Scanner;

import com.graphhopper.util.DistanceCalcEarth;

public class Menu {
    public static ArrayList<LocationPoint> run() {
        ArrayList<LocationPoint> path = new ArrayList<>();
        boolean running = true;
        Scanner scnr = new Scanner(System.in);
        System.out.println("Welcome to the Pensacola traveling salesman solver.");
        while (running) {
            System.out.println("\t1. Enter path.\n" +
                    "\t2. See currently entered path.\n" +
                    "\t3. Run solver.\n" + 
                    "\t4. Exit.");
            String menuInput = scnr.nextLine();
            int menuChoice;
            try {
                menuChoice = Integer.parseInt(menuInput);
            } catch (Exception e) {
                menuChoice = -1;
            }
            switch (menuChoice) {
                case 1:
                    path = readPath(scnr);
                    break;

                case 2: 
                    path.forEach(v -> System.out.println(v));
                    break;

                case 3: 
                    running = false;
                    break;

                case 4:
                    System.exit(0);
                    break;

                case -1:
                    System.out.println("Bad input, try again.\n");
                    break;

                default:
                    System.out.println("Not an option, try again.\n");
            }
        }
        scnr.close();
        return path;
    }

    private static ArrayList<LocationPoint> readPath(Scanner scnr) {
        ArrayList<LocationPoint> path = new ArrayList<>();
        String origin = null;

        // input starting/ending point
        while (origin == null) {
            origin = getNextAddress("Please enter the origin of the loop:", scnr);
        }
        path.add(new LocationPoint(origin));

        // input addresses
        boolean finishedWithInputs = false;

        while (!finishedWithInputs) {

            String nextAddress = getNextAddress("Please enter the next address, or \"n\" when you are finished:", scnr);
            if (nextAddress == null) {
                finishedWithInputs = true; // unnecessary tbh
            } else {
                path.add(new LocationPoint(nextAddress));
            }

            if (finishedWithInputs && (path.size() < 2)) {
                finishedWithInputs = false;
                System.out.println("ERROR, you must enter a destination");
            }
            if (finishedWithInputs && (path.size() == 2)) {
                finishedWithInputs = false;
                System.out.println("You only have 1 stop. There is only 1 path to take. You wouldn't need this program for that.");
            }
        }
        return path;
    }

    private static String getNextAddress(String prompt, Scanner scnr) {

        String input = null;
        boolean done = false;

        while (!done) {

            try {
                System.out.print(prompt);
                input = scnr.nextLine();

                if (input.equals("n")) {
                    return null;
                }

                LocationPoint temp = new LocationPoint(input);
                if (temp.getLat() == 0) {
                    System.out.println("ERROR, ADDRESS NOT RESOLVABLE");
                } else if (checkResolvable(new LocationPoint(input))) {
                    done = true;
                } else {
                    System.out.println("ERROR, ADDRESS OUTSIDE OF MAXIMUM RANGE");
                }

            } catch (Exception e) {

            }

        }

        return input;

    }

    // verifies that a given LocationPoint appears within our dataset
    private static boolean checkResolvable(LocationPoint point) {

        DistanceCalcEarth sphereCalc = new DistanceCalcEarth();

        // these hardcoded values are the lat and lon we chose as the center of our
        // query when downloading the Pensacola.OSM file
        if (sphereCalc.calcDist(30.421309, -87.2169149, point.lat, point.lon) > 20000) {
            return false;
        }

        return true;
    }
}
