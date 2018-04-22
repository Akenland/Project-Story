package com.kylenanakdewa.story.tags.taggable;

import java.util.List;

import org.bukkit.command.CommandSender;

import com.kylenanakdewa.story.tags.Tag;

/**
 * Represents an object that can have RealmsStory tags applied to it.
 */
public interface Taggable {
    /**
     * Gets all tags applied to this object, in order of priority.
     * @return all tags, including all inherited tags, in order of priority
     */
    public List<Tag> getTags();

    /**
     * Gets the abstract Tag for this object, defining the effective behavior and data for this object in a single Tag.
     * @return an abstract Tag for this object
     */
    public Tag getTag();

    /**
     * Checks if this object has specific tags.
     * @param tag the tag(s) to check for
     * @return true if the object has all of these tags
     */
    public boolean hasTag(Tag... tags);

    /**
     * Adds a tag to this object. The tag will be added at highest priority, so its behavior will override all other tags.
     * @param tag the tag to be added to this object
     */
    public void addTag(Tag tag);

    /**
     * Removes a tag from this object.
     * Most implementations will NOT remove inherited tags.
     * @param tag the tag to be removed from this object
     */
    public void removeTag(Tag tag);

    /**
     * Removes all tags from this object.
     * Tags provided by other plugins will not be removed.
     */
    public void removeAllTags();

    /**
     * Reloads the tags and data applied to this object.
     */
    public void reload();


    /**
     * Display information about this taggable to a CommandSender.
     * @param sender the CommandSender to display information to
     */
    public void displayInfo(CommandSender sender);
}