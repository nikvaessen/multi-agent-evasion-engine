package nl.dke.pursuitevasion;

import com.sun.xml.internal.ws.policy.privateutil.PolicyUtils;
import jdk.nashorn.internal.codegen.CompilerConstants;
import nl.dke.pursuitevasion.game.Engine;
import nl.dke.pursuitevasion.game.agents.impl.DistanceAgent;
import nl.dke.pursuitevasion.game.agents.impl.MCTS.CoordinatorEvaderKillKillKillEmAll;
import nl.dke.pursuitevasion.map.impl.Map;

import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.*;

/**
 * Created by Jan on 26-6-2017.
 */
public class ExperimentTest {

    public static void main(String[] args) {
        int amount = 5;
        List<Callable<Map>>  mapFunctions =  new ArrayList<>(); //Experiment.getMapFunctions();
        mapFunctions.add(Experiment::getAverageMapFewHoles);
        mapFunctions.add(Experiment::getAverageMapAverageHoles);
        mapFunctions.add(Experiment::getAverageMapManyHoles);
//        mapFunctions.add(Experiment::getSmallMapAverageHoles);
//        mapFunctions.add(Experiment::getSmallMapManyHoles);
//        mapFunctions.add(Experiment::getSmallMapFewHoles);
        Class evaderClass = CoordinatorEvaderKillKillKillEmAll.class;


        try(FileWriter writer = new FileWriter(evaderClass.getSimpleName() + " - " + amount + ".csv");){
            // Print header
            writer.write("iterations, vertexes, obstacle vertexes, obstacles\n");
            List<ExperimentResult> results  = runExperiments(amount, mapFunctions, evaderClass, writer);
            System.out.println("saved to " + evaderClass.getSimpleName() + " - " + amount + ".csv");
        }
        catch (IOException e){
            System.out.println("Error making file.");
            e.printStackTrace();
        }
    }

    private static List<ExperimentResult> runExperiments(int amount, List<Callable<Map>> mapFunctions, Class evaderClass, FileWriter writer) {
        ArrayList<ExperimentResult> results = new ArrayList<>();
        for (Callable<Map> mapFunction : mapFunctions) {
            ExperimentResult result = runExperiment(amount, mapFunction, evaderClass);
            results.add(result);
            // get Map info
            String mapInfo = result.map.getMapDescription();
            // print iterations
            for (Integer iter : result.iterations) {
                try {
                    writer.append(iter.toString() + "," + mapInfo + "\n");
                    writer.flush();
                }catch (IOException e){
                    e.printStackTrace();
                }
            }
        }
        return results;
    }

    private static ExperimentResult runExperiment(int amount, Callable<Map> mapFunction, Class evaderClass) {
        ArrayList<Class> evaders = new ArrayList<>();
        evaders.add(evaderClass);

        List<Experiment> experiments = setupExperiments(amount, mapFunction, evaders);

        ArrayList<Integer> results = new ArrayList<>();
        int count = 1;
        for (Experiment experiment : experiments) {
            try {
                System.out.printf("starting experiment %d\n", count);
                count++;
                results.add(experiment.start().get());
            } catch (InterruptedException | ExecutionException e){
                e.printStackTrace();
            }
        }
        return new ExperimentResult(results, mapFunction);
    }

    public static List<Experiment> setupExperiments(int amount, Callable<Map> mapFunction, List<Class> evaders){
        ArrayList<Experiment> experiments = new ArrayList<>();
        for (int i = 0; i < amount; i++) {
            Map map;
            try{
                map = mapFunction.call();
            }catch (Exception e){
                e.printStackTrace();
                map = null;
            }
            try{
                experiments.add(new Experiment(evaders, map));
            }catch (ClassNotFoundException e){
                e.printStackTrace();
            }
        }
        return experiments;
    }

}
class ExperimentResult{
    public List<Integer> iterations;

    public Map map;

    public ExperimentResult(List<Integer> iter, Map m){
        this.iterations = iter;
        this.map = map;
    }

    public ExperimentResult(ArrayList<Integer> results, Callable<Map> mapFunction) {
        try{
            this.map = mapFunction.call();
        }
        catch (Exception e){
            e.printStackTrace();
        }
        this.iterations = results;
    }
}


