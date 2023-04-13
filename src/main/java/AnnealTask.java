import java.security.SecureRandom;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Locale;
import java.util.Random;

import com.graphhopper.*;
import com.graphhopper.util.*;
import com.graphhopper.util.shapes.*;

import cooling_functions.CoolingFunction;

public class AnnealTask implements Runnable, Cloneable {
    //thing running the routing
    private GraphHopper hopper;
    //held for copying
    private ArrayList<LocationPoint> originalStopList;
    //start/stop point
    private LocationPoint home;
    private ArrayList<LocationPoint> stopList;
    private int maxRuns;
    private int runs = 0;
    private boolean finished = false;
    private CoolingFunction cooling;
    private double boltzmannFactor;
    private long bestTime = Long.MAX_VALUE;

    /**
     * 
     * @param hopper Hopefully it doesnt break with multithreading
     * @param stopList This is deep copied in the constructor
     * @param home Your start/end location
     * @param maxRuns Max iterations
     * @param cooling either make a new or clone
     */
    public AnnealTask(GraphHopper hopper, ArrayList<LocationPoint> stopList, LocationPoint home, int maxRuns, CoolingFunction cooling, double boltzmannFactor) {
        this.hopper = hopper;
        this.originalStopList = stopList;
        this.home = home;

        //deep copy because the list<locationpoint> is mutated
        this.stopList = new ArrayList<>();
        this.stopList.add(home); //start point
        for (LocationPoint point : stopList)
            this.stopList.add(point);
        this.stopList.add(home); //returning home

        this.maxRuns = maxRuns;
        this.cooling = cooling;
        this.boltzmannFactor = boltzmannFactor;
    }

    @Override
    public void run() {
        //basically casts, points to the same data
        @SuppressWarnings("unchecked")
        ArrayList<GHPoint> ghList = (ArrayList<GHPoint>) (ArrayList<?>) stopList;
        
        GHRequest request = new GHRequest(ghList)
            .setProfile("car")
            .setAlgorithm(Parameters.Algorithms.DIJKSTRA_BI) //should be slow at start, but speed up after a while
            .setLocale(Locale.US);
        //initial calculation
        GHResponse response = hopper.route(request);
        if (response.hasErrors())
            throw new RuntimeException(response.getErrors().toString());
        bestTime = response.getBest().getTime();
        Random random = new SecureRandom(); //random random

        while(runs <= maxRuns) {
            //swap two entries to test if total time goes down
            int swapIndex1 = random.nextInt(stopList.size());
            int swapIndex2 = random.nextInt(stopList.size());
            Collections.swap(stopList, swapIndex1, swapIndex2);
            
            request.setPoints(ghList);
            response = hopper.route(request);
            if (response.hasErrors())
                throw new RuntimeException(response.getErrors().toString());


            long newTime = response.getBest().getTime();
                //if newTime beats bestTime, replace
            if (newTime < bestTime)
                bestTime = newTime;

                /*
                if the boltzmann factor is lower than a random number, set the higher new time as "best"
                as temperature goes to 0 exp(-E/kT) goes to 0
                as temperature goes to inf, exp(-E/kT) goes to 1
                */
            else if (Math.exp(-1 * (bestTime - newTime) / (boltzmannFactor * cooling.getTemp())) < Math.random())
                bestTime = newTime;

                //reverse the swap since it failed both previous tests
            else
                Collections.swap(stopList, swapIndex1, swapIndex2);
            
            cooling.cool();
            runs++;
        }
        finished = true;
    }

    public boolean isFinished() {
        return finished;
    }
    
    /**
     * You should check if it is finished first.
     * @return Best order this thread can come up with, CAN BE NULL!
     */
    public ArrayList<LocationPoint> getBestOrder() {
        return finished ? stopList : null;
    }

    public double getBestTime() {
        return finished ? bestTime : Long.MAX_VALUE;
    }

    public AnnealTask copy() {
        
        Collections.shuffle(originalStopList);

        return new AnnealTask(hopper, originalStopList, home, maxRuns, cooling, boltzmannFactor);
    }
}
