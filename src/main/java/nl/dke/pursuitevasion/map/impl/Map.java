package nl.dke.pursuitevasion.map.impl;

import nl.dke.pursuitevasion.map.AbstractObject;
import nl.dke.pursuitevasion.map.MapPolygon;
import nl.dke.pursuitevasion.map.builders.MapBuilder;

import javax.swing.*;
import javax.swing.filechooser.FileNameExtensionFilter;
import java.io.*;
import java.awt.*;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;

/**
 * A map defines polygon objects on which to "play" the pursuit-evasion game
 *
 * A map consists of floors.
 *
 * Each floor can contain obstacles
 *
 * Created by nik on 2/8/17.
 */
public class Map implements Serializable
{
    /**
     * Every map needs a name
     */
    private String name = "";

    /**
     * Floors in the map
     */
    private Collection<Floor> floors;

    /**
     * Construct the map created by one or more floors
     *
     * @param floors the floors which make up the map
     */
    public Map(Collection<Floor> floors)
    {
        this.floors = Collections.unmodifiableCollection(floors);
    }

    /**
     * Get a read-only Collection of the floors which make up this map
     *
     * @return the collection of floors on this map
     */
    public Collection<Floor> getFloors()
    {
        return floors;
    }

    public static Map loadFile() {

            JFileChooser fc = new JFileChooser("");
            fc.setCurrentDirectory(new File( System.getProperty("user.dir")));
            fc.setAcceptAllFileFilterUsed(true);
            fc.setMultiSelectionEnabled(false);
            fc.setFileFilter(new FileNameExtensionFilter("Serialized","ser"));
           // fc.setLocation(parent.getLocation());
            fc.setVisible(true);


            int returnVal = fc.showOpenDialog(null);
            if(returnVal == JFileChooser.APPROVE_OPTION) {
                System.out.println("You chose to open this file: " +
                        fc.getSelectedFile().getName());



                File f = fc.getSelectedFile();


            try {
                FileInputStream s = new FileInputStream(f);
                ObjectInputStream o = new ObjectInputStream(s);
                Map m = (Map)o.readObject();
                return m;
            }
            catch (IOException | ClassNotFoundException e){
               e.printStackTrace();
            }

        }
        return null;
    }

    /**
     * @param fc the file chooser
     * @param extension like ".txt"
     * @return if
     */
    public static File addExtension(JFileChooser fc, String extension) {
        File file = fc.getSelectedFile();
        String path = file.getAbsolutePath();



        if(!path.endsWith("." + extension))
        {
            file = new File(path +"."+ extension);
        }
        return file;
    }

    public static void saveToFile(Map map) {
        JFileChooser fc = new JFileChooser("");
        fc.setCurrentDirectory(new File( System.getProperty("user.dir")));
        fc.setAcceptAllFileFilterUsed(true);
        fc.setMultiSelectionEnabled(false);
        fc.setFileFilter(new FileNameExtensionFilter("Serialized","ser"));
        // fc.setLocation(parent.getLocation());
        fc.setVisible(true);

        int returnVal = fc.showSaveDialog(null);
        if(returnVal == JFileChooser.APPROVE_OPTION) {
            System.out.println("You chose to save this file: " +
                    fc.getSelectedFile().getName());
            File f = addExtension(fc,"ser");


        FileOutputStream s;
        try{
            s = new FileOutputStream(f);
            ObjectOutputStream os = new ObjectOutputStream(s);
            os.writeObject(map);
            os.close();
            s.close();
        }
        catch (IOException e){
            e.printStackTrace();
        }
         }
    }

    /**
     * Get the collection of polygons which make up all objects of this map
     *
     * @return An ArrayList of Polygons making up this map
     */
    public Collection<MapPolygon> getPolygons()
    {
        ArrayList<MapPolygon> polygons = new ArrayList<>();
        fillPolygonList(polygons);
        polygons.trimToSize();
        return polygons;
    }

    /**
     * Fill a Collection of polygons with all Polygons of this map
     *
     * @param polygons the list of polygons to fill
     */
    private void fillPolygonList(Collection<MapPolygon> polygons)
    {
        fillObjectList(floors, polygons);
        for(Floor floor : floors)
        {
            fillObjectList(floor.getObstacles(), polygons);
            fillObjectList(floor.getGates(), polygons);
        }
    }

    /**
     * Fill a collection of polygons with the polygons of a collection of AbstractObjects
     *
     * @param collection the collection of AbstractObjects
     * @param polygons the collection of polygons to fill
     */
    private void fillObjectList(Collection<? extends AbstractObject> collection, Collection<MapPolygon> polygons)
    {
        for(AbstractObject object : collection)
        {
            polygons.add(object.getPolygon());
        }
    }

    /**
     * Get a simple Map which is a rectangle floor with a box in the middle

     * @return A map object containing a simple map
     */
    public static Map getSimpleMap()
    {
        return getMap("level.ser");

    }

    public static Map getMap(String path) {

        try {
            FileInputStream s = new FileInputStream(path);
            ObjectInputStream o = new ObjectInputStream(s);
            Map m = (Map)o.readObject();
            return m;
        }
        catch (IOException | ClassNotFoundException e){
            MapPolygon mainFloor = new MapPolygon(
                    new int[] {  0, 300, 900, 1200, 900, 300},
                    new int[] {300,   0,   0, 300, 600, 600},
                    6,
                    false
            );

            MapPolygon obstacle = new MapPolygon(
                    new int[] {540, 660, 660, 540},
                    new int[] {240, 240, 360, 360},
                    4,
                    true
            );

            MapPolygon obstacle1 = new MapPolygon(
                    new int[] {300, 250, 350 },
                    new int[] {100, 300, 300},
                    3,
                    true
            );

            return MapBuilder.create()
                    .makeFloor(mainFloor)
                    .addObstacle(obstacle)
                    .addObstacle(obstacle1)
                    .finish()
                    .build();
        }
    }
}
