package com.kylenanakdewa.story.quests.objectives;

import java.util.Arrays;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import com.kylenanakdewa.story.tags.Condition;
import com.kylenanakdewa.story.tags.Interaction;

/**
 * An objective made up of several sub-objectives. 
 * This objective is complete when all of its sub-objectives are complete.
 * @author Kyle Nanakdewa
 */
public class CompoundObjective extends Objective {

    /** The sub-objectives. */
    private Set<Objective> subObjectives;
    /** The description of this objective. If null, sub-objective descriptions are used instead. */
    private String description;


    /**
     * Creates a CompoundObjective.
     * @param subObjectives a set of sub-objectives
     */
    public CompoundObjective(Set<Objective> subObjectives) {
        this.subObjectives = subObjectives;
    }
    /**
     * Creates a CompoundObjective.
     * @param subObjectives an array of sub-objectives
     */
    public CompoundObjective(Objective... subObjectives) {
        this.subObjectives = new HashSet<Objective>(Arrays.asList(subObjectives));
    }


	@Override
	public boolean isCompleted() {
        if(super.isCompleted()) return true;
        // Return false if any sub-objective is incomplete
		for(Objective objective : subObjectives){
            if(!objective.isCompleted()) return false;
        }
        return true;
	}

	@Override
	public boolean isFailed() {
        if(super.isFailed()) return true;
        // Return true if any sub-objective is failed
		for(Objective objective : subObjectives){
            if(objective.isFailed()) return true;
        }
        return false;
    }


    @Override
    public String getIdentifier() {
        return null;
    }


	@Override
	public String getDescription() {
        if(description!=null) return description;

        String desc = "";
        for(Objective objective : subObjectives){
            if(objective.getDescription()!=null){
                desc+= objective.getDescription() + ", ";
            }
        }
        return desc;
    }
    @Override
    public void setDescription(String description) {
        this.description = description;
    }

    @Override
	public Map<Condition, Interaction> getInteractions() {
		return null;
	}


	@Override
	public void setInteractions(Map<Condition, Interaction> interactions) {
		
	}
}