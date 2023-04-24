# Traveling Salesman

This is a Java implementation of the Traveling Salesman Problem.
Uses simulated annealing, genetic crossover, and multithreading.

## Requirements

- At least 2GB of ram (maybe more)
- Java 8 or higher
- Maven
- Windows or Linux
- An internet connection

## Installation

1. Clone the repository
- To run on Windows: `.\windows_run.bat`
- To run on Linux: `./linux_run.sh`

### Alternatively
1. Clone the repository
2. Run `mvn install`
3. Run `java -jar -Xms256m -Xmx2048m target\traveling-salesman-1.0-SNAPSHOT-jar-with-dependencies.jar`
4. Add ` < demo.txt` to input the demo list

## Usage
This program uses a map with a 20km radius localized around 30.42°N 87.21°W, 
which represents Pensacola, Florida and the surrounding area.
 
When entering addresses that may not be unique to Pensacola, 
it is necessary to include ", Pensacola", or the relavent zip code.
This is not done by default, since we also include areas of Milton, Gulf Breeze,
et cetera.

For example, UWF's address can be entered in the following forms:
University of West Florida
11000 University Pkwy, Pensacola
11000 University Pkwy, 32514


This program is designed to generate efficient solutions in the Pensacola area 
for the traveling salesman problem through the use of simulated annealing. 
We generate solutions in a loop, where the user ends in the same location that 
they departed from, such as a central distribution hub or office building.

The user is first prompted with a menu where they can 
1. Enter a path.
2. See the currently entered path.
3. Run the solver.
or
4. Exit prematurely.

After entering a set of addresses, the user may either review the list, 
re-enter the addresses, or run the solver.

After the program determines the optimal route it will print out an estimated
travel time and the order in which the addresses should be visited.

## Authors

- Austin Franklin
- Jack Lovette

## Acknowledgements
This project uses the GraphHopper(https://www.graphhopper.com/) routing library under the Apache License 2.0.