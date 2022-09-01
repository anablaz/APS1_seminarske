import java.io.*;
import java.util.*;

public class Naloga7 {

    private final String inputFilename;
    private final String outputFilename;
    private final Set<Integer> stops = new HashSet<>();
    private final Set<ArrayList<Integer>> optimalStopsPaths = new HashSet<>();
    private final Set<ArrayList<Integer>> optimalChangesPaths = new HashSet<>();
    private BufferedReader reader;
    private PrintWriter writer;
    /**
     * Outer one is routes and inner one the stops
     */
    private int[][] routes;
    private int start;
    private int end;
    private int numberOfRoutes;
    private boolean[][] matrix;
    private int highestStopIndex = 0;
    private int smallestNumberOfChanges;
    private int smallestNumberOfStops;

    public Naloga7(String inputFilename, String outputFilename) {
        this.inputFilename = inputFilename;
        this.outputFilename = outputFilename;
    }

    public static void main(String[] args) throws IOException {
        Naloga7 program = new Naloga7(args[0], args[1]);
        program.initialise();
        program.run();
        program.cleanUp();
    }

    private void run() {
        smallestNumberOfChanges = calculateSmallestNumberOfChanges();
        writer.println(smallestNumberOfChanges);
        smallestNumberOfStops = calculateSmallestNumberOfBusStops();
        writer.println(smallestNumberOfStops);
        int areOptimalPathsTheSame = evaluateTwoOptionalPaths();
        writer.println(areOptimalPathsTheSame);
        writer.flush();
    }

    public void readInput() throws IOException {
        numberOfRoutes = Integer.parseInt(reader.readLine());
        routes = new int[numberOfRoutes][];
        for (int i = 0; i < numberOfRoutes; i++) {
            String stopsString = reader.readLine();
            String[] stopStrings = stopsString.split(",");
            routes[i] = new int[stopStrings.length];
            for (int j = 0; j < stopStrings.length; j++) {
                int stopIndex = Integer.parseInt(stopStrings[j]) - 1;
                routes[i][j] = stopIndex;
                stops.add(stopIndex);
                if (stopIndex > highestStopIndex) {
                    highestStopIndex = stopIndex;
                }
            }
        }
        String firstAndLastStations = reader.readLine();
        String[] firstLast = firstAndLastStations.split(",");
        start = Integer.parseInt(firstLast[0]) - 1;
        end = Integer.parseInt(firstLast[1]) - 1;
    }

    public int evaluateTwoOptionalPaths() {
        if (smallestNumberOfChanges == -1 || smallestNumberOfStops == -1) {
            return -1;
        } else if (areOptimalPathsTheSame()) {
            return 1;
        }
        return 0;
    }

    public boolean areOptimalPathsTheSame() {
        for (ArrayList<Integer> changesPath : optimalChangesPaths) {
            for (ArrayList<Integer> stopsPath : optimalStopsPaths) {
                if (changesPath.equals(stopsPath)) {
                    return true;
                }
            }
        }
        return false;
    }

    public int calculateSmallestNumberOfChanges() {
        Set<Integer> alreadyVisited = new HashSet<>();
        Set<ArrayList<Integer>> nextPaths = new HashSet<>();
        ArrayList<Integer> startPath = new ArrayList<>();
        startPath.add(start);
        nextPaths.add(startPath);
        int numberOfChangesRequired = -1;
        boolean reachedEnd = false;
        do {
            Set<ArrayList<Integer>> currentPaths = nextPaths;
            nextPaths = new HashSet<>(visitStops(currentPaths, alreadyVisited));
            for (ArrayList<Integer> path : nextPaths) {
                int lastStop = path.get(path.size() - 1);
                if (lastStop == end) {
                    reachedEnd = true;
                    optimalChangesPaths.add(path);
                }
            }
            numberOfChangesRequired++;
        } while (!reachedEnd && alreadyVisited.size() < matrix.length);
        if (!reachedEnd) {
            return -1;
        }
        return numberOfChangesRequired;
    }

    public int calculateSmallestNumberOfBusStops() {
        Set<Integer> alreadyVisited = new HashSet<>();
        Set<ArrayList<Integer>> nextPaths = new HashSet<>();
        ArrayList<Integer> startPath = new ArrayList<>();
        startPath.add(start);
        nextPaths.add(startPath);
        int numberOfStopsRequired = -1;
        boolean reachedEnd = false;
        do {
            Set<ArrayList<Integer>> currentPaths = nextPaths;
            nextPaths = new HashSet<>(visitAdjacentStops(currentPaths, alreadyVisited));
            for (ArrayList<Integer> path : currentPaths) {
                int lastStop = path.get(path.size() - 1);
                alreadyVisited.add(lastStop);
                if (lastStop == end) {
                    reachedEnd = true;
                    optimalStopsPaths.add(path);
                }
            }
            numberOfStopsRequired++;
        } while (!reachedEnd && alreadyVisited.size() < matrix.length);
        if (!reachedEnd) {
            return -1;
        }
        return numberOfStopsRequired;
    }

    private Set<ArrayList<Integer>> visitAdjacentStops(Set<ArrayList<Integer>> paths, Set<Integer> alreadyVisited) {
        Set<Integer> stops = new HashSet<>();
        for (ArrayList<Integer> path : paths) {
            stops.add(path.get(path.size() - 1));
        }
        Set<ArrayList<Integer>> nextPaths = new HashSet<>();
        for (ArrayList<Integer> path : paths) {
            Set<Integer> adjacentStops = getAdjacentStops(path.get(path.size() - 1));
            for (int stop : adjacentStops) {
                if (!alreadyVisited.contains(stop) && !stops.contains(stop)) {
                    ArrayList<Integer> pathCopy = (ArrayList<Integer>) path.clone();
                    pathCopy.add(stop);
                    nextPaths.add(pathCopy);
                }
            }
        }
        return nextPaths;
    }

    private Set<Integer> getAdjacentStops(int stopIndex) {
        Set<Integer> stops = new HashSet<>();
        boolean[] stopVector = matrix[stopIndex];
        for (int routeIndex = 0; routeIndex < stopVector.length; routeIndex++) {
            if (stopVector[routeIndex]) {
                stops.addAll(getAdjacentStopsForRoute(routeIndex, stopIndex));
            }
        }
        return stops;
    }

    private Set<Integer> getAdjacentStopsForRoute(int routeIndex, int stopIndex) {
        int[] route = routes[routeIndex];
        Set<Integer> stops = new HashSet<>();
        for (int i = 0; i < route.length; i++) {
            if (stopIndex == route[i]) {
                if (i + 1 < route.length) {
                    stops.add(route[i + 1]);
                }
                if (i - 1 >= 0) {
                    stops.add(route[i - 1]);
                }
            }
        }
        return stops;
    }

    private Set<ArrayList<Integer>> visitStops(Set<ArrayList<Integer>> currentPaths, Set<Integer> alreadyVisited) {
        Set<ArrayList<Integer>> paths = new HashSet<>();
        Set<Integer> allLastStops = new HashSet<>();
        for (ArrayList<Integer> path : currentPaths) {
            allLastStops.add(path.get(path.size() - 1));
        }
        for (ArrayList<Integer> path : currentPaths) {
            Set<ArrayList<Integer>> pathsWithOneChange = getPathsWithOneChange(path);
            for (ArrayList<Integer> pathWithOneChange : pathsWithOneChange) {
                int lastStopOfPathWithOneChange = pathWithOneChange.get(pathWithOneChange.size() - 1);
                if (!alreadyVisited.contains(lastStopOfPathWithOneChange) && !allLastStops.contains(lastStopOfPathWithOneChange)) {
                    paths.addAll(pathsWithOneChange);
                }
            }
        }
        return paths;
    }

    private Set<ArrayList<Integer>> getPathsWithOneChange(ArrayList<Integer> path) {
        Set<ArrayList<Integer>> paths = new HashSet<>();
        int currentStop = path.get(path.size() - 1);
        boolean[] stopVector = matrix[currentStop];
        for (int routeIndex = 0; routeIndex < stopVector.length; routeIndex++) {
            if (stopVector[routeIndex]) {
                paths.addAll(getPathsForRoute(routeIndex, path));
            }
        }
        return paths;
    }

    private Set<ArrayList<Integer>> getPathsForRoute(int routeIndex, ArrayList<Integer> path) {
        Set<ArrayList<Integer>> paths = new HashSet<>();
        Integer currentStop = path.get(path.size() - 1);
        int currentStopIndex = 0;
        int[] route = routes[routeIndex];
        for (int i = 0; i < route.length; i++) {
            if (route[i] == currentStop) {
                currentStopIndex = i;
            }
        }
        ArrayList<Integer> lastPath = path;
        for (int i = currentStopIndex + 1; i < route.length; i++) {
            lastPath = (ArrayList<Integer>) lastPath.clone();
            lastPath.add(route[i]);
            paths.add(lastPath);
        }
        lastPath = path;
        for (int i = currentStopIndex - 1; i >= 0; i--) {
            lastPath = (ArrayList<Integer>) lastPath.clone();
            lastPath.add(route[i]);
            paths.add(lastPath);
        }
        return paths;
    }

    public void makeMatrix() {
        matrix = new boolean[highestStopIndex + 1][numberOfRoutes];
        for (int i = 0; i < numberOfRoutes; i++) {
            int[] currentRoute = routes[i];
            for (int j = 0; j < currentRoute.length; j++) {
                matrix[routes[i][j]][i] = true;
            }
        }
    }

    public void cleanUp() {
        try {
            reader.close();
            writer.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void initialise() throws IOException {
        initialiseReaders();
        readInput();
        makeMatrix();
    }

    public void initialiseReaders() {
        try {
            reader = new BufferedReader(new FileReader(inputFilename));
            writer = new PrintWriter(outputFilename);
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }
    }
}

