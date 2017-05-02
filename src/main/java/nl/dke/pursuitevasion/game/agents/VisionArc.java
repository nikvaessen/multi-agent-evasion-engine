package nl.dke.pursuitevasion.game.agents;
import java.awt.geom.Line2D;
import nl.dke.pursuitevasion.game.agents.AbstractAgent;
import nl.dke.pursuitevasion.map.impl.Floor;
import nl.dke.pursuitevasion.map.impl.Map;
import nl.dke.pursuitevasion.map.impl.Obstacle;

import java.awt.*;
import java.awt.geom.PathIterator;
import java.awt.geom.Point2D;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;

/**
 * Limits the agents that an agent can see
 *
 * Created by Jan on 14-4-2017.
 */
public class VisionArc {

    Angle lowerAngle;
    Angle upperAngle;
    double distance;
    Point origin;
    Map map;
    AbstractAgent visionAgent;
    //HashMap<Floor, ArrayList<Line2D>> lines =

    public VisionArc(AbstractAgent agent, double visionAngle , double visionDistance, Map map){
        visionAgent = agent;
        double facingAngle = agent.getFacingAngle();
        upperAngle = new Angle( (facingAngle + visionAngle/2) % 360);
        lowerAngle = new Angle( (360 + facingAngle - visionAngle/2) % 360 );
        distance = visionDistance;
        origin = agent.location;
    }

    public Collection<AbstractAgent> getVisibleAgents(Collection<AbstractAgent> agents){
        // TODO: filter out own agent.

        // TODO: filter out agent on other floors
        Floor f = visionAgent.getFloor();
        // precalculate all lines created by obstacles in the floor
        Collection<Line2D> lines = getFloorLines(f);
        // check which agents are within the area.
        ArrayList<AbstractAgent> visibleAgents = new ArrayList<AbstractAgent>();
        for(AbstractAgent agent : agents){
            Point location = agent.getLocation();
            if(agent.getFloor() == f && inArc(location) && !isObstructed(location, lines)){
                visibleAgents.add(agent);
            }
        }
        return visibleAgents;
    }

    private boolean isObstructed(Point p, Collection<Line2D> lines){
        // check for obstacles that may obstruct line of sight to an agent
        // Check if a line from the agent to the other agent intersects an obstacle line
        Line2D ray = new Line2D.Double(origin, p);
        for(Line2D line : lines){
            if(ray.intersectsLine(line)){
                return true;
            }
        }
        return false;
    }

    private Collection<Line2D> getFloorLines(Floor floor){
        ArrayList<Polygon> obstructions = new ArrayList<>();
        obstructions.add(floor.getPolygon());
        for (Obstacle obstacle:
                floor.getObstacles()) {
            obstructions.add(obstacle.getPolygon());
        }
        // maybe limit this to the closest polygons.
        // get te lines for all objects in the Floor
        ArrayList<Line2D> lines = new ArrayList<>();
        for(Polygon polygon : obstructions){
            lines.addAll(getLines(polygon));
        }
        return lines;
    }

    private ArrayList<Line2D> getLines(Polygon polygon){
        ArrayList<java.awt.geom.Line2D> lines = new ArrayList<>();
        Point2D start = null;
        Point2D last = null;
        for (PathIterator iter = polygon.getPathIterator(null); !iter.isDone(); iter.next()) {
            double[] points = new double[6];
            int type = iter.currentSegment(points);
            if (type == PathIterator.SEG_MOVETO) {
                Point2D moveP = new Point2D.Double(points[0], points[1]);
                last = moveP;
                start = moveP;
            } else if (type == PathIterator.SEG_LINETO) {
                Point2D newP = new Point2D.Double(points[0], points[1]);
                java.awt.geom.Line2D line = new java.awt.geom.Line2D.Double(last, newP);
                lines.add(line);
                last = newP;
            } else if (type == PathIterator.SEG_CLOSE){
                java.awt.geom.Line2D line = new java.awt.geom.Line2D.Double(start, last);
                lines.add(line);
            }
        }
        return lines;
    }

    private boolean inArc(Point p){
        if(p.distance(origin) > distance){
            return false;
        }
        // calculate angle between the origin and the point
        double adjacent = origin.getX() - p.getX();
        double opposite = origin.getY() - p.getY();
        double angle = Math.atan2(opposite, adjacent);
        if(angle >= lowerAngle.getAngle() && angle <= upperAngle.getAngle()){
            return true;
        }
        return false;

    }

    private Point calculateBase()
    {
        int dx = new Long(Math.round(Math.cos(facing.getRadians()) * radius)).intValue();
        //change sign of y because y works reversed from normal cartesian plane
        int dy = - new Long(Math.round(Math.sin(facing.getRadians()) * radius)).intValue();

        if(logger.isTraceEnabled())
        {
            logger.trace("base {} before changing with dx {} and dy {} facing {}, {}",
                    base, dx, dy, facing.getAngle(), facing.getRadians());
        }

        return new Point(location.x + Math.round(dx), location.y + Math.round(dy));
    }

    public Point getBasePoint() {
        return calculateBase();
    }
}
