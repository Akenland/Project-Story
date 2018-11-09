package com.kylenanakdewa.story.tags.data;

import java.util.HashSet;
import java.util.Iterator;
import java.util.Random;
import java.util.Set;

import com.kylenanakdewa.story.StoryPlugin;
import com.kylenanakdewa.story.quests.objectives.GoToLocationObjective;
import com.kylenanakdewa.story.quests.objectives.Objective;
import com.kylenanakdewa.story.tags.Tag;
import com.kylenanakdewa.story.tags.TagDataSection;

import org.bukkit.configuration.ConfigurationSection;

/**
 * Objective data for a Tag.
 * @author Kyle Nanakdewa
 */
public class ObjectiveData extends TagDataSection {

    /** The objective data. */
    private final ConfigurationSection data = super.data.getConfigurationSection("location");

    /** The objectives in the tag's data. */
    private Set<Objective> objectives;


    public ObjectiveData(Tag tag){
        super(tag, StoryPlugin.plugin);
    }

    
    /**
     * Gets all objectives in this tag.
     * @return a set of objectives
     */
    public Set<Objective> getObjectives(){
        if(objectives==null){
            objectives = new HashSet<Objective>();
            if(data!=null){
                for(String id : data.getKeys(false)){
                    ConfigurationSection objData = data.getConfigurationSection(id);
                    Objective objective = Objective.getFromConfig(objData);
                    if(objective!=null) objectives.add(objective);
                }
            }

            // Add location objective
            if(tag.getLocationData().getPrimaryLocationBeacon()!=null){
                objectives.add(new GoToLocationObjective(tag));
            }

        }

        return objectives;
    }

    /**
     * Gets a objective in this tag, by ID.
     * @return the objective with a matching ID, or null if one does not exist
     */
    public Objective getObjective(String id){
        for(Objective objective : getObjectives()){
            if(objective.getIdentifier().equalsIgnoreCase(id)) return objective;
        }
        return null;
    }

    /**
     * Gets a random objective in this tag.
     * @return a random objective, or null if this tag contains no objectives
     */
    public Objective getRandomObjective(){
        if(getObjectives().isEmpty()) return null;

        // Pick a random objective
        Iterator<Objective> iterator = getObjectives().iterator();
        for(int i=0; i < new Random().nextInt(getObjectives().size()); i++){
            iterator.next();
        }
        return iterator.next();
    }

}