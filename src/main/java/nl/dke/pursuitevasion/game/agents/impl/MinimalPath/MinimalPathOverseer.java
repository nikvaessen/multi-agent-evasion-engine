package nl.dke.pursuitevasion.game.agents.impl.MinimalPath;

import nl.dke.pursuitevasion.game.EngineConstants;
import nl.dke.pursuitevasion.game.MapInfo;
import nl.dke.pursuitevasion.game.Vector2D;
import nl.dke.pursuitevasion.game.agents.AbstractAgent;
import nl.dke.pursuitevasion.game.agents.AgentRequest;
import nl.dke.pursuitevasion.game.agents.Direction;
import nl.dke.pursuitevasion.game.agents.tasks.MinimalPathGuardTask;
import nl.dke.pursuitevasion.game.agents.tasks.WalkToTask;
import nl.dke.pursuitevasion.map.MapPolygon;
import nl.dke.pursuitevasion.map.impl.Floor;
import nl.dke.pursuitevasion.map.impl.Map;

import java.awt.*;
import java.nio.file.Path;
import java.util.*;
import java.util.List;

import org.jgrapht.*;
import org.jgrapht.alg.interfaces.KShortestPathAlgorithm;
import org.jgrapht.alg.shortestpath.KShortestPaths;
import org.jgrapht.alg.shortestpath.PathValidator;
import org.jgrapht.graph.DefaultWeightedEdge;
import org.jgrapht.graph.Subgraph;
import org.jgrapht.graph.WeightedPseudograph;

/**
 * Created by Jan on 24-5-2017.
 * Oversees 3 MinimalPathAgents
 */
public class MinimalPathOverseer {

    /**
     * The list of MinimalPathAgents this overseer controls. Currently designed
     * to hold 3 agents
     */
    private List<MinimalPathAgent> agents;

    /**
     * Maps the agents held in the agents list to the  paths the agents need to guard
     */
    private HashMap<MinimalPathAgent, GraphPath> guardMap;

    /**
     * The complete floor the pursuit-evasion game is played out on.
     * During pursuit, smaller subfloors will be created as areas are guarenteed
     * wherein the evader cannot escape from
     */
    private Floor completeFloor;

    /**
     * The visibility graph of the floor which is used to compute the paths
     * agents will need to guard
     */
    private WeightedGraph<Vector2D, DefaultWeightedEdge> visibilityGraph;

    /**
     * At the start of pursuit, 2 anchor points u and v are selected. They are the 2 points on the floor
     * lying the most distance away from each other
     * These points are used to compute the initial path(s)
     */
    private Vector2D u;
    private Vector2D v;

    /**
     * Current paths selected by the algorithm to be needed to be guarded
     */
    private List<GraphPath<Vector2D, DefaultWeightedEdge>> paths;

    /**
     * Create MinimalPathOverseer, which creates 3 agents it will control
     *
     * @param map the map the agents will play pursuit-evasion in
     * @param startLocation the startLocation of the first agent on the floor of the map
     */
    public MinimalPathOverseer(Map map, Vector2D startLocation)
    {
        // Set the known member variables

        // Information about the environment
        this.completeFloor = map.getFloors().iterator().next();
        this.visibilityGraph = map.getFloors().iterator().next().getVisibilityGraph();

        // The anchor points u and v
        Vector2D[] uv = getFurthestPoints();
        this.u = uv[0];
        this.v = uv[1];

        // Calculate the initial shortest minimal path between u and v
        this.paths = calculateMinimalPaths(this.visibilityGraph, 1, this.u, this.v);

        // Initialize the agents and set the first agent to guard the first path
        this.guardMap = new HashMap<>();
        this.agents = new LinkedList<>();

        for(int i = 0; i < 3; i++)
        {
            int radius = EngineConstants.AGENT_RADIUS;
            MinimalPathAgent agent;
            agents.add(agent =
                    new MinimalPathAgent(map, this.completeFloor,
                            startLocation.add(new Vector2D(i * (radius + 1), 0)),
                            Direction.NORTH, radius, EngineConstants.VISION_RANGE, EngineConstants.VISION_ANGLE,
                            this, i));

            if (i == 0)
            {
                guardMap.put(agent, paths.get(0));
            }
            else
            {
                guardMap.put(agent, null);
            }
        }
    }

    /**
     * Get the amount of agents controlled by this overseer
     *
     * @return the amount of agents controlled by this overseer
     */
    public int getAmountOfAgents()
    {
        return agents.size();
    }

    /**
     * Get an agent controlled by this overseer
     *
     * @param i number of agent
     * @return the ith agent controlled by this overseer
     */
    public MinimalPathAgent getAgent(int i)
    {
        if(i < 0 || i >= agents.size())
        {
            throw new IllegalArgumentException(String.format("Agent number %d does not exist", i));
        }
        return agents.get(i);
    }

    /**
     * Calculate k shortest paths on a given graph with weighted edges between u and v
     *
     * @param g the graph
     * @param k the amount of paths requested
     * @param u start location of the paths
     * @param v the end location of the paths
     * @return a list with k paths between u and v, which are ordered by shortest distance to longest distance
     */
    private List<GraphPath<Vector2D, DefaultWeightedEdge>> calculateMinimalPaths(Graph<Vector2D, DefaultWeightedEdge> g,
                                                                                 int k, Vector2D u, Vector2D v)
    {
        KShortestPathAlgorithm<Vector2D, DefaultWeightedEdge> algorithm = new KShortestPaths<>(g, k);
        return algorithm.getPaths(u, v);
    }

    /**
     * Gets the 2 points furthest away from each other on the completeFloor
     *
     * @return size 2 array holding the points
     */
    private Vector2D[] getFurthestPoints() {
        Floor floor = this.completeFloor;
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
            return agents.size();
        }
        else{
            throw new IllegalStateException("There are already 3 agents instantiated");
        }
    }

    // Checks whether an agent should be making a new request.
    public boolean getShouldDoSomething(MinimalPathAgent agent){
        // check whether the agent already has an assigned path.
    /*    GraphPath<Vector2D, DefaultWeightedEdge> path = guardMap.get(agent);
        if(path == null){

        }*/
        // TODO: implement mechanism to determine whether an agent should make a new request
        return true;

    }

    // Determines what request an agent should make.
    public void getTask(MinimalPathAgent agent, AgentRequest request, MapInfo mapInfo){
        // TODO Make agents guard their path for a minimum amount of iterations.
        GraphPath<Vector2D, DefaultWeightedEdge> path = guardMap.get(agent);
        if(mapInfo.getAgentPoints().size() > 0){
            Vector2D evader = mapInfo.getAgentPoints().get(0);
            if(agent.getAgentNumber() == 3){
                request.add(new WalkToTask(evader));
            }
            if(path != null){
                request.add(new MinimalPathGuardTask(path, evader));
            }

        }
    }

    private void getTaskInternal(MinimalPathAgent agent, AgentRequest request, MapInfo mapInfo)
    {

    }


}
