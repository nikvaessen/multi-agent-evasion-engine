package nl.dke.pursuitevasion.game.agents.impl.minimalPath;

import nl.dke.pursuitevasion.game.EngineConstants;
import nl.dke.pursuitevasion.game.MapInfo;
import nl.dke.pursuitevasion.game.Vector2D;
import nl.dke.pursuitevasion.game.agents.AgentRequest;
import nl.dke.pursuitevasion.game.agents.Direction;
import nl.dke.pursuitevasion.game.agents.tasks.MinimalPathGuardTask;
import nl.dke.pursuitevasion.game.agents.tasks.WalkToTask;
import nl.dke.pursuitevasion.map.impl.Floor;
import nl.dke.pursuitevasion.map.impl.Map;

import java.util.*;
import java.util.List;

import org.jgrapht.*;
import org.jgrapht.alg.interfaces.KShortestPathAlgorithm;
import org.jgrapht.alg.shortestpath.KShortestPaths;
import org.jgrapht.graph.DefaultWeightedEdge;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import static nl.dke.pursuitevasion.game.agents.impl.minimalPath.MinimalPathOverseerState.THIRD_AGENT_CHASING;

/**
 * Created by Jan on 24-5-2017.
 * Oversees 3 MinimalPathAgents
 */
public class MinimalPathOverseer
{

    /**
     * The logger of this class
     */
    private final static Logger logger = LoggerFactory.getLogger(MinimalPathOverseer.class);

    /**
     * The list of MinimalPathAgents this overseer controls. Currently designed
     * to hold 3 agents
     */
    private List<MinimalPathAgent> agents;

    /**
     * Maps the agents held in the agents list to the  paths the agents need to guard
     */
    private HashMap<MinimalPathAgent, GraphPath<Vector2D, DefaultWeightedEdge>> guardMap;

    /**
     * The complete floor the pursuit-evasion game is played out on.
     * During pursuit, smaller subfloors will be created as areas are guaranteed
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
     * The current state the overseer is in. This state determines the tasks the overseen agents are given
     */
    private MinimalPathOverseerState state;

    /**
     * This is a stack which contains increasingly smaller subfloors of the main polygon until
     * the evader is caught
     */
    private LinkedList<Floor> subFloors;

    /**
     * Create MinimalPathOverseer, which creates 3 agents it will control
     *
     * @param map           the map the agents will play pursuit-evasion in
     * @param startLocation the startLocation of the first agent on the floor of the map
     */
    public MinimalPathOverseer(Map map, Vector2D startLocation)
    {
        // Set the known member variables

        // Information about the environment
        this.completeFloor = map.getFloors().iterator().next();
        this.subFloors = new LinkedList<>();
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
                                                startLocation.add(new Vector2D(i * (2*radius + 1), 0)),
                                                Direction.NORTH, radius, EngineConstants.VISION_RANGE,
                                                EngineConstants.VISION_ANGLE,
                                                this, i));

            if(i == 0)
            {
                guardMap.put(agent, paths.get(0));
            }
            else
            {
                guardMap.put(agent, null);
            }
        }

        this.state = MinimalPathOverseerState.FIRST_AGENT_STABILISING;
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
    private Vector2D[] getFurthestPoints()
    {
        Floor floor = this.completeFloor;
        Vector2D[] uv = new Vector2D[2];
        // iterate over all points
        // get the distance from point to all other points
        // keep points with biggest distance
        double maxDistance = 0;
        Collection<Vector2D> vectors = floor.getPolygon().getPoints();
        for(Vector2D u : vectors)
        {
            for(Vector2D v : vectors)
            {
                Double vectorDistance = u.distance(v);
                if(vectorDistance > maxDistance)
                {
                    uv[0] = u;
                    uv[1] = v;
                    maxDistance = vectorDistance;
                }
            }
        }
        return uv;
    }


    /**
     * Determines whether an agent needs to currently do a task
     *
     * @param agent the agent to check for
     * @return true if the agent needs to do something, false otherwise
     */
    boolean getShouldDoSomething(MinimalPathAgent agent)
    {
        int agentNumber = agent.getAgentNumber();
        agent.resetHasNewRequest();
        this.state = determineOverseerState();
        switch(state)
        {
            case FIRST_AGENT_STABILISING:
                if(agentNumber == 0)
                {
                    return true;
                }
                else
                {
                    return false;
                }
            case SECOND_AGENT_STABILISING:
                if(agentNumber == 0 || agentNumber == 1)
                {
                    return true;
                }
                else
                {
                    return false;
                }
            default:
                    return true;
        }
    }

    /**
     * Completes the request for a given agent
     *
     * @param agent the agent to complete the request for
     * @param request the request to complete
     * @param mapInfo the current state of the environment
     */
    void getTask(MinimalPathAgent agent, AgentRequest request, MapInfo mapInfo)
    {
        int agentNumber = agent.getAgentNumber();
        this.state = determineOverseerState();
        logger.trace("in state: {}", this.state);
        switch(state)
        {
            case FIRST_AGENT_STABILISING:
                if(agentNumber == 0)
                {
                    giveMinimalPathGuardTask(agent, request, mapInfo);
                }
                break;
            case SECOND_AGENT_STABILISING:
                if(agent.getAgentNumber() == 0 || agent.getAgentNumber() == 1)
                {
                    giveMinimalPathGuardTask(agent, request, mapInfo);
                }
                break;
            case THIRD_AGENT_EVICTING:
                if(agent.getAgentNumber() == 0 || agent.getAgentNumber() == 1)
                {
                    giveMinimalPathGuardTask(agent, request, mapInfo);
                }
                else
                {
                    //giveEvictTask(agent, request, mapInfo);
                }
                break;
            case THIRD_AGENT_CHASING:
                if(agent.getAgentNumber() == 0 || agent.getAgentNumber() == 1)
                {
                    giveMinimalPathGuardTask(agent, request, mapInfo);
                }
                else
                {
                    //giveCatchTask(agent, request, mapInfo);
                }
                break;
        }
    }

    /**
     * Assign the given agent the task to guard its path
     *
     * @param agent the agent to give the task to
     * @param request the request used to give the tasl
     * @param mapInfo the information about the current environment
     */
    private void giveMinimalPathGuardTask(MinimalPathAgent agent, AgentRequest request, MapInfo mapInfo)
    {
        int agentNumber = agent.getAgentNumber();
        GraphPath<Vector2D, DefaultWeightedEdge> path = guardMap.get(agent);
        if(path == null)
        {
            logger.trace("Agent {} does not have a path", agentNumber + 1);
            return;
        }
        Vector2D evader = mapInfo.getAgentPoints().get(0);
        logger.trace("Giving agent {} MinimalPathGuardTask", agentNumber + 1);
        request.add(new MinimalPathGuardTask(path, evader));
    }

    /**
     * Give the task which will catch the evader given that there are no holes in the environment
     *
     * @param agent the agent to give the task to
     * @param request the request to fill
     * @param mapInfo the information about the environment
     */
    private void giveCatchTask(MinimalPathAgent agent, AgentRequest request, MapInfo mapInfo)
    {
        Vector2D evaderLocation = mapInfo.getAgentPoints().get(0);
        request.add(new WalkToTask(evaderLocation, true));
    }

    /**
     * Give the task which will evict the evader from a polygon with holes or catch it
     *
     * @param agent the agent to give the task to
     * @param request the request to fill
     * @param mapInfo the information about the environment
     */
    private void giveEvictTask(MinimalPathAgent agent, AgentRequest request, MapInfo mapInfo)
    {
        //TODO implement
    }

    /**
     * Get the current paths guarded by agents
     *
     * @return a list of GraphPaths which are currently being guarded by agents
     */
    public List<GraphPath<Vector2D, DefaultWeightedEdge>> getPaths()
    {
        return paths;
    }

    /**
     * Determine in which state the oversees is.
     *
     * @return the current state of the overseer
     */
    private MinimalPathOverseerState determineOverseerState()
    {
        switch(state)
        {
            case FIRST_AGENT_STABILISING:
                MinimalPathAgent agent1 = getAgent(0);
                if(MinimalPathAgentState.ON_PROJECTION.equals(agent1.getState()))
                {
                    enterSecondAgentStabilisingStage();
                    return MinimalPathOverseerState.SECOND_AGENT_STABILISING;
                }
                else
                {
                    return MinimalPathOverseerState.FIRST_AGENT_STABILISING;
                }
            case SECOND_AGENT_STABILISING:
                MinimalPathAgent agent2 = getAgent(1);
                if(MinimalPathAgentState.ON_PROJECTION.equals(agent2.getState()))
                {
                    return decideThirdAgentState();
                }
                else
                {
                    return MinimalPathOverseerState.SECOND_AGENT_STABILISING;
                }
            case THIRD_AGENT_CHASING:
                return THIRD_AGENT_CHASING;
            case THIRD_AGENT_EVICTING:
                if(checkEvictingSuccessful())
                {
                    giveNewRoles();
                    return MinimalPathOverseerState.FIRST_AGENT_STABILISING;
                }
                else
                {
                    return MinimalPathOverseerState.THIRD_AGENT_EVICTING;
                }
            default:
                    throw new IllegalStateException("MinimalPathOverseer is in an illegal state");
        }
    }

    /**
     * Determine whether the third agent should chase or evict the evader
     *
     * @return the current state the oversees is in
     */
    private MinimalPathOverseerState decideThirdAgentState()
    {
        return THIRD_AGENT_CHASING; //todo determine this
    }

    /**
     * Check whether the third agent successfully evicted the agent

     * @return true if the third agent managed to evict, false otherwise
     */
    private boolean checkEvictingSuccessful()
    {
        return true; //todo determine this
    }

    /**
     * Shuffle the new roles after third agent succesfully evicted the agent. The third agent most likely
     * will become the first agent.
     */
    private void giveNewRoles()
    {
        //todo determine which agents become 1, 2 and 3
    }

    /**
     * Modify the currentFloor and its visibility graph such that the firsy agent will stabilize on the shortest path
     */
    private void enterFirstAgentStabilisingStage()
    {

    }

    /**
     * Modify the currentFloor and its visibility graph such that the second agent will stabilize
     * on the 2nd shortest path
     */
    private void enterSecondAgentStabilisingStage()
    {
        // when the first agent has stabilised on the projection, we can determine
        // in which subPolygon created by the division of the path of the first agent
        // the evader is in
        MinimalPathAgent agent1 = agents.get(0);
        GraphPath<Vector2D, DefaultWeightedEdge> path = guardMap.get(agent1);
        ArrayList<Floor> subFloors = completeFloor.getSubFloors(path);
        Vector2D evaderLocation = agent1.getMapInfo().getAgentPoints().get(0);
        logger.trace("Size of subfloors: {}", subFloors.size());
        for(Floor f: subFloors)
        {
            if(f.contains(evaderLocation))
            {
                logger.trace("Evader is in subfloor {}", f.toString());
                this.subFloors.push(f);
                break;
            }
        }

        // The visibility graph now becomes the graph of the new subfloor
        this.visibilityGraph = this.subFloors.peek().getVisibilityGraph();
        // recalculate the 3 shortest path and give the second shortest-path to agent 2
        this.paths = calculateMinimalPaths(this.visibilityGraph, 3, this.u, this.v);
        this.guardMap.put(this.getAgent(1), paths.get(1));
    }

}
