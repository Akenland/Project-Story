package com.kylenanakdewa.story.quests.objectives;

import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;

/**
 * An objective made up of several sub-objectives.
 * This objective is complete when all of its sub-objectives are complete. Objectives can be completed in any order.
 * @author Kyle Nanakdewa
 */
@Deprecated
public class CompoundObjective extends Objective {

    /** The sub-objectives. */
    protected Set<Objective> subObjectives;


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


    /**
     * Gets a list of the sub-objectives in this objective.
     * @return the sub-objectives
     */
    public Set<Objective> getSubObjectives(){
        return subObjectives;
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
        String identifier = "compound[";
        for(Objective objective : subObjectives) identifier += objective.getIdentifier()+",";
        if(identifier.endsWith(",")) identifier = identifier.substring(0, identifier.length());
        identifier += "]";
        return identifier;
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

}