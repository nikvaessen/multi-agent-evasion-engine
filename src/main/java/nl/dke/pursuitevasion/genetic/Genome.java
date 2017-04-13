package nl.dke.pursuitevasion.genetic;

import java.util.ArrayList;
import java.util.Vector;

/** The Getic code of an Individual
 * in this case for all the invaders
 * Created by nibbla on 13.04.17.
 */
public class Genome {
    private ArrayList<String> code;
    private ArrayList<ArrayList<Integer>> permutation = new ArrayList<>();


    public ArrayList<String> getCode() {
        return code;
    }


    public void save(String path) {

    }

    public void load(String path){


    }

    public void setCode(ArrayList<String> code) {
        this.code = code;
    }


    public int getID(int i,int j) {
        if (i>permutation.size())return -1;
        if (j>permutation.get(i).size())return -1;
        return permutation.get(i).get(j);
    }

    public int getSize() {
        return permutation.size();
    }
}
