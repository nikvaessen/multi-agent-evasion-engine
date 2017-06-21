package nl.dke.pursuitevasion.gui.simulator;

import nl.dke.pursuitevasion.game.EngineConstants;
import nl.dke.pursuitevasion.game.Vector2D;
import nl.dke.pursuitevasion.game.agents.AbstractAgent;
import nl.dke.pursuitevasion.game.agents.impl.MCTS.MCTS_2;
import nl.dke.pursuitevasion.game.agents.impl.minimalPath.MinimalPathAgent;
import nl.dke.pursuitevasion.game.agents.impl.minimalPath.MinimalPathOverseer;
import nl.dke.pursuitevasion.map.MapPolygon;
import nl.dke.pursuitevasion.map.impl.Floor;
import nl.dke.pursuitevasion.map.impl.Map;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.swing.*;
import java.awt.*;
import java.awt.geom.Point2D;
import java.util.*;
import java.util.List;

import org.jgrapht.graph.DefaultWeightedEdge;
import org.jgrapht.GraphPath;


/**
 * Created by nik on 2/9/17.
 */
public class MapViewPanel
    extends JPanel
{
    private static Logger logger = LoggerFactory.getLogger(MapViewPanel.class);

    public final static int X_PADDING = 10; //pixels
    public final static int Y_PADDING = 10; //pixels
    public final static int BORDER_STROKE_WIDTH = 5; //pixels
    private MCTS_2 mcts;
    private MCTS_2.MCTSViewSettings mctsViewSettings = new MCTS_2.MCTSViewSettings();

    private Collection<MapPolygon> objects;

    private Collection<AbstractAgent> agents;

    private Dimension preferredSize;

    private MinimalPathOverseer minimalPathOverseer;

    private Map m;

    public MapViewPanel(Map map, Collection<AbstractAgent> agents)
    {
        this.m = map;
        this.objects = map.getPolygons();
        this.agents = agents;
        this.preferredSize = computePreferredSize();
    }

    public void setMCTSPreview(MCTS_2 mcts)
    {
        this.mcts = mcts;
    }

    public void setMCTSViewSettings(MCTS_2.MCTSViewSettings mctsViewSettings)
    {
        this.mctsViewSettings = mctsViewSettings;
    }

    @Override
    protected void paintComponent(Graphics g)
    {
        super.paintComponent(g);

        //draw all polygons which make up the map
        ((Graphics2D) g).setStroke(new BasicStroke(BORDER_STROKE_WIDTH)); //make borders more wide
        for(MapPolygon polygon : objects)
        {
            if(polygon.isSolid())
            {
                g.fillPolygon(polygon);
            }
            else
            {
                g.drawPolygon(polygon);
            }

        }

        //draw all points of the agents
        ((Graphics2D) g).setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        for(AbstractAgent agent : agents)
        {
            if(agent.isEvader())
            {
                g.setColor(EngineConstants.EVADER_COLOR);
            }
            else
            {
                g.setColor(EngineConstants.PURSUER_COLOR);
            }
            Vector2D vLocation = agent.getLocation();
            Point location = new Point(
                new Long(Math.round(vLocation.getX())).intValue(),
                new Long(Math.round(vLocation.getY())).intValue()
            );

            // Draw the agent numbers for the MinimalPathAgents.
            if(agent instanceof MinimalPathAgent)
            {
                g.drawString(Integer.toString(((MinimalPathAgent) agent).getAgentNumber()),
                             (int) Math.round(vLocation.getX()), (int) Math.round(vLocation.getY()) + 10);
                Color color;
                switch(((MinimalPathAgent) agent).getState())
                {
                    case MOVING_TO_PATH:
                        color = Color.ORANGE;
                        break;
                    case MOVING_TO_PROJECTION:
                        color = Color.YELLOW;
                        break;
                    case ON_PROJECTION:
                        color = Color.GREEN;
                        break;
                    default:
                        color = EngineConstants.ENTRY_PURSUER_COLOR;
                        break;
                }
                g.setColor(color);
            }
            int radius = agent.getRadius();
            g.fillOval(location.x - radius, location.y - radius, radius * 2, radius * 2);
            logger.trace("painting agent at {}", location);

            AbstractAgent.VisionArc visionArc = agent.getVisionArc();
            Vector2D base = visionArc.getBasePoint();
            logger.trace("the eyes of the agent are at {}", base);

            // Draw vision arcs
            g.setColor(EngineConstants.VISION_ARC_COLOR);

            double facingAngle = agent.getFacingAngle();
            double visionAngle = agent.getVisionAngle();
            int startingAngle = new Double(facingAngle - Math.round((visionAngle / 2))).intValue() % 360;

            int visionRadius = new Double(agent.getVisionRange()).intValue();

            logger.trace("drawing arc with radius {}, start angle {} and viewing angle {}",
                         visionRadius, startingAngle, new Double(visionAngle).intValue());
            g.fillArc((int) Math.round(base.getX()) - visionRadius, (int) Math.round(base.getY()) - visionRadius,
                      visionRadius * 2, visionRadius * 2,
                      startingAngle, new Double(visionAngle).intValue());
            //g.fillOval(base.x - 2, base.y - 2, 4, 4 );


//            Point left = visionArea.getLeftPoint();
//            g.drawLine(left.x, left.y, base.x, base.y);

        }
        if(mcts != null)
        {
            mcts.paint(g, mctsViewSettings, this);
        }
        if(minimalPathOverseer != null)
        {
            g.setColor(Color.GREEN);
            ((Graphics2D) g).setStroke(new BasicStroke());
            Collection<GraphPath<Vector2D, DefaultWeightedEdge>> paths = minimalPathOverseer.getPaths();

            if(paths != null)
            {

                for(GraphPath<Vector2D, DefaultWeightedEdge> path : paths)
                {
                    List<Vector2D> points = path.getVertexList();
                    Vector2D lastPoint = points.get(0);
                    for(int i = 1; i < points.size(); i++)
                    {
                        Vector2D newPoint = points.get(i);
                        g.drawLine((int) Math.round(lastPoint.getX()), (int) Math.round(lastPoint.getY()),
                                   (int) Math.round(newPoint.getX()), (int) Math.round(newPoint.getY()));
                        lastPoint = newPoint;
                    }
                }
            }
        }
        if(minimalPathOverseer != null)
        {
            for(int i = 0; i < minimalPathOverseer.getAmountOfAgents(); i++)
            {
                MinimalPathAgent agent = minimalPathOverseer.getAgent(i);
                if(agent.getProjectionLocation() != null)
                {
                    g.setColor(Color.BLACK);
                    Point2D p = agent.getProjectionLocation().toPoint();
                    g.drawOval((int) Math.round(p.getX()), (int) Math.round(p.getY()), 3, 3);
                }
            }
        }

        //draw connection of vertices
        ((Graphics2D) g).setStroke(new BasicStroke(1));
        Floor floor= (Floor) m.getFloors().toArray()[0];
        ArrayList<ArrayList<Point>> conns = floor.getTriangulation();
        g.setColor(Color.green);
        for (int i=0; i<conns.size(); i++){
            g.drawLine(conns.get(i).get(0).x, conns.get(i).get(0).y,
                    conns.get(i).get(1).x,conns.get(i).get(1).y);
        }



       /*     //draw second splitted polygon
            ((Graphics2D) g).setStroke(new BasicStroke(2));
            ArrayList<Point> polygonPoints2 = floor.newSimplePolygon2;
            g.setColor(Color.MAGENTA);
            for (int i=0; i<polygonPoints2.size()-1; i++){
                g.drawLine(polygonPoints2.get(i).x, polygonPoints2.get(i).y,
                        polygonPoints2.get(i+1).x, polygonPoints2.get(i+1).y);
            }
*/
        //draw first splitted polygon
        ((Graphics2D) g).setStroke(new BasicStroke(2));
        ArrayList<Point> polygonPoints1 = floor.newSimplePolygon1;
        g.setColor(Color.red);
        for (int i=0; i<polygonPoints1.size()-1; i++){
            g.drawLine(polygonPoints1.get(i).x, polygonPoints1.get(i).y,
                    polygonPoints1.get(i+1).x, polygonPoints1.get(i+1).y);
        }
        g.drawLine(polygonPoints1.get(polygonPoints1.size()-1).x, polygonPoints1.get(polygonPoints1.size()-1).y,
                polygonPoints1.get(0).x, polygonPoints1.get(0).y);

        //draw triangles
        ((Graphics2D) g).setStroke(new BasicStroke(2));
        ArrayList<Polygon> triangles = floor.trianglesToDraw;
        g.setColor(Color.blue);
        for(Polygon p: triangles){
            g.drawPolygon(p);
        }

    }

    @Override
    public Dimension getPreferredSize()
    {
        return preferredSize;
    }

    private Dimension computePreferredSize()
    {
        int minX = 0, maxX = 0, minY = 0, maxY = 0;
        for(MapPolygon polygon : objects)
        {
            for(int x : polygon.xpoints)
            {
                if(x < minX)
                {
                    minX = x;
                    continue;
                }
                if(x > maxX)
                {
                    maxX = x;
                }
            }
            for(int y : polygon.ypoints)
            {
                if(y < minY)
                {
                    minY = y;
                    continue;
                }
                if(y > maxY)
                {
                    maxY = y;
                }
            }
        }
        //+1 such that borders are actually visible
        return new Dimension(maxX - minX + 1, maxY - minY + 1);
    }

    public void setMinimalPathOverseer(MinimalPathOverseer minimalPathOverseer)
    {
        this.minimalPathOverseer = minimalPathOverseer;
    }
}
