package com.kylenanakdewa.story.quests.objectives;

import com.kylenanakdewa.story.quests.objectives.Objective.Status;

import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;

/**
 * Fired when the status of an Objective changes.
 */
public class ObjectiveStatusEvent extends Event {
    private static final HandlerList handlers = new HandlerList();

    private final Objective objective;
    private final Status oldStatus;


    public ObjectiveStatusEvent(Objective objective, Status oldStatus) {
        this.objective = objective;
        this.oldStatus = oldStatus;
    }


	@Override
	public HandlerList getHandlers(){
		return handlers;
	}
	public static HandlerList getHandlerList(){
		return handlers;
    }
    

    /**
     * Gets the objective that changed status.
     * @return the objective
     */
    public Objective getObjective(){
        return objective;
    }

    /**
     * Returns the new Status of the Objective.
     * @return the new status
     */
    public Status getNewStatus(){
        return objective.getStatus();
    }
    /**
     * Returns the previous Status of the Objective.
     * @return the old status
     */
    public Status getOldStatus(){
        return oldStatus;
    }
}