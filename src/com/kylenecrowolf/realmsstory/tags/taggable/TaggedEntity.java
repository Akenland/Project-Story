package com.kylenecrowolf.realmsstory.tags.taggable;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

import org.bukkit.Material;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Entity;
import org.bukkit.entity.HumanEntity;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;

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
                // Realm name
                String realmName = data.getRealm().getName();
                if(realmName!=null && realmName.length()>1) tags.addAll(new Tag(realmName).getTotalInheritedTags());
                // Title - SECURITY ISSUE: Realm officers can set custom titles, allowing them to have any tag
                //String title = data.getTitle();
                //if(title!=null && title.length()>1) tags.addAll(new Tag(title).getTotalInheritedTags());
                // Realm officer
                if(data.isRealmOfficer()) tags.add(new Tag("realmofficer"));
            }
        }
        return tags;       
    }

    public Tag getTag(){
        return new Tag(getTags());
    }

    public boolean hasTag(Tag... tags){
        // Get a list of the names of all tags
        List<String> tagNames = new ArrayList<String>();
        for(Tag t : getTags()) tagNames.add(t.getName());
        List<String> checkTagNames = new ArrayList<String>();
        for(Tag t : tags) checkTagNames.add(t.getName());

        // Filter out variable tags
        for(Tag t : tags){
            String text = t.getName();

            // Player
            if(text.equals("player") && !(entity instanceof Player)) return false;

            // Has item, Take item
            if(text.startsWith("hasitem_") || text.startsWith("takeitem_")){
                // This condition fails instantly if entity is not a player
                if(!(entity instanceof HumanEntity)) return false;
                Inventory inv = ((HumanEntity)entity).getInventory();

                // Split up the string
                String[] itemString = text.replaceFirst("hasitem_", "").replaceFirst("takeitem_", "").split("\\*", 2);
                Material item = Material.matchMaterial(itemString[0]); if(item==null) return false;
                int amount = itemString.length==2 ? Integer.parseInt(itemString[1]) : 1;

                // Check for the item
                if(!inv.contains(item, amount)) return false;

                // Take item
                if(text.startsWith("takeitem_")) inv.removeItem(new ItemStack(item, amount));

                // Remove tag from list to evaluate
                checkTagNames.remove(text);
            }

        }

        // Return true if all tag names are found
        return tagNames.containsAll(checkTagNames);
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
        reload();
    }

    public void removeAllTags(){
        Set<String> tags = entity.getScoreboardTags();
        for(String tag : tags) entity.removeScoreboardTag(tag);
        reload();
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