package nl.dke.pursuitevasion;

import nl.dke.pursuitevasion.map.builders.MapBuilder;
import org.junit.Assert;
import org.junit.Test;

import java.awt.*;


/**
 * Created by nik on 2/9/17.
 */
public class MapBuilderTest
{
    @Test
    public void constructAMap()
    {
        Polygon mainFloor = new Polygon(
                new int[]{0, 100, 100, 0},
                new int[]{0, 0, 100, 100},
                4
        );

        Polygon obstacle = new Polygon(
                new int[]{40, 60, 60, 40},
                new int[]{40, 40, 60, 60},
                4
        );

        Assert.assertNotNull(
                MapBuilder.create()
                        .makeFloor(mainFloor)
                        .addObstacle(obstacle)
                        .finish()
                        .build()
        );
    }

}
