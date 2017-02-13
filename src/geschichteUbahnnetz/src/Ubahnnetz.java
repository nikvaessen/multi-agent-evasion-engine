import java.awt.Color;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Point;
import java.awt.Rectangle;
import java.awt.geom.AffineTransform;
import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Vector;




public class Ubahnnetz {
	Vector<Ubahnlinie> ubahnlinie = new Vector<Ubahnlinie>();
	
	public Ubahnnetz(String pfad){
		
		 BufferedReader br;
		try {
			br = new BufferedReader(new FileReader(pfad));
		
		  String zeile = "";
		  
		  while((zeile = br.readLine()) != null) // solange aktuelle Zeile belegt
		  {
			  addUbahnStation(zeile);
		  }
		} catch (FileNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
	}
	
	private void readLine(String zeile) {
		
	}

	
	public void addUbahnStation(String zeile){
		String[] splitFile = zeile.split("\t");
		SimpleDateFormat df = new SimpleDateFormat("dd'.'MM'.'yyyy");
		String nummerString = splitFile[4];
		Ubahnlinie linie = getUbahnlinie(splitFile[4]);
		if (linie == null){
			int farbeInt = Integer.parseInt(splitFile[6], 16);
		
			Color farbe = new Color(farbeInt);
			Integer nummer = Integer.parseInt(nummerString.substring(nummerString.length()-1));
			String helptext = "";
			if (splitFile.length > 7) helptext = splitFile[7];
			linie = new Ubahnlinie(helptext, nummer,nummerString, farbe);
			ubahnlinie.add(linie);
		}
		
		String name =splitFile[3];
		Date openDate;
		Date endDate;
		try {
			openDate = df.parse(splitFile[0]);
		} catch (ParseException e) {
			
			
			openDate = new Date(1900, 1, 1);
			e.printStackTrace();
		}
		try {
			endDate = df.parse(splitFile[8]);
		} catch (ParseException e) {
			
			
			endDate = new Date(2100, 1, 1);
			e.printStackTrace();
		}
		
		 double longitude =  Double.parseDouble(splitFile[1]);
		 double latitude =  Double.parseDouble(splitFile[2]);
		 int index = Integer.parseInt(splitFile[5]);
		 String helptext = "";
		if (splitFile.length > 7) helptext = splitFile[7];
		 
		
		
		Ubahnstation u = new Ubahnstation(name, latitude, longitude,nummerString,  helptext, openDate,endDate, index);
		linie.addUbahn(u);
		
	}
	
	private Ubahnlinie getUbahnlinie(String nummerString) {
		Integer nummer = Integer.parseInt(nummerString.substring(nummerString.length()-1));
		for (Ubahnlinie ulinie : ubahnlinie) {
			if (nummer == ulinie.getNummer()) 
				return ulinie; 
		}
		return null;
	}

	

	/**
	 * @param g2
	 * @param jahreszahl welchen Stand soll die Ubahnen haben
	 * @param auswahl ein boolean array mit 8 feldern. Feld 0 stellt die Postubahn dar
	 */
	public void drawUbahn(Graphics2D g2, int jahreszahl, boolean[] auswahl, Karte k){
		
			for (Ubahnlinie ubl : ubahnlinie) {
				
				if (auswahl[ubl.getNummer()]) {
					ubl.drawUbahn(g2,jahreszahl, k);
				}
			}
			
		}
	

	public Vector<Ubahnstation> getUbahnen(Point p, int jahreszahl,
			boolean[] auswahl, Karte karte) {
			Vector<Ubahnstation> stationen = new Vector<Ubahnstation>();
		
		for (Ubahnlinie ubl : ubahnlinie) {
			if (auswahl[ubl.getNummer()]) {
					stationen.addAll(ubl.getUbahnstationen(p,jahreszahl,karte));
				
				
				
			}	
		}
		
		return stationen;
	}
	
}
