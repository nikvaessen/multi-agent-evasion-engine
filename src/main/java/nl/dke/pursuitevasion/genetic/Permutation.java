package nl.dke.pursuitevasion.genetic;

import collectingData.Acquisition;
import collectingData.ExposurePoint;
import mainClass.MainMenue;
import nl.dke.pursuitevasion.game.EngineConstants;
import satellite.Satellite;

import java.io.*;
import java.util.*;
import java.util.logging.Logger;

/** This class repressents an permutation.
 * This is Importand for the planning phase in Missionplaner.
 * The Permutation is as long as the acquisition but shifted 
 * @author Nibbla
 *
 */
public class Permutation {


	/**
	 * @author Nibbla
	 *	sorts them Letters points by permutation
	 */
	public class splitExpComperator implements Comparator<Vector<Alphabet>>{
		int[] compPermut;
			public splitExpComperator(int[] permut){
				compPermut = permut;
			}

			private int getIndexOfId(int id){
				for (int i = 0; i<compPermut.length;i++){

					if (compPermut[i] == id){
						return i;
					}
				}
				return -1;
			}


			//compares the index of to ids
			@Override
			public int compare(Vector<Alphabet> ev1,
					Vector<Alphabet> ev2) {
				int index1 = getIndexOfId(ev1.get(0).getAcquisitionID());
				int index2 = getIndexOfId(ev2.get(0).getAcquisitionID());
				if (index1 == -1 || index2 == -1){
					//System.out.println("000");
				}else{

				}
				return (index1<index2 ? -1 : (index1==index2 ? 0 : 1));

			}



	}

	private static final Logger logger = Logger.getLogger(Permutation.class.getName());
	
	private Random r = EngineConstants.random;
	private boolean[] planed;
	private int[] permut; 
	private int size;
	
	expComperator expC;
	splitExpComperator splitExpC;

	public static int hammingAbstand(Permutation p1, Permutation p2){
		int abstand = 0;
		if (p1.size != p2.size) return -1;
		
		for (int i=0;i<p1.size;i++){
			if (p1.permut[i]!=p2.permut[i]) abstand++;
		}
		return abstand;
	}
	
	/** nimmt eine zuf�llige zahl als schnittpunkt. bis zum schnittpunkt wird permutation 1 hergenommen.
	 * dann alle von permuation 2 die noch nicht verwendet worden sind. und dann alle vor dem schnittpunkt;
	 * @param p1
	 * @param p2
	 * @return
	 */
	public static Permutation crossingOver(Permutation p1, Permutation p2){
		int s = p1.getSize();
		Vector<Integer> p3 = new Vector<Integer>(s);
		
		
		int schnitt = new Random().nextInt(s);;
		for (int i=0;i<schnitt;i++){
			p3.add(p1.getID(i));
			
		}
		for (int i=schnitt;i<s;i++){
			if (!p3.contains(p2.getID(i))) p3.add(p2.getID(i));
		}
		for (int i=0;i<schnitt;i++){
			if (!p3.contains(p2.getID(i))) p3.add(p2.getID(i));
		}
		Permutation neuP = new Permutation(p3);
		neuP.recreateComperator();
		return neuP;
		
	}
	
	/** 
	 * @param p1
	 * @param schnitt1 kleiner als schnitt 2
	 * @param schnitt2
	 * @return
	 */
	public static Permutation blockSchnittRandom(Permutation p1, int schnitt1, int schnitt2){
		if (schnitt1 == schnitt2) return p1.clone();
		int s = p1.getSize();
		Random r = EngineConstants.random;
		Vector<Integer> p3 = new Vector<Integer>(s);
		Vector<Integer> randomList = new Vector<Integer>(s);
		for (int i = 0;i<schnitt1;i++){
			p3.add(p1.permut[i]);
		}
		for (int i = schnitt1;i<=schnitt2;i++){
			randomList.add(p1.permut[i]);
		}
		
		for (int i = schnitt1;i<=schnitt2;i++){
			Integer neu = randomList.get(r.nextInt(randomList.size()));
			p3.add(neu);;
			randomList.remove(neu);
		}
		
		for (int i = schnitt2+1;i<s;i++){
			p3.add(p1.permut[i]);
		}
		
		
		
		Permutation neuP = new Permutation(p3);
		//if (!neuP.isConsistent())System.out.println("nicht consistent");
		neuP.recreateComperator();
		return neuP;
	}
	
	
	/** http://en.wikipedia.org/wiki/Edge_recombination_operator
	 * @param p1
	 * @param p2
	 * @return
	 */
	public static Permutation edgeRecombinationcrossingOver(Permutation p1, Permutation p2){
		
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
	 * @param p1
	 * @return
	 */
	private static Vector<int[]> createUnionAdjancyMatrix(Permutation p1) {
		int s = p1.size;
		Vector<int[]> p1AdjancyMatrix  = new Vector<int[]>(s); 
		
		int[]p1_1= {p1.getID(0),p1.getID(p1.getSize()-1),p1.getID(1)};
		p1AdjancyMatrix.add(p1_1);
		for (int i=1;i<s-1;i++){
			int[]p1_i = {p1.getID(i),p1.getID(i-1),p1.getID(i+1)};
			p1AdjancyMatrix.add(p1_i);
		}
		
		
		int[]p1_last= {p1.getID(s-1),p1.getID(0),p1.getID(s-2)};
		p1AdjancyMatrix.add(p1_last);
		return p1AdjancyMatrix;
	}

	/** checks if every member of the acquisition appears once in the permutation
	 * not usefull anymore
	 * @return
	 */
	public boolean isConsistent(){
		boolean[] all = new boolean[permut.length];
		
		try {
			for (int p : permut) {
				all[p] = true;
			}
		} catch (Exception e) {
			return false;
		}
		for (boolean b : all) {
			if (b == false) return false;
		}
		return true;
		
		
	}
	
	public String toString(){
		StringBuilder b = new StringBuilder();
		for (int index : permut) {
			b.append(index + " ");
		}
		return b.toString();
	}
	
	public String toFileString(){
		StringBuilder b = new StringBuilder();
		for (int index : permut) {
			b.append(index + "\n");
		}
		return b.toString();
	}
	

	public Permutation(Random r, int size, boolean random) {
		this.r = r;
		int n = size;
		planed = new boolean[n];
		permut = new int[n];;
		if (!random){
			for (int i=0;i<permut.length;i++){
				permut[i]=i;
			}
		}else{
			Vector<Integer> moglich = new Vector<Integer>(n); 
			for (int i=0;i<permut.length;i++){
				moglich.add(i); //ein topf aus allen zahlen
			}
			for (int i=0;i<permut.length;i++){
				int z = r.nextInt(n-i);
				permut[i] = moglich.get(z);
				moglich.remove(z);
				
			}		       
			   
		}
		
		if (!this.isConsistent()){
			System.out.println("bl���h");
		}
		
		expC = new expComperator(permut);
		splitExpC = new splitExpComperator(permut);
		this.size = n;
	}

	public Permutation(Random r, Acquisition acquisition, boolean random) {
		this(r,acquisition.getAcquisitionData().size(),random);
		
	}
	
	


	/**
	 * @param r
	 * @param permutation
	 * */
	public Permutation(Random r, String permutation) {
		this.r = r;
		if (permutation.isEmpty()){
			permutation = " ";
		}
		String[] ind = permutation.split(" ");
		permut = new int[ind.length];
		for (int i=0;i<permut.length;i++){
			permut[i]=Integer.parseInt(ind[i]);
		}
		planed = new boolean[permut.length];
		
		expC = new expComperator(permut);
		size = permut.length;
		if (!isConsistent()) System.out.println("permutationCreationNotConsistent");
		splitExpC = new splitExpComperator(permut);
		
		this.size = permut.length;
	}


	


	private Permutation() {
	}
	




	public Permutation(Random r, File file, Acquisition acquisition) throws FileNotFoundException, IOException {
		Properties p = new Properties();
		p.load(new FileInputStream(file));

		this.r = r;
		 int lineCounter = 0; // Zählt die Anzahl der Zeilen die geladen wurden.
		 int n = acquisition.getAcquisitionData().size();
		 Vector<Integer> loadPermut = new Vector<Integer>();     
		    try
		    {
		      BufferedReader br = new BufferedReader(new FileReader(file));
		      String zeile = "";
		     
		      while((zeile = br.readLine()) != null) // solange aktuelle Zeile belegt
		      {
		    	if (zeile.equals("default")) {
		    		for (int i=0;i<n;i++){
		    			loadPermut.add(i);
					}
		    		break;
		    	}
		    	if (zeile.equals("random")) {
		    		Vector<Integer> moglich = new Vector<Integer>(n); 
					for (int i=0;i<n;i++){
						moglich.add(i); //ein topf aus allen zahlen
					}
					for (int i=0;i<n;i++){
						int z = r.nextInt(n-i);
						loadPermut.add(moglich.get(z));
						moglich.remove(z);
						
					}		       
		    		break;
		    	}
		    	loadPermut.add(Integer.parseInt(zeile));
		        lineCounter++;
		      }
		      br.close();
		      
		      permut = new int [loadPermut.size()];
		      size = permut.length;
		      for (int i=0; i<size;i++) permut[i] = loadPermut.get(i);
		      
		      planed = new boolean[size];
		      expC = new expComperator(permut);
		      splitExpC = new splitExpComperator(permut);
		      if (!isConsistent()) System.out.println("permutationCreationNotConsistent");
		    }
		    catch (Exception e)
		    {
		    	e.printStackTrace();
			  	logger.severe("Error in permut.java: " + e.toString()) ;
		  		
		    }
		  
		
	}



	public Permutation(Vector<Integer> p3) {
		int s = p3.size();
		permut = new int [s];
		size = s;
	      for (int i=0; i<s;i++) permut[i] = p3.get(i);
	      
	      planed = new boolean[s];
	      expC = new expComperator(permut);
	      splitExpC = new splitExpComperator(permut);
	      //if (!isConsistent()) System.out.println("permutationCreationNotConsistent");
	     
	}



	public Permutation(Random r2,
					   Vector<Vector<ExposurePoint>> expSplitByAcquisition, boolean random) {
		
		r = r2;
		size =expSplitByAcquisition.size();
		permut = new int [size];
		 planed = new boolean[size];
		 if (!random){
				for (int i=0;i<permut.length;i++){
					int id = expSplitByAcquisition.get(i).get(0).getAcquisitionID();
					permut[i]= id;
				}
			}else{
				
				Vector<Integer> moglich = new Vector<Integer>(size); 
				
				for (int i=0;i<permut.length;i++){
					int id = expSplitByAcquisition.get(i).get(0).getAcquisitionID();
					moglich.add(id); //ein topf aus allen zahlen
				}
				for (int i=0;i<permut.length;i++){
					int z = r.nextInt(size-i);
					permut[i] = moglich.get(z);
					moglich.remove(z);
					
				}		       
				   
			}

		 expC = new expComperator(permut);
	     splitExpC = new splitExpComperator(permut);
		
	}

	public int getID(int index){
		if (index > permut.length) return -1;
		return permut[index];
	}
	private int getIndexOfId(int id){
		for (int i = 0; i<permut.length;i++){
			
			if (permut[i] == id){
				return i;
			}
		}
		return -1;
	}

	
	public int getSize() {
		return size;
	}
	

	
	/**
	 * resets the permutation to 0 
	 * resets the permutation for those points wich once were planned but werent taken nor downloaded 
	 * @param satellite 
	 */
	public void resetCurrent(Satellite satellite) {
		
		for (int i = 0; i<permut.length;i++){
			planed[i] = false;
		}
	}
	
	

	public Permutation clone(){
		Permutation p = new Permutation();
		p.r = r;
		
		p.permut = new int[permut.length]; 
		System.arraycopy(permut, 0, p.permut, 0, permut.length); 
		p.planed = new boolean[permut.length];
		System.arraycopy(planed, 0, p.planed, 0, permut.length); 
		p.size = size;
		
		p.expC = new expComperator(permut);
		p.expC.compPermut = new int[permut.length]; 
		System.arraycopy(expC.compPermut, 0, p.expC.compPermut, 0, permut.length); 
		
		p.splitExpC = new splitExpComperator(permut);
		p.splitExpC = splitExpC;
		System.arraycopy(splitExpC.compPermut, 0, p.splitExpC.compPermut, 0, permut.length); 
		
		return p;
	}

	public void recreateComperator(){
		expC = new expComperator(permut);
		
	}



	
	
	/** the user sends his list of exposurepoints he could take at a certan moment
	 * and this class will sort them by the permutation
	 * @return
	 */
	public Vector<ExposurePoint> sortExposurePointsByPermutation(Vector<ExposurePoint> exps){
		
		Collections.sort(exps, expC);
		
		return exps;
	}
	
	public void sortSplitExposurePointsByPermutation(
			Vector<Vector<ExposurePoint>> expSplitByAcquisition) {
		
		Collections.sort(expSplitByAcquisition, splitExpC);
		
		
		
	}
	public boolean isPlaned(ExposurePoint exp) {
		
		return planed[getIndexOfId(exp.getAcquisitionID())];
	}
	public void setPlaned(ExposurePoint exp) {
		 planed[getIndexOfId(exp.getAcquisitionID())]=true;
		
	}


		
		public void swap(int swapPos1, int swapPos2) {
		int i = 0;
		boolean b = false;
				i = permut[swapPos1];
				permut[swapPos1] = permut[swapPos2];
				permut[swapPos2] = i;
				
				
				b = planed[swapPos1];
				planed[swapPos1] = planed[swapPos2];
				planed[swapPos2] = b;
		 
				int j=splitExpC.compPermut[swapPos1];		
				splitExpC.compPermut[swapPos1] = splitExpC.compPermut[swapPos2];
				splitExpC.compPermut[swapPos2] =j;
				
		
	}

		public boolean contains(int id) {
			for (int i= 0; i <permut.length;i++){
				if (permut[i] == id) return true; 
			}
			return false;
		}

	
	
	

}
