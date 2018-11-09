package com.kylenanakdewa.story.quests.objectives;

import com.kylenanakdewa.story.tags.Tag;
import com.kylenanakdewa.story.tags.data.LocationData;

import org.bukkit.Location;

/**
 * Represents an Objective that must be completed at a certain location.
 * @author Kyle Nanakdewa
 */
public abstract class LocationObjective extends Objective {

    /** The tag location data involved in this objective. May be null. */
    protected LocationData locationData;
    /** The location involved in this objective. */
    protected Location location;
    /** The radius of the location. */
    protected double radius;
    /** A friendly name for the location. */
    protected String locationName;


    /**
     * Creates a LocationObjective with the specified location, radius, and display name.
     * @param location the center point of the location
     * @param radius the radius that the character must be within to complete the objective
     * @param locationName the display name for the location
     */
    public LocationObjective(Location location, double radius, String locationName){
        this.location = location;
        this.radius = radius;
        this.locationName = locationName;
    }
    /**
     * Creates a LocationObjective with the specified location and display name.
     * Radius will default to 3.
     * @param location the center point of the location
     * @param locationName the display name for the location
     */
	public LocationObjective(Location location, String locationName){
		this(location, 3, locationName);
    }
    /**
     * Creates a LocationObjective based on the LocationData for the specified Tag.
     * @param tag the tag to get location data for
     */
    public LocationObjective(Tag tag){
        locationData = tag.getLocationData();
        if(locationData.getPrimaryLocationBeacon()!=null){
            location = locationData.getPrimaryLocationBeacon().getKey();
            radius = locationData.getPrimaryLocationBeacon().getValue();
        }
        locationName = !locationData.getDisplayNames().isEmpty() ? locationData.getDisplayName() : location.getBlockX()+" "+location.getBlockY()+" "+location.getBlockZ();
    }


    /**
     * Gets the location of this objective.
     */
    public Location getLocation(){
        return location;
    }

    /**
     * Checks if the specified location meets the requirements for this objective.
     */
    public boolean isWithinLocation(Location location){
        return this.location.distanceSquared(location) <= Math.pow(radius, 2);
    }

    /**
     * Gets the name of the location of this objective.
     */
    public String getLocationName(){
        return locationName;
    }

}