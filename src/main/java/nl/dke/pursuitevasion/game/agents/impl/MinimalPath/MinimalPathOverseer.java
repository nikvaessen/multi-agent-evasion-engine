package nl.dke.pursuitevasion.game.agents.impl.MinimalPath;

import nl.dke.pursuitevasion.game.agents.AgentRequest;
import nl.dke.pursuitevasion.map.impl.Map;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by Jan on 24-5-2017.
 */
public class MinimalPathOverseer {

    private MinimalPathOverseer(Map map){
        // Build visibility graph

        // get u and v

        // Calculate Π1 between u and v




    }

    private static MinimalPathOverseer instance;

    public static MinimalPathOverseer getIntance(){
        if(instance == null){
            throw new NullPointerException("No instance created yet. Use the init function");
        }
        return instance;
    }

    public static MinimalPathOverseer init(Map map){
        return new MinimalPathOverseer(map);
    }

    private static List<MininalPathAgent> agents = new ArrayList<>(3);

    // Adds the agent to the
    public static int addAgent(MininalPathAgent agent){
        if(agents.size() <= 3){
            agents.add(agent);
            return agents.size()-1;
        }
        else{
            throw new IllegalStateException("There are already 3 agents instantiated");
        }
    }

    // Checks whether an agent should be making a new request.
    public boolean getShouldDoSomething(int agentNumber){
        return false;
    }

    // Determines what request an agent should make.
    public void getTask(int agentNumber, AgentRequest request){}

}
