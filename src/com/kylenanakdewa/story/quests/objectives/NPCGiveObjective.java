package com.kylenanakdewa.story.quests.objectives;

import com.kylenanakdewa.story.tags.Interaction;
import com.kylenanakdewa.story.tags.taggable.TempNPC;

import org.bukkit.ChatColor;
import org.bukkit.Material;

import net.citizensnpcs.api.npc.NPC;

/**
 * An objective to give an item to an NPC.
 * @author Kyle Nanakdewa
 */
public class NPCGiveObjective extends NPCObjective {

    /** The type of material that needs to be delivered. */
    private final Material material;
    /** The quantity of items that need to be delivered. */
    private final int quantity;


    /**
     * Creates an objective for giving an item to the specified NPC.
     * @param npc the NPC to talk to
     */
    public NPCGiveObjective(NPC npc, Material material, int quantity) {
        super(npc);
        this.material = material;
        this.quantity = quantity;
    }


    @Override
    public String getIdentifier() {
        return "givenpc_"+npc.getId();
    }

    @Override
    public String getDescription() {
        if(super.getDescription()!=null) return super.getDescription();

        TempNPC npcCharacter = new TempNPC(npc);
        String location = taggedNPC.getTag().getLocationData().getDisplayName();
        if(location==null && npcCharacter.getRealm()!=null) location = "the "+npcCharacter.getRealm().getName();
        location = location!=null ? " in "+location : "";
        return "Bring ITEM_NAME to " + npcCharacter.getFormattedName() + ChatColor.RESET + location;
    }

    @Override
    public Interaction getStartInteraction() {
        if(super.getStartInteraction()!=null) return super.getStartInteraction();

        Interaction interaction = new Interaction();
        TempNPC npcCharacter = new TempNPC(npc);
        String name = npcCharacter.getFormattedName() + ChatColor.RESET;
        String location = taggedNPC.getTag().getLocationData().getDisplayName();
        if(location==null && npcCharacter.getRealm()!=null) location = "the "+npcCharacter.getRealm().getName() + ChatColor.RESET;
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

    @Override
    public Interaction getCompletionInteraction() {
        if(super.getCompletionInteraction()!=null) return super.getCompletionInteraction();
        Interaction interaction = new Interaction();
        interaction.setCharacter(new TempNPC(npc));
        return interaction;
    }
}