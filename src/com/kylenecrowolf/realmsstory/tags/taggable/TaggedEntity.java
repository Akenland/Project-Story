package com.kylenecrowolf.realmsstory.tags.taggable;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.bukkit.command.CommandSender;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;

import com.KyleNecrowolf.RealmsCore.Common.Utils;
import com.KyleNecrowolf.RealmsCore.Player.PlayerData;
import com.KyleNecrowolf.RealmsCore.Prompts.Prompt;
import com.kylenecrowolf.realmsstory.tags.Tag;

/**
 * Represents an entity with RealmsStory Tags. 
 * Note that entities load their tags from the scoreboard, alphabetically. Therefore, it is not possible to manually order tags by priority.
 * Inherited tags will still be loaded in order.
 */
public class TaggedEntity implements Taggable {

    /**
     * The entity represented by this object.
     */
    private final Entity entity;
    /**
     * All tags applied to this entity, 
     */
    private List<Tag> tags;

    /**
     * Gets a TaggedEntity for an entity in the world.
     * @param entity the entity to retrieve tags for
     */
    public TaggedEntity (Entity entity){
        this.entity = entity;
    }

    /**
     * Get the entity represented by this TaggedEntity.
     */
    public Entity getEntity(){
        return entity;
    }


    public List<Tag> getTags(){
        if(tags==null){
            tags = new ArrayList<Tag>();
            // Load tags from scoreboard
            for(String tag : entity.getScoreboardTags()) tags.addAll(new Tag(tag).getTotalInheritedTags());

            // If it's a player, load realm and title
            if(entity instanceof Player){
                PlayerData data = new PlayerData((Player)getEntity());
                String realmName = data.getRealm().getName();
                if(realmName!=null && realmName.length()>1) tags.addAll(new Tag(realmName).getTotalInheritedTags());
                String title = data.getTitle();
                if(title!=null && title.length()>1) tags.addAll(new Tag(title).getTotalInheritedTags());
            }
        }
        return tags;       
    }

    public Tag getTag(){
        return new Tag(getTags());
    }

    public boolean hasTag(Tag... tags){
        return getTags().containsAll(Arrays.asList(tags));
    }

    public void addTag(Tag tag){
        getTags().add(0, tag);
        entity.addScoreboardTag(tag.getName());
    }

    /**
     * Removes a tag from this object.
     * This method will only remove scoreboard tags; it will NOT remove inherited tags.
     * @param tag the tag to be removed from this object
     */
    public void removeTag(Tag tag){
        entity.removeScoreboardTag(tag.getName());
        tags = null;
    }

    public void reload(){
        tags = null;
    }


    public void displayInfo(CommandSender sender){
        Prompt prompt = new Prompt();
        prompt.addQuestion(Utils.infoText+"--- Entity: "+Utils.messageText+entity.getName()+Utils.infoText+" ---");
        prompt.addQuestion(Utils.infoText+"Has the following tags:");
        for(Tag tag : getTags()){
            prompt.addAnswer(tag.getName(),"");
        }
        prompt.display(sender);
    }
}