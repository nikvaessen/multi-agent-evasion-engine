package nl.dke.pursuitevasion;

import com.sun.corba.se.impl.orbutil.graph.Graph;
import nl.dke.pursuitevasion.game.Vector2D;
import nl.dke.pursuitevasion.map.MapPolygon;
import nl.dke.pursuitevasion.map.impl.Floor;
import nl.dke.pursuitevasion.map.impl.Map;
import nl.dke.pursuitevasion.map.impl.Obstacle;
import org.jgrapht.GraphPath;
import org.jgrapht.WeightedGraph;
import org.jgrapht.alg.interfaces.KShortestPathAlgorithm;
import org.jgrapht.alg.shortestpath.KShortestPaths;
import org.jgrapht.graph.DefaultWeightedEdge;

import java.awt.*;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by nik on 13/06/17.
 */
public class FloorSplitTest
{
    public static void main(String[] args)
    {
        testSplitting();
    }

    public static void testSplitting()
    {
        Map map = Map.getSimpleMap();
        Floor floor = map.getFloors().iterator().next();

        WeightedGraph<Vector2D, DefaultWeightedEdge> vg = floor.getVisibilityGraph();

        KShortestPathAlgorithm<Vector2D, DefaultWeightedEdge> kShortestPathAlgorithm =
            new KShortestPaths<Vector2D, DefaultWeightedEdge>(vg, 1);

        Vector2D u = new Vector2D(0,0);
        Vector2D v = new Vector2D(600, 600);

        List<GraphPath<Vector2D, DefaultWeightedEdge>> paths =
            kShortestPathAlgorithm.getPaths(u, v);

        GraphPath<Vector2D, DefaultWeightedEdge> shortestPath = paths.get(0);

        System.out.printf("Shortest path from u to v:\n%s\n", shortestPath.getVertexList() );
        System.out.printf("Vertexes of main floor:\n%s\n", floor.getPolygonGraph().vertexSet());
        System.out.printf("Edges of main floor:\n%s\n", floor.getPolygonGraph().edgeSet());

        System.out.printf("Vertexes of obstacle:\n%s\n" , floor.getObstacles().iterator().next().getPolygonGraph()
                .vertexSet());


        ArrayList<Floor> floors = floor.getSubFloors(shortestPath);
        printFloor(floors.get(0));
        printFloor(floors.get(1));
    }

    private static void printFloor(Floor f)
    {
        System.out.printf("main floor polygon: %s\n", f.getPolygonGraph().vertexSet());
        for(Obstacle o: f.getObstacles())
        {
            System.out.printf("\tobstacle: %s \n", o.getPolygonGraph().vertexSet());
        }
    }

    private static void testContaining()
    {
        Polygon p1 = new Polygon();
        p1.addPoint(0, 0);
        p1.addPoint(600,0);
        p1.addPoint(600,600);
        p1.addPoint(360, 240);

        Polygon p2 = new Polygon();
        p2.addPoint(0,0);
        p2.addPoint(360, 240);
        p2.addPoint(600, 600);
        p2.addPoint(0, 600);

        MapPolygon obstacle = new MapPolygon();
        obstacle.addPoint(240,240);
        obstacle.addPoint(360, 240);
        obstacle.addPoint(360, 360);
        obstacle.addPoint(240, 360);

        //System.out.println("Checking for p1 = " + p1.getPoints());
        for(Vector2D v : obstacle.getPoints())
        {
            boolean vInsideP = p1.contains(v.toPoint());
            System.out.printf("v = %s is inside P: %b\n", v, vInsideP);
        }

        //System.out.println("Checking for p2 = " + p2.getPoints());
        for(Vector2D v : obstacle.getPoints())
        {
            boolean vInsideP = p2.contains(new Double(v.getX()).intValue(), new Double(v.getY()).intValue());
            System.out.printf("v = %s is inside P: %b\n", v, vInsideP);
        }

        System.out.println(p1.contains(360,240));
        System.out.println(p2.contains(360, 240));
    }

}
