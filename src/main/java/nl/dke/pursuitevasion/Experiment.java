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
