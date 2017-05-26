package nl.dke.pursuitevasion.game.agents.impl.MinimalPath;

import nl.dke.pursuitevasion.game.Vector2D;
import nl.dke.pursuitevasion.game.agents.AgentRequest;
import nl.dke.pursuitevasion.map.MapPolygon;
import nl.dke.pursuitevasion.map.impl.Floor;
import nl.dke.pursuitevasion.map.impl.Map;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Vector;

import org.jgrapht.*;
import org.jgrapht.alg.interfaces.KShortestPathAlgorithm;
import org.jgrapht.alg.shortestpath.KShortestPaths;

import org.jgrapht.graph.DefaultWeightedEdge;
import org.jgrapht.graph.UndirectedWeightedSubgraph;

/**
 * Created by Jan on 24-5-2017.
 */
public class MinimalPathOverseer {

    private static MinimalPathOverseer instance;
    private static List<MininalPathAgent> agents = new ArrayList<>(3);

    private MinimalPathOverseer(Map map){
        // Build visibility graph

        // TODO replace PLACEHOLDER LINE.
        //
        UndirectedWeightedSubgraph<Vector2D, DefaultWeightedEdge> visibilityGraph =
                new UndirectedWeightedSubgraph<Vector2D, DefaultWeightedEdge>(null);
        // get u and v
        Vector2D[] uv = getFurthestPoints(map);
        Vector2D u = uv[0];
        Vector2D v = uv[1];
        // Calculate Π1 between u and v
        KShortestPathAlgorithm<Vector2D, DefaultWeightedEdge> a = new KShortestPaths<Vector2D, DefaultWeightedEdge>(visibilityGraph, 3);
        List<GraphPath<Vector2D, DefaultWeightedEdge>> paths = a.getPaths(u, v);


    }

    /**
     * Gets the 2 points furthest away from each other.
     * @param map
     * @return Vector2D[] : size 2 array holding the points
     */
    private Vector2D[] getFurthestPoints(Map map) {
        // TODO fix ugly line
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

    })

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

    // Registers an agent with the overseer
    public static int registerAgent(MininalPathAgent agent){
        if(agents.size() <= 3){
            agents.add(agent);
            return agents.size()-1;
        }
        else{
            throw new IllegalStateException("There are already 3 agents instantiated");
        }
    }

    // Checks whether an agent should be making a new request.
    public boolean getShouldDoSomething(int agentNumber){}

    // Determines what request an agent should make.
    public void getTask(int agentNumber, AgentRequest request){}

}
