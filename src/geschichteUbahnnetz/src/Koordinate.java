import java.awt.Point;



public class Koordinate {
	double longitude;
	double latitude;
	
	public Koordinate(double longitude, double latidu){
		this.longitude = longitude;
		this.latitude = latidu;
	}
	
	public Koordinate(String longit, String latit) {
		this.longitude = Double.valueOf(longit);
		this.latitude = Double.valueOf(latit);
	}

	public Point getScreenKoord(Karte karte){
		return karte.koordinateToScreenPointNeu(this);
		//return karte.koordinateToScreenPoint(this);
		
		
	}
	
	public String toString(){
		String s = latitude + "\t" + longitude;
		return s;
	}
}
