import java.io.*;
import java.util.*;

public class Naloga6 {

    private String inputFilename;
    private String outputFilename;

    private BufferedReader reader;
    private PrintWriter writer;

    private Deque<String> outputStack = new ArrayDeque<>();
    private Deque<String> operatorStack = new ArrayDeque<>();

    private Map<String, Integer> operatorPrecedences = new HashMap<>();

    public static void main(String[] args) throws Exception {
        Naloga6 program = new Naloga6(args[0], args[1]);
        program.initialise();
        program.callShuntingYardAlg();
        program.cleanUp();
    }

    public int getTreeHeight(int currentTreeHeight, Deque<String> remainingStack) {
        String token = remainingStack.pop();
        if (!isOperator(token)) {
            return currentTreeHeight + 1;
        }
        if (token.equals("NOT")) {
            return getTreeHeight(currentTreeHeight + 1, remainingStack);
        }
        int leftHeight = getTreeHeight(currentTreeHeight + 1, remainingStack);
        int rightHeight = getTreeHeight(currentTreeHeight + 1, remainingStack);
        return Math.max(leftHeight, rightHeight);
    }

    public void callShuntingYardAlg() throws Exception {
        StringTokenizer input = new StringTokenizer(reader.readLine());
        Deque<String> tokenStack = new ArrayDeque<>();
        while (input.hasMoreTokens()) {
            addInputToTokenStack(tokenStack, input.nextToken());
        }
        while (!tokenStack.isEmpty()) {
            evaluateToken(tokenStack.pop());
        }
        while (!operatorStack.isEmpty()) {
            if (Objects.equals(operatorStack.peek(), "(")) {
                throw new Exception("Parentheses do not match!");
            }
            String operator = operatorStack.pop();
            outputStack.push(operator);
        }
        print();
    }

    private void addInputToTokenStack(Deque<String> tokenStack, String token) {
        if (token.startsWith("(")) {
            tokenStack.push(")");
            addInputToTokenStack(tokenStack, token.substring(1));
            return;
        }
        if (token.endsWith(")")) {
            addInputToTokenStack(tokenStack, token.substring(0, token.length() - 1));
            tokenStack.push("(");
            return;
        }
        tokenStack.push(token);
    }

    private void evaluateToken(String token) throws Exception {
        if (isOperator(token)) {
            while (isTheTopOfTheOperatorStackAnOperator() && hasHigherPrecedence(token, operatorStack.peek())) {
                String topOperator = operatorStack.pop();
                outputStack.push(topOperator);
            }
            operatorStack.push(token);
        } else if (token.equals("(")) {
            operatorStack.push(token);
        } else if (token.equals(")")) {
            while (!operatorStack.peek().equals("(")) {
                if (operatorStack.isEmpty()) {
                    throw new Exception("Parentheses do not match!");
                }
                String operator = operatorStack.pop();
                outputStack.push(operator);
            }
            if (!Objects.equals(operatorStack.peek(), "(")) {
                throw new Exception("Parentheses do not match!");
            }
            operatorStack.pop();
        } else {
            outputStack.push(token);
        }
    }

    private boolean isTheTopOfTheOperatorStackAnOperator() {
        String token = operatorStack.peek();
        return token != null && isOperator(token);
    }

    private boolean hasHigherPrecedence(String o1, String o2) {
        return operatorPrecedences.get(o1) > operatorPrecedences.get(o2);
    }

    private boolean isOperator(String token) {
        return token.equals("OR") || token.equals("AND") || token.equals("NOT");
    }

    public void print() {
        writer.println(String.join(",", outputStack));
        writer.println(getTreeHeight(0, outputStack));
        writer.flush();
    }

    public Naloga6(String inputFilename, String outputFilename) {
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
        initialiseOperatorPrecedences();
    }

    private void initialiseOperatorPrecedences() {
        operatorPrecedences.put("NOT", 1);
        operatorPrecedences.put("AND", 2);
        operatorPrecedences.put("OR", 3);
    }

    private void initialiseReaders() {
        try {
            reader = new BufferedReader(new FileReader(inputFilename));
            writer = new PrintWriter(outputFilename);
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }
    }
}


