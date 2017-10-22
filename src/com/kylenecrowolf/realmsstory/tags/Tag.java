package com.kylenecrowolf.realmsstory.tags;

import com.KyleNecrowolf.RealmsCore.Common.ConfigAccessor;
import com.KyleNecrowolf.RealmsCore.Common.Utils;
import com.KyleNecrowolf.RealmsCore.Prompts.Prompt;
import com.KyleNecrowolf.RealmsCore.Realm.Realm;
import com.kylenecrowolf.realmsstory.Main;

import java.util.ArrayList;
import java.util.List;

import org.bukkit.ChatColor;
import org.bukkit.command.CommandSender;
import org.bukkit.configuration.file.FileConfiguration;

/**
 * A tag that can be applied to game objects and used to identify their behavior.
 */
public class Tag {

    //// Tag properties
    /**
     * The unique name of this tag.
     */
    private final String name;
    /**
     * Whether this tag's data has been loaded from file.
     */
    private boolean loaded;
    /**
     * The FileConfiguration for this tag.
     */
    private FileConfiguration file;
    /**
     * All tags inherited by this tag, in order of priority. When defining behaviors for tagged objects, start with this tag, then iterate through the inherited tags.
     */
    private List<Tag> inheritedTags = new ArrayList<Tag>();


    //// Tag data
    /**
     * The {@link Realm} associated with this tag.
     */
    private Realm realm;


    /**
     * Gets a Tag by name.
     * @param name the unique name of the tag
     */
    public Tag(String name){
        this.name = ChatColor.stripColor(name.toLowerCase());
    }
    /**
     * Gets an abstract tag, that inherits the provided tags.
     * Abstract tags do not exist in file, and are simply a way to combine multiple tags into one.
     * @param tags the tags to inherit/combine into this tag
     */
    public Tag(List<Tag> tags){
        name = null;
        inheritedTags.addAll(tags);
    }


    /**
     * Load the tag from file, if it exists.
     */
    private void load(){
        // If already loaded, return
        if(loaded) return;

        if(name!=null){
            // Get the file
            file = new ConfigAccessor("tags\\"+name+".yml", Main.plugin).getConfig();

            // Load inherited tags
            for(String tagName : file.getStringList("inherit")) inheritedTags.add(new Tag(tagName));
        }

        loaded = true;
        
        for(Tag tag : getTotalInheritedTags()){
            // Load Realm
            if(realm==null){
                String realmName = tag.getData().getString("data.realm");
                Utils.notifyAdmins("Found realm "+realmName+" for tag "+name);
                if(realmName!=null){
                    realm = new Realm(realmName);
                    Utils.notifyAdmins("Assigned realm "+realm.getFullName()+" for tag "+name);
                }
            }
        }
        if(realm!=null && realm.exists()){
            if(!realm.getName().equalsIgnoreCase(name)) inheritedTags.add(new Tag(realm.getName()));
            Utils.notifyAdmins("Saved realm "+realm.getFullName()+" for tag "+name);
        }
    }


    /**
     * Gets the unique name of this Tag.
     * @return the unique name of this tag
     */
    public String getName(){
        return name;
    }


    /**
     * Gets directly inherited tags. Does not return indirectly inherited tags (tags inherited from other tags).
     * @return the inherited tags
     */
    public List<Tag> getDirectInheritedTags(){
        load();
        return inheritedTags;
    }
    /**
     * Gets all inherited tags, including indirectly inherited tags, and this tag.
     * @return all tags, up to and including this tag
     */
    public List<Tag> getTotalInheritedTags(){
        List<Tag> allTags = new ArrayList<Tag>();
        if(name!=null) allTags.add(this);
        for(Tag tag : getDirectInheritedTags()){
            allTags.addAll(tag.getTotalInheritedTags());
        }
        return allTags;
    }


    /**
     * Displays information about this tag to a {@link CommandSender}.
     * @param sender the {@link CommandSender} to display information to
     */
    public void displayInfo(CommandSender sender){
        Prompt prompt = new Prompt();
        prompt.addQuestion(Utils.infoText+"--- Tag: "+Utils.messageText+name+Utils.infoText+" ---");
        prompt.addQuestion(Utils.infoText+"Inherits the following tags:");
        for(Tag tag : getTotalInheritedTags()){
            prompt.addAnswer(tag.name, "");
        }
        prompt.display(sender);
    }


    /**
     * Gets the {@link FileConfiguration} for this tag.
     */
    public FileConfiguration getData(){
        load();
        return file;
    }


    /**
     * Gets the {@link Realm} associated with this tag.
     * @return the Realm, or null if this tag is not associated with any Realm.
     */
    public Realm getRealm(){
        load();
        if(realm!=null) Utils.notifyAdmins("getRealm called on "+name+", returning realm "+realm.getFullName());
        else Utils.notifyAdmins("getRealm called on "+name+", returning null");
        return realm;
    }
}