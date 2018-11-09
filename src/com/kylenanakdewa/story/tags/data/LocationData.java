package com.kylenanakdewa.story.tags.data;

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.Map.Entry;

import com.kylenanakdewa.core.common.Utils;
import com.kylenanakdewa.story.StoryPlugin;
import com.kylenanakdewa.story.tags.Tag;
import com.kylenanakdewa.story.tags.TagDataSection;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.configuration.ConfigurationSection;

/**
 * Location data for a Tag.
 * @author Kyle Nanakdewa
 */
public class LocationData extends TagDataSection {

    /** The location data. */
    private final ConfigurationSection data = super.data.getConfigurationSection("location");

    /** The locations in this data, and their radii. */
    private Map<Location,Double> locationBeacons;

    /** The primary location, used for calculating distances or directions. Typically near the center point. */
    private Entry<Location,Double> primaryLocation;


    public LocationData(Tag tag){
        super(tag, StoryPlugin.plugin);
    }


    /**
     * Gets the parent location.
     * @return the parent location's tag, or null if not set
     */
    public Tag getParent(){
        String parentName = data!=null ? data.getString("parent") : null;
        return parentName!=null ? Tag.get(parentName) : null;
    }

    /**
     * Gets the location beacons that determine the area of this location.
     * @return a map of location beacons, and their radii
     */
    public Map<Location,Double> getBeacons(){
        if(locationBeacons==null){
            locationBeacons = new HashMap<Location, Double>();
            if(data!=null){
                List<String> entries = data.getStringList("beacons");
                for(String entry : entries){
                    String[] entryData = entry.split(" ");
                    if(entryData.length!=4) Utils.notifyAdminsError("Invalid location beacon in tag "+tag.getName());
                    else{
                        World world = Bukkit.getWorld(entryData[0]);
                        if(world==null) Utils.notifyAdminsError("Invalid world for location beacon in tag "+tag.getName());

                        else try{
                            double x = Double.parseDouble(entryData[1]);
                            double z = Double.parseDouble(entryData[2]);
                            double r = Double.parseDouble(entryData[3]);

                            Location loc = new Location(world, x, 64, z);

                            locationBeacons.put(loc, r);

                            // First entry becomes primary location
                            if(primaryLocation==null){
                                primaryLocation = locationBeacons.entrySet().iterator().next();
                            }
                        }
                        catch(NumberFormatException e){
                            Utils.notifyAdminsError("Invalid values for location beacon in tag "+tag.getName());
                        }
                    }
                }
            }
        }

        return locationBeacons;
    }

    /**
     * Gets the primary location beacon.
     * This is typically near the center of the location, and should be used for calculating distances or directions.
     * @return the primary location beacon's location and radius, or null if there are no location beacons
     */
    public Entry<Location,Double> getPrimaryLocationBeacon(){
        if(getBeacons().isEmpty()) return null;
        return primaryLocation;
    }

    /**
     * Gets a list of all display names for this location, in order of commonness.
     * @return a list of all display names
     */
    public List<String> getDisplayNames(){
        return data!=null ? data.getStringList("display-names") : Arrays.asList();
    }
    /**
     * Gets a random display name for this location.
     * @return a random display name, or null if no names were set
     */
    public String getDisplayName(){
        if(getDisplayNames().isEmpty()) return null;
        while(true){
            for(String name : getDisplayNames()){
                if(new Random().nextBoolean()) return name;
            }
        }
    }

    /**
     * Gets a list of all directional names for this location.
     * @return a list of all directional names
     */
    public List<String> getDirectionalNames(){
        return data!=null ? data.getStringList("directional-names") : Arrays.asList();
    }
    /**
     * Gets a random directional name for this location.
     * @return a random directional name, or null if no names were set
     */
    public String getDirectionalName(){
        List<String> names = getDirectionalNames();
        if(names.isEmpty()) return null;
        return names.get(new Random().nextInt(names.size()));
    }

}