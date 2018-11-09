package com.kylenanakdewa.story.quests.objectives;

import com.kylenanakdewa.story.tags.Interaction;
import com.kylenanakdewa.story.tags.taggable.TempNPC;

import net.citizensnpcs.api.npc.NPC;

/**
 * An objective to talk to an NPC.
 * @author Kyle Nanakdewa
 */
public class NPCTalkObjective extends NPCObjective {

    /**
     * Creates an objective for talking to the specified NPC.
     * @param npc the NPC to talk to
     */
    public NPCTalkObjective(NPC npc){
        super(npc);
    }


    @Override
    public String getIdentifier() {
        return "talknpc_"+npc.getId();
    }

    @Override
    public String getDescription() {
        return super.getDescription()==null ? "Talk to "+new TempNPC(npc).getFormattedName() : super.getDescription();
    }

    @Override
    public Interaction getStartInteraction() {
        if(super.getStartInteraction()!=null) return super.getStartInteraction();

        Interaction interaction = new Interaction();
        TempNPC npcCharacter = new TempNPC(npc);
        String name = npcCharacter.getFormattedName();
        String location = taggedNPC.getTag().getLocationData().getDisplayName();
        if(location==null && npcCharacter.getRealm()!=null) location = "the "+npcCharacter.getRealm().getName();
        if(location==null) location = "Akenland";
        interaction.addQuestion("You need to go talk to "+name+" in "+location+".");
        interaction.addQuestion("Could you go talk to "+name+"? They can be found in "+location+".");
        interaction.addQuestion("I need you to go talk to "+name+". You can find them in "+location+".");
        interaction.addQuestion("Go talk to "+name+". They're in "+location+".");
        interaction.addQuestion("I'd like you to talk to "+name+". You'll find them in "+location+".");
        interaction.addQuestion("You need to go meet with "+name+" in "+location+".");
        interaction.addQuestion("Could you go meet with "+name+"? They can be found in "+location+".");
        interaction.addQuestion("I need you to go meet with "+name+". You can find them in "+location+".");
        interaction.addQuestion("Go meet with "+name+". They're in "+location+".");
        interaction.addQuestion("I'd like you to meet with "+name+". You'll find them in "+location+".");
        interaction.setRandomQuestions(true);
        return interaction;
    }
}