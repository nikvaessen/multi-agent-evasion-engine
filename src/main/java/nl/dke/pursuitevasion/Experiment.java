package nl.dke.pursuitevasion;

import nl.dke.pursuitevasion.game.Vector2D;
import nl.dke.pursuitevasion.map.MapPolygon;
import nl.dke.pursuitevasion.map.builders.MapBuilder;
import nl.dke.pursuitevasion.map.impl.Map;

/**
 * Created by nik on 6/22/17.
 */
public class Experiment
{
    public static Map getSmallMapFewHoles()
    {
        MapPolygon floor = new MapPolygon(false);
        floor.addPoint(new Vector2D(0,0));
        floor.addPoint(new Vector2D(100, 0));
        floor.addPoint(new Vector2D(100, 100));
        floor.addPoint(new Vector2D(0, 100));

        MapPolygon obstacle = new MapPolygon(true);
        obstacle.addPoint(new Vector2D());
        obstacle.addPoint(new Vector2D());
        obstacle.addPoint(new Vector2D());
        obstacle.addPoint(new Vector2D());

        return MapBuilder.create()
                .makeFloor(floor)
                .addObstacle(obstacle)
                .finish()
                .build();
    }

    public static Map getSmallMapAverageHoles()
    {
        return MapBuilder.create().build();
    }

    public static Map getSmallMapManyHoles()
    {
        return MapBuilder.create().build();
    }
    
    public static Map getAverageMapFewHoles()
    {
        return MapBuilder.create().build();

    }

    public static Map getAverageMapAverageHoles()
    {
        return MapBuilder.create().build();
    }

    public static Map getAverageMapManyHoles()
    {
        return MapBuilder.create().build();
    }

    public static Map getBigMapFewHoles()
    {
        return MapBuilder.create().build();

    }

    public static Map getBigMapAverageHoles()
    {
        return MapBuilder.create().build();
    }

    public static Map getBigMapManyHoles()
    {
        return MapBuilder.create().build();
    }

}
