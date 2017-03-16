package nl.dke.pursuitevasion.gui.simulator;

import nl.dke.pursuitevasion.game.agents.AbstractAgent;
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

    private Collection<MapPolygon> objects;

    private Collection<AbstractAgent> agents;

    private Dimension preferredSize;

    public MapViewPanel(Map map, Collection<AbstractAgent> agents)
    {
        this.objects = map.getPolygons();
        this.agents  = agents;
        this.preferredSize = computePreferredSize();
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
            Point location = agent.getLocation();
            int radius = agent.getRadius();
            g.fillOval(location.x - radius, location.y - radius,radius * 2, radius * 2);
            logger.trace("painting agent at {}", location);

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
