package nl.dke.pursuitevasion.game.agents.tasks;

import nl.dke.pursuitevasion.game.Vector2D;
import nl.dke.pursuitevasion.game.agents.AbstractAgent;
import nl.dke.pursuitevasion.game.agents.AgentCommand;
import nl.dke.pursuitevasion.game.agents.impl.MinimalPath.MinimalPathOverseer;
import org.jgrapht.GraphPath;
import org.jgrapht.graph.DefaultWeightedEdge;

import java.awt.geom.Line2D;
import java.awt.geom.Point2D;
import java.util.ArrayList;
 import java.util.List;

/**
 * Created by Jan on 25-5-2017.
 */
public class MinimalPathGuardTask extends AbstractAgentTask{

    private GraphPath<Vector2D, DefaultWeightedEdge> path;
    private AbstractAgent evader;

    public MinimalPathGuardTask(GraphPath<Vector2D, DefaultWeightedEdge> path, AbstractAgent evader){
        this.path = path;
        this.evader = evader;
    }

    @Override
    protected AgentCommand computeAgentCommand(AbstractAgent agent, double maxDistance, double maxRotation) {
        // check if we are on the path
        if(onPath(agent)){
            // if we are -> move to projection of evader on path
            return moveToProjection(agent, maxDistance, maxRotation);
        }
        else{
            // if we are not -> move to closest node on path
            return moveToClosestPathVertex(agent, maxDistance, maxRotation);
        }

    }

    private AgentCommand moveToClosestPathVertex(AbstractAgent agent, double maxDistance, double maxRotation) {
         Vector2D destination = closestPathVertex(agent.getLocation());
         return new WalkToTask(destination).computeAgentCommand(agent, maxDistance, maxRotation);
    }

    private AgentCommand moveToProjection(AbstractAgent agent, double maxDistance, double maxRotation) {
        // get the location of the projection
        Vector2D e = findProjection();
        // check if the agent is on the same path edge as the projection
        if(onProjectionPathSegment(agent.getLocation(), e)){
            // if it is -> move to the projection location
            return moveToProjectionLocation(agent, e, maxDistance, maxRotation);
        }
        else{
            // else -> move to to the closest node
            return moveToClosestPathVertex(agent, maxDistance, maxRotation);
        }
    }

    private AgentCommand moveToProjectionLocation(AbstractAgent agent, Vector2D e, double maxDistance, double maxRotation) {
        WalkToTask t = new WalkToTask(e);
        return t.computeAgentCommand(agent, maxDistance, maxRotation);
    }

    private boolean onProjectionPathSegment(Vector2D agentLocation, Vector2D ePrime) {
        // get the path segment of the projection
        ArrayList<Line2D> lines = linesFromPath();
        Point2D point = ePrime.toPoint();
        double minDist = 0.02; // error margin.
        Line2D pathSegment = null;
        for(Line2D line : lines){
            if (line.ptSegDist(point) < minDist){
                pathSegment = line;
                break;
            }
        }
        if (pathSegment == null) {
            throw new NullPointerException("Projection not on path!");
        }

        // check if the agentLocation is on that line.
        return pathSegment.ptSegDist(agentLocation.toPoint()) < minDist;
    }

    private Vector2D findProjection() {
        // get the node in path closest to the evader
        Vector2D closestNode = closestPathVertex(evader.getLocation());
        // Get the closest line segments
        Line2D line = closestLineSegment(closestNode);
        return getClosestPointOnSegment(line, evader.getLocation());
    }

    private Line2D closestLineSegment(Vector2D closestNode) {
        List<Line2D> segments = adjacentLineSegments(closestNode);
        Point2D e = evader.getLocation().toPoint();
        Line2D firstSegment = segments.get(0);
        Line2D secondSegment = segments.get(1);
        return firstSegment.ptSegDist(e) < secondSegment.ptSegDist(e) ? firstSegment : secondSegment;
    }

    private List<Line2D> adjacentLineSegments(Vector2D closestNode) {
        List<Vector2D> verteces = path.getVertexList();
        int index = verteces.indexOf(closestNode);
        List<Vector2D> segmentEnds = new ArrayList<>(2);
        // TODO: Yes I know this is ugly...
        if(index == 0){
            segmentEnds.add(verteces.get(1));
        }
        else if(index == verteces.size() -1){
            segmentEnds.add(verteces.get(index -1));
        }
        else{
            segmentEnds.add(verteces.get(index+1));
            segmentEnds.add(verteces.get(index-1));
        }

        List<Line2D> adjacentSegments = new ArrayList<>(2);
        for (Vector2D segmentEnd: segmentEnds){
            adjacentSegments.add(new Line2D.Double(closestNode.toPoint(), segmentEnd.toPoint()));
        }

        return adjacentSegments;
    }

    private Vector2D closestPathVertex(Vector2D vector){
        double min = Double.MAX_VALUE;
        Vector2D closest = null;
        for (Vector2D vertex : path.getVertexList()){
            double distance = vector.distance(vertex);
            if(distance < min){
                closest = vertex;
                min = distance;
            }
        }

        if(closest == null){
            throw new IllegalArgumentException("Could not find closest vertex.");
        }
        return closest;
    }

    /**
     * Returns closest point on segment to point
     *
     * @return closets point on segment to point
     */
    public static Vector2D getClosestPointOnSegment(Line2D line, Vector2D point)
    {
        Vector2D start = Vector2D.fromPoint2D(line.getP1());
        Vector2D end = Vector2D.fromPoint2D(line.getP2());
        double xDelta = end.getX() - start.getX();
        double yDelta = end.getY() - start.getX();

        if ((xDelta == 0) && (yDelta == 0))
        {
            throw new IllegalArgumentException("Segment start equals segment end");
        }

        double u = ((point.getX() - start.getX()) * xDelta + (point.getY() - start.getY()) * yDelta) / (xDelta * xDelta + yDelta * yDelta);

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
    }

    /**
     * Checks whether the agent is on the path.
     * Returns true when an agents is close (<0.02)
     * to any of the line segments of the path
     * @param agent
     * @return boolean describing whether an agent is close
     */
    private boolean onPath(AbstractAgent agent) {
        Point2D location = agent.getLocation().toPoint();
        ArrayList<Line2D> lines = linesFromPath();
        // Check if the agent is close to any of the lines
        // includes a small error margin in the form of minDist
        double minDist = 0.2;
        for (Line2D line : lines) {
            if(line.ptSegDist(location) < minDist){
                return true;
            }
        }
        return false;
    }

    private ArrayList<Line2D> linesFromPath() {
        ArrayList<Line2D> lines = new ArrayList<>();
        List<Vector2D> points = path.getVertexList();
        // Contruct lines from the path
        Vector2D firstVector = null;
        for(Vector2D vector : points) {
            if(firstVector == null){
                firstVector = vector;
            }
            else{
                lines.add(new Line2D.Double(firstVector.toPoint(), vector.toPoint()));
                firstVector = vector;
            }
        }
        return lines;
    }

    @Override
    protected boolean completesTask(AgentCommand command) {
        // Ask the overseer
        return false;
    }
}
