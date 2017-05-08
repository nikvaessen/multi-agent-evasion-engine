package nl.dke.pursuitevasion.genetic;

import nl.dke.pursuitevasion.game.Engine;
import nl.dke.pursuitevasion.game.EngineConstants;
import nl.dke.pursuitevasion.game.agents.Direction;
import nl.dke.pursuitevasion.game.agents.impl.GeneticAgent;
import nl.dke.pursuitevasion.map.impl.Floor;
import nl.dke.pursuitevasion.map.impl.Map;


import java.awt.*;
import java.util.ArrayList;
import java.util.Random;
import java.util.Vector;
import java.util.logging.Logger;

/**
 * Created by nibbla on 13.04.17.
 *
 * This class represents a group of agents. Those blindly will work down their genetic code.
 */
public class GeneticAgentFactory
{


    public static ArrayList<GeneticAgent> getAgents(Map map, Floor startingFloor, Point startLocation, Direction startsFacing, int radius, double visionRange, double visionAngle,Genome g){
        ArrayList<String>  codes = g.getCode();
        ArrayList<GeneticAgent> agents = new ArrayList<>();

       for (String c:codes){
           GeneticAgent singleAgent = new GeneticAgent(map, startingFloor, startLocation, startsFacing, radius, visionRange, visionAngle, c);
           agents.add(singleAgent);
       }

        return  agents;
    }

    /** mutates the genetic code. the rate is the amount of genetic code wich will be changed on average.
     * @param g
     * @param rate
     */
    private static void mutate(Genome g, double rate) {

        ArrayList<String>  codes = g.getCode();
        Random r = EngineConstants.random;
            for (int i = 0; i < codes.size(); i++){
                String c = codes.get(i);
                StringBuilder b = new StringBuilder(c);
                int length = b.length();
                for (int j = 0; j < length; j++) {

                    if (!(r.nextFloat()<=rate))continue;
                    char old = b.charAt(j);
                    char new_ = Alphabet.getRandom();
                    b.setCharAt(j,new_);
                   // if(logger.isDebugEnabled())
                   // {
                   //     logger.debug("Mutated in String {} at {} Position {} with {}", i, j, old , new_);
                   // }
                }

            }



        g.setCode(codes);
    }




    /** http://en.wikipedia.org/wiki/Edge_recombination_operator
     * @param p1
     * @param p2
     * @return
     */
    public static Permutation edgeRecombinationcrossingOver(Genome p1, Genome p2){

        Vector<Integer> p3;

        Vector<int[]> p1AdjancyMatrix;
        p1AdjancyMatrix = createAdjancyMatrix(p1);


        Vector<int[]> p2AdjancyMatrix;
        p2AdjancyMatrix = createAdjancyMatrix(p2);

        Vector<Vector <Integer>> union = createUnion(p1AdjancyMatrix,p2AdjancyMatrix);
        p3 = createPathFromUnion(union,p1AdjancyMatrix,p2AdjancyMatrix);

        Permutation neuP = new Permutation(p3);
        //if (!neuP.isConsistent())System.out.println("nicht consistentedgeRecombinationcrossingOver");
        neuP.recreateComperator();
        return neuP;
    }

    /** http://en.wikipedia.org/wiki/Edge_recombination_operator
     * @param p1
     * @return
     */
    private static Vector<int[]> createAdjancyMatrix(Genome p1) {
     /*   int s = p1.size;
        Vector<int[]> p1AdjancyMatrix  = new Vector<int[]>(s);

        int[]p1_1= {p1.getID(0),p1.getID(p1.getSize()-1),p1.getID(1)};
        p1AdjancyMatrix.add(p1_1);
        for (int i=1;i<s-1;i++){
            int[]p1_i = {p1.getID(i),p1.getID(i-1),p1.getID(i+1)};
            p1AdjancyMatrix.add(p1_i);
        }


        int[]p1_last= {p1.getID(s-1),p1.getID(0),p1.getID(s-2)};
        p1AdjancyMatrix.add(p1_last);
        return p1AdjancyMatrix;*/ return null;
    }

    /**http://en.wikipedia.org/wiki/Edge_recombination_operator
     * @param p1AdjancyMatrix
     * @param p2AdjancyMatrix
     * @return
     */
    private static Vector<Vector<Integer>> createUnion(Vector<int[]> p1AdjancyMatrix,
                                                       Vector<int[]> p2AdjancyMatrix) {
        int s = p1AdjancyMatrix.size();
        //bei union ist der index der knoten alle anderen die nachbarn
        Vector<Vector<Integer>> UnionAdjancyMatrix  = new Vector<Vector<Integer>>(s);
        for (int i=0;i<s;i++){

            //get I node of each;
            int[] node1 = null;
            int id = p1AdjancyMatrix.get(i)[0];

            node1= p1AdjancyMatrix.get(i);

            int[] node2 = null;
            for (int j=0;j<s;j++) {
                if (p2AdjancyMatrix.get(j)[0] == id){
                    node2= p2AdjancyMatrix.get(j);
                    break;
                }
            }
            Vector<Integer> neigbours = new Vector<Integer>(5);
            neigbours.add(id);
            neigbours.add(node1[1]); neigbours.add(node1[2]);
            if (!neigbours.contains(node2[1])) neigbours.add(node2[1]);
            if (!neigbours.contains(node2[2])) neigbours.add(node2[2]);
            UnionAdjancyMatrix.add(neigbours);


        }
        return UnionAdjancyMatrix;
    }

    /** http://en.wikipedia.org/wiki/Edge_recombination_operator
     * @param union
     * @param p1AdjancyMatrix
     * @param p2AdjancyMatrix
     * @return
     */
    private static Vector<Integer> createPathFromUnion(Vector<Vector <Integer>> union,
                                                       Vector<int[]> p1AdjancyMatrix, Vector<int[]> p2AdjancyMatrix) {
        Vector<Integer> p3 = new Vector<Integer>(p1AdjancyMatrix.size());
        Random r = EngineConstants.random;
        Integer node;
        if (r.nextBoolean()) node  = p1AdjancyMatrix.get(0)[0];
        else node  = p2AdjancyMatrix.get(0)[0];;

        p3.add(node);
        while (p3.size()<p1AdjancyMatrix.size()){

            for (Vector<Integer> n : union) {
                if (n.contains(node)){
                    if (!n.get(0).equals(node)) {
                        n.remove(node);
                    }
                }
            }
            node =  getNextNode(node, union,p3);
            p3.add(node);
        }
        return p3;
    }

    /** http://en.wikipedia.org/wiki/Edge_recombination_operator
     * If N's neighbor list is non-empty
     then let N* be the neighbor of N with the fewest neighbors in its list (or a random one, should there be multiple)
     else let N* be a randomly chosen node that is not in K
     * @param union
     * @param node
     * @param p3
     * @return
     */
    private static Integer getNextNode(Integer node, Vector<Vector<Integer>> union, Vector<Integer> p3) {
        Vector<Integer> set = null; //der knoten in Union;
        Random r = EngineConstants.random;
        //get Node From Union
        for (Vector<Integer> vi : union) {
            if (vi.get(0).equals(node)) {set = vi;break;}
        }
        ;
        if (set == null){
            System.out.println("why");
        }
        if (set.size()==1){ //dieser Knoten hat keine nachbarn mehr. einen zuf�lligen anderen knoten
            while(true){
                Integer newNode = union.get(r.nextInt(union.size())).get(0);
                if (!p3.contains(newNode)){
                    return newNode;
                }
            }
        }
        //bei union ist der erste index der knoten; alle anderen die nachbarn
        Vector<Integer> smalest = new Vector<Integer>();
        smalest.add(set.get(1));
        for (int i = 2;i<set.size();i++){
            int id = set.get(i);
            int unionSize =0;
            int smalestSize = 0;
            for (Vector<Integer> vi : union) {
                if (vi.get(0)==id) {unionSize = vi.size();break;}
            }
            for (Vector<Integer> vi : union) {
                if (vi.get(0)==smalest.get(0)) {smalestSize = vi.size();break;}
            }

            if (unionSize>smalestSize) continue;
            if (unionSize<smalestSize)  smalest.clear();
            smalest.add(id);

        }
        if (smalest.size()==1) return smalest.get(0);
        Integer newNode = smalest.get(r.nextInt(smalest.size()));


        return newNode;
    }
}
