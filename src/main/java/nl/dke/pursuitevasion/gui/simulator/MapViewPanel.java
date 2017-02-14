package nl.dke.pursuitevasion.gui.simulator;

import nl.dke.pursuitevasion.agents.AbstractAgent;
import nl.dke.pursuitevasion.map.AbstractObject;
import nl.dke.pursuitevasion.map.impl.Floor;
import nl.dke.pursuitevasion.map.impl.Map;

import javax.swing.*;
import java.awt.*;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;

/**
 * Created by nik on 2/9/17.
 */
public class MapViewPanel
        extends JPanel
{

    private Collection<Polygon> objects;

    private Collection<AbstractAgent> agents;

    public MapViewPanel(Map map, Collection<AbstractAgent> agents)
    {
        this.objects = Collections.unmodifiableCollection(map.getPolygons());
        this.agents  = Collections.unmodifiableCollection(agents);
    }

    @Override
    protected void paintComponent(Graphics g)
    {
        super.paintComponent(g);

        //draw all polygons which make up the map
        for(Polygon polygon : objects)
        {
            g.drawPolygon(polygon);
        }

        //draw all points of the agents
        for(AbstractAgent agent : agents)
        {
            g.fillOval(10, 10, 1, 1);
        }
    }
}
