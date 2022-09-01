import java.io.*;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

public class Naloga10 {
    private String inputFilename;
    private String outputFilename;

    private BufferedReader reader;
    private PrintWriter writer;

    private int numberOfGroups;

    private List<Point> points;
    private List<Distance> distances = new ArrayList<>();
    private int numberOfPoints;
    private ArrayList<Group> groups;
    private int numberOfAvailableGroups;

    public Naloga10(String inputFilename, String outputFilename) {
        this.inputFilename = inputFilename;
        this.outputFilename = outputFilename;
    }

    public static void main(String[] args) throws IOException {
        Naloga10 program = new Naloga10(args[0], args[1]);
        program.initialise();
        program.run();
        program.cleanUp();
    }

    private void run() throws IOException {
        readInput();
        calculateDistancesBetweenEachPoint();
        sortDistances();
        do {
            calculateSmallestDistance();
        } while (groups.size() > numberOfAvailableGroups);
        for (Group group : groups) {
            group.sort();
        }
        sortGroups();
        print();
    }

    public void sortGroups() {
        groups.sort(Comparator.comparingInt(o -> o.getPoints().get(0).getLabel()));
    }

    public void readInput() throws IOException {
        numberOfPoints = Integer.parseInt(reader.readLine());
        points = new ArrayList<>(numberOfPoints);
        groups = new ArrayList<>(numberOfPoints);
        for (int i = 0; i < numberOfPoints; i++) {
            String coordinatesString = reader.readLine();
            String[] coordinatesStrings = coordinatesString.split(",");
            double x = Double.parseDouble(coordinatesStrings[0]);
            double y = Double.parseDouble(coordinatesStrings[1]);
            Point point = new Point(x, y, i + 1);
            points.add(point);
            Group group = new Group(point);
            point.setGroup(group);
            groups.add(group);
        }
        numberOfGroups = Integer.parseInt(reader.readLine());
        numberOfAvailableGroups = numberOfGroups;
    }

    public void calculateSmallestDistance() {
        Distance smallestDistance = distances.get(0);
        groupPoints(smallestDistance);
        distances.remove(0);
    }

    private void groupPoints(Distance smallestDistance) {
        Point point1 = points.get(smallestDistance.getPoint1Index());
        Group point1Group = point1.getGroup();
        Point point2 = points.get(smallestDistance.getPoint2Index());
        Group point2Group = point2.getGroup();
        if (!point1Group.equals(point2Group)) {
            point1Group.addAllPoints(point2Group);
            for (Point point : point2Group.getPoints()) {
                point.setGroup(point1Group);
            }
            groups.remove(point2Group);
        }
    }

    public void calculateDistancesBetweenEachPoint() {
        for (int i = 0; i < points.size(); i++) {
            for (int j = i + 1; j < points.size(); j++) {
                Distance distance = new Distance(getDistanceBetweenTwoPoints(points.get(i), points.get(j)), i, j);
                distances.add(distance);
            }
        }
    }

    public void sortDistances() {
        distances.sort((o1, o2) -> {
            double distance1 = o1.getDistance();
            double distance2 = o2.getDistance();
            return Double.compare(distance1, distance2);
        });
    }

    public double getDistanceBetweenTwoPoints(Point point1, Point point2) {
        double differenceXCoordinates = point1.getX() - point2.getX();
        double differenceYCoordinates = point1.getY() - point2.getY();
        double xDifferenceSquared = differenceXCoordinates * differenceXCoordinates;
        double yDifferenceSquared = differenceYCoordinates * differenceYCoordinates;
        double distanceSquared = xDifferenceSquared + yDifferenceSquared;
        return Math.sqrt(distanceSquared);
    }

    public void print() {
        for (Group group : groups) {
            List<String> labels = new ArrayList<>();
            for (Point point : group.getPoints()) {
                labels.add("" + point.getLabel());
            }
            String groupLabels = String.join(",", labels);
            writer.println(groupLabels);
        }
        writer.flush();
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

    private class Point {
        private double x;
        private double y;
        private Group group;
        private int label;

        private Point(double x, double y, int label) {
            this.x = x;
            this.y = y;
            this.label = label;
        }

        public int getLabel() {
            return label;
        }

        public Group getGroup() {
            return group;
        }

        public void setGroup(Group group) {
            this.group = group;
        }

        public double getX() {
            return x;
        }

        public double getY() {
            return y;
        }
    }

    private class Distance {
        private double distance;
        private int point1Index;
        private int point2Index;

        public Distance(double distance, int point1Index, int point2Index) {
            this.distance = distance;
            this.point1Index = point1Index;
            this.point2Index = point2Index;
        }

        public double getDistance() {
            return distance;
        }

        public int getPoint1Index() {
            return point1Index;
        }

        public int getPoint2Index() {
            return point2Index;
        }
    }

    private class Group {
        private List<Point> points = new ArrayList<>();

        public Group(Point point) {
            points.add(point);
        }

        public List<Point> getPoints() {
            return points;
        }

        public void addAllPoints(Group group) {
            points.addAll(group.points);
        }

        public void sort() {
            points.sort(Comparator.comparingInt(Point::getLabel));
        }
    }
}
