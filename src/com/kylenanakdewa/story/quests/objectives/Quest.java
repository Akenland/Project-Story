package com.kylenanakdewa.story.quests.objectives;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;

import com.kylenanakdewa.story.tags.Interaction;

/**
 * An objective made up of several sub-objectives. 
 * This objective is complete when all of its sub-objectives are completed, in order.
 * @author Kyle Nanakdewa
 */
public class Quest extends Objective {

    /** The sub-objectives. */
    protected List<Objective> subObjectives;


    /**
     * Creates a Quest.
     * @param subObjectives a list of sub-objectives
     */
    public Quest(List<Objective> subObjectives) {
        setSubObjectives(subObjectives);
    }
    /**
     * Creates a Quest.
     * @param subObjectives an array of sub-objectives
     */
    public Quest(Objective... subObjectives) {
        this(new ArrayList<Objective>(Arrays.asList(subObjectives)));
    }


    protected void setSubObjectives(List<Objective> subObjectives){
        this.subObjectives = subObjectives;

        // Add the following objective to the completion interaction of the preceding action
        Iterator<Objective> iterator = subObjectives.iterator();
        Objective previous = null;
        while(iterator.hasNext()){
            Objective current = iterator.next();
            if(previous!=null){
                Interaction completion = current.getCompletionInteraction();
                if(completion==null){
                    completion = new Interaction();
                    current.setCompletionInteraction(completion);
                }
                Collection<Objective> objectives = completion.getObjectives();
                if(objectives==null) completion.setObjective(previous);
                else objectives.add(previous);
            } else {
                Interaction start = getStartInteraction();
                if(start==null){
                    start = new Interaction();
                    setStartInteraction(start);
                }
                Collection<Objective> objectives = start.getObjectives();
                if(objectives==null) start.setObjective(current);
                else objectives.add(current);
            }
            previous = current;
        }
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
        String identifier = "quest[";
        for(Objective objective : subObjectives) identifier += objective.getIdentifier()+",";
        if(identifier.endsWith(",")) identifier = identifier.substring(0, identifier.length());
        identifier += "]";
        return identifier;
    }


	@Override
	public String getDescription() {
        if(description!=null) return description;

        // Return first incomplete objective description
        for(Objective objective : subObjectives){
            if(!objective.isCompleted()){
                return objective.getDescription();
            }
        }

        // If all objectives are complete, return the last objective's description, so it shows on the title at the end
        return !subObjectives.isEmpty() ? subObjectives.get(subObjectives.size()-1).getDescription() : "[Empty Quest]";
    }

}