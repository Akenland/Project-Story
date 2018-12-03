package com.kylenanakdewa.story.quests.objectives;

import java.util.Map;

import com.kylenanakdewa.story.tags.Condition;
import com.kylenanakdewa.story.tags.Interaction;
import com.kylenanakdewa.story.tags.Tag;
import com.kylenanakdewa.story.tags.taggable.TaggedNPC;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.configuration.ConfigurationSection;

import net.citizensnpcs.api.CitizensAPI;

/**
 * Represents an Objective that can be completed by a Character.
 * @author Kyle Nanakdewa
 */
public abstract class Objective {

    public static Objective loadObjective(String identifier){
        if(identifier.startsWith("talknpc")){
            String content = identifier.split("_", 2)[1];
            if(Character.isDigit(content.charAt(0))){
                int npcID = Integer.parseInt(content);
                return new NPCTalkObjective(CitizensAPI.getNPCRegistry().getById(npcID));
            } else {
                return new NPCTalkObjective(TaggedNPC.getRandomNPC(new Condition(content)).getNPC());
            }
        }

        if(identifier.startsWith("gotoloc")){
            String[] idContents = identifier.replaceFirst("_", " ").split(" ", 7);
            if(idContents.length==2){
                return new GoToLocationObjective(Tag.get(idContents[1]));
            }
            String worldName = idContents[1];
            double x = Double.parseDouble(idContents[2]);
            double y = Double.parseDouble(idContents[3]);
            double z = Double.parseDouble(idContents[4]);
            double radius = Double.parseDouble(idContents[5]);
            String locName = idContents.length==7 ? idContents[6] : null;

            Location loc = new Location(Bukkit.getWorld(worldName), x, y, z);

            return new GoToLocationObjective(loc, radius, locName);
        }

        if(identifier.startsWith("tag:")){
            String[] idContents = identifier.replace("tag:", "").split(".", 2);
            return Tag.get(idContents[0]).getObjectiveData().getObjective(idContents[1]);
        }


        return new DummyObjective(identifier.split("_", 2)[0], identifier.split("_", 2)[1]);
    }


    /**
     * Loads an Objective from a ConfigurationSection.
     * @param config the ConfigurationSection containing the objective data
     * @return the objective, or null if no valid data was found
     */
    public static Objective getFromConfig(ConfigurationSection config){
        if(config==null) return null;

        // Load the data from file
        Objective objective = loadObjective(config.getName());
        if(config.contains("description")) objective.setDescription(config.getString("description"));
        if(config.contains("onStart")) objective.setStartInteraction(Interaction.getFromConfig(config.getConfigurationSection("onStart")));
        if(config.contains("onComplete")) objective.setStartInteraction(Interaction.getFromConfig(config.getConfigurationSection("onComplete")));
        if(config.contains("onFail")) objective.setStartInteraction(Interaction.getFromConfig(config.getConfigurationSection("onFail")));

		return objective;
    }


    /** A concise (one-line) description of how to complete this objective. */
    protected String description;

    /** The interactions that are shown to characters with this objective. */
    protected Map<Condition,Interaction> conditionalInteractions;
    /** The interaction shown when this objective is started. */
    protected Interaction startInteraction;
    /** The interaction shown when this objective is successfully completed. */
    protected Interaction completeInteraction;
    /** The interaction shown when this objective is failed. */
    protected Interaction failInteraction;

    /** The status of this objective. */
    private Status status = Status.ACTIVE;

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
     * Override this method to change what happens when this objective is completed.
     * Remember to call super.setCompleted() so that the objective is actually completed.
     */
    public void setCompleted(){
        status = Status.COMPLETED;
        Bukkit.getServer().getPluginManager().callEvent(new ObjectiveStatusEvent(this, getStatus()));
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
     * Override this method to change what happens when this objective is failed.
     * Remember to call super.setFailed() so that the objective is actually completed.
     */
    public void setFailed(){
        status = Status.FAILED;
        Bukkit.getServer().getPluginManager().callEvent(new ObjectiveStatusEvent(this, getStatus()));
    }


    /**
     * Gets the unique identifier for this Objective.
     */
    public abstract String getIdentifier();


    /**
     * Gets a concise (one-line) description of how to complete this objective.
     * @return the short description of this objective
     */
    public String getDescription(){
        return description;
    }
    /**
     * Sets a concise (one-line) description of how to complete this objective.
     * @param description the new short description for this objective, or null to clear
     */
    public void setDescription(String description){
        this.description = description;
    }

    /**
     * Gets a long description of how to complete this objective.
     * @return the long description of this objective
     */
    //public abstract String getLongDescription();
    /**
     * Sets a long description of how to complete this objective.
     * @param description the new long description for this objective, or null to clear
     */
    //public abstract void setLongDescription(String description);


    /**
     * Gets the interactions that are shown to characters with this objective.
     * The condition is on which to display the interaction, evaluated on the NPC that is interacted with.
     * @return the interactions for this objective, or null if none are set
     */
    public Map<Condition,Interaction> getInteractions(){
        return conditionalInteractions;
    }
    /**
     * Sets the interactions that are shown to characters with this objective.
     * The condition is on which to display the interaction, evaluated on the NPC that is interacted with.
     * @param interactions the new interactions for this objective, or null to clear
     */
    public void setInteractions(Map<Condition,Interaction> interactions){
        conditionalInteractions = interactions;
    }

    /**
     * Gets the interaction shown when this objective is started.
     * @return the start interaction, or null if none was set
     */
    public Interaction getStartInteraction(){
        return startInteraction;
    }
    /**
     * Sets the interaction shown when this objective is started.
     * @param interaction the new start interaction, or null to clear
     */
    public void setStartInteraction(Interaction interaction){
        startInteraction = interaction;
    }

    /**
     * Gets the interaction shown when this objective is successfully completed.
     * @return the completion interaction, or null if none was set
     */
    public Interaction getCompletionInteraction(){
        return completeInteraction;
    }
    /**
     * Sets the interaction shown when this objective is successfully completed.
     * @param interaction the new completion interaction, or null to clear
     */
    public void setCompletionInteraction(Interaction interaction){
        completeInteraction = interaction;
    }

    /**
     * Gets the interaction shown when this objective is failed.
     * @return the fail interaction, or null if none was set
     */
    public Interaction getFailInteraction(){
        return failInteraction;
    }
    /**
     * Sets the interaction shown when this objective is failed.
     * @param interaction the new fail interaction, or null to clear
     */
    public void setFailInteraction(Interaction interaction){
        failInteraction = interaction;
    }
}