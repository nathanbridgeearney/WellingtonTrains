// This program is copyright VUW.
// You are granted permission to use it to construct your answer to a COMP103 assignment.
// You may not distribute it in any other way without permission.

/* Code for COMP103 - 2021T2, Assignment 2
 * Name:
 * Username:
 * ID:
 */

import ecs100.*;

import java.net.SecureCacheResponse;
import java.util.*;
import java.io.*;
import java.nio.file.*;

/**
 * WellingtonTrains
 * A program to answer queries about Wellington train lines and timetables for
 * the train services on those train lines.
 * <p>
 * See the assignment page for a description of the program and what you have to do.
 */

public class WellingtonTrains {
    //Fields to store the collections of Stations and Lines
    /*# YOUR CODE HERE */
    Map<String, Station> allStations = new HashMap<>();
    Map<String, TrainLine> allTrainLines = new HashMap<>();

    // Fields for the suggested GUI.
    private String stationName;        // station to get info about, or to start journey from
    private String lineName;           // train line to get info about.
    private String destinationName;
    private int startTime = 0;         // time for enquiring about

    /**
     * main method:  load the data and set up the user interface
     */
    public static void main(String[] args) {
        WellingtonTrains wel = new WellingtonTrains();
        wel.loadData();   // load all the data
        wel.setupGUI();   // set up the interface
    }

    /**
     * Load data files
     */
    public void loadData() {
        loadStationData();
        UI.println("Loaded Stations");
        loadTrainLineData();
        UI.println("Loaded Train Lines");
        // The following is only needed for the Completion and Challenge
        loadTrainServicesData();
        UI.println("Loaded Train Services");
    }


    /**
     * User interface has buttons for the queries and text fields to enter stations and train line
     * You will need to implement the methods here.
     */
    public void setupGUI() {
        UI.addButton("All Stations", this::listAllStations);
        UI.addButton("Stations by name", this::listStationsByName);
        UI.addButton("All Lines", this::listAllTrainLines);
        UI.addTextField("Station", (String name) -> this.stationName = name);
        UI.addTextField("Train Line", (String name) -> this.lineName = name);
        UI.addTextField("Destination", (String name) -> this.destinationName = name);
        UI.addTextField("Time (24hr)", (String time) ->
        {
            try {
                this.startTime = Integer.parseInt(time);
            } catch (Exception e) {
                UI.println("Enter four digits");
            }
        });
        UI.addButton("Lines of Station", () -> listLinesOfStation(this.stationName));
        UI.addButton("Stations on Line", () -> listStationsOnLine(this.lineName));
        UI.addButton("Stations connected?", () -> checkConnected(this.stationName, this.destinationName));
        UI.addButton("Next Services", () -> findNextServices(this.stationName, this.startTime));
        UI.addButton("Find Trip", () -> findTrip(this.stationName, this.destinationName, this.startTime));

        UI.addButton("Quit", UI::quit);
        UI.setMouseListener(this::doMouse);

        UI.setWindowSize(900, 400);
        UI.setDivider(0.2);

        // this is just to remind you to start the program using main!
        if (allStations.isEmpty()) {
            UI.setFontSize(36);
            UI.drawString("Start the program from main", 2, 36);
            UI.drawString("in order to load the data", 2, 80);
            UI.sleep(2000);
            UI.quit();
        } else {
            UI.drawImage("data/geographic-map.png", 0, 0);
            UI.drawString("Click to list closest stations", 2, 12);
        }
    }


    public void doMouse(String action, double x, double y) {
        if (action.equals("released")) {
            /*# YOUR CODE HERE */
            UI.clearText();
            closest(new double[]{x, y});
        }
    }

    private void closest(double[] mouseCoords) {
        int count = 0;
        TreeMap<Double, String> stationMap = new TreeMap<>();
        allStations.forEach((key, value) -> stationMap.put((distance(value.getXCoord(), value.getYCoord(), mouseCoords)), key));
        for (Double key : stationMap.keySet()) {
            if (count >= 10) {
                break;
            } else {
                UI.println(count + 1 + ": " + stationMap.get(key) + " is " + key + " km away");
                count++;
            }
        }
    }

    private Double distance(double x, double y, double[] co) {
        double xdiff = x >= co[0] ? x - co[0] : co[0] - x;
        double ydiff = y >= co[1] ? y - co[1] : co[1] - y;
        double length = Math.hypot(xdiff, ydiff);
        length = (double) ((int) (length * 100)) / 100;
        return length;
    }
    // Methods for loading data and answering queries

    /*# YOUR CODE HERE */
    public void loadStationData() {
        try {
            List<String> allStat = Files.readAllLines(Path.of("data/stations.data"));
            for (String line : allStat) {
                Scanner sc = new Scanner(line);
                String statName = sc.next();
                int statZone = sc.nextInt();
                int statX = sc.nextInt();
                int statY = sc.nextInt();
                Station station = new Station(statName, statZone, statX, statY);
                allStations.put(statName, station);
            }
        } catch (IOException e) {
            e.printStackTrace();
        }

    }

    public void loadTrainLineData() {
        try {
            List<String> allLines = Files.readAllLines(Path.of("data/train-lines.data"));
            for (String line : allLines) {
                Scanner sc = new Scanner(line);
                String name = sc.next();
                TrainLine trainline = new TrainLine(name);
                allTrainLines.put(name, trainline);
                List<String> line2 = Files.readAllLines(Path.of("data/" + name + "-stations.data"));
                for (String i : line2) {
                    Scanner sc2 = new Scanner(i);
                    String statName = sc2.next();
                    Station station = allStations.get(statName);
                    trainline.addStation(station);
                    station.addTrainLine(trainline);
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void loadTrainServicesData() {
        try {
            List<String> allLines = Files.readAllLines(Path.of("data/train-lines.data"));
            for (String line : allLines) {
                Scanner sc = new Scanner(line);
                String name = sc.next();
                TrainLine trainline = allTrainLines.get(name);
                List<String> line2 = Files.readAllLines(Path.of("data/" + name + "-services.data"));
                for (String i : line2) {
                    TrainService trainService = new TrainService(trainline);
                    Scanner sc2 = new Scanner(i);
                    while (sc2.hasNext()) {
                        int time = sc2.nextInt();
                        trainService.addTime(time);
                    }
                    trainline.addTrainService(trainService);
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void findTrip(String stationName, String destinationName, int startTime) {
        UI.clearText();
        boolean done = false;
        Station firstStat = allStations.get(stationName);
        Station lastStat = allStations.get(destinationName);
        ArrayList<TrainLine> first = new ArrayList<>();
        for (Station station : allStations.values()) {
            if (Objects.equals(stationName, station.getName())) {
                first.addAll(station.getTrainLines());
            }
        }
        if (!first.isEmpty() && lastStat != null) {
            for (TrainLine trainlines : first) {
                if (lastStat.getTrainLines().contains(trainlines)) {
                    if (trainlines.getStations().indexOf(firstStat) < trainlines.getStations().indexOf(lastStat)) {
                        for (TrainService trainservice : trainlines.getTrainServices()) {
                            int two = trainservice.getTimes().get(trainlines.getStations().indexOf(firstStat));
                            if (two >= startTime) {
                                if (!done) {
                                    int t = trainservice.getTimes().get(trainlines.getStations().indexOf(firstStat));
                                    int finalTime = trainservice.getTimes().get(trainlines.getStations().indexOf(lastStat));
                                    int zones = 1 + Math.abs(firstStat.getZone() - lastStat.getZone());
                                    UI.println(trainservice + "leaves " + stationName + " at " + two + " and arrives at " + finalTime);
                                    UI.println("It passes through " + zones + " zones");
                                    done = true;
                                }
                            }
                        }
                    }
                }
            }
        } else {
            UI.println("Please input a correct station");
        }
    }


    private void findNextServices(String stationName, int startTime) {
        UI.clearText();
        ArrayList<TrainLine> first = new ArrayList<>();
        for (Station station : allStations.values()) {
            if (Objects.equals(stationName, station.getName())) {
                first.addAll(station.getTrainLines());
            }
        }
        for (TrainLine trainlines : first) {
            boolean done = false;
            for (TrainService trainservice : trainlines.getTrainServices()) {
                for (Integer times : trainservice.getTimes()) {
                    int two = times;
                    if (two >= startTime) {
                        if (!done) {
                            UI.println(trainservice.getTrainID() + " " + times);
                            done = true;
                        }
                    }
                }
            }
        }
    }

    private void checkConnected(String stationName, String destinationName) {
        UI.clearText();
        Station first = allStations.get(stationName);
        Station last = allStations.get(destinationName);
        ArrayList<TrainLine> firstStation = new ArrayList<>();
        for (TrainLine trainline : allTrainLines.values()) {
            if (trainline.getStations().contains(first)) {
                firstStation.add(trainline);
            }
        }
        for (TrainLine i : firstStation) {
            if (i.getStations().indexOf(first) <= i.getStations().indexOf(last)) {
                UI.println(i);
            }
        }
    }


    private void listStationsOnLine(String lineName) {
        UI.clearText();
        ArrayList<Station> temp = new ArrayList<>();
        for (TrainLine trainline : allTrainLines.values()) {
            if (Objects.equals(lineName, trainline.getName())) {
                temp.addAll(trainline.getStations());
            }
        }
        for (Station i : temp) UI.println(i);
    }

    private void listLinesOfStation(String stationName) {
        UI.clearText();
        ArrayList<TrainLine> temp = new ArrayList<>();
        for (Station station : allStations.values()) {
            if (Objects.equals(stationName, station.getName())) {
                temp.addAll(station.getTrainLines());
            }
        }
        for (TrainLine i : temp) UI.println(i);
        if (temp.isEmpty()) {
            UI.println("Please input a proper Station Name");
        }
    }

    private void listAllTrainLines() {
        UI.clearText();
        ArrayList<TrainLine> temp = new ArrayList<>(allTrainLines.values());
        for (TrainLine i : temp) UI.println(i);
    }

    private void listStationsByName() {
        UI.clearText();
        ArrayList<Station> temp = new ArrayList<>();
        for (Station stations : allStations.values()) {
            temp.add(stations);
            Collections.sort(temp);
        }
        for (Station i : temp) UI.println(i);
    }

    private void listAllStations() {
        UI.clearText();
        for (Station stations : allStations.values())
            UI.println(stations.getName() + " (Zone: " + stations.getZone() + " Lines: " + stations.getTrainLines().size() + ")");
    }
}
