package nl.dke.pursuitevasion;

import nl.dke.pursuitevasion.map.builders.MapBuilder;
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
        MapBuilder.create(1)
                .makeFloor()
                    .addObstacle(new Polygon())
                    .addObstacle(new Polygon())
                    .finish(new Polygon())
                .makeFloor()
                    .addObstacle(new Polygon())
                    .finish(new Polygon())
                .makeFloor()
                    .finish(new Polygon())
                //.makeGate()
                //.verify()
                .build();
    }
}
