package com.kylenanakdewa.story.quests.objectives;

import com.kylenanakdewa.story.tags.taggable.TaggedNPC;

import net.citizensnpcs.api.npc.NPC;

/**
 * An objective involving an NPC.
 * @author Kyle Nanakdewa
 */
public abstract class NPCObjective extends Objective {

	/** The Citizens NPC involved in this objective. */
	protected NPC npc;
	/** The TaggedNPC involved in this objective. */
	protected TaggedNPC taggedNPC;


	public NPCObjective(NPC npc){
		this.npc = npc;
		taggedNPC = TaggedNPC.getTaggedNPC(npc);
	}

}