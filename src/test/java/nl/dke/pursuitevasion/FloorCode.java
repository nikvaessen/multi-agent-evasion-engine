package nl.dke.pursuitevasion;

import nl.dke.pursuitevasion.game.Vector2D;
import nl.dke.pursuitevasion.map.MapPolygon;
import nl.dke.pursuitevasion.map.builders.MapBuilder;
import nl.dke.pursuitevasion.map.impl.Floor;
import nl.dke.pursuitevasion.map.impl.Map;
import nl.dke.pursuitevasion.map.impl.Obstacle;

import java.text.MessageFormat;

/**
 * Created by Jan on 26-6-2017.
 */
public class FloorCode {

    public static void main(String[] args) {

        String code = getConstructionCode(Map.getMap("averageMany.ser"));
        System.out.println(code);
        System.out.println("\n");

        code = getConstructionCode(Map.getMap("averageAverage.ser"));
        System.out.println(code);
        System.out.println("\n");

        code = getConstructionCode(Map.getMap("averageFew.ser"));
        System.out.println(code);

    }

    static public String getConstructionCode(Map map){

        Floor floor = map.getFloors().iterator().next();
        StringBuilder builder = new StringBuilder("MapPolygon floor = new MapPolygon(false);\n");
        for (Vector2D vector: floor.getPolygon().getPoints()) {
            String vectorString = MessageFormat.format("{0}, {1}", vector.getX(), vector.getY());
            vectorString = vectorString.replace('.', '_');
            builder.append(
                    MessageFormat.format("floor.addPoint(new Vector2D({0}));\n", vectorString )
            );
        }
        builder.append("\n");

        StringBuilder addObstacleBuilder = new StringBuilder();
        int count = 0;
        for (Obstacle obstacle : floor.getObstacles()) {
            builder.append(MessageFormat.format("MapPolygon obstacle{0} = new MapPolygon(true);\n", count));
            for (Vector2D vector : obstacle.getPolygon().getPoints()) {
                String vectorString = MessageFormat.format("{0}, {1}", vector.getX(), vector.getY());
                vectorString = vectorString.replace('.', '_');
                builder.append(MessageFormat.format("obstacle{0}.addPoint(new Vector2D({1}));\n", count, vectorString));
            }
            builder.append("\n");

            addObstacleBuilder.append(MessageFormat.format(".addObstacle(obstacle{0})\n", count));
            count++;
        }

        builder.append("return MapBuilder.create()\n" +
                ".makeFloor(floor)\n");
        builder.append(addObstacleBuilder.toString());

        builder.append(".finish()\n.build();");
        return builder.toString();
    }
}
