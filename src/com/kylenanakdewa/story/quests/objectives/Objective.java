package com.kylenanakdewa.story.quests.objectives;

import org.bukkit.Bukkit;

/**
 * Represents an Objective that can be completed by a Character.
 * @author Kyle Nanakdewa
 */
public abstract class Objective {

    private Status status;

    /**
     * Represents the status of an Objective.
     */
    public enum Status {
        /** The objective is active, and can successfully be completed. */
        ACTIVE,
        /** The objective has been fully completed. */
        COMPLETED,
        /** The objective has been failed, and is no longer possible to complete. */
        FAILED;
    }

    /**
     * Returns the Status of this Objective.
     * @return the status
     */
    public Status getStatus(){
        return status;
    }

    /**
     * Returns true if this objective has been fully completed.
     * @return true if the objective is complete, otherwise false
     */
    public boolean isCompleted(){
        return status.equals(Status.COMPLETED);
    }
    /**
     * Marks this objective as fully completed.
     */
    public void setCompleted(){
        Bukkit.getServer().getPluginManager().callEvent(new ObjectiveStatusEvent(this, getStatus()));
        status = Status.COMPLETED;
    }

    /**
     * Returns true if this objective can no longer be successfully completed. 
     * The Character is considered to have failed the objective.
     * @return true if the objective is impossible to complete, otherwise false
     */
    public boolean isFailed(){
        return status.equals(Status.FAILED);
    }
    /**
     * Marks this objective as failed (cannot be successfully completed).
     */
    public void setFailed(){
        Bukkit.getServer().getPluginManager().callEvent(new ObjectiveStatusEvent(this, getStatus()));
        status = Status.FAILED;
    }


    /**
     * Gets the unique identifier for this Objective.
     */
    public abstract String getIdentifier();


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