import java.io.*;
import java.util.*;

public class Naloga8 {
    private String inputFilename;
    private String outputFilename;

    private BufferedReader reader;
    private PrintWriter writer;
    private HashMap<Integer, Node> nodesById;
    private Set<Integer> childIds;
    private Node rootNode;
    private LinkedList<Node> coordinates;

    public static void main(String[] args) throws IOException {
        Naloga8 program = new Naloga8(args[0], args[1]);
        program.initialise();
        program.run();
        program.cleanUp();
    }

    private void run() throws IOException {
        readInput();
        findRootNode();
        buildTree();
        assignXCoordinates();
        print();
    }

    private ArrayList<Node> getSortedNodes() {
        ArrayList<Node> nodes = new ArrayList<>(coordinates);
        nodes.sort(Comparator.comparingInt(Node::getDepth).thenComparingInt(Node::getXCoordinate));
        return nodes;
    }

    private void assignXCoordinates() {
        int index = 0;
        for (Node node : coordinates) {
            node.setXCoordinate(index);
            index++;
        }
    }

    private void buildTree() {
        coordinates = new LinkedList<>();
        coordinates.add(rootNode);
        insertNodesIntoList(rootNode, 0, 0);
    }

    private void insertNodesIntoList(Node node, int index, int depth) {
        Node left = nodesById.get(node.getLeftId());
        Node right = nodesById.get(node.getRightId());
        if (right != null) {
            coordinates.add(index + 1, right);
            right.setDepth(depth + 1);
        }
        if (left != null) {
            coordinates.add(index, left);
            left.setDepth(depth + 1);
        }
        if (right != null) {
            insertNodesIntoList(right, left != null ? index + 2 : index + 1, depth + 1);
        }
        if (left != null) {
            insertNodesIntoList(left, index, depth + 1);
        }
    }

    private void findRootNode() {
        for (Node node : nodesById.values()) {
            if (!childIds.contains(node.getId())) {
                rootNode = node;
                rootNode.setDepth(0);
            }
        }
    }

    private void readInput() throws IOException {
        int numberOfNodes = Integer.parseInt(reader.readLine());
        nodesById = new HashMap<>();
        childIds = new HashSet<>();
        for (int i = 0; i < numberOfNodes; i++) {
            String nodeString = reader.readLine();
            String[] nodeInformation = nodeString.split(",");
            int nodeId = Integer.parseInt(nodeInformation[0]);
            int nodeValue = Integer.parseInt(nodeInformation[1]);
            int leftChildId = Integer.parseInt(nodeInformation[2]);
            childIds.add(leftChildId);
            int rightChildId = Integer.parseInt(nodeInformation[3]);
            childIds.add(rightChildId);
            Node node = new Node(nodeId, nodeValue, leftChildId, rightChildId);
            nodesById.put(nodeId, node);
        }
    }

    public void print() {
        for (Node node : getSortedNodes()) {
            writer.print(node.getValue());
            writer.print(",");
            writer.print(node.getXCoordinate());
            writer.print(",");
            writer.print(node.getDepth());
            writer.println();
        }
        writer.flush();
    }

    public Naloga8(String inputFilename, String outputFilename) {
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
        private int value;
        private int depth;
        private int xCoordinate;
        private int leftId;
        private int rightId;

        public Node(int id, int value, int leftId, int rightId) {
            this.id = id;
            this.value = value;
            this.leftId = leftId;
            this.rightId = rightId;
        }

        public int getId() {
            return id;
        }

        public int getValue() {
            return value;
        }

        public int getDepth() {
            return depth;
        }

        public void setDepth(int depth) {
            this.depth = depth;
        }

        public int getXCoordinate() {
            return xCoordinate;
        }

        public void setXCoordinate(int xCoordinate) {
            this.xCoordinate = xCoordinate;
        }

        public int getLeftId() {
            return leftId;
        }

        public int getRightId() {
            return rightId;
        }
    }
}
