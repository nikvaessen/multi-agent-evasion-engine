package nl.dke.pursuitevasion;

import nl.dke.pursuitevasion.game.Engine;
import nl.dke.pursuitevasion.game.EngineConstants;
import nl.dke.pursuitevasion.game.Vector2D;
import nl.dke.pursuitevasion.game.agents.AbstractAgent;
import nl.dke.pursuitevasion.game.agents.Direction;
import nl.dke.pursuitevasion.game.agents.impl.DistanceAgent;
import nl.dke.pursuitevasion.game.agents.impl.MCTS.CoordinatorEvaderKillKillKillEmAll;
import nl.dke.pursuitevasion.game.agents.impl.RandomAgent;
import nl.dke.pursuitevasion.game.agents.impl.minimalPath.MinimalPathAgent;
import nl.dke.pursuitevasion.game.agents.impl.minimalPath.MinimalPathOverseer;
import nl.dke.pursuitevasion.map.MapPolygon;
import nl.dke.pursuitevasion.map.builders.MapBuilder;
import nl.dke.pursuitevasion.map.impl.Floor;
import nl.dke.pursuitevasion.map.impl.Map;
import org.jgrapht.experimental.alg.IntArrayGraphAlgorithm;

import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Callable;
import java.util.concurrent.Future;

/**
 * Created by nik on 6/22/17.
 */
public class Experiment
{

    private Engine engine;

    public Experiment(List<Class> evaders, Map map) throws ClassNotFoundException{

        List<AbstractAgent> agents = new ArrayList<>();
        Engine simulationEngine = new Engine(map, agents, 60);
        Floor f = map.getFloors().iterator().next();
        for (Class c : evaders) {

            if(c == CoordinatorEvaderKillKillKillEmAll.class){
                CoordinatorEvaderKillKillKillEmAll a = new CoordinatorEvaderKillKillKillEmAll(simulationEngine, map, f, map.getEvaderSpawnLocation(), Direction.NORTH, EngineConstants.AGENT_RADIUS, EngineConstants.VISION_RANGE, EngineConstants.VISION_ANGLE, agents, false);
            }
            else if(c == DistanceAgent.class){
                agents.add(new DistanceAgent(map, f, map.getEvaderSpawnLocation(), Direction.NORTH, EngineConstants.AGENT_RADIUS, EngineConstants.VISION_RANGE, EngineConstants.VISION_ANGLE));
            }
            else{
                throw new ClassNotFoundException("Class is not known");
            }
        }
        MinimalPathOverseer overseer = new MinimalPathOverseer(map);
        agents.addAll(overseer.getAgents());
        engine = simulationEngine;

    }

    public Future<Integer> start(){
        return engine.start();
    }


    public static Map getSmallMapFewHoles()
    {
        MapPolygon floor = new MapPolygon(false);
        floor.addPoint(new Vector2D(0,0));
        floor.addPoint(new Vector2D(600, 0));
        floor.addPoint(new Vector2D(600, 600));
        floor.addPoint(new Vector2D(0, 600));

        MapPolygon obstacle = new MapPolygon(true);
        obstacle.addPoint(new Vector2D(240, 240));
        obstacle.addPoint(new Vector2D(360, 240));
        obstacle.addPoint(new Vector2D(360, 360));
        obstacle.addPoint(new Vector2D(240, 360));

        return MapBuilder.create()
                .makeFloor(floor)
                .addObstacle(obstacle)
                .finish()
                .build();
    }

    public static Map getSmallMapAverageHoles()
    {
        MapPolygon floor = new MapPolygon(false);
        floor.addPoint(new Vector2D(0,0));
        floor.addPoint(new Vector2D(600, 0));
        floor.addPoint(new Vector2D(600, 600));
        floor.addPoint(new Vector2D(0, 600));

        MapPolygon obstacle1 = new MapPolygon(true);
        obstacle1.addPoint(new Vector2D(100, 100));
        obstacle1.addPoint(new Vector2D(200, 100));
        obstacle1.addPoint(new Vector2D(200, 200));
        obstacle1.addPoint(new Vector2D(100, 200));

        MapPolygon obstacle2 = new MapPolygon(true);
        obstacle2.addPoint(new Vector2D(400, 100));
        obstacle2.addPoint(new Vector2D(500, 100));
        obstacle2.addPoint(new Vector2D(500, 200));
        obstacle2.addPoint(new Vector2D(400, 200));

        MapPolygon obstacle3 = new MapPolygon(true);
        obstacle3.addPoint(new Vector2D(250, 250));
        obstacle3.addPoint(new Vector2D(350, 250));
        obstacle3.addPoint(new Vector2D(350, 450));
        obstacle3.addPoint(new Vector2D(250, 450));

        MapPolygon obstacle4 = new MapPolygon(true);
        obstacle4.addPoint(new Vector2D(150, 475));
        obstacle4.addPoint(new Vector2D(450, 475));
        obstacle4.addPoint(new Vector2D(450, 575));
        obstacle4.addPoint(new Vector2D(150, 575));

        return MapBuilder.create()
                .makeFloor(floor)
                    .addObstacle(obstacle1)
                    .addObstacle(obstacle2)
                    .addObstacle(obstacle3)
                    .addObstacle(obstacle4)
                .finish()
                .build();
    }

    public static Map getSmallMapManyHoles()
    {
        MapPolygon floor = new MapPolygon(false);
        floor.addPoint(new Vector2D(0,0));
        floor.addPoint(new Vector2D(600, 0));
        floor.addPoint(new Vector2D(600, 600));
        floor.addPoint(new Vector2D(0, 600));

        MapPolygon obstacle1 = new MapPolygon(true);
        obstacle1.addPoint(new Vector2D(25, 50));
        obstacle1.addPoint(new Vector2D(75, 50));
        obstacle1.addPoint(new Vector2D(75, 550));
        obstacle1.addPoint(new Vector2D(25, 550));

        MapPolygon obstacle2 = new MapPolygon(true);
        obstacle2.addPoint(new Vector2D(525, 50));
        obstacle2.addPoint(new Vector2D(575, 50));
        obstacle2.addPoint(new Vector2D(575, 550));
        obstacle2.addPoint(new Vector2D(525, 550));

        MapPolygon obstacle3 = new MapPolygon(true);
        obstacle3.addPoint(new Vector2D(100, 50));
        obstacle3.addPoint(new Vector2D(500, 50));
        obstacle3.addPoint(new Vector2D(500, 100));
        obstacle3.addPoint(new Vector2D(100, 100));

        MapPolygon obstacle4 = new MapPolygon(true);
        obstacle4.addPoint(new Vector2D(110, 125));
        obstacle4.addPoint(new Vector2D(490, 125));
        obstacle4.addPoint(new Vector2D(490, 175));
        obstacle4.addPoint(new Vector2D(110, 175));

        MapPolygon obstacle5 = new MapPolygon(true);
        obstacle5.addPoint(new Vector2D(110, 200));
        obstacle5.addPoint(new Vector2D(490, 200));
        obstacle5.addPoint(new Vector2D(490, 250));
        obstacle5.addPoint(new Vector2D(110, 250));

        MapPolygon obstacle6 = new MapPolygon(true);
        obstacle6.addPoint(new Vector2D(110, 350));
        obstacle6.addPoint(new Vector2D(490, 350));
        obstacle6.addPoint(new Vector2D(490, 400));
        obstacle6.addPoint(new Vector2D(110, 400));

        MapPolygon obstacle7 = new MapPolygon(true);
        obstacle7.addPoint(new Vector2D(110, 425));
        obstacle7.addPoint(new Vector2D(490, 425));
        obstacle7.addPoint(new Vector2D(490, 475));
        obstacle7.addPoint(new Vector2D(110, 475));

        MapPolygon obstacle8 = new MapPolygon(true);
        obstacle8.addPoint(new Vector2D(100, 500));
        obstacle8.addPoint(new Vector2D(500, 500));
        obstacle8.addPoint(new Vector2D(500, 550));
        obstacle8.addPoint(new Vector2D(100, 550));

        return MapBuilder.create().
                makeFloor(floor)
                .addObstacle(obstacle1)
                .addObstacle(obstacle2)
                .addObstacle(obstacle3)
                .addObstacle(obstacle4)
                .addObstacle(obstacle5)
                .addObstacle(obstacle6)
                .addObstacle(obstacle7)
                .addObstacle(obstacle8)
                .finish()
                .build();
    }
    
    public static Map getAverageMapFewHoles()
    {
        MapPolygon floor = new MapPolygon(false);
        floor.addPoint(new Vector2D(0,0));
        floor.addPoint(new Vector2D(600, 0));
        floor.addPoint(new Vector2D(600, 600));
        floor.addPoint(new Vector2D(0, 600));

        MapPolygon obstacle1 = new MapPolygon(true);
        obstacle1.addPoint(new Vector2D());
        obstacle1.addPoint(new Vector2D());
        obstacle1.addPoint(new Vector2D());
        obstacle1.addPoint(new Vector2D());

        MapPolygon obstacle2 = new MapPolygon(true);
        obstacle2.addPoint(new Vector2D());
        obstacle2.addPoint(new Vector2D());
        obstacle2.addPoint(new Vector2D());
        obstacle2.addPoint(new Vector2D());

        MapPolygon obstacle3 = new MapPolygon(true);
        obstacle3.addPoint(new Vector2D());
        obstacle3.addPoint(new Vector2D());
        obstacle3.addPoint(new Vector2D());
        obstacle3.addPoint(new Vector2D());

        MapPolygon obstacle4 = new MapPolygon(true);
        obstacle4.addPoint(new Vector2D());
        obstacle4.addPoint(new Vector2D());
        obstacle4.addPoint(new Vector2D());
        obstacle4.addPoint(new Vector2D());

        MapPolygon obstacle5 = new MapPolygon(true);
        obstacle5.addPoint(new Vector2D());
        obstacle5.addPoint(new Vector2D());
        obstacle5.addPoint(new Vector2D());
        obstacle5.addPoint(new Vector2D());

        MapPolygon obstacle6 = new MapPolygon(true);
        obstacle6.addPoint(new Vector2D());
        obstacle6.addPoint(new Vector2D());
        obstacle6.addPoint(new Vector2D());
        obstacle6.addPoint(new Vector2D());

        MapPolygon obstacle7 = new MapPolygon(true);
        obstacle7.addPoint(new Vector2D());
        obstacle7.addPoint(new Vector2D());
        obstacle7.addPoint(new Vector2D());
        obstacle7.addPoint(new Vector2D());

        MapPolygon obstacle8 = new MapPolygon(true);
        obstacle8.addPoint(new Vector2D());
        obstacle8.addPoint(new Vector2D());
        obstacle8.addPoint(new Vector2D());
        obstacle8.addPoint(new Vector2D());

        return MapBuilder.create().
                makeFloor(floor)
                .addObstacle(obstacle1)
                .addObstacle(obstacle2)
                .addObstacle(obstacle3)
                .addObstacle(obstacle4)
                .addObstacle(obstacle5)
                .addObstacle(obstacle6)
                .addObstacle(obstacle7)
                .addObstacle(obstacle8)
                .finish()
                .build();
    }

    public static Map getAverageMapAverageHoles()
    {
        MapPolygon floor = new MapPolygon(false);
        floor.addPoint(new Vector2D(0,0));
        floor.addPoint(new Vector2D(600, 0));
        floor.addPoint(new Vector2D(600, 600));
        floor.addPoint(new Vector2D(0, 600));

        MapPolygon obstacle1 = new MapPolygon(true);
        obstacle1.addPoint(new Vector2D());
        obstacle1.addPoint(new Vector2D());
        obstacle1.addPoint(new Vector2D());
        obstacle1.addPoint(new Vector2D());

        MapPolygon obstacle2 = new MapPolygon(true);
        obstacle2.addPoint(new Vector2D());
        obstacle2.addPoint(new Vector2D());
        obstacle2.addPoint(new Vector2D());
        obstacle2.addPoint(new Vector2D());

        MapPolygon obstacle3 = new MapPolygon(true);
        obstacle3.addPoint(new Vector2D());
        obstacle3.addPoint(new Vector2D());
        obstacle3.addPoint(new Vector2D());
        obstacle3.addPoint(new Vector2D());

        MapPolygon obstacle4 = new MapPolygon(true);
        obstacle4.addPoint(new Vector2D());
        obstacle4.addPoint(new Vector2D());
        obstacle4.addPoint(new Vector2D());
        obstacle4.addPoint(new Vector2D());

        MapPolygon obstacle5 = new MapPolygon(true);
        obstacle5.addPoint(new Vector2D());
        obstacle5.addPoint(new Vector2D());
        obstacle5.addPoint(new Vector2D());
        obstacle5.addPoint(new Vector2D());

        MapPolygon obstacle6 = new MapPolygon(true);
        obstacle6.addPoint(new Vector2D());
        obstacle6.addPoint(new Vector2D());
        obstacle6.addPoint(new Vector2D());
        obstacle6.addPoint(new Vector2D());

        MapPolygon obstacle7 = new MapPolygon(true);
        obstacle7.addPoint(new Vector2D());
        obstacle7.addPoint(new Vector2D());
        obstacle7.addPoint(new Vector2D());
        obstacle7.addPoint(new Vector2D());

        MapPolygon obstacle8 = new MapPolygon(true);
        obstacle8.addPoint(new Vector2D());
        obstacle8.addPoint(new Vector2D());
        obstacle8.addPoint(new Vector2D());
        obstacle8.addPoint(new Vector2D());

        return MapBuilder.create().
                makeFloor(floor)
                .addObstacle(obstacle1)
                .addObstacle(obstacle2)
                .addObstacle(obstacle3)
                .addObstacle(obstacle4)
                .addObstacle(obstacle5)
                .addObstacle(obstacle6)
                .addObstacle(obstacle7)
                .addObstacle(obstacle8)
                .finish()
                .build();
    }

    public static Map getAverageMapManyHoles()
    {
        MapPolygon floor = new MapPolygon(false);
        floor.addPoint(new Vector2D(0,0));
        floor.addPoint(new Vector2D(600, 0));
        floor.addPoint(new Vector2D(600, 600));
        floor.addPoint(new Vector2D(0, 600));

        MapPolygon obstacle1 = new MapPolygon(true);
        obstacle1.addPoint(new Vector2D());
        obstacle1.addPoint(new Vector2D());
        obstacle1.addPoint(new Vector2D());
        obstacle1.addPoint(new Vector2D());

        MapPolygon obstacle2 = new MapPolygon(true);
        obstacle2.addPoint(new Vector2D());
        obstacle2.addPoint(new Vector2D());
        obstacle2.addPoint(new Vector2D());
        obstacle2.addPoint(new Vector2D());

        MapPolygon obstacle3 = new MapPolygon(true);
        obstacle3.addPoint(new Vector2D());
        obstacle3.addPoint(new Vector2D());
        obstacle3.addPoint(new Vector2D());
        obstacle3.addPoint(new Vector2D());

        MapPolygon obstacle4 = new MapPolygon(true);
        obstacle4.addPoint(new Vector2D());
        obstacle4.addPoint(new Vector2D());
        obstacle4.addPoint(new Vector2D());
        obstacle4.addPoint(new Vector2D());

        MapPolygon obstacle5 = new MapPolygon(true);
        obstacle5.addPoint(new Vector2D());
        obstacle5.addPoint(new Vector2D());
        obstacle5.addPoint(new Vector2D());
        obstacle5.addPoint(new Vector2D());

        MapPolygon obstacle6 = new MapPolygon(true);
        obstacle6.addPoint(new Vector2D());
        obstacle6.addPoint(new Vector2D());
        obstacle6.addPoint(new Vector2D());
        obstacle6.addPoint(new Vector2D());

        MapPolygon obstacle7 = new MapPolygon(true);
        obstacle7.addPoint(new Vector2D());
        obstacle7.addPoint(new Vector2D());
        obstacle7.addPoint(new Vector2D());
        obstacle7.addPoint(new Vector2D());

        MapPolygon obstacle8 = new MapPolygon(true);
        obstacle8.addPoint(new Vector2D());
        obstacle8.addPoint(new Vector2D());
        obstacle8.addPoint(new Vector2D());
        obstacle8.addPoint(new Vector2D());

        return MapBuilder.create().
                makeFloor(floor)
                .addObstacle(obstacle1)
                .addObstacle(obstacle2)
                .addObstacle(obstacle3)
                .addObstacle(obstacle4)
                .addObstacle(obstacle5)
                .addObstacle(obstacle6)
                .addObstacle(obstacle7)
                .addObstacle(obstacle8)
                .finish()
                .build();
    }

    public static Map getBigMapFewHoles()
    {
        MapPolygon floor = new MapPolygon(false);
        floor.addPoint(new Vector2D(0,0));
        floor.addPoint(new Vector2D(600, 0));
        floor.addPoint(new Vector2D(600, 600));
        floor.addPoint(new Vector2D(0, 600));

        MapPolygon obstacle1 = new MapPolygon(true);
        obstacle1.addPoint(new Vector2D());
        obstacle1.addPoint(new Vector2D());
        obstacle1.addPoint(new Vector2D());
        obstacle1.addPoint(new Vector2D());

        MapPolygon obstacle2 = new MapPolygon(true);
        obstacle2.addPoint(new Vector2D());
        obstacle2.addPoint(new Vector2D());
        obstacle2.addPoint(new Vector2D());
        obstacle2.addPoint(new Vector2D());

        MapPolygon obstacle3 = new MapPolygon(true);
        obstacle3.addPoint(new Vector2D());
        obstacle3.addPoint(new Vector2D());
        obstacle3.addPoint(new Vector2D());
        obstacle3.addPoint(new Vector2D());

        MapPolygon obstacle4 = new MapPolygon(true);
        obstacle4.addPoint(new Vector2D());
        obstacle4.addPoint(new Vector2D());
        obstacle4.addPoint(new Vector2D());
        obstacle4.addPoint(new Vector2D());

        MapPolygon obstacle5 = new MapPolygon(true);
        obstacle5.addPoint(new Vector2D());
        obstacle5.addPoint(new Vector2D());
        obstacle5.addPoint(new Vector2D());
        obstacle5.addPoint(new Vector2D());

        MapPolygon obstacle6 = new MapPolygon(true);
        obstacle6.addPoint(new Vector2D());
        obstacle6.addPoint(new Vector2D());
        obstacle6.addPoint(new Vector2D());
        obstacle6.addPoint(new Vector2D());

        MapPolygon obstacle7 = new MapPolygon(true);
        obstacle7.addPoint(new Vector2D());
        obstacle7.addPoint(new Vector2D());
        obstacle7.addPoint(new Vector2D());
        obstacle7.addPoint(new Vector2D());

        MapPolygon obstacle8 = new MapPolygon(true);
        obstacle8.addPoint(new Vector2D());
        obstacle8.addPoint(new Vector2D());
        obstacle8.addPoint(new Vector2D());
        obstacle8.addPoint(new Vector2D());

        return MapBuilder.create().
                makeFloor(floor)
                .addObstacle(obstacle1)
                .addObstacle(obstacle2)
                .addObstacle(obstacle3)
                .addObstacle(obstacle4)
                .addObstacle(obstacle5)
                .addObstacle(obstacle6)
                .addObstacle(obstacle7)
                .addObstacle(obstacle8)
                .finish()
                .build();
    }

    public static Map getBigMapAverageHoles()
    {
        MapPolygon floor = new MapPolygon(false);
        floor.addPoint(new Vector2D(0,0));
        floor.addPoint(new Vector2D(600, 0));
        floor.addPoint(new Vector2D(600, 600));
        floor.addPoint(new Vector2D(0, 600));

        MapPolygon obstacle1 = new MapPolygon(true);
        obstacle1.addPoint(new Vector2D());
        obstacle1.addPoint(new Vector2D());
        obstacle1.addPoint(new Vector2D());
        obstacle1.addPoint(new Vector2D());

        MapPolygon obstacle2 = new MapPolygon(true);
        obstacle2.addPoint(new Vector2D());
        obstacle2.addPoint(new Vector2D());
        obstacle2.addPoint(new Vector2D());
        obstacle2.addPoint(new Vector2D());

        MapPolygon obstacle3 = new MapPolygon(true);
        obstacle3.addPoint(new Vector2D());
        obstacle3.addPoint(new Vector2D());
        obstacle3.addPoint(new Vector2D());
        obstacle3.addPoint(new Vector2D());

        MapPolygon obstacle4 = new MapPolygon(true);
        obstacle4.addPoint(new Vector2D());
        obstacle4.addPoint(new Vector2D());
        obstacle4.addPoint(new Vector2D());
        obstacle4.addPoint(new Vector2D());

        MapPolygon obstacle5 = new MapPolygon(true);
        obstacle5.addPoint(new Vector2D());
        obstacle5.addPoint(new Vector2D());
        obstacle5.addPoint(new Vector2D());
        obstacle5.addPoint(new Vector2D());

        MapPolygon obstacle6 = new MapPolygon(true);
        obstacle6.addPoint(new Vector2D());
        obstacle6.addPoint(new Vector2D());
        obstacle6.addPoint(new Vector2D());
        obstacle6.addPoint(new Vector2D());

        MapPolygon obstacle7 = new MapPolygon(true);
        obstacle7.addPoint(new Vector2D());
        obstacle7.addPoint(new Vector2D());
        obstacle7.addPoint(new Vector2D());
        obstacle7.addPoint(new Vector2D());

        MapPolygon obstacle8 = new MapPolygon(true);
        obstacle8.addPoint(new Vector2D());
        obstacle8.addPoint(new Vector2D());
        obstacle8.addPoint(new Vector2D());
        obstacle8.addPoint(new Vector2D());

        return MapBuilder.create().
                makeFloor(floor)
                .addObstacle(obstacle1)
                .addObstacle(obstacle2)
                .addObstacle(obstacle3)
                .addObstacle(obstacle4)
                .addObstacle(obstacle5)
                .addObstacle(obstacle6)
                .addObstacle(obstacle7)
                .addObstacle(obstacle8)
                .finish()
                .build();
    }

    public static Map getBigMapManyHoles()
    {
        MapPolygon floor = new MapPolygon(false);
        floor.addPoint(new Vector2D(0,0));
        floor.addPoint(new Vector2D(600, 0));
        floor.addPoint(new Vector2D(600, 600));
        floor.addPoint(new Vector2D(0, 600));

        MapPolygon obstacle1 = new MapPolygon(true);
        obstacle1.addPoint(new Vector2D());
        obstacle1.addPoint(new Vector2D());
        obstacle1.addPoint(new Vector2D());
        obstacle1.addPoint(new Vector2D());

        MapPolygon obstacle2 = new MapPolygon(true);
        obstacle2.addPoint(new Vector2D());
        obstacle2.addPoint(new Vector2D());
        obstacle2.addPoint(new Vector2D());
        obstacle2.addPoint(new Vector2D());

        MapPolygon obstacle3 = new MapPolygon(true);
        obstacle3.addPoint(new Vector2D());
        obstacle3.addPoint(new Vector2D());
        obstacle3.addPoint(new Vector2D());
        obstacle3.addPoint(new Vector2D());

        MapPolygon obstacle4 = new MapPolygon(true);
        obstacle4.addPoint(new Vector2D());
        obstacle4.addPoint(new Vector2D());
        obstacle4.addPoint(new Vector2D());
        obstacle4.addPoint(new Vector2D());

        MapPolygon obstacle5 = new MapPolygon(true);
        obstacle5.addPoint(new Vector2D());
        obstacle5.addPoint(new Vector2D());
        obstacle5.addPoint(new Vector2D());
        obstacle5.addPoint(new Vector2D());

        MapPolygon obstacle6 = new MapPolygon(true);
        obstacle6.addPoint(new Vector2D());
        obstacle6.addPoint(new Vector2D());
        obstacle6.addPoint(new Vector2D());
        obstacle6.addPoint(new Vector2D());

        MapPolygon obstacle7 = new MapPolygon(true);
        obstacle7.addPoint(new Vector2D());
        obstacle7.addPoint(new Vector2D());
        obstacle7.addPoint(new Vector2D());
        obstacle7.addPoint(new Vector2D());

        MapPolygon obstacle8 = new MapPolygon(true);
        obstacle8.addPoint(new Vector2D());
        obstacle8.addPoint(new Vector2D());
        obstacle8.addPoint(new Vector2D());
        obstacle8.addPoint(new Vector2D());

        return MapBuilder.create().
                makeFloor(floor)
                .addObstacle(obstacle1)
                .addObstacle(obstacle2)
                .addObstacle(obstacle3)
                .addObstacle(obstacle4)
                .addObstacle(obstacle5)
                .addObstacle(obstacle6)
                .addObstacle(obstacle7)
                .addObstacle(obstacle8)
                .finish()
                .build();
    }

    public static List<Callable<Map>> getMapFunctions() {
        ArrayList<Callable<Map>> functions = new ArrayList<>();
        ///
        functions.add(Experiment::getAverageMapFewHoles);
        functions.add(Experiment::getAverageMapAverageHoles);
        functions.add(Experiment::getAverageMapManyHoles);

        functions.add(Experiment::getBigMapFewHoles);
        functions.add(Experiment::getBigMapAverageHoles);
        functions.add(Experiment::getBigMapManyHoles);

        functions.add(Experiment::getSmallMapFewHoles);
        functions.add(Experiment::getSmallMapAverageHoles);
        functions.add(Experiment::getSmallMapManyHoles);
        ///
        return functions;
    }
}
