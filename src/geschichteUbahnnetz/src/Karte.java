import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Image;
import java.awt.Point;
import java.awt.event.MouseEvent;
import java.awt.geom.AffineTransform;
import java.awt.geom.NoninvertibleTransformException;
import java.awt.geom.Point2D;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Properties;
import java.util.Vector;


import javax.swing.JPanel;


public class Karte extends JPanel{
	Image leBild;
	Properties leBildProp;
	String outputpath = "E:\\Freizeitgestalltung\\Programmieren\\IdeaProjects\\multi-agent-evasion-engine\\src\\output\\";
	Boolean showUbahn;
	Ubahnnetz ubahn;
	int jahreszahl = 1980;
	boolean[] auswahl = {false,false,false,false,false,false,false,false,false};
	//private Koordinate LO;
	//private Koordinate RU;
	double zoom = 1;
	double xversatz = 0;
	double yversatz = 0;
	
	private double LOLong;
	private double LOLat;
	private double RULat;
	private double RULong;
	private double LongDelt;
	private double LatDelt;
	private double CenterLat;
	private double CenterLong;
	private double currentX;
	private double currentY;
	private double previousX;
	private double previousY;
	
	final int auswahlradius = 10;
	
	public void setJahreszahl(int jahreszahl) {
		this.jahreszahl = jahreszahl;
	}

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;

	
	
	public void paintComponent(Graphics g2) {
		super.paintComponent(g2);
		Graphics2D g = (Graphics2D) g2.create();
		AffineTransform tx = getCurrentTransformMouseDrag();
		g.drawImage(leBild, tx, this);
		
		ubahn.drawUbahn(g, jahreszahl, auswahl, this);
		
	}

	public Karte() {
		super();
		
		
		loadHintergrundbild();
		createUbahn();
	}

	private void createUbahn() {
		ubahn = new Ubahnnetz(outputpath + "Ubahnen.csv");
		
	}

	private void loadHintergrundbild() {
		try {
			Path currentRelativePath = Paths.get("");
			String s = currentRelativePath.toAbsolutePath().toString();
			System.out.println("Current relative path is: " + s);
			leBild = javax.imageio.ImageIO.read(new File(outputpath + "muenchen1.jpg"));
			leBildProp = new Properties();
			leBildProp.load(new FileReader(outputpath + "muenchen1.txt"));
			LOLong = Double.valueOf(leBildProp.getProperty("LOLong"));
			LOLat = Double.valueOf(leBildProp.getProperty("LOLat"));
			RULat = Double.valueOf(leBildProp.getProperty("RULat"));
			RULong = Double.valueOf(leBildProp.getProperty("RULong"));
 			LongDelt = RULong - LOLong;
			LatDelt  = LOLat - RULat;
			CenterLat = RULat + LatDelt *0.5;
			CenterLong = LOLong + LongDelt *0.5;
			currentX = (double)-leBild.getWidth(null)/2;
			currentY = (double)-leBild.getHeight(null) /2;
			
		} catch (IOException e) {
			
			e.printStackTrace();
		}
	}

	public double getPointWidth() {
		return 10;
	}

	public void drawLineToScreen(Graphics2D g, int x1, int y1, int x2, int y2,
			Color colorRepresentation, int strokeWidth) {
		g.setStroke(new BasicStroke(strokeWidth));
		int screenW = this.getWidth();
		g.setColor(colorRepresentation);

		
		g.drawLine(x1, y1, x2, y2);
		

	}
	public Point koordinateToScreenPointNeu(Koordinate k){
		
		double transPY;
		double transPX;
		
		/*Point2D transPoint = getTranslatedPoint(point.getX(), point.getY());
		// transPoint = getTranslatedPointInvers(point.getX(), point.getY());
		double zwischenY =  LOLat - RULat;
		lat = (leBild.getHeight(null) - transPoint.getY()) / leBild.getHeight(null) * zwischenY + RULat;
		
		double zwischenX =  RULong - LOLong;
		longi = transPoint.getX() / leBild.getWidth(null) * zwischenX + LOLong;
		
		
		Koordinate k = new Koordinate(longi,lat);*/
		double pointx;
		double pointy;
		double longi = k.longitude;
		double lat = k.latitude;
		
		
		double zwischenX =  RULong - LOLong;
		pointx = (longi - LOLong) * leBild.getWidth(null) / zwischenX;
		
		double zwischenY =  LOLat - RULat;
		pointy = -1 * ((lat - RULat) /zwischenY * leBild.getHeight(null)) + this.leBild.getHeight(null);
		
//		double zwischenX =  RULong - LOLong;
//		double thisW = leBild.getWidth(null);
//		transPX = ((k.longitude - LOLong) * thisW / zwischenX);
		
//		double zwischenY =  LOLat - RULat;
//		double thisH =  leBild.getHeight(null);
//		transPY = -1*((k.latitude - RULat) * thisH /zwischenY - thisH);
		
		//Point2D transPoint = getTranslatedPoint(pointx, pointy);
		Point2D transPoint = getTranslatedPointInvers2(pointx,pointy);
		
		pointx =(transPoint.getX() + currentX) * zoom + this.getWidth()/2;
		pointy = (transPoint.getY() + currentY) * zoom + this.getHeight()/2 ;; 
		
		Point p =  new Point( (int)(pointx),  (int)(pointy));
		System.out.println(p.x + ":::" + p.y + "center" + currentX + "  " + currentY);
		return p;
		
		
		
		
	}
	public Koordinate getKoordinateFromBildschirmKoordinate(Point point) {
		double lat; 
		double longi;
		
		Point2D transPoint = getTranslatedPoint(point.getX(), point.getY());
		// transPoint = getTranslatedPointInvers(point.getX(), point.getY());
		double th = leBild.getHeight(null) * zoom;
		double thW = this.getWidth();
		double zwischenY =  LOLat - RULat;
		lat = (leBild.getHeight(null) - transPoint.getY()) / leBild.getHeight(null) * zwischenY + RULat;
		
		double zwischenX =  RULong - LOLong;
		longi = transPoint.getX() / leBild.getWidth(null) * zwischenX + LOLong;
		
		
		Koordinate k = new Koordinate(longi,lat);
		return k;

	}
	
	public Point koordinateToScreenPoint(Koordinate k){
		Point p = new Point();
		double thisH = this.getHeight();
		 
		
		//Point2D transPoint = getTranslatedPoint(point.getX(), point.getY());
		
		double zwischenY =  ((k.latitude - RULat) / LatDelt) * this.getHeight();
		
		p.y = (int) Math.round((thisH - zwischenY));
		double thisW = this.getWidth();
		double zwischenX = ((k.longitude - LOLong) / LongDelt);
		
		p.x = (int) (Math.round(zwischenX));
		//Point2D transPoint = getTranslatedPoint2(p.getX(), p.getY());
		Point2D transPoint = getTranslatedPointInvers(p.getX(), p.getY());
		p =  new Point( (int)(transPoint.getX()+currentX),  (int)(transPoint.getY()+currentY));
		System.out.println(p.x + ":::" + p.y);
		return p;
		//Return
		
		
		
		
		
		
		
		
	}
	
	public Vector<Ubahnstation> getUbahnFromBildschirmKoordinate(Point p){
		
		Vector<Ubahnstation> ubahnen = ubahn.getUbahnen(p, jahreszahl, auswahl, this);
		return ubahnen;
		
		
		
	}
	
	private AffineTransform getCurrentTransformMouseDrag() {
        
        AffineTransform tx = new AffineTransform();
         
        double centerX = (double)getWidth() / 2;
        double centerY = (double)getHeight() / 2;
        
        tx.translate(centerX, centerY);
       
        tx.scale(zoom, zoom);
        tx.translate(currentX, currentY);
        
        //System.out.println(zoom + " " + currentX + "  " + currentY);
        return tx;
        
    }
	
	private AffineTransform getCurrentTransformAnders() {
        
        AffineTransform tx = new AffineTransform();
         
        double centerX = leBild.getWidth(null) / 2;
        double centerY = leBild.getHeight(null) / 2;
        
        tx.translate(centerX, centerY);
       
        tx.scale(zoom, zoom);
        tx.translate(currentX, currentY);
        
        //System.out.println(zoom + " " + currentX + "  " + currentY);
        return tx;
        
    }
	
	


	public void mouseDragged(MouseEvent e){
		// Determine the old and new mouse coordinates based on the translated coordinate space.
        Point2D adjPreviousPoint = getTranslatedPoint(previousX, previousY);
        Point2D adjNewPoint = getTranslatedPoint(e.getX(), e.getY());
        
        double newX = adjNewPoint.getX() - adjPreviousPoint.getX();
        double newY = adjNewPoint.getY() - adjPreviousPoint.getY();

        previousX = e.getX();
        previousY = e.getY();
         
        currentX += newX;
        currentY += newY;
        
	}
	private Point2D getTranslatedPoint(double panelX, double panelY) {
		AffineTransform tx = getCurrentTransformMouseDrag();
        Point2D point2d = new Point2D.Double(panelX, panelY);
        try {
            return tx.inverseTransform(point2d, null);
           
        } catch (NoninvertibleTransformException ex) {
            ex.printStackTrace();
            return null;
        }
	}
	private Point2D getTranslatedPoint2(double panelX, double panelY) {
		AffineTransform tx = getCurrentTransformAnders();
        Point2D point2d = new Point2D.Double(panelX, panelY);
        try {
            return tx.inverseTransform(point2d, null);
           
        } catch (NoninvertibleTransformException ex) {
            ex.printStackTrace();
            return null;
        }
	}
	
	private Point getTranslatedPointInvers(double panelX, double panelY) {
		AffineTransform tx = getCurrentTransformMouseDrag();
		
        Point2D point2d = new Point2D.Double(panelX, panelY);
        
            tx.transform(point2d, null);
           
        
        int x = (int) point2d.getX();
        int y = (int) point2d.getY();
        Point p = new Point(x,y);
        return p;
        
	}
	private Point getTranslatedPointInvers2(double panelX, double panelY) {
		AffineTransform tx = getCurrentTransformAnders();
		
        Point2D point2d = new Point2D.Double(panelX, panelY);
        
            tx.transform(point2d, null);
           
        
        int x = (int) point2d.getX();
        int y = (int) point2d.getY();
        Point p = new Point(x,y);
        return p;
        
	}
	

	public void mousePressed(MouseEvent e){
		previousX = e.getX();
        previousY = e.getY();
	}

	public void zoomAendern(double scrollAmount) {
		
		zoom += 0.1*scrollAmount*zoom;
        zoom = Math.max(0.00001, zoom);
        //System.out.println("zoom: " + zoom);
        
	}

	public void setCenter(Point point) {
		Koordinate cent = getKoordinateFromBildschirmKoordinate(point);
		CenterLat = cent.latitude;
		CenterLong = cent.longitude;
		
	}
	
	public void switchAuswahl(int i){
		auswahl[i]=!auswahl[i];
	}
	
	
		
}
