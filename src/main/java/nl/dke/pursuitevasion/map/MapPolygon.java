package nl.dke.pursuitevasion.map;

import java.awt.*;

/**
 * Created by nik on 26/02/17.
 */
public class MapPolygon extends java.awt.Polygon
{
    private boolean solid;

    public MapPolygon(int[] xPoints, int[] yPoints, int n, boolean solid)
    {
        super(xPoints, yPoints, n);
        this.solid = solid;
    }

    public MapPolygon(Polygon polygon, boolean solid)
    {
        this(polygon.xpoints, polygon.ypoints, polygon.npoints, solid);
    }

    public boolean isSolid()
    {
        return solid;
    }
}
