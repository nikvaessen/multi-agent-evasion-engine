package nl.dke.pursuitevasion;

import nl.dke.pursuitevasion.game.Vector2D;
import nl.dke.pursuitevasion.map.impl.Floor;
import nl.dke.pursuitevasion.map.impl.Map;
import org.jgrapht.GraphPath;
import org.jgrapht.WeightedGraph;
import org.jgrapht.alg.shortestpath.KShortestPaths;
import org.jgrapht.graph.DefaultWeightedEdge;

import java.util.List;
import java.util.Set;


/**
 * Created by nik on 5/24/17.
 */
public class VisibilityGraphTest
{
    public static void main(String[] args)
    {
        testShortestPaths();
    }

    private static void testShortestPaths()
    {
        Map map = Map.getSimpleMap();

        Floor f = map.getFloors().iterator().next();

        WeightedGraph<Vector2D, DefaultWeightedEdge> g = f.getVisibilityGraph();

        Set<Vector2D> vertexes = g.vertexSet();
        int count = 0;
        Vector2D v1 = null, v2 = null;
        System.out.printf("There are %d vertexes:\n", vertexes.size());
        for(Vector2D v : vertexes)
        {
            if(count == 0)
            {
                v1 = v;
            }
            if(count == 2)
            {
                v2 = v;
            }
            count++;
            System.out.println(v);
        }

        Set<DefaultWeightedEdge> edges = g.edgeSet();
        System.out.printf("There are %d edges:\n", edges.size());
        for(DefaultWeightedEdge e : edges)
        {
            System.out.println(e + " " + g.getEdgeWeight(e));
        }


        KShortestPaths<Vector2D, DefaultWeightedEdge> kShortestPaths = new KShortestPaths<>(g, 3);


        List<GraphPath<Vector2D, DefaultWeightedEdge>> paths =
                kShortestPaths.getPaths(v1, v2);

        System.out.println(paths.size());
        for(GraphPath p : paths)
        {
            System.out.println(p);
        }

    }


}
