import java.io.File;
import java.util.*;

public class Graph {
    private int vertexCt;  // Number of vertices in the graph.
    private int[] pred = new int[vertexCt];   // Predecessor array
    private int[][] capacity;  // Adjacency  matrix
    private int[][] residual; // residual matrix
    private int[][] edgeCost; // cost of edges in the matrix
    private String graphName;  //The file from which the graph was created.
    private int totalFlow; // total achieved flow
    private int source = 0; // start of all paths
    private int sink; // end of all paths

    public Graph(String fileName) {
        this.vertexCt = 0;
        source  = 0;
        this.graphName = "";
        makeGraph(fileName);

    }

    /**
     * Method to add an edge
     *
     * @param source      start of edge
     * @param destination end of edge
     * @param cap         capacity of edge
     * @param weight      weight of edge, if any
     * @return edge created
     */
    private boolean addEdge(int source, int destination, int cap, int weight) {
        if (source < 0 || source >= vertexCt) return false;
        if (destination < 0 || destination >= vertexCt) return false;
        capacity[source][destination] = cap;
        residual[source][destination] = cap;
        edgeCost[source][destination] = weight;
        edgeCost[destination][source] = -weight;
        return true;
    }

    /**
     * Method to get a visual of the graph
     *
     * @return the visual
     */
    public String printMatrix(String label, int[][] m) {
        StringBuilder sb = new StringBuilder();
        sb.append("\n " + label+ " \n     ");
        for (int i=0; i < vertexCt; i++)
            sb.append(String.format("%5d", i));
        sb.append("\n");
        for (int i = 0; i < vertexCt; i++) {
            sb.append(String.format("%5d",i));
            for (int j = 0; j < vertexCt; j++) {
                sb.append(String.format("%5d",m[i][j]));
            }
            sb.append("\n");
        }
        return sb.toString();
    }

    /**
     * Method to make the graph
     *
     * @param filename of file containing data
     */
    private void makeGraph(String filename) {
        try {
            graphName = filename;
            System.out.println("\n****Find Flow " + filename);
            Scanner reader = new Scanner(new File(filename));
            vertexCt = reader.nextInt();
            capacity = new int[vertexCt][vertexCt];
            residual = new int[vertexCt][vertexCt];
            edgeCost = new int[vertexCt][vertexCt];
            for (int i = 0; i < vertexCt; i++) {
                for (int j = 0; j < vertexCt; j++) {
                    capacity[i][j] = 0;
                    residual[i][j] = 0;
                    edgeCost[i][j] = 0;
                }
            }

            // If weights, need to grab them from file
            while (reader.hasNextInt()) {
                int v1 = reader.nextInt();
                int v2 = reader.nextInt();
                int cap = reader.nextInt();
                int weight = reader.nextInt();
                if (!addEdge(v1, v2, cap, weight))
                    throw new Exception();
            }

            reader.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
        sink = vertexCt - 1;
        System.out.println( printMatrix("Edge Cost" ,edgeCost));
    }

    /**
     * Method to determine if an edge exists from U to V
     *
     * @param u the starting node
     * @param v the ending node
     */
    private boolean doesEdgeExist(int u, int v){
        return residual[u][v] != 0;
    }

    /**
     * Method using Bellman Ford algorithm to determine if there exists a path from S to T
     *
     * @param s the source of the pathfinder
     * @param t the target of the pathfinder
     */
    private boolean hasAugmentingCheapestPath(int s, int t) {
        pred = new int[vertexCt];
        int[] cost = new int[vertexCt];
        for (int i = 0; i < cost.length; i++) {
            cost[i] = 999;
            pred[i] = 999;
        }
        cost[s] = 0;
        for (int i = 0; i < vertexCt; i++) {
            for (int u = 0; u < vertexCt; u++) {
                for (int v = 0; v < vertexCt; v++) {
                    if (doesEdgeExist(u, v) && cost[v] > cost[u] + edgeCost[u][v]) {
                        cost[v] = cost[u] + edgeCost[u][v];
                        pred[v] = u;
                    }
                }
            }
        }
        return pred[t] != 999;
    }

    /**
     * Method using Ford Fulkerson algorithm to determine min cost for max flow
     *
     * @param s the source of the min cost max flow graph
     * @param t the target of the min cost max flow graph
     */
    public void FordFulkerson(int s, int t){
        while (hasAugmentingCheapestPath(s, t)){
            int prev;
            int availFlow = 999;
            StringBuilder pathString = new StringBuilder();
            pathString.append(t);
            for (int v = t; v != s; v = prev) {
                prev = pred[v];
                pathString.insert(0, prev + " -> ");
                availFlow = Math.min(availFlow, residual[prev][v]);
            }
            int totalCost = 0;
            for (int v = t; v != s; v = prev) {
                prev = pred[v];
                residual[prev][v] -= availFlow;
                totalCost += edgeCost[prev][v];
                residual[v][prev] += availFlow;
            }
            totalFlow += availFlow;
            System.out.println(pathString + " (" + availFlow + ")  $" + totalCost);
        }
    }

    /**
     * Method to print the final edge flow
     */
    public void finalEdgeFlow(){
        for (int i = 0; i < vertexCt; i++){
            for (int j = 0; j < vertexCt; j++){
                if (capacity[i][j] != 0 && (capacity[i][j] - residual[i][j]) > 0){
                    System.out.println("Flow " + i + " -> " + j + " (" + (capacity[i][j] - residual[i][j]) + ")  $" + edgeCost[i][j]);
                }
            }
        }
        System.out.println("TOTAL FLOW: " + totalFlow);
    }

    /**
     * Method to show the paths found from Bellman Ford Algorithm
     * and to show the final edge flow
     */
    public void minCostMaxFlow(){
        System.out.println( printMatrix("Capacity", capacity));
        System.out.println("WEIGHTED FLOW: ");
        FordFulkerson(source, sink);
        System.out.println();
        System.out.println("FINAL EDGE FLOW: ");
        finalEdgeFlow();
    }

    public static void main(String[] args) {
        String[] files = {"transport0.txt", "transport1.txt", "transport2.txt", "transport3.txt", "flow10.txt"};
        for (String fileName : files) {
            Graph graph = new Graph(fileName);
            graph.minCostMaxFlow();
        }
    }
}