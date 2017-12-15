package com.kylenanakdewa.realmsstory.tags;

import com.KyleNecrowolf.RealmsCore.Common.ConfigAccessor;
import com.KyleNecrowolf.RealmsCore.Common.Utils;
import com.KyleNecrowolf.RealmsCore.Prompts.Prompt;
import com.KyleNecrowolf.RealmsCore.Realm.Realm;
import com.kylenanakdewa.realmsstory.StoryPlugin;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import org.bukkit.ChatColor;
import org.bukkit.command.CommandSender;
import org.bukkit.configuration.file.FileConfiguration;

/**
 * A tag that can be applied to game objects and used to identify their behavior.
 */
public class Tag {

    //// Tag registry
    /**
     * A collection of every Tag currently in use on this server.
     */
    private static final HashMap<String,Tag> tagRegistry = new HashMap<String,Tag>();

    /**
     * Gets a Tag that is in use on this server.
     */
    public static Tag get(String tagName){
        // Removes spaces and color codes - tag names may not contain spaces
        tagName = ChatColor.stripColor(tagName.toLowerCase().replace(" ", ""));

        // Retrieve tag from registry
        Tag tag = tagRegistry.get(tagName);

        // If not present, load tag
        if(tag==null){
            tag = new Tag(tagName);
            tagRegistry.put(tagName, tag);
        }

        return tag;
    }

    /**
     * Reloads all tags.
     */
    public static void reloadAll(){
        tagRegistry.clear();
    }


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
     * The {@link Realm} associated with this Tag.
     */
    private Realm realm;
    /**
     * A location name associated with this Tag. Can be any string.
     */
    private String locationName;
    /**
     * Conditions on which to display marker icons above tagged objects.
     */
    private List<Condition> markerConditions;


    /**
     * Gets a Tag by name.
     * @param name the unique name of the tag
     */
    protected Tag(String name){
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
            file = new ConfigAccessor("tags\\"+name+".yml", StoryPlugin.plugin).getConfig();

            // Load inherited tags
            for(String tagName : file.getStringList("inherit")) inheritedTags.add(Tag.get(tagName));
        }

        loaded = true;
        
        for(Tag tag : getTotalInheritedTags()){
            // Load Realm
            if(realm==null){
                String realmName = tag.getData().getString("data.realm");
                if(realmName!=null) realm = new Realm(realmName);
            }
            // Load location name
            if(locationName==null) locationName = tag.getData().getString("data.location");

            // Load conditions on which to display marker
            if(markerConditions==null||markerConditions.isEmpty()){
                markerConditions = new ArrayList<Condition>();
                List<String> conditionStrings = tag.getData().getStringList("marker");
                for(String c : conditionStrings)
                    markerConditions.add(new Condition(c));
            }
        }
        if(realm!=null && realm.exists()) if(!realm.getName().equalsIgnoreCase(name)) inheritedTags.add(Tag.get(realm.getName()));
    }


    /**
     * Gets the unique name of this Tag.
     * @return the unique name of this tag
     */
    public String getName(){
        return name;
    }


    @Override
    public boolean equals(Object object){
        // If the object being compared is a Tag, check names only, as Tags are unique by names
        if(object instanceof Tag) return name.equals(((Tag)object).getName());
        return super.equals(object);
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
     * Gets the {@link FileConfiguration} for this Tag.
     */
    public FileConfiguration getData(){
        load();
        return file;
    }


    /**
     * Gets the {@link Realm} associated with this Tag.
     * @return the Realm, or null if this tag is not associated with any Realm
     */
    public Realm getRealm(){
        load();
        return realm;
    }
    /**
     * Gets the location name associated with this Tag. Can be any string.
     * @return the location name, or null if one was not found
     */
    public String getLocationName(){
        load();
        return locationName;
    }
    /**
     * Gets every {@link Condition} on which to display a marker above tagged objects.
     * @return a list of conditions
     */
    public List<Condition> getMarkerConditions(){
        load();
        return markerConditions;
    }
}