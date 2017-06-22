package nl.dke.pursuitevasion.game.agents.tasks;

import nl.dke.pursuitevasion.game.Vector2D;
import nl.dke.pursuitevasion.game.agents.AbstractAgent;
import nl.dke.pursuitevasion.game.agents.AgentCommand;
import nl.dke.pursuitevasion.game.agents.impl.minimalPath.MinimalPathAgent;
import nl.dke.pursuitevasion.game.agents.impl.minimalPath.MinimalPathAgentState;

import org.jgrapht.Graph;
import org.jgrapht.GraphPath;
import org.jgrapht.alg.interfaces.KShortestPathAlgorithm;
import org.jgrapht.alg.shortestpath.KShortestPaths;
import org.jgrapht.graph.DefaultWeightedEdge;
import org.jgrapht.graph.SimpleWeightedGraph;
import org.jgrapht.graph.Subgraph;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.xml.bind.annotation.XmlType;
import java.awt.geom.Line2D;
import java.awt.geom.Point2D;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by Jan on 25-5-2017.
 */
public class MinimalPathGuardTask extends AbstractAgentTask
{

    private static Logger logger = LoggerFactory.getLogger(MinimalPathGuardTask.class);

    private GraphPath<Vector2D, DefaultWeightedEdge> path;
    private Vector2D evaderLocation;

    public MinimalPathGuardTask(GraphPath<Vector2D, DefaultWeightedEdge> path, Vector2D location)
    {
        if(path == null){
            throw new NullPointerException("path is null");
        }
        this.path = path;
        this.evaderLocation = location;
    }

    @Override
    protected AgentCommand computeAgentCommand(AbstractAgent agent, double maxDistance, double maxRotation)
    {
        int agentNumber = ((MinimalPathAgent)agent).getAgentNumber();
        // check if we are on the path
        if(onPath(agent, path))
        {
            logger.debug("Agent {} moving to projection", agentNumber);
            // if we are -> move to projection of evader on path
            return moveToProjection(agent, maxDistance, maxRotation);
        }
        else
        {
            logger.debug("Agent {} moving to closest point on path", agentNumber);
            ((MinimalPathAgent) agent).setState(MinimalPathAgentState.MOVING_TO_PATH);
            // if we are not -> move to closest point on path
            return moveToClosestPointOnPath(agent, maxDistance, maxRotation);
        }

    }

    private AgentCommand moveToClosestPointOnPath(AbstractAgent agent, double maxDistance, double maxRotation)
    {
        Vector2D agentLocation = agent.getLocation();
        Vector2D closestVertex = closestPathVertex(agentLocation);
        Line2D closestSegment = closestLineSegment(closestVertex, agentLocation, agent);
        Vector2D destination = getClosestPointOnSegment(closestSegment, agentLocation);
        return new WalkToTask(destination, true).computeAgentCommand(agent, maxDistance, maxRotation);
    }

    private AgentCommand moveToProjection(AbstractAgent agent, double maxDistance, double maxRotation)
    {
        // get the location of the projection
        Vector2D e = findProjection(agent);
        ((MinimalPathAgent)agent).setProjectionLocation(e);
        // check if the agent is on the same path edge as the projection
        if(onProjectionPathSegment(agent.getLocation(), e))
        {
            // if it is -> move to the projection location
            // If we can make it to the projection location
            if(agent.getLocation().distance(e) <= maxDistance + agent.getRadius()*2){
                // Set state to ON_PROJECTION
                ((MinimalPathAgent)agent).setState(MinimalPathAgentState.ON_PROJECTION);
            }
            else{
                // Else set it to MOVE_TO_PROJECTION
                ((MinimalPathAgent)agent).setState(MinimalPathAgentState.MOVING_TO_PROJECTION);
            }
            if(e.equals(agent.getLocation())){
                return new AgentCommand(agent, agent.getLocation());
            }
            return moveToProjectionLocation(agent, e, maxDistance, maxRotation);
        }
        else
        {
            ((MinimalPathAgent) agent).setState(MinimalPathAgentState.MOVING_TO_PROJECTION);
            // else -> Follow path to the node closest to the projection.
            Vector2D destination = getPathTo(e, agent);
            return new WalkToTask(destination).computeAgentCommand(agent, maxDistance, maxRotation);
        }
    }

    private Vector2D getPathTo(Vector2D e, AbstractAgent agent) {
        Vector2D evaderVertex = closestPathVertex(evaderLocation);
        Vector2D startVertex = closestPathVertex(agent.getLocation());
        if(startVertex == evaderVertex){
            return startVertex;
        }
        // get Path from closest node to the node closest to the evader
        // prune the original graph to only have the path edges

        Graph<Vector2D, DefaultWeightedEdge> pathGraph = createPathGraph(path);
        KShortestPathAlgorithm<Vector2D, DefaultWeightedEdge> k = new KShortestPaths<>(pathGraph, 1);
        GraphPath<Vector2D, DefaultWeightedEdge> subPath = k.getPaths(startVertex, evaderVertex).get(0);
        // check if we are already on the path
        if(onPath(agent, subPath)) {
            // if we are -> find the target Vertex of the edge we are on
            Line2D edge = closestPathEdge(subPath, agent.getLocation());
            return Vector2D.fromPoint2D(edge.getP2());
        }
        else{
            // else -> move to the startNode
           return subPath.getStartVertex();
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

    private AgentCommand moveToProjectionLocation(AbstractAgent agent, Vector2D e, double maxDistance,
                                                  double maxRotation)
    {

        WalkToTask t = new WalkToTask(e, true);
        return t.computeAgentCommand(agent, maxDistance, maxRotation);
    }

    /**
     * @param agentLocation location of the agent
     * @param ePrime evader projection on path
     * @return whether the agent is on the same line segment of the path as the projection.
     */
    private boolean onProjectionPathSegment(Vector2D agentLocation, Vector2D ePrime)
    {
        // get the path segment of the projection
        ArrayList<Line2D> lines = linesFromPath(path);
        Point2D point = ePrime.toPoint();
        double minDist = Double.MAX_VALUE;
        Line2D pathSegment = null;
        // get the closest line segment
        for(Line2D line : lines)
        {
            double distance = line.ptSegDist(point);
            if(distance < minDist)
            {
                pathSegment = line;
                minDist = distance;
            }
        }
        if(pathSegment == null)
        {
            throw new NullPointerException("Projection not on path!");
        }

        // check if the agentLocation is on that line.
        return pathSegment.ptSegDist(agentLocation.toPoint()) < 0.2; // 0.2 is for margin of error.
    }

    /**
     * Finds the projection of the evaderLocation on the path.
     * @param agent agent
     * @return projection of evader on path
     */private Vector2D findProjection(AbstractAgent agent) {

//        Graph<Vector2D, DefaultWeightedEdge> g = path.getGraph();
//        List<DefaultWeightedEdge> edgeList = path.getEdgeList();
//        List<Line2D> lines = new ArrayList<>();
//        for (DefaultWeightedEdge defaultWeightedEdge : edgeList) {
//            Vector2D edgeSource = g.getEdgeSource(defaultWeightedEdge);
//            Vector2D edgeTarget = g.getEdgeTarget(defaultWeightedEdge);
//            lines.add(new Line2D.Double(edgeSource.toPoint(), edgeTarget.toPoint()));
//        }
//
//        // Get the closest line segment(s) to the evader.
//        // Get the smallest distance
//        double min = Double.MAX_VALUE;
//        for (Line2D line : lines) {
//            double dist = line.ptSegDist(evaderLocation.toPoint());
//            if(dist < min){
//                min = dist;
//            }
//        }
//        final double minDist = min;
//        // get all lines with the distance of (about)min
//        List<Line2D> closeLines = new ArrayList<>();
//        lines.forEach(
//                (Line2D line)->{if(Math.abs(line.ptSegDist(evaderLocation.toPoint())-minDist) < 0.01){
//                    closeLines.add(line);
//                }
//        });
//
//        // Calculate projections for the closest lines
//        List<Vector2D> projections = new ArrayList<>();
//        for (Line2D line : closeLines) {
//            projections.add(getClosestPointOnSegment(line, evaderLocation));
//        }
//        // Return the projection closest to the agent
//        Vector2D closestProjection = null;
//        double projectionDist = Double.MAX_VALUE;
//        for (Vector2D projection : projections) {
//            double distance = projection.distance(agent.getLocation());
//            if(distance < projectionDist){
//                projectionDist = distance;
//                closestProjection = projection;
//            }
//        }
//        if (closestProjection == null) {
//            throw new NullPointerException("We done fucked up");
//        }
//        return closestProjection;


        // old code
//
        // get the node in path closest to the evader
        Vector2D closestNode = closestPathVertex(evaderLocation);
        // Get the closest line segments
        Line2D line = closestLineSegment(closestNode, evaderLocation, agent);
        Vector2D closestPoint =  getClosestPointOnSegment(line, evaderLocation);
        // Check if the point is on the line segment
        if(line.ptSegDist(closestPoint.toPoint()) < 0.0001){
            return closestPoint;
        }
        // else return the node
        return closestNode;
    }

    /**
     *
     * @param closestNode Node closest to the target
     * @param location location of the target
     * @param agent agent to move to the target
     * @return line segment close to the evader and the pursuer
     */
    private Line2D closestLineSegment(Vector2D closestNode, Vector2D location, AbstractAgent agent)
    {
        List<Line2D> segments = adjacentLineSegments(closestNode);
        Point2D e = location.toPoint();
        Line2D firstSegment = segments.get(0);
        try
        {
            // try to get the second segment
            Line2D secondSegment = segments.get(1);
            double firstDistance = firstSegment.ptSegDist(e);
            double secondDistance = secondSegment.ptSegDist(e);
            // if the distance is almost equal
            if(Math.abs(firstDistance - secondDistance) < 0.002){
                // return the line segment that is closest to the agent.
                Point2D agentLocation = agent.getLocation().toPoint();
                return firstSegment.ptSegDist(agentLocation) < secondSegment.ptSegDist(agentLocation) ? firstSegment : secondSegment;
            }
            return firstSegment.ptSegDist(e) < secondSegment.ptSegDist(e) ?
                firstSegment :
                secondSegment;
        }
        // If there is no second segment
        catch(IndexOutOfBoundsException exception)
        {
            // the first one is always the closest
            return firstSegment;
        }
    }

    private List<Line2D> adjacentLineSegments(Vector2D closestNode)
    {
        List<Vector2D> verteces = path.getVertexList();
        int index = verteces.indexOf(closestNode);
        List<Vector2D> segmentEnds = new ArrayList<>(2);
        // TODO: Yes I know this is ugly...
        if(index == 0)
        {
            segmentEnds.add(verteces.get(1));
        }
        else if(index == verteces.size() - 1)
        {
            segmentEnds.add(verteces.get(index - 1));
        }
        else
        {
            segmentEnds.add(verteces.get(index + 1));
            segmentEnds.add(verteces.get(index - 1));
        }

        List<Line2D> adjacentSegments = new ArrayList<>(2);
        for(Vector2D segmentEnd : segmentEnds)
        {
            adjacentSegments.add(new Line2D.Double(closestNode.toPoint(), segmentEnd.toPoint()));
        }

        return adjacentSegments;
    }

    private Vector2D closestPathVertex(Vector2D vector)
    {
        double min = Double.MAX_VALUE;
        Vector2D closest = null;
        for(Vector2D vertex : path.getVertexList())
        {
            double distance = vector.distance(vertex);
            if(distance < min)
            {
                closest = vertex;
                min = distance;
            }
        }

        if(closest == null)
        {
            throw new IllegalArgumentException("Could not find closest vertex.");
        }
        return closest;
    }

    private Vector2D getClosestPointOnSegment(Line2D line, Vector2D point)
    {
        // Calculate line coefficient
        double ydiff = line.getY1() - line.getY2();
        double xdiff = line.getX1() - line.getX2();
        double linecoeff = ydiff / xdiff;

        double intersectcoeff;
        if(linecoeff == -0.0 || linecoeff == 0.0){
            intersectcoeff = 100_000_000;
        }
        else if(Double.isInfinite(linecoeff)){
            intersectcoeff = 0;
            linecoeff = 100_000_000;
        }
        else{
            intersectcoeff = -1/linecoeff;
        }

        double b1 = line.getY1() - linecoeff * line.getX1();


        double b2 = point.getY() - intersectcoeff * point.getX();
        double xIntersect = (b2 - b1) / (linecoeff - intersectcoeff);
        double yIntersect = linecoeff * xIntersect + b1;

        return new Vector2D(xIntersect, yIntersect);


    }

    /*
     * Returns closest point on segment to point
     *
     * @return closets point on segment to point

    public Vector2D getClosestPointOnSegment(Line2D line, Vector2D point)
    {
    Vector2D start = Vector2D.fromPoint2D(line.getP1());
    Vector2D end = Vector2D.fromPoint2D(line.getP2());
    double xDelta = end.getX() - start.getX();
    double yDelta = end.getY() - start.getX();

    if ((xDelta == 0) && (yDelta == 0))
    {
    throw new IllegalArgumentException("Segment start equals segment end");
    }

    double u = ((point.getX() - start.getX()) * xDelta + (point.getY() - start.getY()) * yDelta) / (xDelta * xDelta +
    yDelta * yDelta);

    Vector2D closestPoint;
    if (u < 0)
    {
    closestPoint = new Vector2D(start);
    }
    else if (u > 1)
    {
    closestPoint = new Vector2D(end);
    }
    else
    {
    closestPoint = new Vector2D(start.getX() + u * xDelta, start.getY() + u * yDelta);
    }

    return closestPoint;
    }*/

    /**
     * Checks whether the agent is on the path.
     * Returns true when an agents is close (<0.02)
     * to any of the line segments of the path
     *
     * @param agent agent
     * @return boolean describing whether an agent is close
     */
    private boolean onPath(AbstractAgent agent, GraphPath<Vector2D, DefaultWeightedEdge> graphPath)
    {
        Point2D location = agent.getLocation().toPoint();
        ArrayList<Line2D> lines = linesFromPath(graphPath);
        // Check if the agent is close to any of the lines
        // includes a small error margin in the form of minDist
        double minDist = 0.2;
        for(Line2D line : lines)
        {
            if(line.ptSegDist(location) < minDist)
            {
                return true;
            }
        }
        return false;
    }

    private Line2D closestPathEdge(GraphPath<Vector2D, DefaultWeightedEdge> graphPath, Vector2D agentLocation){
        Point2D location = agentLocation.toPoint();
        List<Line2D> lines = linesFromPath(graphPath);
        Line2D closestLine = null;
        double minDist = Double.MAX_VALUE;
        for (Line2D line : lines){
            double dist = line.ptSegDist(location);
            if(dist< minDist){
                minDist = dist;
                closestLine = line;
            }
        }
        if (closestLine != null) {
            return closestLine;
        }
        else{
            throw new IllegalArgumentException("Path has no edges");
        }
    }

    private ArrayList<Line2D> linesFromPath(GraphPath<Vector2D, DefaultWeightedEdge> graphPath)
    {
        ArrayList<Line2D> lines = new ArrayList<>();
        List<Vector2D> points = graphPath.getVertexList();
        // Contruct lines from the path
        Vector2D firstVector = null;
        for(Vector2D vector : points)
        {
            if(firstVector == null)
            {
                firstVector = vector;
            }
            else
            {
                lines.add(new Line2D.Double(firstVector.toPoint(), vector.toPoint()));
                firstVector = vector;
            }
        }
        return lines;
    }

    @Override
    protected boolean completesTask(AgentCommand command)
    {
        return true;
    }
}
