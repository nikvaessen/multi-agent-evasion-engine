package nl.dke.pursuitevasion.game.agents.impl.minimalPath;

import nl.dke.pursuitevasion.game.EngineConstants;
import nl.dke.pursuitevasion.game.MapInfo;
import nl.dke.pursuitevasion.game.Vector2D;
import nl.dke.pursuitevasion.game.agents.AgentRequest;
import nl.dke.pursuitevasion.game.agents.Direction;
import nl.dke.pursuitevasion.game.agents.tasks.MinimalPathGuardTask;
import nl.dke.pursuitevasion.game.agents.tasks.WalkToTask;
import nl.dke.pursuitevasion.map.MapPolygon;
import nl.dke.pursuitevasion.map.impl.Floor;
import nl.dke.pursuitevasion.map.impl.Map;

import java.util.*;
import java.util.List;
import java.util.logging.LogManager;

import org.apache.commons.collections4.queue.CircularFifoQueue;
import org.apache.log4j.Level;
import org.jgrapht.*;
import org.jgrapht.alg.interfaces.KShortestPathAlgorithm;
import org.jgrapht.alg.shortestpath.KShortestPaths;
import org.jgrapht.graph.*;
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
     * This is the SubFloor containing the evader;
     * It is updated every time a new path is guarded.
     */
    public Floor Pe;

    /**
     * Create MinimalPathOverseer, which creates 3 agents it will control
     *
     * @param map           the map the agents will play pursuit-evasion in
     */
    public MinimalPathOverseer(Map map)
    {
        Vector2D startLocation = map.getPursuerSpawnLocation();
        // Set the known member variables

        // Information about the environment
        this.completeFloor = map.getFloors().iterator().next();
        this.subFloors = new LinkedList<>();
        this.visibilityGraph = map.getFloors().iterator().next().getVisibilityGraph();
        this.Pe = completeFloor;

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
                                                startLocation,
                                                Direction.NORTH, radius, EngineConstants.VISION_RANGE,
                                                EngineConstants.VISION_ANGLE,
                                                this, i));

            if(i == 0)
            {
                GraphPath<Vector2D, DefaultWeightedEdge> path = paths.get(0);
                agent.setState(MinimalPathAgentState.MOVING_TO_PATH);
                guardMap.put(agent, path);
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
        return true;
//        MinimalPathAgentState state = agent.getState();
//        if(state == MinimalPathAgentState.MOVING_TO_PROJECTION || state == MinimalPathAgentState.MOVING_TO_PATH){
//            return true;
//        }
//        // Check whether the situation is "stable" i.e. the currently assigned paths are being guarded
//        // Nothing should be done until the situation is stable.
//        {
//            if(!situationStable(agent)){return false;}
//        }
//
//        // check whether the agent is doing anything
//        if(state == MinimalPathAgentState.NO_PATH){
//            return true;
//        }
//
//
//        GraphPath agentPath = guardMap.get(agent);
//        // check if agent path bounds Pe
//        boolean bounds = boundsPe(agentPath);
//        // if agent bounds or is chasing -> return false
//        // else return true;
//        return !bounds;


        /*
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
        }*/
    }

    /**
     * Determines whether the current situation is "stable"
     * Agents should consider a situation stable when all other agents are done moving
     * or when they themselves are causing the "instability"
     * @return Whether the situation is stable for this agent
     */
    private boolean situationStable() {
        for (MinimalPathAgent minimalPathAgent : agents) {
            MinimalPathAgentState state = minimalPathAgent.getState();
            if(state == MinimalPathAgentState.MOVING_TO_PATH || state == MinimalPathAgentState.MOVING_TO_PROJECTION){
                return false;
            }
        }
        return true;
    }

    /**
     * Checks whether a given path bounds Pe (i.e if every edge of the path is on Pe
     * @param agentPath path of an agent
     * @return boolean indicating whether this path bounds Pe
     */
    private boolean boundsPe(GraphPath<Vector2D, DefaultWeightedEdge> agentPath) {
        SimpleGraph<Vector2D, DefaultEdge> polygonGraph = Pe.getPolygonGraph();
        List<DefaultWeightedEdge> pathVertexes = agentPath.getEdgeList();
        Graph<Vector2D, DefaultWeightedEdge> graph = agentPath.getGraph();

        for (DefaultWeightedEdge pathEdge : pathVertexes) {
            Vector2D source = graph.getEdgeSource(pathEdge);
            Vector2D target = graph.getEdgeTarget(pathEdge);
            // check if there is an edge in the polygon for the edge in the path.
            Set<DefaultEdge> edges = polygonGraph.getAllEdges(source, target);
            if(edges == null){
                // vertex(es) in path do not exist in Pe
                // therefore path cannot bound Pe
                return false;
            }
            if(edges.size() == 0){
                // Pe does not have an edge between source and target in path
                return false;
            }
        }
        return true;
    }

    private boolean boundsPe(MinimalPathAgent agent){
        GraphPath<Vector2D, DefaultWeightedEdge> path = guardMap.get(agent);
        return path != null && boundsPe(path);
//        if(path == null){
//            return false;
//        }
//        return boundsPe(path);


    }

    private CircularFifoQueue<MinimalPathAgent> boundingAgents = new CircularFifoQueue<>(2);

    /**
     * Completes the request for a given agent
     *
     * @param agent the agent to complete the request for
     * @param request the request to complete
     * @param mapInfo the current state of the environment
     */
    void getTask(MinimalPathAgent agent, AgentRequest request, MapInfo mapInfo)
    {
        Vector2D evaderLocation = mapInfo.getAgentPoints().get(0);
        // redetermine Pe
        Pe = recalculatePe(evaderLocation);
//        if(!Pe.contains(evaderLocation)){
//            logger.debug("recalculating Pe");
//        }

        // check if we are bounding or if the situation does not allow us to stop guarding
        if((isBounding(agent)|| !situationStable()) && (agent.getState() != MinimalPathAgentState.NO_PATH && agent.getState() != MinimalPathAgentState.CHASING))
        // if we are -> continue doing that
        {
            continueGuarding(agent, request, mapInfo);
        }
        else{
            // if we are not and situation is stable -> either shrink or evict
            if(situationStable()){
                // if holes -> Make agent guard new path in Pe
                // if no holes -> Evict E
                if(Pe.getObstacles().size() > 0 && !Pe.isSimple()){
                    // shrink
                    logger.debug("shrinking with agent {}", agent.getAgentNumber());
                    shrink(agent, request, mapInfo);
                }
                else{
                    // evict
                    logger.debug("evicting with agent {}", agent.getAgentNumber());
                    evict(agent, request, mapInfo);
                    return;
                }
            }
            // unless you are already chasing
            if(agent.getState() == MinimalPathAgentState.CHASING){
                evict(agent, request, mapInfo);
            }
        }
    }

    private Floor recalculatePe(Vector2D evaderLocation) {
        // Pe is no longer valid
        // determine the bounding paths
//        List<GraphPath<Vector2D, DefaultWeightedEdge>> boundingPaths = getBoundingPaths();
//        if(boundingPaths.size()==3){boundingPaths = getPathsBoundingPe();}

        List<GraphPath<Vector2D, DefaultWeightedEdge>> boundingPaths = getBoundingPaths();
        if(boundingPaths.size() == 0){
            return completeFloor;
        }
        else if(boundingPaths.size() == 1){
            GraphPath<Vector2D, DefaultWeightedEdge> path = boundingPaths.get(0);
            return pruneFloor(path, evaderLocation, completeFloor);
        }
        else if(boundingPaths.size()== 2){
            GraphPath<Vector2D, DefaultWeightedEdge> path1 = boundingPaths.get(0);
            GraphPath<Vector2D, DefaultWeightedEdge> path2 = boundingPaths.get(1);

            MapPolygon boundingPolygon = joinPathsToPolygon(path1, path2);
            // this means that Pe is bound by these 2 paths
            if(boundingPolygon.contains(evaderLocation)){
                // therefore we can prune the complete floor with these paths sequentually to get Pe
                return pruneFloor(path2, evaderLocation, pruneFloor(path1, evaderLocation, completeFloor));
            }
            // Otherwise prune the floor apart from each other and pick the smallest one.
            Floor f1 = pruneFloor(path1, evaderLocation, completeFloor);
            Floor f2 = pruneFloor(path2, evaderLocation, completeFloor);

            return f1.getPolygon().getArea() < f2.getPolygon().getArea() ? f1 : f2;

        }
        else if(boundingPaths.size() == 3){
            List<Floor> potentialPes = getPotentialPes(boundingPaths, evaderLocation);
            double minArea = Double.MAX_VALUE;
            Floor smallestFloor = null;
            for (Floor potentialPe : potentialPes) {
                double area = potentialPe.getPolygon().getArea();
                if(area < minArea){
                    smallestFloor = potentialPe;
                    minArea = area;
                }
            }
            return smallestFloor;
        }

        throw(new IllegalStateException("Invalid amount of paths "));

    }

    private List<Floor> getPotentialPes(List<GraphPath<Vector2D, DefaultWeightedEdge>> paths, Vector2D evaderLocation) {
        List<Floor> PeCandidates = new ArrayList<>();

        for(int i = 0 ; i < paths.size(); i ++){
            for(int j = i+1 ; j < paths.size(); j ++){
                GraphPath<Vector2D, DefaultWeightedEdge> path1 = paths.get(i);
                GraphPath<Vector2D, DefaultWeightedEdge> path2 = paths.get(j);
                try{
                    Floor f = pruneFloor(path1, evaderLocation, pruneFloor(path2, evaderLocation, Pe));
                    PeCandidates.add(f);
                }
                catch (IllegalStateException e){
                    // lol
                }
            }
        }
        return PeCandidates;
    }


    /**
     * gets the paths that are stable
     * @return list of stably guarded paths
     */
    private List<GraphPath<Vector2D, DefaultWeightedEdge>> getGuardedPaths() {
        List<GraphPath<Vector2D, DefaultWeightedEdge>> guardedPaths = new ArrayList<>();
        for (MinimalPathAgent minimalPathAgent : guardMap.keySet()) {
            MinimalPathAgentState state = minimalPathAgent.getState();
            if(state == MinimalPathAgentState.ON_PROJECTION || state == MinimalPathAgentState.MOVING_TO_PROJECTION){
                GraphPath<Vector2D, DefaultWeightedEdge> path = guardMap.get(minimalPathAgent);
                if (path != null) {
                    guardedPaths.add(path);
                }
            }
        }
        return guardedPaths;
    }


/*
    private Floor determinePe(Vector2D evaderLocation) {
        // get the bounding paths
        List<GraphPath<Vector2D, DefaultWeightedEdge>> boundingPaths = getBoundingPaths();

        // if there is only one
        if(paths.size() == 1){
            // split Pe based on that path
            pruneFloor(paths.get(1), evaderLocation);
        }

        else{
            // 3 cases
            // Pe is bounded by the paths

            // Pe is bounded by path 1 and the polygon
            // Pe is bounded by path 2 and the polygon

        }

    }*/

    private boolean isBounding(MinimalPathAgent agent) {
        // check if our path is bounding (i.e. entirely on Pe)
        return boundsPe(agent);
    }

    private void continueGuarding(MinimalPathAgent agent, AgentRequest request, MapInfo mapInfo) {
        Vector2D evaderLocation = mapInfo.getAgentPoints().get(0);
        GraphPath<Vector2D, DefaultWeightedEdge> path = guardMap.get(agent);
        request.add(new MinimalPathGuardTask(path, evaderLocation));
    }

    private void evict(MinimalPathAgent agent, AgentRequest request, MapInfo mapInfo) {
        Vector2D evaderLocation = mapInfo.getAgentPoints().get(0);
        agent.setState(MinimalPathAgentState.CHASING);
        request.add(new WalkToTask(evaderLocation, true));
    }

    private void shrink(MinimalPathAgent agent, AgentRequest request, MapInfo mapInfo) {
        Vector2D evaderLocation = mapInfo.getAgentPoints().get(0);
        Graph<Vector2D, DefaultWeightedEdge> vGraph = Pe.getVisibilityGraph();
        getNewUV(evaderLocation);
        int index = 1;
        //if Pe is bounded by 2 paths get the third shortest path
        if(peBoundedBy2OtherPaths(agent)){
            index = 2;
        }
        KShortestPaths<Vector2D, DefaultWeightedEdge> k = new KShortestPaths<>(vGraph, index+1);
        List<GraphPath<Vector2D, DefaultWeightedEdge>> graphPaths = k.getPaths(u, v);
        GraphPath<Vector2D,DefaultWeightedEdge> path = graphPaths.get(index);
        if(alreadyGuarded(path)){
            path = graphPaths.get(0);
        }

        boundingAgents.add(agent);
        guardMap.put(agent, path);
        // TODO recalculate Pe when u or v change so there are no "handles" on the polygon
        getNewUV(evaderLocation);
        Floor newPe;
        try {
            newPe = pruneFloor(path, evaderLocation, Pe);
        }
        // Pe does not contain E anymore
        catch (IllegalStateException e){
            newPe = recalculatePe(evaderLocation);
        }
        Pe = newPe;
        // Setting the state here to make the situation "unstable"
        agent.setState(MinimalPathAgentState.MOVING_TO_PATH);
        boundingAgents.add(agent);
        request.add(new MinimalPathGuardTask(path, evaderLocation));
    }

    private void recalculatePaths() {
        // U and V have changed therefore we can remove the prefixes of the paths
        for (MinimalPathAgent agent : guardMap.keySet()) {
            // if path contains the new U and V
            GraphPath<Vector2D, DefaultWeightedEdge> path = guardMap.get(agent);
            if (path != null) {
                List<Vector2D> vertexList = path.getVertexList();
                if(vertexList.contains(u) && vertexList.contains(v)){
                    KShortestPaths<Vector2D, DefaultWeightedEdge> k = new KShortestPaths<>(createPathGraph(path), 1);
                    GraphPath<Vector2D, DefaultWeightedEdge> newPath = k.getPaths(u, v).get(0);
                    guardMap.put(agent, newPath);
                }
            }
        }
    }

    private Graph<Vector2D, DefaultWeightedEdge> createPathGraph(GraphPath<Vector2D, DefaultWeightedEdge> path) {
        Graph<Vector2D, DefaultWeightedEdge> pathGraph = path.getGraph();
        SimpleWeightedGraph<Vector2D, DefaultWeightedEdge> g = new SimpleWeightedGraph<>(DefaultWeightedEdge.class);
        // Add all vertexes to the graph
        for (Vector2D vertex : path.getVertexList()) {
            g.addVertex(vertex);
        }
        // Add all the edges to the graph
        for (DefaultWeightedEdge edge : path.getEdgeList()) {
            g.addEdge(pathGraph.getEdgeSource(edge), pathGraph.getEdgeTarget(edge),edge);
        }
        return g;
    }

    private boolean alreadyGuarded(GraphPath<Vector2D, DefaultWeightedEdge> path) {
        for (GraphPath<Vector2D, DefaultWeightedEdge> guardedPath : getBoundingPaths()) {
            if(samePath(guardedPath, path)){
                return true;
            }
        }
        return false;
    }

    private boolean samePath(GraphPath<Vector2D, DefaultWeightedEdge> guardedPath, GraphPath<Vector2D, DefaultWeightedEdge> path){
        // check length
        List<Vector2D> guardVertexes = guardedPath.getVertexList();
        List<Vector2D> pathVertexes = path.getVertexList();
        if(guardVertexes.size() != pathVertexes.size()){
            return false;
        }
        // check al vertexes
        for (int i =0; i < guardVertexes.size(); i++) {
            if(!guardVertexes.get(i).equals(pathVertexes.get(i))){
                return false;
            }
        }
        return true;
    }

    private boolean peBoundedBy2OtherPaths(MinimalPathAgent agent) {
        Collection<Vector2D> points = Pe.getPolygon().getPoints();
        List<GraphPath<Vector2D, DefaultWeightedEdge>> guardPaths = new ArrayList<>(guardMap.values());
        guardPaths.remove(guardMap.get(agent));
        GraphPath<Vector2D, DefaultWeightedEdge> p1 = guardPaths.get(0);
        GraphPath<Vector2D, DefaultWeightedEdge> p2 = guardPaths.get(1);
        if(p1 == null || p2 == null){return false;}

        List<Vector2D> path1 = p1.getVertexList();
        List<Vector2D> path2 = p2.getVertexList();

        for (Vector2D point : points) {
            if(!path1.contains(point) && !path2.contains(point)){
                return false;
            }
        }
        return true;

    }

    /**
     * Calculates new values for u and v for the given paths.
     */
    private void getNewUV(Vector2D evaderLocation) {
        // get paths of the bounding agents
        if(boundingAgents.size() < 2){return;}

        List<GraphPath<Vector2D, DefaultWeightedEdge>> guardPaths = getPathsBoundingPe();
        if(guardPaths.size() < 2){return;}
        GraphPath<Vector2D, DefaultWeightedEdge> path1 = guardPaths.get(0);
        GraphPath<Vector2D, DefaultWeightedEdge> path2 = guardPaths.get(1);

        // check if the evader is in the polygon created by the 2 paths
        MapPolygon pathFloor = joinPathsToPolygon(path1, path2);
        // Do not move u or v
        if(!pathFloor.contains(evaderLocation)){return;}

        ArrayList<Floor> subFloors = Pe.getSubFloors(path1);
        if(has2BoundingPaths(subFloors.get(0), subFloors.get(1), path1, path2, evaderLocation) == 0){return;}


        if(!path1.getStartVertex().equals(path2.getStartVertex())){throw new IllegalArgumentException("paths do not share a starting vertex");}
        if(!path1.getEndVertex().equals(path2.getEndVertex())){throw new IllegalArgumentException("paths do not share an end vertex");}
        // move U until paths go a different way
        Vector2D newU = findPathSplit(path1, path2, u);
        Vector2D newV = findPathSplit(path1, path2, v);
        if(!u.equals(newU)){
            logger.debug("u changed from {} to {}", u, newU);
            u = newU;
        }
        if(!v.equals(newV)){
            logger.debug("v changed from {} to {}", v, newV);
            v = newV;
        }


    }

    private List<GraphPath<Vector2D, DefaultWeightedEdge>> getBoundingPaths() {
        List<GraphPath<Vector2D, DefaultWeightedEdge>> paths = new ArrayList<>(guardMap.values());
        paths.removeIf(Objects::isNull);
        return paths;
    }

    private List<GraphPath<Vector2D, DefaultWeightedEdge>> getPathsBoundingPe(){
        List<GraphPath<Vector2D, DefaultWeightedEdge>> boundingPaths = getBoundingPaths();
        boundingPaths.removeIf(g -> !boundsPe(g));
        return boundingPaths;

    }

    private MapPolygon joinPathsToPolygon(GraphPath<Vector2D, DefaultWeightedEdge> path1, GraphPath<Vector2D, DefaultWeightedEdge> path2) {
        // check if path starts and ends are the same.
        if(!path1.getStartVertex().equals(path2.getStartVertex())){throw new IllegalArgumentException("paths do not share a starting vertex");}
        if(!path1.getEndVertex().equals(path2.getEndVertex())){throw new IllegalArgumentException("paths do not share an end vertex");}
        Vector2D start = path1.getStartVertex();
        Vector2D end = path1.getEndVertex();
        List<Vector2D> path1VertexList = path1.getVertexList();
        List<Vector2D> path2VertexList = path2.getVertexList();
        // remove common vertexes
        reduceCommonVertexes(path1VertexList, path2VertexList, start, end);

        MapPolygon p = new MapPolygon();
        // walk one of the paths excluding the end vertex
        for (Vector2D vector : path1VertexList) {
            if(!vector.equals(end)){
                p.addPoint(vector);
            }
        }
        // walk the other path in reverse while excluding the original start vertex
        Collections.reverse(path2VertexList);
        for (Vector2D vector : path2VertexList) {
            if(!vector.equals(start)){
                p.addPoint(vector);
            }
        }
        return p;


    }

    private void reduceCommonVertexes(List<Vector2D> path1, List<Vector2D> path2, Vector2D start, Vector2D end) {

        try{

            Vector2D startSplit = findPathSplit(path1, path2, start);
            Vector2D endSplit =  findPathSplit(path1, path2, end);
            if(startSplit != start && endSplit != end) {
                path1.retainAll(path1.subList(path1.indexOf(startSplit), path1.indexOf(endSplit)+1));
                path2.retainAll(path2.subList(path2.indexOf(startSplit), path2.indexOf(endSplit)+1));
            }
        }
        //
        catch (IllegalArgumentException e){
            logger.debug("paths are identical");
        }
    }

    private Vector2D findPathSplit(GraphPath<Vector2D, DefaultWeightedEdge> path1, GraphPath<Vector2D, DefaultWeightedEdge> path2, Vector2D start) {
        return findPathSplit(path1.getVertexList(), path2.getVertexList(), start);
    }

    private Vector2D findPathSplit(List<Vector2D> path1, List<Vector2D> path2, Vector2D start) {
        Vector2D lastEdge = start;
        // walk the paths from start until they split
        List<Vector2D> path1Vertexes = new ArrayList<>(path1);
        List<Vector2D> path2Vertexes = new ArrayList<>(path2);

        if(path1Vertexes.indexOf(lastEdge) != 0){
            Collections.reverse(path1Vertexes);
        }
        if(path2Vertexes.indexOf(lastEdge) != 0){
            Collections.reverse(path2Vertexes);
        }
        for (int i = 0; i < path1Vertexes.size(); i++) {
            if(!path1Vertexes.get(i).equals(path2Vertexes.get(i))){
                return lastEdge;
            }
            else{lastEdge = path1Vertexes.get(i);}
        }
        throw new IllegalArgumentException("The two paths are identical");
    }

    private Floor pruneFloor(GraphPath<Vector2D, DefaultWeightedEdge> path, Vector2D evaderLocation, Floor toPrune) {
//        if(!boundsPe(path)) {
//            path = removePathEdgesOnPe(path);
//        }
        List<Floor> floors = new ArrayList<>(toPrune.getSubFloors(path));

        // both floors contain the evader
        if(floors.size() == 2 && floors.get(0).contains(evaderLocation) && floors.get(1).contains(evaderLocation))
        {
            // return the smaller floor
            return floors.get(0).getPolygon().getArea() < floors.get(1).getPolygon().getArea() ? floors.get(0) : floors.get(1);
        }

        for (Floor floor : floors){
            boolean contains = floor.contains(evaderLocation);
            if(contains){
                return floor;
            }
        }
        throw(new IllegalStateException("Evader is in none of the subFloors"));
    }

    private GraphPath<Vector2D, DefaultWeightedEdge> removePathEdgesOnPe(GraphPath<Vector2D, DefaultWeightedEdge> path) {
        SimpleGraph<Vector2D, DefaultEdge> polygonGraph = Pe.getPolygonGraph();
        Graph<Vector2D, DefaultWeightedEdge> pathGraph = createPathGraph(path);
        // walk the path from start as long as the edges are shared
        Vector2D startVertex = path.getStartVertex();
        List<Vector2D> vertexList = path.getVertexList();
        for (int i = 1; i < vertexList.size() - 1; i++) {
            Vector2D newVertex = vertexList.get(i);
            if(polygonGraph.getEdge(startVertex, newVertex) != null){
                // Path edge exists in Pe
                // So remove it
                pathGraph.removeEdge(startVertex, newVertex);
                startVertex = newVertex;
            }
            else {
                // Path does not exist in Pe
                // So startVertex is the last shared vertex
                break;
            }
        }

        Vector2D endVertex = path.getEndVertex();
        Collections.reverse(vertexList);
        for (int i = 1; i < vertexList.size() - 1; i++) {
            Vector2D newVertex = vertexList.get(i);
            if(polygonGraph.getEdge(startVertex, newVertex) != null){
                // Path edge exists in Pe
                // So remove it
                pathGraph.removeEdge(endVertex, newVertex);
                startVertex = newVertex;
            }
            else {
                // Path does not exist in Pe
                // So endVertex is the last shared vertex
                break;
            }
        }
        KShortestPaths<Vector2D, DefaultWeightedEdge> k = new KShortestPaths<Vector2D, DefaultWeightedEdge>(pathGraph, 1);
        return k.getPaths(startVertex, endVertex).get(0);




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
        request.add(new WalkToTask(evaderLocation, false));
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
    public Collection<GraphPath<Vector2D, DefaultWeightedEdge>> getPaths()
    {
        return guardMap.values();
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
        this.paths = calculateMinimalPaths(this.visibilityGraph, 2, this.u, this.v);
        this.guardMap.put(this.getAgent(1), paths.get(1));
    }

    /**
     * In any iteration of the minimal-path algorithm, the 1st shortest path divides a
     * polygon up into two subPolygons. One of these subPolygons contains the evader.
     * The polygon with the evader will be further divided by the 2nd shortest path in the
     * evader subPolygon. This second path will also divide the subPolygon with the evader into
     * two distinct polygons. One of these polygons will be bounded by the two paths, the other
     * will be bounded by the second path and the polygon
     *
     * This method will be given the two subFloors created by dividing the subPolygon of the evader
     * up into two smaller subFloors and will return 0 when the evader is inside the subPolygon which
     * is bounded by one path and the polygon, 1 when bounded by 2 paths and this polygon is the
     * first given and 2 when it's bounded by the 2 paths and is the second given
     *
     * @param subFloor1 the first floor created by the second path
     * @param subFloor2 the second floor created by the second path
     * @param p1 the 1st shortest-path
     * @param p2 the 2nd shortest-path
     * @param evader the location of the evader
     * @return 0 when evader is inside subfloor bounded by only 1 path,
     * 1 when the evader is in the 1st given floor and this floor is bounded by both given paths
     * 2 when the evader is in the 2nd given floor and this floor is bounded by both given paths
     */
    private int has2BoundingPaths(Floor subFloor1, Floor subFloor2, GraphPath<Vector2D, DefaultWeightedEdge> p1,
                                  GraphPath<Vector2D, DefaultWeightedEdge> p2, Vector2D evader)
    {
        // the 3 important polygons
        MapPolygon polygon1 = subFloor1.getPolygon();
        MapPolygon polygon2 = subFloor2.getPolygon();
        MapPolygon boundedPolygon = new MapPolygon();

        // create the bounded polygon
        LinkedList<Vector2D> vertexes = new LinkedList<>();
        // first add the first path
        vertexes.addAll(p1.getVertexList());
        // then include the second path in reverse, without the start and end vertex
        List<Vector2D> p2VertexList = p2.getVertexList();
        for (int i = p2VertexList.size() - 2; i > 0; i--)
        {
            vertexes.add(p2VertexList.get(i));
        }

        // and create the MapPolygon
        for (Vector2D v : vertexes)
        {
            boundedPolygon.addPoint(new Double(v.getX()).intValue(), new Double(v.getY()).intValue());
        }

        // check if any polygon equals the bounded polygon and if the evader is inside that polygon
        if (polygon1.equals(boundedPolygon) && polygon1.contains(evader))
        {
            return 1;
        }
        else if (polygon2.equals(boundedPolygon) && polygon2.contains(evader))
        {
            return 2;
        }
        else
        {
            return 0;
        }
    }

    public List<MinimalPathAgent> getAgents(){return agents;}

}
