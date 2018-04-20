package com.kylenanakdewa.story.quests.objectives;

/**
 * Represents an Objective that can be completed by a Character.
 * @author Kyle Nanakdewa
 */
public abstract class Objective {

    /**
     * Returns true if this objective has been fully completed.
     * @return true if the objective is complete, otherwise false
     */
    public abstract boolean isCompleted();

    /**
     * Returns true if this objective can no longer be successfully completed. 
     * The Character is considered to have failed the objective.
     * @return true if the objective is impossible to complete, otherwise false
     */
    public abstract boolean isFailed();


    /**
     * Gets a concise (one-line) description of how to complete this objective.
     * @return the description of this objective
     */
    public abstract String getDescription();

    /**
     * Sets a concise (one-line) description of how to complete this objective.
     * @param description the new description for this objective, or null to clear
     */
    public abstract void setDescription(String description);

}