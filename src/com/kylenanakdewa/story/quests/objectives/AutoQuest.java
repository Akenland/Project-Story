package com.kylenanakdewa.story.quests.objectives;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;
import java.util.Random;

import com.kylenanakdewa.core.common.Utils;
import com.kylenanakdewa.story.tags.Interaction;
import com.kylenanakdewa.story.tags.Tag;
import com.kylenanakdewa.story.tags.data.LocationData;
import com.kylenanakdewa.story.tags.taggable.TaggedNPC;
import com.kylenanakdewa.story.tags.taggable.TempNPC;

import org.bukkit.Material;
import org.bukkit.inventory.ItemStack;

import net.citizensnpcs.api.npc.NPC;

/**
 * AutoQuest
 * @author Kyle Nanakdewa
 */
public class AutoQuest extends Quest {

    /** The NPC that gave out this objective. May be null. */
    protected NPC npc;

    /**
     * Creates an AutoQuest, where the starting objective is within the specified tags.
     */
    public AutoQuest(Collection<Tag> tags, NPC npc){
        this.npc = npc;
        List<Objective> subObjectives = new ArrayList<Objective>();
        // Pick out 1-4 objectives
        for(int o=0; o < new Random().nextInt(4); o++){
            // Pick a random tag and objective, max 10 attempts
            int attempts = 0;
            Objective objective = null;
            while(attempts<10 && objective==null){
                attempts++;
                // Pick a random objective
                Iterator<Tag> iterator = tags.iterator();
                for(int i=0; i < new Random().nextInt(tags.size()); i++) iterator.next();
                objective = iterator.next().getObjectiveData().getRandomObjective();

                // Avoid duplicate objectives
                if(objective!=null){
                    if(npc!=null && objective.getIdentifier().equalsIgnoreCase("talknpc_"+npc.getId())) objective = null;
                    else for(Objective previousObjectives : subObjectives){
                        if(previousObjectives.getIdentifier().equalsIgnoreCase(objective.getIdentifier())) objective = null;
                    }
                }
            }
            if(objective==null) Utils.notifyAdminsError("[Story] AutoQuest couldn't find an objective.");
            else {
                subObjectives.add(objective);
                // Next objective uses tags from previous objective
                if(objective instanceof NPCObjective) tags = ((NPCObjective)objective).taggedNPC.getTags();
                if(objective instanceof LocationObjective){
                    LocationData locData = ((LocationObjective)objective).locationData;
                    tags = locData==null ? tags : locData.getTag().getTotalInheritedTags();
                }
            }
        }

        if(!subObjectives.isEmpty() && npc!=null){
            // Add objective to return to NPC
            NPCTalkObjective npcObjective = new NPCTalkObjective(npc);
            npcObjective.setDescription("Return to "+new TempNPC(npc).getFormattedName());
            Interaction completion = new Interaction();
            completion.addQuestion("Thank you, PLAYER_TITLE. Here is something for your trouble.");
            completion.addQuestion("Thank you, PLAYER_TITLE! Here, have this.");
            completion.addQuestion("Good work, PLAYER_TITLE. Please, take this.");
            completion.addQuestion("Excellent job, PLAYER_TITLE. Here is your payment.");
            completion.addQuestion("Thank you for your help, PLAYER_TITLE. Here's your reward.");
            completion.setRandomQuestions(true);
            completion.setItems(new ItemStack(Material.EMERALD, 10*subObjectives.size()));
            completion.setCharacter(new TempNPC(npc));
            npcObjective.setCompletionInteraction(completion);
            // Add it as the final objective
            subObjectives.add(npcObjective);


            // Set character for first sub-objectives starting interaction to be the same as the quest's
            Interaction subStart = subObjectives.get(0).getStartInteraction();
            if(subStart==null){
                subStart = new Interaction();
                subObjectives.get(0).setStartInteraction(subStart);
            }
            if(subStart.getCharacter()==null) subStart.setCharacter(new TempNPC(npc));
        }

        Utils.notifyAdmins("[Story] AutoQuest generated with "+subObjectives.size()+" objectives.");
        subObjectives.forEach(obj -> Utils.notifyAdmins("[Story] - "+obj.getIdentifier()+" - "+obj.getDescription()));
        setSubObjectives(subObjectives);
    }
    /**
     * Creates an AutoQuest, started from a specific NPC.
     */
    public AutoQuest(NPC npc){
        this(TaggedNPC.getTaggedNPC(npc).getTags(), npc);
    }
    /**
     * Creates an AutoQuest, where the starting objective is within the specified tags.
     */
    public AutoQuest(Collection<Tag> tags){
        this(tags, null);
    }


    @Override
    public String getIdentifier() {
        return npc==null ? super.getIdentifier().replaceFirst("quest", "autoquest") : "autoquest_npc_"+npc.getId();
    }

}