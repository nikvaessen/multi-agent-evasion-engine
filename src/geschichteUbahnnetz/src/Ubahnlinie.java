import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.Point;
import java.awt.Rectangle;
import java.util.Collections;
import java.util.Comparator;
import java.util.Vector;
import java.util.Comparator;
import javax.naming.ldap.SortControl;




public class Ubahnlinie {
	String helptext;
	int nummer;
	public int getNummer() {
		return nummer;
	}


	Color farbe;
	Vector<Ubahnstation> ubahnen = new Vector<Ubahnstation>();
	private String nummerstring;
	
	
	
	public Ubahnlinie(String helptext, int nummer, String nummerString, Color farbe) {
		super();
		this.helptext = helptext;
		this.nummer = nummer;
		this.farbe = farbe;
		this.nummerstring = nummerString;
		
	}
	
	public void addUbahn(Ubahnstation ubahn){
		ubahnen.add(ubahn);
		sortUbahnen();
		
	}
	
	

	private void sortUbahnen() {
		UbahnstationComperator ubc = new UbahnstationComperator();
		Collections.sort(ubahnen,ubc);
		
	}

	public void drawUbahn(Graphics2D g2, int jahreszahl, Karte k){
		for (int i = 0; i < ubahnen.size()-1; i ++){
			
			if (ubahnen.get(i).existent(jahreszahl) ){
				Ubahnstation naechsteUbahn = getNextUbahn(ubahnen.get(i), jahreszahl);
				
				
				if (naechsteUbahn != null){
					drawLine(g2, ubahnen.get(i), naechsteUbahn,k , farbe);
				}
				
				
			}

		}
		
		g2.setStroke(new BasicStroke(1));
		for (Ubahnstation ubahn : ubahnen) {
			if (ubahn.existent(jahreszahl)){
				if (ubahn.getIndex() == 30) {
					Point p = ubahn.getScreenKoord(k);
				}
				ubahn.draw(g2,k,farbe);
			}
		}
	}

	private Ubahnstation getNextUbahn(Ubahnstation ubahnstation, int jahreszahl) {
		int naechstHoeher = 99;
		Ubahnstation next = null;
		for (Ubahnstation ubahn2 : ubahnen) {
			if (ubahn2.existent(jahreszahl)&& 
					(ubahn2.getIndex()- ubahnstation.getIndex() < naechstHoeher)&&
					(ubahn2.getIndex()- ubahnstation.getIndex() > 0)){
					naechstHoeher = ubahn2.getIndex()- ubahnstation.getIndex();
					next = ubahn2;
				
			}
				
		}
		return next;
	}

	private void drawLine(Graphics2D g2, Ubahnstation ubahnstation,
			Ubahnstation ubahnstation2, Karte karte, Color farbe2) {
		
			Point p1 = ubahnstation.getScreenKoord(karte);
			Point p2 = ubahnstation2.getScreenKoord(karte);
			
			
			
			karte.drawLineToScreen(g2, p1.x, p1.y, p2.x, p2.y, farbe2, 5);
		
	}
	
	

	private class UbahnstationComperator implements Comparator<Ubahnstation> 
	{

		public int compare(Ubahnstation ev1, Ubahnstation ev2) {
			return (ev1.getIndex()<ev2.getIndex() ? -1 : (ev1.getIndex()==ev2.getIndex() ? 0 : 1));
		}
	}



	public Vector<Ubahnstation> getUbahnstationen(Point p, int jahreszahl, Karte karte) {
		Vector<Ubahnstation> statione = new Vector<Ubahnstation>();
		int boxrahmen = 10;
		int boxrahmen2 = 5;
		Rectangle r = new Rectangle(p.x - boxrahmen2, p.y - boxrahmen2, boxrahmen, boxrahmen);
		for (Ubahnstation ubahnstation : ubahnen) {
			Point p1 = ubahnstation.getScreenKoord(karte);
			if (ubahnstation.bereitsGebaut(jahreszahl) && r.contains(p1)){
				statione.add(ubahnstation);
			}
			
		}
		return statione;
	}
	
}
