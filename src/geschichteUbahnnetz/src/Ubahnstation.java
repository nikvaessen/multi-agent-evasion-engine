import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.Point;
import java.text.SimpleDateFormat;
import java.util.Date;


public class Ubahnstation extends Koordinate{
	final String name;
	String helptext;
	final int baujahr;
	final Date eroeffnungsDatum;
	private int index;
	SimpleDateFormat df = new SimpleDateFormat("dd'.'MM'.'yyyy");
	private String linie;
	private int endjahr;
	
	public int getIndex() {
		return index;
	}

	
	public Ubahnstation(String name, double lat, double longe , String Linie, String helptext, Date eroeffnungsDatum, Date endDate, int index){
		super(longe, lat);
		this.name = name;
		this.helptext = helptext;
		
		this.eroeffnungsDatum = eroeffnungsDatum;
		
		this.baujahr= eroeffnungsDatum.getYear()+1900;
		this.endjahr= endDate.getYear()+1900; 
		
		 this.linie = Linie;
		this.index = index;
		
		
	}

	public String getLinie() {
		return linie;
	}


	public boolean bereitsGebaut(int jahreszahl) {
		if (jahreszahl >= baujahr) return true;
		return false;
	}
	public boolean nochExistent(int jahreszahl) {
		if (jahreszahl < endjahr) return true;
		return false;
	}

	public void draw(Graphics2D g2, Karte k, Color c) {
		Point p = super.getScreenKoord(k);
		double d = k.getPointWidth();
		int di = (int) d;
		int di2 = (int) (d/2);
		g2.setColor(c);
		g2.fillOval(p.x-di2, p.y-di2, di, di);
		g2.setColor(Color.black);
		g2.drawOval(p.x-di2, p.y-di2, di, di);
	}
	
	
	public String toString(){
		StringBuilder sb = new StringBuilder();
		sb.append("Name: " + name + "\n");
		sb.append("Linie: " + linie + "\n");
		sb.append("Eröffnet am: " + df.format(eroeffnungsDatum) + "\n");
		sb.append(helptext + "\n");
		
		
		return sb.toString();
		
	}


	public boolean existent(int jahreszahl) {
		return bereitsGebaut(jahreszahl) && nochExistent(jahreszahl);
	}
	
}
