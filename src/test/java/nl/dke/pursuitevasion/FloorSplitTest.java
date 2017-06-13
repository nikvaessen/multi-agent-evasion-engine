package nl.dke.pursuitevasion;

import com.sun.corba.se.impl.orbutil.graph.Graph;
import nl.dke.pursuitevasion.game.Vector2D;
import nl.dke.pursuitevasion.map.impl.Floor;
import nl.dke.pursuitevasion.map.impl.Map;
import org.jgrapht.GraphPath;
import org.jgrapht.WeightedGraph;
import org.jgrapht.alg.interfaces.KShortestPathAlgorithm;
import org.jgrapht.alg.shortestpath.KShortestPaths;
import org.jgrapht.graph.DefaultWeightedEdge;

import java.util.List;

/**
 * Created by nik on 13/06/17.
 */
public class FloorSplitTest
{
    public static void main(String[] args)
    {
        Map map = Map.getSimpleMap();
        Floor floor = map.getFloors().iterator().next();

        WeightedGraph<Vector2D, DefaultWeightedEdge> vg = floor.getVisibilityGraph();

        KShortestPathAlgorithm<Vector2D, DefaultWeightedEdge> kShortestPathAlgorithm =
            new KShortestPaths<Vector2D, DefaultWeightedEdge>(vg, 1);

        List<GraphPath<Vector2D, DefaultWeightedEdge>> paths =
            kShortestPathAlgorithm.getPaths(new Vector2D(0, 0), new Vector2D(600, 600));

        GraphPath<Vector2D, DefaultWeightedEdge> shortestPath = paths.get(0);

        System.out.println(shortestPath);

        floor.getSubFloors(shortestPath);
    }
}
