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

        MinimalPathOverseer overseer = new MinimalPathOverseer(map);
        agents.addAll(overseer.getAgents());
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
        floor.addPoint(new Vector2D(69, 325));
        floor.addPoint(new Vector2D(127, 118));
        floor.addPoint(new Vector2D(311, 43));
        floor.addPoint(new Vector2D(730, 32));
        floor.addPoint(new Vector2D(1_041, 175));
        floor.addPoint(new Vector2D(1_239, 492));
        floor.addPoint(new Vector2D(1_228, 758));
        floor.addPoint(new Vector2D(1_062, 963));
        floor.addPoint(new Vector2D(662, 977));
        floor.addPoint(new Vector2D(270, 957));
        floor.addPoint(new Vector2D(115, 775));

        MapPolygon obstacle0 = new MapPolygon(true);
        obstacle0.addPoint(new Vector2D(333, 564));
        obstacle0.addPoint(new Vector2D(376, 347));
        obstacle0.addPoint(new Vector2D(549, 456));
        obstacle0.addPoint(new Vector2D(421, 707));

        MapPolygon obstacle1 = new MapPolygon(true);
        obstacle1.addPoint(new Vector2D(940, 390));
        obstacle1.addPoint(new Vector2D(1_098, 500));
        obstacle1.addPoint(new Vector2D(1_051, 612));
        obstacle1.addPoint(new Vector2D(873, 592));

        return MapBuilder.create()
                .makeFloor(floor)
                .addObstacle(obstacle0)
                .addObstacle(obstacle1)
                .finish()
                .build();
    }

    public static Map getAverageMapAverageHoles()
    {
        MapPolygon floor = new MapPolygon(false);
        floor.addPoint(new Vector2D(69, 325));
        floor.addPoint(new Vector2D(127, 118));
        floor.addPoint(new Vector2D(311, 43));
        floor.addPoint(new Vector2D(730, 32));
        floor.addPoint(new Vector2D(1_041, 175));
        floor.addPoint(new Vector2D(1_239, 492));
        floor.addPoint(new Vector2D(1_228, 758));
        floor.addPoint(new Vector2D(1_062, 963));
        floor.addPoint(new Vector2D(662, 977));
        floor.addPoint(new Vector2D(270, 957));
        floor.addPoint(new Vector2D(115, 775));

        MapPolygon obstacle0 = new MapPolygon(true);
        obstacle0.addPoint(new Vector2D(333, 564));
        obstacle0.addPoint(new Vector2D(376, 347));
        obstacle0.addPoint(new Vector2D(549, 456));
        obstacle0.addPoint(new Vector2D(421, 707));

        MapPolygon obstacle1 = new MapPolygon(true);
        obstacle1.addPoint(new Vector2D(940, 390));
        obstacle1.addPoint(new Vector2D(1_098, 500));
        obstacle1.addPoint(new Vector2D(1_051, 612));
        obstacle1.addPoint(new Vector2D(873, 592));

        MapPolygon obstacle2 = new MapPolygon(true);
        obstacle2.addPoint(new Vector2D(561, 131));
        obstacle2.addPoint(new Vector2D(685, 116));
        obstacle2.addPoint(new Vector2D(756, 208));
        obstacle2.addPoint(new Vector2D(693, 264));
        obstacle2.addPoint(new Vector2D(574, 221));

        MapPolygon obstacle3 = new MapPolygon(true);
        obstacle3.addPoint(new Vector2D(732, 430));
        obstacle3.addPoint(new Vector2D(769, 545));
        obstacle3.addPoint(new Vector2D(681, 555));
        obstacle3.addPoint(new Vector2D(677, 385));

        MapPolygon obstacle4 = new MapPolygon(true);
        obstacle4.addPoint(new Vector2D(652, 764));
        obstacle4.addPoint(new Vector2D(838, 755));
        obstacle4.addPoint(new Vector2D(713, 860));

        return MapBuilder.create()
                .makeFloor(floor)
                .addObstacle(obstacle0)
                .addObstacle(obstacle1)
                .addObstacle(obstacle2)
                .addObstacle(obstacle3)
                .addObstacle(obstacle4)
                .finish()
                .build();
        }

    public static Map getAverageMapManyHoles()
    {
        MapPolygon floor = new MapPolygon(false);
        floor.addPoint(new Vector2D(69, 325));
        floor.addPoint(new Vector2D(127, 118));
        floor.addPoint(new Vector2D(311, 43));
        floor.addPoint(new Vector2D(730, 32));
        floor.addPoint(new Vector2D(1_041, 175));
        floor.addPoint(new Vector2D(1_239, 492));
        floor.addPoint(new Vector2D(1_228, 758));
        floor.addPoint(new Vector2D(1_062, 963));
        floor.addPoint(new Vector2D(662, 977));
        floor.addPoint(new Vector2D(270, 957));
        floor.addPoint(new Vector2D(115, 775));

        MapPolygon obstacle0 = new MapPolygon(true);
        obstacle0.addPoint(new Vector2D(333, 564));
        obstacle0.addPoint(new Vector2D(376, 347));
        obstacle0.addPoint(new Vector2D(549, 456));
        obstacle0.addPoint(new Vector2D(421, 707));

        MapPolygon obstacle1 = new MapPolygon(true);
        obstacle1.addPoint(new Vector2D(940, 390));
        obstacle1.addPoint(new Vector2D(1_098, 500));
        obstacle1.addPoint(new Vector2D(1_051, 612));
        obstacle1.addPoint(new Vector2D(873, 592));

        MapPolygon obstacle2 = new MapPolygon(true);
        obstacle2.addPoint(new Vector2D(561, 131));
        obstacle2.addPoint(new Vector2D(685, 116));
        obstacle2.addPoint(new Vector2D(756, 208));
        obstacle2.addPoint(new Vector2D(693, 264));
        obstacle2.addPoint(new Vector2D(574, 221));

        MapPolygon obstacle3 = new MapPolygon(true);
        obstacle3.addPoint(new Vector2D(732, 430));
        obstacle3.addPoint(new Vector2D(769, 545));
        obstacle3.addPoint(new Vector2D(681, 555));
        obstacle3.addPoint(new Vector2D(677, 385));

        MapPolygon obstacle4 = new MapPolygon(true);
        obstacle4.addPoint(new Vector2D(652, 764));
        obstacle4.addPoint(new Vector2D(838, 755));
        obstacle4.addPoint(new Vector2D(713, 860));

        MapPolygon obstacle5 = new MapPolygon(true);
        obstacle5.addPoint(new Vector2D(946, 708));
        obstacle5.addPoint(new Vector2D(1_092, 702));
        obstacle5.addPoint(new Vector2D(1_080, 808));
        obstacle5.addPoint(new Vector2D(990, 847));
        obstacle5.addPoint(new Vector2D(916, 789));

        MapPolygon obstacle6 = new MapPolygon(true);
        obstacle6.addPoint(new Vector2D(407, 770));
        obstacle6.addPoint(new Vector2D(504, 797));
        obstacle6.addPoint(new Vector2D(504, 845));
        obstacle6.addPoint(new Vector2D(512, 908));
        obstacle6.addPoint(new Vector2D(475, 913));
        obstacle6.addPoint(new Vector2D(417, 917));
        obstacle6.addPoint(new Vector2D(376, 868));
        obstacle6.addPoint(new Vector2D(348, 810));
        obstacle6.addPoint(new Vector2D(373, 774));

        MapPolygon obstacle7 = new MapPolygon(true);
        obstacle7.addPoint(new Vector2D(211, 193));
        obstacle7.addPoint(new Vector2D(263, 119));
        obstacle7.addPoint(new Vector2D(398, 193));
        obstacle7.addPoint(new Vector2D(378, 266));
        obstacle7.addPoint(new Vector2D(266, 327));
        obstacle7.addPoint(new Vector2D(166, 285));

        MapPolygon obstacle8 = new MapPolygon(true);
        obstacle8.addPoint(new Vector2D(853, 285));
        obstacle8.addPoint(new Vector2D(1_041, 295));
        obstacle8.addPoint(new Vector2D(986, 321));

        MapPolygon obstacle9 = new MapPolygon(true);
        obstacle9.addPoint(new Vector2D(818, 181));
        obstacle9.addPoint(new Vector2D(931, 165));
        obstacle9.addPoint(new Vector2D(988, 228));
        obstacle9.addPoint(new Vector2D(883, 238));

        return MapBuilder.create()
                .makeFloor(floor)
                .addObstacle(obstacle0)
                .addObstacle(obstacle1)
                .addObstacle(obstacle2)
                .addObstacle(obstacle3)
                .addObstacle(obstacle4)
                .addObstacle(obstacle5)
                .addObstacle(obstacle6)
                .addObstacle(obstacle7)
                .addObstacle(obstacle8)
                .addObstacle(obstacle9)
                .finish()
                .build();
    }

    public static Map getBigMapFewHoles()
    {
        MapPolygon floor = new MapPolygon(false);
        floor.addPoint(new Vector2D(81, 136));
        floor.addPoint(new Vector2D(331, 133));
        floor.addPoint(new Vector2D(328, 239));
        floor.addPoint(new Vector2D(225, 242));
        floor.addPoint(new Vector2D(222, 337));
        floor.addPoint(new Vector2D(903, 321));
        floor.addPoint(new Vector2D(894, 186));
        floor.addPoint(new Vector2D(736, 169));
        floor.addPoint(new Vector2D(745, 28));
        floor.addPoint(new Vector2D(1_135, 38));
        floor.addPoint(new Vector2D(1_187, 182));
        floor.addPoint(new Vector2D(1_050, 191));
        floor.addPoint(new Vector2D(1_070, 337));
        floor.addPoint(new Vector2D(1_279, 334));
        floor.addPoint(new Vector2D(1_286, 726));
        floor.addPoint(new Vector2D(1_037, 726));
        floor.addPoint(new Vector2D(1_015, 506));
        floor.addPoint(new Vector2D(1_119, 506));
        floor.addPoint(new Vector2D(1_118, 471));
        floor.addPoint(new Vector2D(468, 466));
        floor.addPoint(new Vector2D(464, 661));
        floor.addPoint(new Vector2D(591, 664));
        floor.addPoint(new Vector2D(590, 578));
        floor.addPoint(new Vector2D(734, 572));
        floor.addPoint(new Vector2D(719, 799));
        floor.addPoint(new Vector2D(59, 798));

        MapPolygon obstacle0 = new MapPolygon(true);
        obstacle0.addPoint(new Vector2D(233, 383));
        obstacle0.addPoint(new Vector2D(1_104, 387));
        obstacle0.addPoint(new Vector2D(1_108, 428));
        obstacle0.addPoint(new Vector2D(242, 419));

        return MapBuilder.create()
                .makeFloor(floor)
                .addObstacle(obstacle0)
                .finish()
                .build();
    }

    public static Map getBigMapAverageHoles() {
        MapPolygon floor = new MapPolygon(false);
        floor.addPoint(new Vector2D(81, 136));
        floor.addPoint(new Vector2D(331, 133));
        floor.addPoint(new Vector2D(328, 239));
        floor.addPoint(new Vector2D(225, 242));
        floor.addPoint(new Vector2D(222, 337));
        floor.addPoint(new Vector2D(903, 321));
        floor.addPoint(new Vector2D(894, 186));
        floor.addPoint(new Vector2D(736, 169));
        floor.addPoint(new Vector2D(745, 28));
        floor.addPoint(new Vector2D(1_135, 38));
        floor.addPoint(new Vector2D(1_187, 182));
        floor.addPoint(new Vector2D(1_050, 191));
        floor.addPoint(new Vector2D(1_070, 337));
        floor.addPoint(new Vector2D(1_279, 334));
        floor.addPoint(new Vector2D(1_286, 726));
        floor.addPoint(new Vector2D(1_037, 726));
        floor.addPoint(new Vector2D(1_015, 506));
        floor.addPoint(new Vector2D(1_119, 506));
        floor.addPoint(new Vector2D(1_118, 471));
        floor.addPoint(new Vector2D(468, 466));
        floor.addPoint(new Vector2D(464, 661));
        floor.addPoint(new Vector2D(591, 664));
        floor.addPoint(new Vector2D(590, 578));
        floor.addPoint(new Vector2D(734, 572));
        floor.addPoint(new Vector2D(719, 799));
        floor.addPoint(new Vector2D(59, 798));

        MapPolygon obstacle0 = new MapPolygon(true);
        obstacle0.addPoint(new Vector2D(233, 383));
        obstacle0.addPoint(new Vector2D(1_104, 387));
        obstacle0.addPoint(new Vector2D(1_108, 428));
        obstacle0.addPoint(new Vector2D(242, 419));

        MapPolygon obstacle1 = new MapPolygon(true);
        obstacle1.addPoint(new Vector2D(273, 624));
        obstacle1.addPoint(new Vector2D(401, 735));
        obstacle1.addPoint(new Vector2D(172, 720));

        MapPolygon obstacle2 = new MapPolygon(true);
        obstacle2.addPoint(new Vector2D(1_159, 579));
        obstacle2.addPoint(new Vector2D(1_199, 605));
        obstacle2.addPoint(new Vector2D(1_188, 647));
        obstacle2.addPoint(new Vector2D(1_131, 660));
        obstacle2.addPoint(new Vector2D(1_125, 622));

        MapPolygon obstacle3 = new MapPolygon(true);
        obstacle3.addPoint(new Vector2D(922, 75));
        obstacle3.addPoint(new Vector2D(1_080, 87));
        obstacle3.addPoint(new Vector2D(1_023, 139));
        obstacle3.addPoint(new Vector2D(845, 121));

        return MapBuilder.create()
                .makeFloor(floor)
                .addObstacle(obstacle0)
                .addObstacle(obstacle1)
                .addObstacle(obstacle2)
                .addObstacle(obstacle3)
                .finish()
                .build();
    }

    public static Map getBigMapManyHoles()
    {
        MapPolygon floor = new MapPolygon(false);
        floor.addPoint(new Vector2D(81, 136));
        floor.addPoint(new Vector2D(331, 133));
        floor.addPoint(new Vector2D(328, 239));
        floor.addPoint(new Vector2D(225, 242));
        floor.addPoint(new Vector2D(222, 337));
        floor.addPoint(new Vector2D(903, 321));
        floor.addPoint(new Vector2D(894, 186));
        floor.addPoint(new Vector2D(736, 169));
        floor.addPoint(new Vector2D(745, 28));
        floor.addPoint(new Vector2D(1_135, 38));
        floor.addPoint(new Vector2D(1_187, 182));
        floor.addPoint(new Vector2D(1_050, 191));
        floor.addPoint(new Vector2D(1_070, 337));
        floor.addPoint(new Vector2D(1_279, 334));
        floor.addPoint(new Vector2D(1_286, 726));
        floor.addPoint(new Vector2D(1_037, 726));
        floor.addPoint(new Vector2D(1_015, 506));
        floor.addPoint(new Vector2D(1_119, 506));
        floor.addPoint(new Vector2D(1_118, 471));
        floor.addPoint(new Vector2D(468, 466));
        floor.addPoint(new Vector2D(464, 661));
        floor.addPoint(new Vector2D(591, 664));
        floor.addPoint(new Vector2D(590, 578));
        floor.addPoint(new Vector2D(734, 572));
        floor.addPoint(new Vector2D(719, 799));
        floor.addPoint(new Vector2D(59, 798));

        MapPolygon obstacle0 = new MapPolygon(true);
        obstacle0.addPoint(new Vector2D(233, 383));
        obstacle0.addPoint(new Vector2D(1_104, 387));
        obstacle0.addPoint(new Vector2D(1_108, 428));
        obstacle0.addPoint(new Vector2D(242, 419));

        MapPolygon obstacle1 = new MapPolygon(true);
        obstacle1.addPoint(new Vector2D(273, 624));
        obstacle1.addPoint(new Vector2D(401, 735));
        obstacle1.addPoint(new Vector2D(172, 720));

        MapPolygon obstacle2 = new MapPolygon(true);
        obstacle2.addPoint(new Vector2D(1_159, 579));
        obstacle2.addPoint(new Vector2D(1_199, 605));
        obstacle2.addPoint(new Vector2D(1_188, 647));
        obstacle2.addPoint(new Vector2D(1_131, 660));
        obstacle2.addPoint(new Vector2D(1_125, 622));

        MapPolygon obstacle3 = new MapPolygon(true);
        obstacle3.addPoint(new Vector2D(922, 75));
        obstacle3.addPoint(new Vector2D(1_080, 87));
        obstacle3.addPoint(new Vector2D(1_023, 139));
        obstacle3.addPoint(new Vector2D(845, 121));

        MapPolygon obstacle4 = new MapPolygon(true);
        obstacle4.addPoint(new Vector2D(245, 480));
        obstacle4.addPoint(new Vector2D(274, 489));
        obstacle4.addPoint(new Vector2D(276, 515));
        obstacle4.addPoint(new Vector2D(278, 542));
        obstacle4.addPoint(new Vector2D(269, 548));
        obstacle4.addPoint(new Vector2D(245, 550));
        obstacle4.addPoint(new Vector2D(221, 548));
        obstacle4.addPoint(new Vector2D(212, 526));
        obstacle4.addPoint(new Vector2D(210, 505));
        obstacle4.addPoint(new Vector2D(223, 490));

        MapPolygon obstacle5 = new MapPolygon(true);
        obstacle5.addPoint(new Vector2D(232, 165));
        obstacle5.addPoint(new Vector2D(285, 192));
        obstacle5.addPoint(new Vector2D(176, 219));
        obstacle5.addPoint(new Vector2D(121, 200));
        obstacle5.addPoint(new Vector2D(169, 163));

        MapPolygon obstacle6 = new MapPolygon(true);
        obstacle6.addPoint(new Vector2D(640, 681));
        obstacle6.addPoint(new Vector2D(699, 667));
        obstacle6.addPoint(new Vector2D(670, 760));
        obstacle6.addPoint(new Vector2D(585, 751));

        MapPolygon obstacle7 = new MapPolygon(true);
        obstacle7.addPoint(new Vector2D(1_188, 380));
        obstacle7.addPoint(new Vector2D(1_239, 379));
        obstacle7.addPoint(new Vector2D(1_227, 531));
        obstacle7.addPoint(new Vector2D(1_196, 475));

        MapPolygon obstacle8 = new MapPolygon(true);
        obstacle8.addPoint(new Vector2D(382, 494));
        obstacle8.addPoint(new Vector2D(429, 495));
        obstacle8.addPoint(new Vector2D(424, 653));
        obstacle8.addPoint(new Vector2D(346, 561));

        return MapBuilder.create()
                .makeFloor(floor)
                .addObstacle(obstacle0)
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
