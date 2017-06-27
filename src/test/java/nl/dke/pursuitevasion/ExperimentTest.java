package nl.dke.pursuitevasion;

import com.sun.xml.internal.ws.policy.privateutil.PolicyUtils;
import jdk.nashorn.internal.codegen.CompilerConstants;
import nl.dke.pursuitevasion.game.agents.impl.DistanceAgent;
import nl.dke.pursuitevasion.game.agents.impl.MCTS.CoordinatorEvaderKillKillKillEmAll;
import nl.dke.pursuitevasion.map.impl.Map;

import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;

/**
 * Created by Jan on 26-6-2017.
 */
public class ExperimentTest {

    public static void main(String[] args) {
        int amount = 5;
        List<Callable<Map>>  mapFunctions =  new ArrayList<>(); //Experiment.getMapFunctions();
        mapFunctions.add(Experiment::getSmallMapFewHoles);
        mapFunctions.add(Experiment::getSmallMapAverageHoles);
        mapFunctions.add(Experiment::getSmallMapManyHoles);
        Class evaderClass = CoordinatorEvaderKillKillKillEmAll.class;

        List<ExperimentResult> results  = runExperiments(amount, mapFunctions, evaderClass);

        try(FileWriter writer = new FileWriter(evaderClass.getSimpleName() + " - " + amount + ".csv");){
            // Print header
            writer.write("iterations, vertexes, obstacle vertexes, obstacles\n");
            for (ExperimentResult result : results) {
                // get Map info
                String mapInfo = result.map.getMapDescription();
                // print iterations
                for (Integer iter : result.iterations) {
                    writer.append(iter.toString() + "," + mapInfo + "\n");

                }
            }
            writer.flush();
        }
        catch (IOException e){
            System.out.println("Error making file.");
            e.printStackTrace();
        }

    }

    private static List<ExperimentResult> runExperiments(int amount, List<Callable<Map>> mapFunctions, Class evaderClass) {
        ArrayList<ExperimentResult> results = new ArrayList<>();
        for (Callable<Map> mapFunction : mapFunctions) {
            results.add(runExperiment(amount, mapFunction, evaderClass));
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


