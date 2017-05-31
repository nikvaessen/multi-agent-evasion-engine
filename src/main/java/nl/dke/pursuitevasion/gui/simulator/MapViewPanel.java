package nl.dke.pursuitevasion.gui.simulator;

import nl.dke.pursuitevasion.game.Engine;
import nl.dke.pursuitevasion.game.EngineConstants;
import nl.dke.pursuitevasion.game.Vector2D;
import nl.dke.pursuitevasion.game.agents.AbstractAgent;
import nl.dke.pursuitevasion.game.agents.impl.MCTS.MCTS_2;
import nl.dke.pursuitevasion.game.agents.impl.MinimalPath.MinimalPathAgent;
import nl.dke.pursuitevasion.map.MapPolygon;
import nl.dke.pursuitevasion.map.impl.Map;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.swing.*;
import java.awt.*;
import java.util.*;


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
    private  MCTS_2 mcts;
    private MCTS_2.MCTSViewSettings mctsViewSettings = new MCTS_2.MCTSViewSettings();

    private Collection<MapPolygon> objects;

    private Collection<AbstractAgent> agents;

    private Dimension preferredSize;


    public MapViewPanel(Map map, Collection<AbstractAgent> agents)
    {
        this.objects = map.getPolygons();
        this.agents  = agents;
        this.preferredSize = computePreferredSize();
    }

    public void setMCTSPreview(MCTS_2 mcts){
        this.mcts = mcts;
    }
    public void setMCTSViewSettings(MCTS_2.MCTSViewSettings mctsViewSettings){
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
            if(agent.isEvader()){
                g.setColor(EngineConstants.EVADER_COLOR);
            }
            else{
                g.setColor(EngineConstants.PURSUER_COLOR);}
            Vector2D vLocation = agent.getLocation();
            Point location = new Point(
                    new Long(Math.round(vLocation.getX())).intValue(),
                    new Long(Math.round(vLocation.getY())).intValue()
            );

            int radius = agent.getRadius();
            g.fillOval(location.x - radius, location.y - radius,radius * 2, radius * 2);
            logger.trace("painting agent at {}", location);

            AbstractAgent.VisionArc visionArc = agent.getVisionArc();
            Vector2D base = visionArc.getBasePoint();
            logger.trace("the eyes of the agent are at {}",base);

            // Draw the agent numbers for the MinimalPathAgents.
            if(agent instanceof MinimalPathAgent){
                g.drawString(Integer.toString(((MinimalPathAgent) agent).getAgentNumber()),
                        (int)Math.round(vLocation.getX()), (int)Math.round(vLocation.getY()) + 10);
            }
            // Draw vision arcs
            g.setColor(EngineConstants.VISION_ARC_COLOR);

            double facingAngle = agent.getFacingAngle();
            double visionAngle = agent.getVisionAngle();
            int startingAngle = new Double(facingAngle - Math.round((visionAngle / 2))).intValue() % 360;

            int  visionRadius = new Double(agent.getVisionRange()).intValue();

            logger.trace("drawing arc with radius {}, start angle {} and viewing angle {}",
                    visionRadius, startingAngle, new Double(visionAngle).intValue());
            g.fillArc((int)Math.round(base.getX()) - visionRadius, (int)Math.round(base.getY()) - visionRadius, visionRadius * 2, visionRadius * 2,
                    startingAngle, new Double(visionAngle).intValue());
            //g.fillOval(base.x - 2, base.y - 2, 4, 4 );


//            Point left = visionArea.getLeftPoint();
//            g.drawLine(left.x, left.y, base.x, base.y);

        }
        if (mcts != null) {
            mcts.paint(g,mctsViewSettings,this);
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

}
