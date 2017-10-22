package com.kylenecrowolf.realmsstory.tags.taggable;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.bukkit.command.CommandSender;
import org.bukkit.event.EventHandler;

import com.KyleNecrowolf.RealmsCore.Common.Utils;
import com.KyleNecrowolf.RealmsCore.Prompts.Prompt;
import com.KyleNecrowolf.RealmsCore.Prompts.PromptActionEvent;
import com.kylenecrowolf.realmsstory.tags.NPCTag;
import com.kylenecrowolf.realmsstory.tags.Tag;
import net.citizensnpcs.api.CitizensAPI;
import net.citizensnpcs.api.event.NPCRightClickEvent;
import net.citizensnpcs.api.persistence.Persist;
import net.citizensnpcs.api.trait.Trait;
import net.citizensnpcs.api.trait.TraitName;

/**
 * Represents a Citizens NPC Trait with RealmsStory Tags.
 */
@TraitName("tags")
public class TaggedNPC extends Trait implements Taggable {

    /**
     * All tags applied to this NPC
     */
    private List<Tag> tags;

    /**
     * Tags saved to this NPC in the saves file
     */
    @Persist("tags") private List<String> savedTags;


    public TaggedNPC(){
        super("tags");
    }


    public List<Tag> getTags(){
        if(tags==null){
            tags = new ArrayList<Tag>();
            // Load tags from NPC saves
            if(savedTags!=null) for(String tag : savedTags) tags.addAll(new Tag(tag).getTotalInheritedTags());

            // Load other traits
            for(Trait trait : getNPC().getTraits()) tags.addAll(new Tag(trait.getName()).getTotalInheritedTags());
        }
        return tags;       
    }

    public Tag getTag(){
        return new NPCTag(getTags());
    }

    public boolean hasTag(Tag... tags){
        return getTags().containsAll(Arrays.asList(tags));
    }

    public void addTag(Tag tag){
        //getTags().add(0, tag);
        if(savedTags==null) savedTags = new ArrayList<String>();
        savedTags.add(0, tag.getName());
        tags = null;
    }

    /**
     * Removes a tag from this object.
     * This method will only remove tags saved to this NPC; it will NOT remove inherited tags.
     * @param tag the tag to be removed from this object
     */
    public void removeTag(Tag tag){
        savedTags.remove(tag.getName());
        tags = null;
    }

    public void reload(){
        tags = null;
    }


    public void displayInfo(CommandSender sender){
        Prompt prompt = new Prompt();
        prompt.addQuestion(Utils.infoText+"--- NPC: "+Utils.messageText+getNPC().getFullName()+Utils.infoText+" "+getNPC().getId()+" ---");
        prompt.addQuestion(Utils.infoText+"Has the following tags:");
        for(Tag tag : getTags()){
            prompt.addAnswer(tag.getName(),"");
        }
        prompt.display(sender);
    }


    /**
     * Player chat event. Looks for the most significant conversation in tags, and displays it to player.
     */
    @EventHandler
    public void onPlayerInteract(NPCRightClickEvent event){
        if(event.getNPC() != this.getNPC()) return;
        ((NPCTag)getTag()).displayConversation(event.getClicker(), event.getNPC());
    }
    /**
     * Player chat response event. Displays the requested prompt when player chooses a response to a previous conversation.
     */
    @EventHandler
    public void onPlayerPromptResponse(PromptActionEvent event){
        if(event.isType("npc")){
            String action[] = event.getAction().split("\\.", 2);
            TaggedNPC npc = getTaggedNPC(Integer.parseInt(action[0]));
            if(npc.getNPC() != this.getNPC()) return;
            ((NPCTag)npc.getTag()).displayConversation(event.getPlayer(), npc.getNPC(), action[1]);
        }
    }


    /**
     * Gets the TaggedNPC {@link Trait} for an {@link net.citizensnpcs.api.npc.NPC}.
     * @param npcID the ID of the {@link net.citizensnpcs.api.npc.NPC}
     * @return the TaggedNPC trait
     */
    public static TaggedNPC getTaggedNPC(int npcID){
        return CitizensAPI.getNPCRegistry().getById(npcID).getTrait(TaggedNPC.class);
    }
}