import java.io.*;
import java.util.*;

public class Naloga9 {
    private String inputFilename;
    private String outputFilename;

    private BufferedReader reader;
    private PrintWriter writer;

    private int numberOfPathsInAGraph;
    private List<Movement> movements = new ArrayList<>();
    private List<Connection> connections = new ArrayList<>();
    private Map<Integer, Node> nodesById = new HashMap<>();
    private Map<TwoNodesKey, Connection> connectionsByNodes = new HashMap<>();

    private class TwoNodesKey {
        private int node1;
        private int node2;

        public TwoNodesKey(int node1, int node2) {
            this.node1 = node1;
            this.node2 = node2;
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (o == null || getClass() != o.getClass()) return false;
            TwoNodesKey that = (TwoNodesKey) o;
            return node1 == that.node1 && node2 == that.node2;
        }

        @Override
        public int hashCode() {
            return Objects.hash(node1, node2);
        }
    }

    public static void main(String[] args) throws IOException {
        Naloga9 program = new Naloga9(args[0], args[1]);
        program.initialise();
        program.run();
        program.cleanUp();
    }

    private void run() throws IOException {
        readInput();
        buildGraph();
        calculateMovements();
        printBusiestConnections();
    }

    private void calculateMovements() {
        for (Movement movement : movements) {
            calculateMovement(movement);
        }
    }

    private void calculateMovement(Movement movement) {
        ArrayList<Node> path = new ArrayList<>();
        int start = movement.getStart();
        Node startNode = nodesById.get(start);
        path.add(startNode);
        HashSet<Node> alreadyVisited = new HashSet<>();
        alreadyVisited.add(startNode);
        int target = movement.getTarget();
        Node targetNode = nodesById.get(target);
        ArrayList<ArrayList<Node>> paths = new ArrayList<>();
        paths.add(path);
        ArrayList<ArrayList<Node>> pathsToTarget = new ArrayList<>();
        do {
            paths = continuePaths(paths, alreadyVisited);
            for (ArrayList<Node> checkedPath : paths) {
                Node last = checkedPath.get(checkedPath.size() - 1);
                if (last == targetNode) {
                    pathsToTarget.add(checkedPath);
                }
            }
        } while (pathsToTarget.isEmpty() && !paths.isEmpty());
        if (!pathsToTarget.isEmpty()) {
            List<Node> optimalPath = getOptimalPath(pathsToTarget);
            int numberOfPassengers = movement.getNumberOfPassengers();
            addPassengers(optimalPath, numberOfPassengers);
        }
    }

    private void addPassengers(List<Node> path, int numberOfPassengers) {
        for (int i = 1; i < path.size(); i++) {
            TwoNodesKey key = new TwoNodesKey(path.get(i).getId(), path.get(i - 1).getId());
            Connection connection = connectionsByNodes.get(key);
            connection.addPassengers(numberOfPassengers);
        }
    }

    private ArrayList<ArrayList<Node>> continuePaths(ArrayList<ArrayList<Node>> paths, HashSet<Node> alreadyVisited) {
        ArrayList<ArrayList<Node>> nextPaths = new ArrayList<>();
        HashSet<Node> nextAlreadyVisited = new HashSet<>();
        for (ArrayList<Node> path : paths) {
            Node currentNode = path.get(path.size() - 1);
            List<Node> connectedNodes = currentNode.getConnectedNodes();
            for (Node node : connectedNodes) {
                if (!alreadyVisited.contains(node)) {
                    ArrayList<Node> nextPath = (ArrayList<Node>) path.clone();
                    nextPath.add(node);
                    nextPaths.add(nextPath);
                    nextAlreadyVisited.add(node);
                }
            }
        }
        alreadyVisited.addAll(nextAlreadyVisited);
        return nextPaths;
    }

    private List<Node> getOptimalPath(ArrayList<ArrayList<Node>> paths) {
        paths.sort((path1, path2) -> {
            int size1 = path1.size();
            int size2 = path2.size();
            if (size1 != size2) {
                return size1 - size2;
            }

            for (int i = 0; i < size1; i++) {
                int nodeId1 = path1.get(i).getId();
                int nodeId2 = path2.get(i).getId();
                if (nodeId1 != nodeId2) {
                    return nodeId1 - nodeId2;
                }
            }

            return 0;
        });
        return paths.get(0);
    }

    private void buildGraph() {
        for (Connection connection : connections) {
            int node1Id = connection.getNodeId1();
            Node node1 = nodesById.get(node1Id);
            int node2Id = connection.getNodeId2();
            Node node2 = nodesById.get(node2Id);
            node1.addConnection(node2);
            node2.addConnection(node1);
        }
    }

    private void readInput() throws IOException {
        String parameters = reader.readLine();
        String[] separatedParameters = parameters.split(",");
        numberOfPathsInAGraph = Integer.parseInt(separatedParameters[0]);
        int numberOfFacts = Integer.parseInt(separatedParameters[1]);
        for (int i = 0; i < numberOfPathsInAGraph; i++) {
            String connectionsString = reader.readLine();
            String[] connectionsStrings = connectionsString.split(",");
            int nodeId1 = Integer.parseInt(connectionsStrings[0]);
            int nodeId2 = Integer.parseInt(connectionsStrings[1]);
            Connection connection = new Connection(nodeId1, nodeId2);
            connections.add(connection);
            connectionsByNodes.put(new TwoNodesKey(nodeId1, nodeId2), connection);
            connectionsByNodes.put(new TwoNodesKey(nodeId2, nodeId1), connection);
            createNode(nodeId1);
            createNode(nodeId2);
        }
        for (int i = 0; i < numberOfFacts; i++) {
            String movementString = reader.readLine();
            String[] movementsStrings = movementString.split(",");
            movements.add(new Movement(Integer.parseInt(movementsStrings[0]), Integer.parseInt(movementsStrings[1]), Integer.parseInt(movementsStrings[2])));
        }
    }

    public void createNode(int nodeId) {
        if (!nodesById.containsKey(nodeId)) {
            nodesById.put(nodeId, new Node(nodeId));
        }
    }

    public void printBusiestConnections() {
        connections.sort((o1, o2) -> {
            if (o1.getPassengerCount() != o2.getPassengerCount()) {
                return o2.getPassengerCount() - o1.getPassengerCount();
            }
            if (o1.getNodeId1() != o2.getNodeId1()) {
                return o1.getNodeId1() - o2.getNodeId1();
            }
            return o1.getNodeId2() - o2.getNodeId2();
        });
        int size = connections.size();
        for (int i = 0; i < size; i++) {
            Connection connection = connections.get(i);
            writer.print(connection.getNodeId1());
            writer.print(",");
            writer.print(connection.getNodeId2());
            writer.println();
            if (i + 1 >= size || connections.get(i + 1).getPassengerCount() < connection.getPassengerCount()) {
                writer.flush();
                return;
            }
        }
    }

    public Naloga9(String inputFilename, String outputFilename) {
        this.inputFilename = inputFilename;
        this.outputFilename = outputFilename;
    }

    public void cleanUp() {
        try {
            reader.close();
            writer.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void initialise() {
        initialiseReaders();
    }

    private void initialiseReaders() {
        try {
            reader = new BufferedReader(new FileReader(inputFilename));
            writer = new PrintWriter(outputFilename);
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }
    }

    private class Node {
        private int id;
        private List<Node> connectedNodes = new ArrayList<>();

        public Node(int id) {
            this.id = id;
        }

        public void addConnection(Node node) {
            connectedNodes.add(node);
        }

        public int getId() {
            return id;
        }

        public List<Node> getConnectedNodes() {
            return connectedNodes;
        }
    }

    private class Connection {
        private int nodeId1;
        private int nodeId2;
        private int passengerCount;

        public Connection(int nodeId1, int nodeId2) {
            this.nodeId1 = nodeId1;
            this.nodeId2 = nodeId2;
        }

        public int getNodeId1() {
            return nodeId1;
        }

        public int getNodeId2() {
            return nodeId2;
        }

        public int getPassengerCount() {
            return passengerCount;
        }

        public void addPassengers(int passengerCount) {
            this.passengerCount += passengerCount;
        }
    }

    private class Movement {
        private int start;
        private int target;
        private int numberOfPassengers;

        public Movement(int start, int target, int numberOfPassengers) {
            this.start = start;
            this.target = target;
            this.numberOfPassengers = numberOfPassengers;
        }

        public int getStart() {
            return start;
        }

        public int getTarget() {
            return target;
        }

        public int getNumberOfPassengers() {
            return numberOfPassengers;
        }
    }
}
