package nl.dke.pursuitevasion.game.agents.impl.MinimalPath;

import nl.dke.pursuitevasion.game.Vector2D;
import nl.dke.pursuitevasion.game.agents.AbstractAgent;
import nl.dke.pursuitevasion.game.agents.AgentRequest;
import nl.dke.pursuitevasion.game.agents.tasks.MinimalPathGuardTask;
import nl.dke.pursuitevasion.map.impl.Floor;
import nl.dke.pursuitevasion.map.impl.Map;

import java.util.*;

import org.jgrapht.*;
import org.jgrapht.alg.interfaces.KShortestPathAlgorithm;
import org.jgrapht.alg.shortestpath.KShortestPaths;

import org.jgrapht.graph.DefaultWeightedEdge;
import org.jgrapht.graph.WeightedPseudograph;

/**
 * Created by Jan on 24-5-2017.
 * Oversees 3 MinimalPathAgents
 */
public class MinimalPathOverseer {
    // The instance of the singleton
    private static MinimalPathOverseer instance;
    // agents that are supervised by the overseer
    static List<MinimalPathAgent> agents = new ArrayList<>(3);
    // maps agents to guardpaths
    private HashMap<MinimalPathAgent, GraphPath> guardMap;

    private WeightedPseudograph<Vector2D, DefaultWeightedEdge> visibilityGraph;
    private Map map;
    private Vector2D u;
    private Vector2D v;
    private List<GraphPath<Vector2D, DefaultWeightedEdge>> paths;


    private MinimalPathOverseer(Map map){
        this.map = map;
        // Build visibility graph

        // TODO replace PLACEHOLDER LINE with the actual visibility graph.
        visibilityGraph = constructVisibilityGraph(map);

        // get u and v
        Vector2D[] uv = getFurthestPoints();
        this.u = uv[0];
        this.v = uv[1];
        // Calculate minimal paths between u and v
        paths = calculateMinimalPaths();
    }

    public List<GraphPath<Vector2D, DefaultWeightedEdge>> calculateMinimalPaths(){
        KShortestPathAlgorithm<Vector2D, DefaultWeightedEdge> a = new KShortestPaths<>(visibilityGraph, 2);
        return a.getPaths(u, v);
    }

    /**
     * Gets the 2 points furthest away from each other.
     * @return Vector2D[] : size 2 array holding the points
     */
    public Vector2D[] getFurthestPoints() {
        Floor floor = map.getFloors().iterator().next();
        Vector2D[] uv = new Vector2D[2];
        // iterate over all points
        // get the distance from point to all other points
        // keep points with biggest distance
        double maxDistance = 0;
        Collection<Vector2D> vectors = floor.getPolygon().getPoints();
        for(Vector2D u : vectors){
            for(Vector2D v : vectors){
                Double vectorDistance = u.distance(v);
                if(vectorDistance > maxDistance){
                    uv[0] = u;
                    uv[1] = v;
                    maxDistance = vectorDistance;
                }
            }
        }
        return uv;
    }
    private static WeightedPseudograph<Vector2D, DefaultWeightedEdge> constructVisibilityGraph(Map map) {
        WeightedPseudograph<Vector2D, DefaultWeightedEdge> g = new WeightedPseudograph<>(
                DefaultWeightedEdge.class);
        // simple polygon. So all verteces can see each other.
        Floor f = map.getFloors().iterator().next();
        Collection<Vector2D> points = f.getPolygon().getPoints();
        for (Vector2D point : points) {
            g.addVertex(point);
        }

        for (Vector2D point1 : points) {
            for (Vector2D point2: points) {
                g.addEdge(point1, point2);
                DefaultWeightedEdge e = g.getEdge(point1, point2);
                g.setEdgeWeight(e, point1.distance(point2));
            }
        }
        return g;

    }


    // The overseer is a Singleton
    public static MinimalPathOverseer getIntance(){
        if(instance == null){
            throw new NullPointerException("No instance created yet. Use the init function");
        }
        return instance;
    }

    // Creates the overseer singleton with a map.
    public static MinimalPathOverseer init(Map map){
        instance = new MinimalPathOverseer(map);
        return instance;
    }

    // Assigns agents 1 and 2 to the first 2 minimal paths.
    private void assignPaths(){
        guardMap.put(agents.get(0), paths.get(0));
        guardMap.put(agents.get(1), paths.get(1));
    }

    // Registers an agent with the overseer
    public int registerAgent(MinimalPathAgent agent){
        if(agents.size() < 3){
            agents.add(agent);
            guardMap.put(agent, null);
            if(agents.size() == 3){
                assignPaths();
            }
            return agents.size()-1;
        }
        else{
            throw new IllegalStateException("There are already 3 agents instantiated");
        }
    }

    // Checks whether an agent should be making a new request.
    public boolean getShouldDoSomething(MinimalPathAgent agent){
        // TODO: implement mechanism to determine whether an agent should make a new request
        return false;

    }

    // Determines what request an agent should make.
    public void getTask(MinimalPathAgent agent, AgentRequest request){
        request.add(new MinimalPathGuardTask(guardMap.get(agent), agent.getEvader()));
    }

}
