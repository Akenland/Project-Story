package com.kylenanakdewa.story.quests.objectives;

import net.citizensnpcs.api.npc.NPC;

/**
 * An objective involving an NPC.
 * @author Kyle Nanakdewa
 */
public abstract class NPCObjective extends Objective {

	/** The NPC involved in this objective. */
	private NPC npc;


	public NPCObjective(NPC npc){
		this.npc = npc;
	}

}